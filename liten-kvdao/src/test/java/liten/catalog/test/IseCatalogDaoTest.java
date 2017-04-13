package liten.catalog.test;

import liten.catalog.dao.IseCatalogDao;
import liten.catalog.dao.support.DefaultIseCatalogDao;
import liten.catalog.model.Ise;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Alexander Shabanov
 */
public final class IseCatalogDaoTest extends XodusTestBase {
  private IseCatalogDao catalogDao;

  @Before
  public void init() {
    catalogDao = new DefaultIseCatalogDao(environment);
  }

  @Test
  public void shouldSaveItem() {
    final Ise.Item item = Ise.Item.newBuilder()
        .setAlias("Alias")
        .setType("book")
        .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("First"))
        .build();

    doInTestTransaction(tx -> {
      final String id = catalogDao.persist(tx, item);

      final Ise.Item savedItem = catalogDao.getById(tx, id);
      assertEquals(Ise.Item.newBuilder(item).setId(id).build(), savedItem);
      final List<String> prefixes = catalogDao.getNameHints(tx, null, "");
      assertEquals(Collections.singletonList("F"), prefixes);
    });
  }

  @Test
  public void shouldGetEmptyPrefixes() {
    List<String> prefixes = environment.computeInTransaction(tx -> catalogDao.getNameHints(tx, null, ""));
    assertEquals(Collections.emptyList(), prefixes);

    prefixes = environment.computeInTransaction(tx -> catalogDao.getNameHints(tx, null, "A"));
    assertEquals(Collections.emptyList(), prefixes);

    prefixes = environment.computeInTransaction(tx -> catalogDao.getNameHints(tx, "book", ""));
    assertEquals(Collections.emptyList(), prefixes);

    prefixes = environment.computeInTransaction(tx -> catalogDao.getNameHints(tx, "book", "A"));
    assertEquals(Collections.emptyList(), prefixes);
  }
}
