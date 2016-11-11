package liten.catalog.dao.support;

import liten.catalog.dao.CatalogQueryDao;
import liten.catalog.dao.CatalogUpdaterDao;
import liten.catalog.dao.model.*;
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
  public IceEntry getEntry(long itemId, String language) {
    // get product
    final IceItem item = db.queryForObject("SELECT e.name AS type, i.default_title FROM ice_item AS i\n" +
        "INNER JOIN entity_type AS e ON e.id=i.type_id WHERE i.id=?",
        IceItemRowMapper.INSTANCE,
        itemId);

    final IceEntry entry = new IceEntry();
    entry.setItem(item);
    return entry;
  }

  @Override
  public List<Long> addEntries(List<IceEntry> entries) {
    // TODO: batch insert
    return entries.stream().map(this::addEntry).collect(Collectors.toList());
  }

  @Override
  public void deleteEntry(long id) {
    db.update("DELETE FROM ice_instance AS inst " +
        "INNER JOIN ice_sku AS sku ON sku.id=inst.sku_id WHERE sku.item_id=?", id);
    db.update("DELETE FROM ice_sku WHERE item_id=?", id);
    db.update("DELETE FORM ice_item WHERE id=?", id);
  }

  @Override
  public Long addEntry(IceEntry entry) {
    log.debug("Adding entry={}", entry);

    // insert item
    final IceItem item = entry.getItem();
    final Long itemId = db.queryForObject("SELECT seq_ice_item.nextval", Long.class);
    db.update("INSERT INTO ice_item (id, type_id, default_title) VALUES (?, ?, ?)",
        itemId,
        getTypeIdByName(item.getType()),
        item.getDefaultTitle());

    // insert SKUs
    int skuId = 0;
    for (final IceEntry.SkuEntry skuEntry : entry.getSkuEntries()) {
      ++skuId;
      final IceSku sku = skuEntry.getSku();
      db.update("INSERT INTO ice_sku (item_id, sku_id, title, language_id, wikipedia_url) VALUES (?, ?, ?, ?, ?)",
          itemId,
          skuId,
          sku.getTitle(),
          sku.getLanguageId(),
          null);

      // insert instances
      int instanceId = 0;
      for (final IceInstance instance : skuEntry.getInstances()) {
        ++instanceId;
        db.update("INSERT INTO ice_instance (item_id, sku_id, instance_id, created, origin_id, download_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
            itemId,
            skuId,
            instanceId,
            instance.getCreated().asCalendar(),
            instance.getOriginId(),
            instance.getDownloadId());
      }
    }

    return itemId;
  }

  @Override
  public void setRelation(long leftItemId, long rightItemId, String type) {
    db.update("INSERT INTO ice_item_relations (?, ?, ?) VALUES (?, ?, ?)",
        leftItemId, rightItemId, getTypeIdByName(type));
  }

  @Override
  public List<IceEntry> getEntries(long startItemId, int limit) {
    throw new UnsupportedOperationException();
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
      return new IceItem(rs.getString("type"), rs.getString("default_title"));
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
