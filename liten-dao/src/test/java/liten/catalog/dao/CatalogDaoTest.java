package liten.catalog.dao;

import com.truward.time.UtcTime;
import liten.catalog.dao.model.*;
import liten.dao.model.ModelWithId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
        IceEntry.newBuilder().setItem(IceItem.newBuilder(template).setId(3L).build()).build()
    ));

    assertEquals(asList(1L, 2L, 3L), queryDao.getEntries(IceEntryFilter.NONE, ModelWithId.INVALID_ID, 100)
        .stream().map(e -> e.getItem().getId()).collect(Collectors.toList()));
    assertEquals(asList(2L, 3L), queryDao.getEntries(IceEntryFilter.NONE, 1L, 100)
        .stream().map(e -> e.getItem().getId()).collect(Collectors.toList()));

    assertEquals(singletonList(2L), queryDao.getEntries(IceEntryFilter.NONE, 1L, 1)
        .stream().map(e -> e.getItem().getId()).collect(Collectors.toList()));
    assertEquals(singletonList(3L), queryDao.getEntries(IceEntryFilter.NONE, 2L, 1)
        .stream().map(e -> e.getItem().getId()).collect(Collectors.toList()));
    assertEquals(singletonList(3L), queryDao.getEntries(IceEntryFilter.NONE, 2L, 100)
        .stream().map(e -> e.getItem().getId()).collect(Collectors.toList()));

    assertEquals(emptyList(), queryDao.getEntries(IceEntryFilter.NONE, 2L, 0)
        .stream().map(e -> e.getItem().getId()).collect(Collectors.toList()));
    assertEquals(emptyList(), queryDao.getEntries(IceEntryFilter.NONE, 3L, 100)
        .stream().map(e -> e.getItem().getId()).collect(Collectors.toList()));
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
  }

  //
  // Private
  //

  private long addLanguage(long id, String alias) {
    updaterDao.addEntry(IceEntry.newBuilder()
        .setItem(IceItem.newBuilder().setId(id).setType("language").setAlias(alias).build())
        .build());
    return id;
  }

  private long addEnLanguage() {
    return addLanguage(100L, "en");
  }
}
