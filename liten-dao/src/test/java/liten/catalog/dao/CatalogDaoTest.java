package liten.catalog.dao;

import com.truward.time.UtcTime;
import liten.catalog.dao.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Shabanov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring/CatalogDaoTest-context.xml")
@Transactional
public final class CatalogDaoTest {
  private static final long EN = 100L;
  private static final long RU = 101L;

  @Resource CatalogUpdaterDao updaterDao;
  @Resource CatalogQueryDao queryDao;

  @Test
  public void shouldQueryNextItemIdForEmptyDb() {
    assertEquals(1L, queryDao.getNextItemId());
  }

  @Test
  public void shouldQueryNextItemId() {
    final long langId = addEnLanguage();
    assertEquals(1L + langId, queryDao.getNextItemId());
  }

  @Test
  public void shouldInsertAndQueryRawItem() {
    final long id = addEnLanguage();

    final IceEntry entry = queryDao.getEntry(id);
    assertEquals("en", entry.getItem().getAlias());
    assertTrue(entry.getSkuEntries().isEmpty());
  }

  @Test
  public void shouldInsertUpdateQueryAndDeleteInstance() {
    final long langId = addEnLanguage();
    final long bookId = 110L;
    final IceEntry entry = IceEntry.newBuilder()
        .setItem(IceItem.newBuilder()
            .setId(bookId)
            .setType("book")
            .setModCounter(1) // modCounter == 1 (default modification value)
            .setAlias("The Crow (Book)")
            .build())
        .addSku(IceSku.newBuilder()
            .setId(1L)
            .setTitle("The Crow")
            .setLanguageId(langId)
            .build())
        .addInstance(1L, IceInstance.newBuilder()
            .setCreated(UtcTime.valueOf(System.currentTimeMillis(), true))
            .setDownloadId(456L)
            .setOriginId(123L)
            .build())
        .build();

    updaterDao.addEntry(entry);

    final IceEntry actualEntry = queryDao.getEntry(bookId);
    assertEquals(entry, actualEntry);
  }

