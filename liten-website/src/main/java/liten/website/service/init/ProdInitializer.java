package liten.website.service.init;

import jetbrains.exodus.env.Transaction;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.model.Ise;
import liten.catalog.util.IseNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Production data initializer.
 */
public final class ProdInitializer {
  private static final int SANITY_CHECK_ITEM_LIMIT = 10;

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Resource
  private IseCatalogDao catalogDao;

  @PostConstruct
  public void init() {
    catalogDao.getEnvironment().executeInTransaction(this::verifyCatalogContents);
  }

  private void verifyCatalogContents(Transaction tx) {
    // verify that catalog actually has some sane data
    final Ise.Item enLang = catalogDao.getByExternalId(tx, IseNames.newAlias("en"));
    if (enLang == null) {
      throw new IllegalStateException("No english lang found");
    }

    final Ise.ItemQueryResult itemQuery = catalogDao.getItems(
        tx,
        Ise.ItemQuery.newBuilder().setLimit(SANITY_CHECK_ITEM_LIMIT).build());
    if (StringUtils.isEmpty(itemQuery.getCursor())) {
      throw new IllegalStateException("Catalog doesn't seem valid: too few items found");
    }

    log.info("Catalog DB: prod sanity check passed");
  }
}
