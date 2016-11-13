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

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
  public void shouldInsertAndQueryRawItem() {
    final long id = addEnLanguage();

    final IceEntry entry = queryDao.getEntry(id, IceEntryFilter.forLanguages("en"));
    assertEquals("en", entry.getDisplayTitle());
    assertFalse(entry.isDefaultInstancePresent());
  }

  @Test
  public void shouldInsertUpdateQueryAndDeleteInstance() {
    final long langId = addEnLanguage();
    final long bookId = 110L;
    updaterDao.addEntry(IceEntry.newBuilder()
        .setItem(IceItem.newBuilder().setId(bookId).setType("book").setAlias("The Crow (Book)").build())
        .addSku(IceSku.newBuilder().setId(1L).setTitle("The Crow").setLanguageId(langId).build())
        .addInstance(1L, IceInstance.newBuilder()
            .setCreated(UtcTime.now()).setDownloadId(456L).setOriginId(123L).build())
        .build());

    {
      final IceEntry entry = queryDao.getEntry(bookId, IceEntryFilter.forLanguages("en"));
      assertEquals("The Crow", entry.getDisplayTitle());
      assertTrue(entry.isDefaultInstancePresent());
      assertEquals(123L, entry.getDefaultInstance().getOriginId());
      assertEquals(456L, entry.getDefaultInstance().getDownloadId());
      assertEquals("en", entry.getRelatedItem(entry.getDefaultSkuEntry().getSku().getLanguageId()).getAlias());
    }

    final Consumer<IceEntry> entryTestFn = (entry) -> {
      assertEquals("The Crow", entry.getDisplayTitle());
      assertTrue(entry.isDefaultInstancePresent());
      assertEquals(123L, entry.getDefaultInstance().getOriginId());
      assertEquals(456L, entry.getDefaultInstance().getDownloadId());
      assertEquals("en", entry.getRelatedItem(entry.getDefaultSkuEntry().getSku().getLanguageId()).getAlias());
    };

    assertEntryEquals(bookId, IceEntryFilter.forLanguages("en"), entryTestFn);
    assertEntryEquals(bookId, IceEntryFilter.NONE, entryTestFn);
  }

  @Test
  public void shouldAllowAddingMultipleUnaliasedItems() {
    final IceItem template = IceItem.newBuilder().setType("book").build();

    updaterDao.addEntries(Arrays.asList(
        IceEntry.newBuilder().setItem(IceItem.newBuilder(template).setId(1L).build()).build(),
        IceEntry.newBuilder().setItem(IceItem.newBuilder(template).setId(2L).build()).build(),
        IceEntry.newBuilder().setItem(IceItem.newBuilder(template).setId(3L).build()).build()
    ));

    queryDao.getEntries(IceEntryFilter.NONE, ModelWithId.INVALID_ID, 100);
  }

  @Test(expected = DuplicateKeyException.class)
  public void shouldRejectDuplicateAlias() {
    final long enLangId = addEnLanguage();
    final IceEntry enLang = queryDao.getEntry(enLangId, IceEntryFilter.forLanguages("en"));

    final long otherId = enLangId + 1;
    final IceEntry other = IceEntry.newBuilder()
        .setItem(IceItem.newBuilder(enLang.getItem()).setId(otherId).build())
        .build();

    updaterDao.addEntry(other);
  }

  //
  // Private
  //

  private long addEnLanguage() {
    final long id = 100L;
    updaterDao.addEntry(IceEntry.newBuilder()
        .setItem(IceItem.newBuilder().setId(id).setType("language").setAlias("en").build())
        .build());

    return id;
  }

  private void assertEntryEquals(long id, IceEntryFilter filter, Consumer<IceEntry> testFn) {
    testFn.accept(queryDao.getEntry(id, filter));
  }
}
