package liten.website.service.init;

import liten.catalog.dao.IseCatalogDao;
import liten.catalog.dao.support.IseCatalogSampleData;
import liten.catalog.model.Ise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Developer mode data initializer.
 */
public final class DemoInitializer {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Resource
  private IseCatalogDao catalogDao;

  @PostConstruct
  public void init() {
    log.info("Initializing demo data");

    catalogDao.getEnvironment().executeInTransaction(tx -> {
      for (final Ise.Item item : IseCatalogSampleData.createItemList()) {
        catalogDao.persist(tx, item);
      }
    });

    log.info("Demo data added");
  }
}
