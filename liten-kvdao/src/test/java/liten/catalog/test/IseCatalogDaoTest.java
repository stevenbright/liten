package liten.catalog.test;

import com.truward.dao.exception.ItemNotFoundException;
import jetbrains.exodus.env.Transaction;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.dao.exception.DuplicateExternalIdException;
import liten.catalog.dao.support.DefaultIseCatalogDao;
import liten.catalog.model.Ise;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Alexander Shabanov
 */
public final class IseCatalogDaoTest extends XodusTestBase {
  private IseCatalogDao catalogDao;

  final Ise.Item templateItem = Ise.Item.newBuilder().setType("book").build();

  final Ise.ExternalId extId1 = Ise.ExternalId.newBuilder().setIdType("librus").setIdValue("987654").build();
  final Ise.ExternalId extId2 = Ise.ExternalId.newBuilder().setIdType("isbn").setIdValue("1-22-33").build();
  final Ise.ExternalId extId3 = Ise.ExternalId.newBuilder().setIdType("imdb").setIdValue("ABC123").build();

  @Before
  public void init() {
    catalogDao = new DefaultIseCatalogDao(environment);
  }

  @Test
  public void shouldGetNullItemForNonExistentExternalIds() {
    doInTestTransaction(tx -> {
      assertNull(catalogDao.getByExternalId(tx, extId1));
      assertNull(catalogDao.getByExternalId(tx, extId2));
      assertNull(catalogDao.getByExternalId(tx, extId3));
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

  @Test
  public void shouldSaveSimpleItem() {
    final Ise.Item item = Ise.Item.newBuilder(templateItem)
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

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailSavingItemWithDuplicateSkuId() {
    environment.executeInTransaction(tx -> catalogDao.persist(tx, Ise.Item.newBuilder(templateItem)
        .addSkus(Ise.Sku.newBuilder().setId("S1").setLanguage("en").setTitle("One"))
        .addSkus(Ise.Sku.newBuilder().setId("S1").setLanguage("es").setTitle("Dos"))
        .build()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailSavingItemWithDuplicateEntryId() {
    environment.executeInTransaction(tx -> catalogDao.persist(tx, Ise.Item.newBuilder(templateItem)
        .addSkus(Ise.Sku.newBuilder().setId("S1").setLanguage("en").setTitle("One"))
        .addSkus(Ise.Sku.newBuilder().setId("S2").setLanguage("es")
            .addEntries(Ise.Entry.newBuilder().setId("I1"))
            .addEntries(Ise.Entry.newBuilder().setId("I1"))
            .setTitle("Dos"))
        .build()));
  }

  @Test
  public void shouldSaveThenUpdateThenLookupByExternalId() {
    final Ise.Item item = Ise.Item.newBuilder(templateItem)
        .addExternalIds(extId1).addExternalIds(extId2)
        .addSkus(Ise.Sku.newBuilder()
            .setId("S1")
            .setLanguage("en")
            .setTitle("First")
            .addEntries(Ise.Entry.newBuilder().setId("I1")
                .setDownloadInfo(Ise.DownloadInfo.newBuilder().setDownloadId("123").setDownloadType("librus"))
                .setCreatedTimestamp(1234000L))
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

  @Test
  public void shouldUseSingleTransaction() {
    final Ise.Item item = Ise.Item.newBuilder(templateItem)
        .addExternalIds(extId1).addExternalIds(extId2).addExternalIds(extId3)
        .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("First"))
        .build();

    final String id = doInTestTransaction(tx -> {
      return catalogDao.persist(tx, item);
    });

    doInTestTransaction(tx -> {
      assertNull(catalogDao.getByExternalId(tx, extId1));
      assertNull(catalogDao.getByExternalId(tx, extId2));
      assertNull(catalogDao.getByExternalId(tx, extId3));

      try {
        catalogDao.getById(tx, id);
        fail("Changes made in transaction that was rolled back shall not be visible outside");
      } catch (ItemNotFoundException e) {
        assertTrue(e.getMessage().length() > 0);
      }
    });
  }

  @Test
  public void shouldNotOverrideExistingExternalId() {
    doInTestTransaction(tx -> {
      final String id1 = catalogDao.persist(tx, Ise.Item.newBuilder().setType("book")
          .addExternalIds(extId1).addExternalIds(extId2)
          .build());
      try {
        catalogDao.persist(tx, Ise.Item.newBuilder().setType("book")
            .addExternalIds(extId2)
            .build());
        fail("Should not be able to persist another item with the same external ID");
      } catch (DuplicateExternalIdException e) {
        assertEquals(extId2, e.getExternalId());
        assertEquals(id1, e.getMappedItemId());
        assertTrue(e.getFailedItemId().length() > 0);
      }
    });
  }

  @Test
  public void shouldGetEmptyItemQueryResult() {
    final Ise.ItemQueryResult itemQueryResult = environment.computeInTransaction(tx -> catalogDao.getItems(tx,
        Ise.ItemQuery.newBuilder().setLimit(IseCatalogDao.DEFAULT_LIMIT).build()));

    assertTrue(itemQueryResult.getCursor().length() == 0);
    assertEquals(0, itemQueryResult.getItemsCount());
  }

  @Test
  public void shouldGetItemPages() {
    doInTestTransaction(tx -> {
      final Set<Ise.Item> items = new HashSet<>();
      for (int i = 0; i < 40; ++i) {
        final Ise.Item item = Ise.Item.newBuilder()
            .setType(i % 3 == 0 ? "author" : "book")
            .addSkus(Ise.Sku.newBuilder()
                .setId("1")
                .setTitle("x" + i + "-item")
                .build())
            .build();
        final String id = catalogDao.persist(tx, item);
        items.add(Ise.Item.newBuilder(item).setId(id).build());
      }

      assertEquals(items, new HashSet<>(getAllItems(tx, Ise.ItemQuery.newBuilder().setLimit(7).build())));
      assertEquals(items.stream().filter(i -> i.getType().equals("author")).collect(Collectors.toSet()),
          new HashSet<>(getAllItems(tx, Ise.ItemQuery.newBuilder().setType("author").setLimit(3).build())));

      return null;
    });
  }

  @Test
  public void shouldGetPrefixes() {
    doInTestTransaction(tx -> {
      final Ise.Item item1 = Ise.Item.newBuilder().setType("numbers")
          .addExternalIds(extId1)
          .addSkus(Ise.Sku.newBuilder().setId("EN-1").setLanguage("en").setTitle("One"))
          .addSkus(Ise.Sku.newBuilder().setId("EN-2").setLanguage("en").setTitle("Two"))
          .addSkus(Ise.Sku.newBuilder().setId("EN-3").setLanguage("en").setTitle("Three"))
          .addSkus(Ise.Sku.newBuilder().setId("EN-4").setLanguage("en").setTitle("Four"))
          .addSkus(Ise.Sku.newBuilder().setId("EN-5").setLanguage("en").setTitle("Five"))
          .addSkus(Ise.Sku.newBuilder().setId("ES-1").setLanguage("es").setTitle("Uno"))
          .addSkus(Ise.Sku.newBuilder().setId("ES-2").setLanguage("es").setTitle("Dos"))
          .addSkus(Ise.Sku.newBuilder().setId("ES-3").setLanguage("es").setTitle("Tres"))
          .addSkus(Ise.Sku.newBuilder().setId("ES-4").setLanguage("es").setTitle("Cuatro"))
          .addSkus(Ise.Sku.newBuilder().setId("ES-5").setLanguage("es").setTitle("Cinco"))
          .build();
      final Ise.Item item2 = Ise.Item.newBuilder().setType("newspaper")
          .addExternalIds(extId2)
          .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Times"))
          .build();

      final String[] ids = {
          catalogDao.persist(tx, item1),
          catalogDao.persist(tx, item2),
      };

      assertEquals(Ise.Item.newBuilder(item1).setId(ids[0]).build(), catalogDao.getById(tx, ids[0]));
      assertEquals(Ise.Item.newBuilder(item2).setId(ids[1]).build(), catalogDao.getById(tx, ids[1]));

      assertEquals(Arrays.asList("C", "D", "F", "O", "T", "U"), catalogDao.getNameHints(tx, "numbers", ""));
      assertEquals(Arrays.asList("Th", "Tr", "Tw"), catalogDao.getNameHints(tx, "numbers", "T"));
      assertEquals(Arrays.asList("Th", "Ti", "Tr", "Tw"), catalogDao.getNameHints(tx, "", "T"));
      assertEquals(Collections.singletonList("Ti"), catalogDao.getNameHints(tx, "newspaper", "T"));
      assertEquals(Collections.emptyList(), catalogDao.getNameHints(tx, "book", "T"));
      assertEquals(Collections.emptyList(), catalogDao.getNameHints(tx, "", "One"));
      assertEquals(Collections.singletonList("Cuat"), catalogDao.getNameHints(tx, "numbers", "Cua"));
    });
  }

  //
  // Private
  //

  private List<Ise.Item> getAllItems(Transaction tx, Ise.ItemQuery templateItemQuery) {
    final List<Ise.Item> retrievedItems = new ArrayList<>();
    for (String nextCursor = "";;) {
      final Ise.ItemQueryResult queryResult = catalogDao.getItems(tx, Ise.ItemQuery.newBuilder(templateItemQuery)
          .setCursor(nextCursor)
          .build());
      retrievedItems.addAll(queryResult.getItemsList());
      if (queryResult.getCursor().length() == 0) {
        break;
      }
      nextCursor = queryResult.getCursor();
    }

    return retrievedItems;
  }
}
