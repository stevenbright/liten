package liten.catalog.dao.support;

import liten.catalog.dao.CatalogUpdaterDao;
import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceItem;
import liten.catalog.dao.model.IceSku;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Defines fixture data for catalog
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class SampleCatalogFixture {
  private SampleCatalogFixture() {}

  private static final long EN = 101L;
  private static final long RU = 100L;

  private static final long NOVEL       = 200L;
  private static final long FANTASY     = 201L;
  private static final long DETECTIVE   = 203L;
  private static final long SCIFI       = 205L;
  private static final long RELIGION    = 211L;

  private static final long ORIGIN1     = 301L;
  private static final long ORIGIN2     = 302L;

  private static final long AUTHOR1     = 400L;

  private static final long BOOK1       = 500L;
  private static final long BOOK2       = 501L;

  public static void addSampleData(CatalogUpdaterDao d) {
    addItem(d, EN, "en", "language", "English", "Английский");
    addItem(d, RU, "ru", "language", "Russian", "Английский");

    addItem(d, NOVEL, "novel", "genre", "Novel", "Новелла");
    addItem(d, FANTASY, "fantasy", "genre", null, null);
    addItem(d, DETECTIVE, "detective", "genre", null, null);
    addItem(d, SCIFI, "sci-fi", "genre", null, null);
    addItem(d, RELIGION, "relig", "genre", "Religion", "Религия");

    addItem(d, ORIGIN1, "origin1", "origin", null, null);
    addItem(d, ORIGIN2, "origin2", "origin", null, null);

    d.addEntry(IceEntry.newBuilder().setItem(IceItem.newBuilder().setId(BOOK1).setType("book").build())
        .addSku(IceSku.newBuilder().setId(1).setLanguageId(RU)
            .setTitle("Библия").build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle("Holy Bible").build()).build());

    d.setRelation(ORIGIN1, BOOK1, "origin");
    d.setRelation(RELIGION, BOOK1, "genre");

    d.addEntry(IceEntry.newBuilder().setItem(IceItem.newBuilder().setId(AUTHOR1).setType("author").build())
        .addSku(IceSku.newBuilder().setId(1).setLanguageId(RU)
            .setTitle("Лев Николаевич Толстой").build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle("Leo Tolstoy").build()).build());

    d.addEntry(IceEntry.newBuilder().setItem(IceItem.newBuilder().setId(BOOK2).setType("book").build())
        .addSku(IceSku.newBuilder().setId(1).setLanguageId(RU)
            .setTitle("Война и мир").build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle("War and Peace").build()).build());
    d.setRelation(ORIGIN2, BOOK1, "origin");
    d.setRelation(NOVEL, BOOK1, "genre");
  }

  public static long addItem(CatalogUpdaterDao d,
                             long id,
                             String alias,
                             String type,
                             @Nullable String enName,
                             @Nullable String ruName) {
    final IceEntry.Builder builder = IceEntry.newBuilder()
        .setItem(IceItem.newBuilder()
            .setId(id)
            .setAlias(alias)
            .setType(type)
            .build());

    if (enName != null) {
      builder.addSku(IceSku.newBuilder()
          .setId(1)
          .setLanguageId(EN)
          .setTitle(enName)
          .build());
    }

    if (ruName != null) {
      builder.addSku(IceSku.newBuilder()
          .setId(2)
          .setLanguageId(RU)
          .setTitle(ruName)
          .build());
    }

    d.addEntry(builder.build());

    return id;
  }
}
