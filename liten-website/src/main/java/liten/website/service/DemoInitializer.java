package liten.website.service;

import liten.catalog.dao.CatalogUpdaterDao;
import liten.catalog.dao.support.SampleCatalogFixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author Alexander Shabanov
 */
public class DemoInitializer {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Resource
  private CatalogUpdaterDao updaterDao;

  @PostConstruct
  public void init() {
    log.info("Initializing demo data");
    SampleCatalogFixture.addSampleData(updaterDao);
    log.info("Demo data added");
  }
}
