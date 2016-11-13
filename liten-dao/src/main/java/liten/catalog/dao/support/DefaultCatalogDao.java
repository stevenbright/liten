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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.ResultSet;
import java.sql.SQLException;
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
  public IceItem getItem(long itemId) {
    return db.queryForObject("SELECT i.id, e.name AS type, i.alias FROM ice_item AS i\n" +
            "INNER JOIN entity_type AS e ON e.id=i.type_id WHERE i.id=?",
        IceItemRowMapper.INSTANCE,
        itemId);
  }

  @Override
  public IceEntry getEntry(long itemId, IceEntryFilter filter) {
    final IceEntry.Builder entry = IceEntry.newBuilder();
    entry.setItem(getItem(itemId));

    final List<IceSku> skus = db.query("SELECT sku_id, title, language_id FROM ice_sku " +
            "WHERE item_id=? ORDER BY sku_id",
        IceSkuRowMapper.INSTANCE, itemId);
    for (final IceSku sku : skus) {
      final IceItem languageItem = getItem(sku.getLanguageId());
      if ((!filter.isUseLanguageFilter()) || filter.getLanguageAliases().contains(languageItem.getAlias())) {
        // get instances
        entry.addSku(IceSku.newBuilder(sku).setLanguageId(languageItem.getId()).build());
        final List<IceInstance> instances = db.query(
            "SELECT instance_id, created, origin_id, download_id FROM ice_instance WHERE item_id=? AND sku_id=?",
            IceInstanceMapper.INSTANCE, itemId, sku.getId());
        for (final IceInstance instance : instances) {
          entry.addInstance(sku.getId(), instance);
        }

        // set related item - language
        entry.addRelatedItem(languageItem);
        break;
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
    db.update("INSERT INTO ice_item_relations (?, ?, ?) VALUES (?, ?, ?)",
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

    return entryIds.stream().map(itemId -> getEntry(itemId, filter)).collect(Collectors.toList());
  }

        @Override
  public List<IceRelation> getLeftRelations(long rightItemId, @Nullable String type, long startItemId, int limit) {
    return db.query("SELECT e.name AS type, ir.left_id AS id FROM ice_item_relations AS ir\n" +
            "INNER JOIN entity_type AS e ON e.id=ir.type_id\n" +
            "WHERE ((?=0) OR (ir.left_id>?)) AND ((? IS NULL) OR (e.type=?)) AND (ir.right_id=?)\n" +
            "ORDER BY ir.left_id LIMIT ?",
        IceRelationRowMapper.INSTANCE,
        startItemId, startItemId,
        type, type,
        rightItemId,
        limit);
  }

  @Override
  public List<IceRelation> getRightRelations(long rightItemId, @Nullable String type, long startItemId, int limit) {
    return db.query("SELECT e.name AS type, ir.right_id AS id FROM ice_item_relations AS ir\n" +
            "INNER JOIN entity_type AS e ON e.id=ir.type_id\n" +
            "WHERE ((?=0) OR (ir.right_id>?)) AND ((? IS NULL) OR (e.type=?)) AND (ir.left_id=?)\n" +
            "ORDER BY ir.right_id LIMIT ?",
        IceRelationRowMapper.INSTANCE,
        startItemId, startItemId,
        type, type,
        rightItemId,
        limit);
  }

  //
  // Private
  //

  private String getTypeIdByName(String name) {
    return db.queryForObject("SELECT id FROM entity_type WHERE name=?", String.class, name);
  }

  private static final class IceItemRowMapper implements RowMapper<IceItem> {
    private static final IceItemRowMapper INSTANCE = new IceItemRowMapper();

    @Override
    public IceItem mapRow(ResultSet rs, int i) throws SQLException {
      return IceItem.newBuilder()
          .setId(rs.getLong("id"))
          .setType(rs.getString("type"))
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
