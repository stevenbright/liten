package liten.catalog.dao.support;

import com.truward.time.jdbc.UtcTimeSqlUtil;
import liten.catalog.dao.CatalogQueryDao;
import liten.catalog.dao.CatalogUpdaterDao;
import liten.catalog.dao.model.*;
import liten.dao.model.ModelWithId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
@Transactional(propagation = Propagation.REQUIRED)
public final class DefaultCatalogDao implements CatalogQueryDao, CatalogUpdaterDao {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final JdbcOperations db;

  public DefaultCatalogDao(JdbcOperations jdbcOperations) {
    this.db = Objects.requireNonNull(jdbcOperations, "jdbcOperations");
  }

  @Override
  public long getNextItemId() {
    final Long maxId = db.queryForObject("SELECT MAX(id) FROM ice_item", Long.class);
    return 1L + (maxId != null ? maxId : 0L);
  }

  @Override
  public IceItem getItem(long itemId) {
    return db.queryForObject("SELECT i.id, e.name AS type, i.mod_counter, i.alias FROM ice_item AS i\n" +
            "INNER JOIN entity_type AS e ON e.id=i.type_id WHERE i.id=?",
        IceItemRowMapper.INSTANCE,
        itemId);
  }

  @Override
  public IceEntry getEntry(long itemId) {
    final IceEntry.Builder entry = IceEntry.newBuilder();
    entry.setItem(getItem(itemId));

    // add SKUs
    final List<IceSku> skus = db.query("SELECT sku_id, title, language_id FROM ice_sku " +
            "WHERE item_id=? ORDER BY sku_id",
        IceSkuRowMapper.INSTANCE, itemId);
    for (final IceSku sku : skus) {
      entry.addSku(sku);

      // add instances
      final List<IceInstance> instances = db.query(
          "SELECT instance_id, created, origin_id, download_id FROM ice_instance WHERE item_id=? AND sku_id=?",
          IceInstanceMapper.INSTANCE, itemId, sku.getId());
      for (final IceInstance instance : instances) {
        entry.addInstance(sku.getId(), instance);
      }
    }

    return entry.build();
  }

  @Override
  public void addEntries(List<IceEntry> entries) {
    //noinspection Convert2streamapi
    for (final IceEntry entry : entries) {
      addEntry(entry);
    }
  }

  @Override
  public void deleteEntry(long id) {
    db.update("DELETE FROM ice_instance AS inst " +
        "INNER JOIN ice_sku AS sku ON sku.id=inst.sku_id WHERE sku.item_id=?", id);
    db.update("DELETE FROM ice_sku WHERE item_id=?", id);
    db.update("DELETE FORM ice_item WHERE id=?", id);
  }

  @Override
  public void addEntry(IceEntry entry) {
    log.debug("Adding entry={}", entry);

    // insert item
    final IceItem item = entry.getItem();
    final long itemId = item.getValidId();
    db.update("INSERT INTO ice_item (id, type_id, alias) VALUES (?, ?, ?)",
        itemId,
        getTypeIdByName(item.getType()),
        item.getAlias());

    // insert SKUs
    for (final IceEntry.SkuEntry skuEntry : entry.getSkuEntries()) {
      final IceSku sku = skuEntry.getSku();
      db.update("INSERT INTO ice_sku (item_id, sku_id, title, language_id, wikipedia_url) VALUES (?, ?, ?, ?, ?)",
          itemId,
          sku.getId(),
          sku.getTitle(),
          sku.getLanguageId(),
          null);

      // insert instances
      for (final IceInstance instance : skuEntry.getInstances()) {
        db.update("INSERT INTO ice_instance (item_id, sku_id, instance_id, created, origin_id, download_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
            itemId,
            sku.getId(),
            instance.getId(),
            instance.getCreated().asCalendar(),
            instance.getOriginId(),
            instance.getDownloadId());
      }
    }
  }

  @Override
  public void setRelation(long leftItemId, long rightItemId, String type) {
    db.update("INSERT INTO ice_item_relations (left_id, right_id, type_id) VALUES (?, ?, ?)",
        leftItemId, rightItemId, getTypeIdByName(type));
  }

