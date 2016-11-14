package liten.catalog.dao;

import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceEntryFilter;
import liten.catalog.dao.support.SampleCatalogFixture;
import liten.dao.model.ModelWithId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Alexander Shabanov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring/CatalogDaoTest-context.xml")
@Transactional
public final class SampleCatalogFixtureTest {
  @Resource CatalogUpdaterDao updaterDao;
  @Resource CatalogQueryDao queryDao;

  @Test
  public void shouldInsertAndQuerySampleData() {
    SampleCatalogFixture.addSampleData(updaterDao);

    final List<IceEntry> entries = queryDao.getEntries(IceEntryFilter.NONE, ModelWithId.INVALID_ID, 3);
    assertEquals(3, entries.size());
  }
}
