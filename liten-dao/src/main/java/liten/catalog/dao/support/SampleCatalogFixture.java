package liten.catalog.dao.support;

import liten.catalog.dao.CatalogUpdaterDao;
import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceItem;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Defines fixture data for catalog
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class SampleCatalogFixture {
  private SampleCatalogFixture() {}

  public static void addSampleData(CatalogUpdaterDao d) {
    final long en = addItem(d, 101L, "en", "language");
    final long ru = addItem(d, 100L, "ru", "language");
    final long es = addItem(d, 103L, "es", "language");

    final long novel = addItem(d, 101L, "en", "language");
    final long fantasy = addItem(d, 100L, "ru", "language");
    final long detective = addItem(d, 103L, "es", "language");
    final long scifi = addItem(d, 103L, "es", "language");


  }

  public static long addItem(CatalogUpdaterDao d, long id, String alias, String type) {
    d.addEntry(IceEntry.newBuilder()
        .setItem(IceItem.newBuilder().setId(id)
            .build())
        .build());
    return id;
  }
}