  @Override
  public List<IceEntry> getEntries(IceEntryFilter filter, long startItemId, int limit) {
    final Long startIdParam = ModelWithId.getNullOrValidId(startItemId);
    final List<Long> entryIds = db.queryForList("SELECT id FROM ice_item\n" +
            "WHERE (? IS NULL) OR (id > ?) ORDER BY id LIMIT ?",
        Long.class,
        startIdParam,
        startIdParam,
        limit);

    return entryIds.stream().map(this::getEntry).collect(Collectors.toList());
  }

  @Override
  public List<IceRelation> getRelations(IceRelationQuery query) {
    final StringBuilder queryBuilder = new StringBuilder(100);
    final List<Object> params = new ArrayList<>();

    final String relatedParamName;
    final String opposingParamName;
    switch (query.getDirection()) {
      case LEFT:
        relatedParamName = "left_id";
        opposingParamName = "right_id";
        break;
      case RIGHT:
        relatedParamName = "right_id";
        opposingParamName = "left_id";
        break;
      default:
        throw new IllegalStateException("Unknown direction=" + query.getDirection());
    }

    queryBuilder.append("SELECT e.name AS type, ir.")
        .append(opposingParamName).append(" AS id FROM ice_item_relations AS ir\n")
        .append("INNER JOIN entity_type AS e ON e.id=ir.type_id\n")
        .append("WHERE ir.").append(relatedParamName).append(" = ?");
    params.add(query.getRelatedItemId());

    if (ModelWithId.isValidId(query.getStartItemId())) {
      queryBuilder.append(" AND (? > ir.").append(opposingParamName).append(")");
      params.add(query.getStartItemId());
    }

    if (!query.getRelationTypes().isEmpty()) {
      queryBuilder.append(" AND e.name IN (");

      for (int i = 0; i < query.getRelationTypes().size(); ++i) {
        if (i == 0) {
          queryBuilder.append('?');
        } else {
          queryBuilder.append(", ?");
        }
      }

      params.addAll(query.getRelationTypes());
      queryBuilder.append(')');
    }

    queryBuilder.append(" ORDER BY id LIMIT ?");
    params.add(query.getLimit());

    return db.query(queryBuilder.toString(), IceRelationRowMapper.INSTANCE, params.toArray(new Object[params.size()]));
  }

  //
  // Private
  //

  private Long getTypeIdByName(String name) {
    return db.queryForObject("SELECT id FROM entity_type WHERE name=?", Long.class, name);
  }

  private static final class IceItemRowMapper implements RowMapper<IceItem> {
    private static final IceItemRowMapper INSTANCE = new IceItemRowMapper();

    @Override
    public IceItem mapRow(ResultSet rs, int i) throws SQLException {
      return IceItem.newBuilder()
          .setId(rs.getLong("id"))
          .setType(rs.getString("type"))
          .setModCounter(rs.getInt("mod_counter"))
          .setAlias(rs.getString("alias"))
          .build();
    }
  }

  private static final class IceSkuRowMapper implements RowMapper<IceSku> {
    private static final IceSkuRowMapper INSTANCE = new IceSkuRowMapper();

    @Override
    public IceSku mapRow(ResultSet rs, int i) throws SQLException {
      return IceSku.newBuilder()
          .setId(rs.getLong("sku_id"))
          .setLanguageId(rs.getLong("language_id"))
          .setTitle(rs.getString("title"))
          .build();
    }
  }

  private static final class IceInstanceMapper implements RowMapper<IceInstance> {
    private static final IceInstanceMapper INSTANCE = new IceInstanceMapper();

    @Override
    public IceInstance mapRow(ResultSet rs, int i) throws SQLException {
      return IceInstance.newBuilder()
          .setId(rs.getLong("instance_id"))
          .setCreated(UtcTimeSqlUtil.getUtcTime(rs, "created"))
          .setOriginId(rs.getLong("origin_id"))
          .setDownloadId(rs.getLong("download_id"))
          .build();
    }
  }

  private static final class IceRelationRowMapper implements RowMapper<IceRelation> {
    public static final IceRelationRowMapper INSTANCE = new IceRelationRowMapper();

    @Override
    public IceRelation mapRow(ResultSet rs, int i) throws SQLException {
      return new IceRelation(rs.getString("type"), rs.getLong("id"));
    }
  }
}