  @Test
  public void shouldAllowAddingMultipleUnaliasedItems() {
    final IceItem template = IceItem.newBuilder().setType("book").build();

    updaterDao.addEntries(asList(
        IceEntry.newBuilder().setItem(IceItem.newBuilder(template).setId(1L).build()).build(),
        IceEntry.newBuilder().setItem(IceItem.newBuilder(template).setId(2L).build()).build(),
        IceEntry.newBuilder().setItem(IceItem.newBuilder(template).setType("author").setId(3L).build()).build()
    ));

    assertEquals(asList(1L, 2L, 3L), getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.NONE)));
    assertEquals(asList(1L, 2L, 3L), getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder().build())));
    assertEquals(asList(2L, 3L), getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder()
        .setStartItemId(1L).setLimit(9).build())));

    assertEquals(singletonList(2L),
        getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder().setStartItemId(1L).setLimit(1).build())));
    assertEquals(singletonList(3L),
        getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder().setStartItemId(2L).setLimit(1).build())));
    assertEquals(singletonList(3L),
        getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder().setStartItemId(2L).setLimit(9).build())));

    assertEquals(emptyList(), getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder()
        .setStartItemId(2L).setLimit(0).build())));
    assertEquals(emptyList(), getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder()
        .setStartItemId(3L).setLimit(9).build())));

    // type-based filtering
    assertEquals(asList(1L, 2L), getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder()
        .setType("book").build())));
    assertEquals(singletonList(2L), getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder()
        .setType("book").setStartItemId(1L).setLimit(9).build())));
    assertEquals(singletonList(3L), getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder()
        .setType("author").build())));
    assertEquals(emptyList(), getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder()
        .setType("genre").build())));
  }

  @Test(expected = DuplicateKeyException.class)
  public void shouldRejectDuplicateAlias() {
    final long enLangId = addEnLanguage();
    final IceEntry enLang = queryDao.getEntry(enLangId);

    final long otherId = enLangId + 1;
    final IceEntry other = IceEntry.newBuilder()
        .setItem(IceItem.newBuilder(enLang.getItem()).setId(otherId).build())
        .build();

    updaterDao.addEntry(other);
  }

  @Test
  public void shouldQueryEmptyRelations() {
    final long langId = addEnLanguage();
    assertEquals(emptyList(), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setDirection(IceRelationQuery.Direction.LEFT)
        .setRelatedItemId(langId)
        .build()));

    assertEquals(emptyList(), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setDirection(IceRelationQuery.Direction.RIGHT)
        .setRelatedItemId(langId)
        .build()));

    assertEquals(emptyList(), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setLimit(100)
        .setRelatedItemId(langId)
        .setStartItemId(1L)
        .setDirection(IceRelationQuery.Direction.RIGHT)
        .build()));

    assertEquals(emptyList(), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setLimit(100)
        .setRelatedItemId(langId)
        .setStartItemId(1L)
        .setDirection(IceRelationQuery.Direction.RIGHT)
        .addRelationType("language")
        .addRelationType("book")
        .build()));
  }

  @Test
  public void shouldQueryComplexRelations() {
    final long i[] = {
        addLanguage(100L, "zzz"),
        addLanguage(1L, "a"),
        addLanguage(2L, "b"),
        addLanguage(3L, "c"),
        addLanguage(4L, "d"),
        addLanguage(5L, "e"),
        addLanguage(6L, "f"),
        addLanguage(7L, "g"),
    };

    updaterDao.setRelation(i[1], i[2], "language");
    updaterDao.setRelation(i[1], i[3], "language");
    updaterDao.setRelation(i[1], i[4], "language");
    updaterDao.setRelation(i[1], i[5], "language");
    updaterDao.setRelation(i[1], i[7], "language");
    updaterDao.setRelation(i[2], i[1], "language");
    updaterDao.setRelation(i[2], i[3], "language");
    updaterDao.setRelation(i[2], i[7], "language");
    updaterDao.setRelation(i[4], i[7], "language");
    updaterDao.setRelation(i[5], i[7], "language");
    updaterDao.setRelation(i[6], i[7], "language");
    updaterDao.setRelation(i[7], i[3], "language");

    assertEquals(emptyList(), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setDirection(IceRelationQuery.Direction.LEFT)
        .setRelatedItemId(i[0])
        .build()));
    assertEquals(emptyList(), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setDirection(IceRelationQuery.Direction.RIGHT)
        .setRelatedItemId(i[0])
        .build()));

    assertEquals(asList(i[2], i[3], i[4], i[5], i[7]), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setRelatedItemId(i[1])
        .build()).stream().map(IceRelation::getRelatedItemId).collect(Collectors.toList()));
    assertEquals(emptyList(), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setRelatedItemId(i[1])
        .addRelationType("book")
        .build()));

    assertEquals(asList(i[1], i[3], i[7]), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setRelatedItemId(i[2])
        .build()).stream().map(IceRelation::getRelatedItemId).collect(Collectors.toList()));

    assertEquals(singletonList(i[1]), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setRelatedItemId(i[2])
        .setDirection(IceRelationQuery.Direction.RIGHT)
        .build()).stream().map(IceRelation::getRelatedItemId).collect(Collectors.toList()));

    // paginated query (left relation)
    assertEquals(asList(i[2], i[3]), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setRelatedItemId(i[1])
        .setDirection(IceRelationQuery.Direction.LEFT)
        .setLimit(2)
        .build()).stream().map(IceRelation::getRelatedItemId).collect(Collectors.toList()));
    assertEquals(asList(i[4], i[5]), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setRelatedItemId(i[1])
        .setStartItemId(i[3])
        .setDirection(IceRelationQuery.Direction.LEFT)
        .setLimit(2)
        .build()).stream().map(IceRelation::getRelatedItemId).collect(Collectors.toList()));
    assertEquals(singletonList(i[7]), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setRelatedItemId(i[1])
        .setStartItemId(i[5])
        .setDirection(IceRelationQuery.Direction.LEFT)
        .setLimit(2)
        .build()).stream().map(IceRelation::getRelatedItemId).collect(Collectors.toList()));

    // paginated query (right relation)
    assertEquals(asList(i[1], i[2]), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setRelatedItemId(i[7])
        .setDirection(IceRelationQuery.Direction.RIGHT)
        .setLimit(2)
        .build()).stream().map(IceRelation::getRelatedItemId).collect(Collectors.toList()));
    assertEquals(asList(i[4], i[5]), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setRelatedItemId(i[7])
        .setStartItemId(i[2])
        .setDirection(IceRelationQuery.Direction.RIGHT)
        .setLimit(2)
        .build()).stream().map(IceRelation::getRelatedItemId).collect(Collectors.toList()));
    assertEquals(singletonList(i[6]), queryDao.getRelations(IceRelationQuery.newBuilder()
        .setRelatedItemId(i[7])
        .setStartItemId(i[5])
        .setDirection(IceRelationQuery.Direction.RIGHT)
        .setLimit(2)
        .build()).stream().map(IceRelation::getRelatedItemId).collect(Collectors.toList()));
  }

  @Test
  public void shouldFilterByNamePrefix() {
    // Test missing hints
    assertEquals(emptyList(), queryDao.getSkuNameHints("book", null));
    assertEquals(emptyList(), queryDao.getSkuNameHints("book", "A"));

    // Given:
    addLanguage(EN, "en");
    addLanguage(RU, "ru");

    addEntity(1000, "book", "Йййй", "Yyyy");
    addEntity(1001, "book", "Йййв", "Yyyv");
    addEntity(1002, "book", "Йййб", "Yyyb");
    addEntity(1003, "book", "Ййлй", "Yyly");
    addEntity(1004, "book", "Абв", "ABC");
    addEntity(1005, "book", "Абг", "ABD");
    addEntity(1006, "book", "Солнце", "Sun");
    addEntity(1007, "genre", "Искусство", "Аrt");
    addEntity(1008, "genre", "Иога", "Yoga");

    assertEquals(asList("Y", "А", "И"), queryDao.getSkuNameHints("genre", null));
    assertEquals(asList("A", "S", "Y", "А", "Й", "С"), queryDao.getSkuNameHints("book", null));

    assertEquals(asList("Ио", "Ис"), queryDao.getSkuNameHints("genre", "И"));
    assertEquals(singletonList("AB"), queryDao.getSkuNameHints("book", "A"));
    assertEquals(asList("ABC", "ABD"), queryDao.getSkuNameHints("book", "AB"));

    assertEquals(asList(1004L, 1005L), getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder()
        .setType("book").setNamePrefix("A").build())));
    assertEquals(singletonList(1005L), getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder()
        .setType("book").setStartItemId(1004L).setNamePrefix("Аб").build())));
    assertEquals(asList(1007L, 1004L, 1005L), getIdsFromIceEntries(queryDao.getEntries(IceEntryQuery.newBuilder()
        .setNamePrefix("А").build())));
  }

  //
  // Private
  //

  private static List<Long> getIdsFromIceEntries(List<IceEntry> entries) {
    return entries.stream().map(e -> e.getItem().getId()).collect(Collectors.toList());
  }

  private long addLanguage(long id, String alias) {
    updaterDao.addEntry(IceEntry.newBuilder()
        .setItem(IceItem.newBuilder().setId(id).setType("language").setAlias(alias).build())
        .build());
    return id;
  }

  private long addEntity(long id, String type, String ruName, String enName) {
    updaterDao.addEntry(IceEntry.newBuilder()
        .setItem(IceItem.newBuilder().setId(id).setType(type).build())
        .addSku(IceSku.newBuilder()
            .setId(id * 2)
            .setLanguageId(EN)
            .setTitle(enName)
            .build())
        .addSku(IceSku.newBuilder()
            .setId(id * 2 + 1)
            .setLanguageId(RU)
            .setTitle(ruName)
            .build())
        .build());
    return id;
  }

  private long addEnLanguage() {
    return addLanguage(EN, "en");
  }
}
