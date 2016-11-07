package liten.catalog.dao;

import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

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
  public void shouldInsertAndQueryItems() {
    final Long id = updaterDao.addEntry(createEntry("language", "en"));

    final IceEntry entry = queryDao.getEntry(id, "en");
    assertEquals("en", entry.getDisplayTitle());
  }

  private static IceEntry createEntry(String type, String defaultTitle) {
    final IceEntry entry = new IceEntry();
    entry.setItem(new IceItem(type, defaultTitle));
    return entry;
  }
}
