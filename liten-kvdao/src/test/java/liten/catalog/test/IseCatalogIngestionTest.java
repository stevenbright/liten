package liten.catalog.test;

import liten.catalog.dao.IseCatalogDao;
import liten.catalog.dao.support.DefaultIseCatalogDao;
import liten.catalog.dao.support.IseCatalogIngestionHelper;
import liten.catalog.model.Ise;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static liten.catalog.dao.support.IseCatalogSampleData.createItemList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Alexander Shabanov
 */
public final class IseCatalogIngestionTest extends XodusTestBase {
  private IseCatalogDao catalogDao;
  private IseCatalogIngestionHelper ingestionHelper;

  @Before
  public void init() {
    catalogDao = new DefaultIseCatalogDao(environment);
    ingestionHelper = new IseCatalogIngestionHelper(environment, catalogDao);
  }

  @Test
  public void shouldExportData() throws IOException {
    final List<Ise.Item> items = createItemList();

    environment.executeInTransaction(tx -> {
      for (int i = 0; i < items.size(); ++i) {
        final Ise.Item item = items.get(i);
        items.set(i, Ise.Item.newBuilder(item)
            .setId(catalogDao.persist(tx, item))
            .build());
      }
    });

    items.sort(Comparator.comparing(Ise.Item::getId));

    final String json;
    try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      ingestionHelper.exportData(outputStream);
      json = outputStream.toString(StandardCharsets.UTF_8.name());
    }
    assertFalse(json.isEmpty());
  }

  @Test
  public void shouldRestoreDump() throws IOException {
    try (final InputStream inputStream = getClass().getClassLoader()
        .getResourceAsStream("catalog-fixture-v0.json")) {
      ingestionHelper.importData(inputStream);
    }

    final List<Ise.Item> items = new ArrayList<>();
    for (final Ise.ItemQuery.Builder qb = Ise.ItemQuery.newBuilder().setLimit(10);;) {
      final Ise.ItemQueryResult res = environment.computeInTransaction(tx -> catalogDao.getItems(tx, qb.build()));
      items.addAll(res.getItemsList());
      if (res.getCursor().isEmpty()) {
        break;
      }
      qb.setCursor(res.getCursor());
    }

    assertEquals(createItemList().size(), items.size());
  }
}
