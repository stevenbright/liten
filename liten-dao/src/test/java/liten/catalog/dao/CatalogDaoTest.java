package liten.catalog.dao;

import com.truward.time.UtcTime;
import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceInstance;
import liten.catalog.dao.model.IceItem;
import liten.catalog.dao.model.IceSku;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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

    final IceEntry entry = queryDao.getEntry(id, "en");
    assertEquals("en", entry.getDisplayTitle());
    assertFalse(entry.isDefaultInstancePresent());
  }

  @Test
  public void shouldInsertUpdateQueryAndDeleteInstance() {
    final long langId = addEnLanguage();
    final long bookId = 110L;
    updaterDao.addEntry(IceEntry.newBuilder()
        .setItem(IceItem.newBuilder().setId(bookId).setType("book").setDefaultTitle("The Crow (Book)").build())
        .addSku(IceSku.newBuilder().setId(1L).setTitle("The Crow").setLanguageId(langId).build())
        .addInstance(1L, IceInstance.newBuilder()
            .setCreated(UtcTime.now()).setDownloadId(456L).setOriginId(123L).build())
        .build());

    final IceEntry entry = queryDao.getEntry(bookId, "en");

    assertEquals("The Crow", entry.getDisplayTitle());
    assertTrue(entry.isDefaultInstancePresent());
    assertEquals(123L, entry.getDefaultInstance().getOriginId());
    assertEquals(456L, entry.getDefaultInstance().getDownloadId());
  }

  private long addEnLanguage() {
    final long id = 100L;
    updaterDao.addEntry(IceEntry.newBuilder()
        .setItem(IceItem.newBuilder().setId(id).setType("language").setDefaultTitle("en").build())
        .build());

    return id;
  }
}
