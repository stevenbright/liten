package liten.catalog.dao.support;

import liten.catalog.dao.CatalogQueryDao;
import liten.catalog.dao.CatalogUpdaterDao;
import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceInstance;
import liten.catalog.dao.model.IceItem;
import liten.catalog.dao.model.IceSku;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Alexander Shabanov
 */
@Transactional
public class DefaultCatalogDao implements CatalogQueryDao, CatalogUpdaterDao {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final JdbcOperations db;

  public DefaultCatalogDao(JdbcOperations jdbcOperations) {
    this.db = Objects.requireNonNull(jdbcOperations, "jdbcOperations");
  }

  @Override
  public IceEntry getEntry(long itemId, String language) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Long> addEntries(List<IceEntry> entries) {
    // TODO: batch insert
    return entries.stream().map(this::addEntry).collect(Collectors.toList());
  }

  @Override
  public void deleteEntry(long id) {
    throw new UnsupportedOperationException();
  }

  //
  // Private
  //

  private long addEntry(IceEntry entry) {
    log.debug("Adding entry={}", entry);

    // insert item
    final IceItem item = entry.getItem();
    final Long itemId = db.queryForObject("SELECT seq_ice_item.nextval", Long.class);
    db.update("INSERT INTO ice_item (id, type, created, updated, default_title)",
        itemId,
        getTypeIdByName(item.getType()),
        item.getCreated().asCalendar(),
        item.getUpdated().asCalendar(),
        item.getDefaultTitle());

    // insert SKUs
    for (final IceEntry.SkuEntry skuEntry : entry.getSkuEntries()) {
      final IceSku sku = skuEntry.getSku();
      final Long skuId = db.queryForObject("SELECT seq_ice_sku.nextval", Long.class);
      db.update("INSERT INTO ice_sku (id, item_id, title, created, updated, language_id, wikipedia_url)",
          skuId,
          itemId,
          getTypeIdByName(sku.getType()),
          sku.getCreated().asCalendar(),
          sku.getUpdated().asCalendar(),
          sku.getLanguageId(),
          null);

      // insert instances
      for (final IceInstance instance : skuEntry.getInstances()) {
        final Long instanceId = db.queryForObject("SELECT seq_ice_instance.nextval", Long.class);
        db.update("INSERT INTO ice_instance (id, sku_id, created, updated, origin_id, download_id)",
            instanceId,
            skuId,
            instance.getCreated().asCalendar(),
            instance.getUpdated().asCalendar(),
            instance.getOriginId(),
            instance.getDownloadId());
      }
    }

    return itemId;
  }

  private String getTypeIdByName(String name) {
    return db.queryForObject("SELECT id FROM entity_type WHERE name=?", String.class, name);
  }
}
