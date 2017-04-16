package liten.catalog.test;

import liten.catalog.dao.IseCatalogDao;
import liten.catalog.dao.support.DefaultIseCatalogDao;
import liten.catalog.model.Ise;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
  public void shouldSaveSimpleItem() {
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

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailSavingItemWithDuplicateSkuId() {
    environment.executeInTransaction(tx -> catalogDao.persist(tx, Ise.Item.newBuilder()
        .setAlias("Alias")
        .setType("book")
        .addSkus(Ise.Sku.newBuilder().setId("S1").setLanguage("en").setTitle("One"))
        .addSkus(Ise.Sku.newBuilder().setId("S1").setLanguage("es").setTitle("Dos"))
        .build()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailSavingItemWithDuplicateEntryId() {
    environment.executeInTransaction(tx -> catalogDao.persist(tx, Ise.Item.newBuilder()
        .setAlias("Alias")
        .setType("book")
        .addSkus(Ise.Sku.newBuilder().setId("S1").setLanguage("en").setTitle("One"))
        .addSkus(Ise.Sku.newBuilder().setId("S2").setLanguage("es")
            .addEntries(Ise.Entry.newBuilder().setId("I1"))
            .addEntries(Ise.Entry.newBuilder().setId("I1"))
            .setTitle("Dos"))
        .build()));
  }

  @Test
  public void shouldSaveThenUpdateThenLookupByExternalId() {
    final Ise.ExternalId extId1 = Ise.ExternalId.newBuilder().setIdType("librus").setIdValue("987654").build();
    final Ise.ExternalId extId2 = Ise.ExternalId.newBuilder().setIdType("isbn").setIdValue("1-22-33").build();
    final Ise.ExternalId extId3 = Ise.ExternalId.newBuilder().setIdType("imdb").setIdValue("ABC123").build();

    final Ise.Item item = Ise.Item.newBuilder()
        .setAlias("Alias")
        .setType("book")
        .addExternalIds(extId1).addExternalIds(extId2)
        .addSkus(Ise.Sku.newBuilder()
            .setId("S1")
            .setLanguage("en")
            .setTitle("First")
            .addEntries(Ise.Entry.newBuilder().setId("I1").setCreatedTimestamp(1234000L))
            .addEntries(Ise.Entry.newBuilder().setId("I2")))
        .build();

    doInTestTransaction(tx -> {
      final String id = catalogDao.persist(tx, item);

      Ise.Item savedItem = catalogDao.getById(tx, id);
      assertEquals(Ise.Item.newBuilder(item).setId(id).build(), savedItem);
      assertEquals(savedItem, catalogDao.getByExternalId(tx, extId1));
      assertEquals(savedItem, catalogDao.getByExternalId(tx, extId2));
      assertNull(catalogDao.getByExternalId(tx, extId3));

      savedItem = Ise.Item.newBuilder(savedItem).clearExternalIds().addExternalIds(extId3).build();
      catalogDao.persist(tx, savedItem);

      final Ise.Item anotherItem = catalogDao.getById(tx, id);
      assertEquals(savedItem, anotherItem);
      assertNull(catalogDao.getByExternalId(tx, extId1));
      assertNull(catalogDao.getByExternalId(tx, extId2));
      assertEquals(savedItem, catalogDao.getByExternalId(tx, extId3));
    });
  }
}
