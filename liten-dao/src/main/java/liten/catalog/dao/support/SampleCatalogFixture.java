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
  private static final long AUTHOR2     = 405L;
  private static final long AUTHOR3     = 410L;

  private static final long BOOK1       = 500L;
  private static final long BOOK2       = 501L;
  private static final long BOOK3       = 502L;
  private static final long BOOK4       = 503L;
  private static final long BOOK5       = 504L;
  private static final long BOOK6       = 505L;
  private static final long BOOK7       = 506L;
  private static final long BOOK8       = 507L;

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

    d.setRelation(BOOK1, ORIGIN1, "origin");
    d.setRelation(BOOK1, RELIGION, "genre");

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
    d.setRelation(BOOK2, ORIGIN2, "origin");
    d.setRelation(BOOK2, NOVEL, "genre");
    d.setRelation(BOOK2, AUTHOR1, "author");

    d.addEntry(IceEntry.newBuilder().setItem(IceItem.newBuilder().setId(AUTHOR2).setType("author").build())
        .addSku(IceSku.newBuilder().setId(1).setLanguageId(RU)
            .setTitle("Аркадий Стругацкий").build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle("Arkady Strugatsky").build()).build());
    d.addEntry(IceEntry.newBuilder().setItem(IceItem.newBuilder().setId(AUTHOR3).setType("author").build())
        .addSku(IceSku.newBuilder().setId(1).setLanguageId(RU)
            .setTitle("Борис Стругацкий").build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle("Boris Strugatsky").build()).build());

    d.addEntry(IceEntry.newBuilder().setItem(IceItem.newBuilder().setId(BOOK3).setType("book").build())
        .addSku(IceSku.newBuilder().setId(1).setLanguageId(RU)
            .setTitle("Страна багровых туч").build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle("The Land of Crimson Clouds").build()).build());
    d.setRelation(BOOK3, ORIGIN2, "origin");
    d.setRelation(BOOK3, NOVEL, "genre");
    d.setRelation(BOOK3, SCIFI, "genre");
    d.setRelation(BOOK3, AUTHOR2, "author");
    d.setRelation(BOOK3, AUTHOR3, "author");

    d.addEntry(IceEntry.newBuilder().setItem(IceItem.newBuilder().setId(BOOK4).setType("book").build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle("Test 4").build()).build());
    d.addEntry(IceEntry.newBuilder().setItem(IceItem.newBuilder().setId(BOOK5).setType("book").build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle("Test 5").build()).build());
    d.addEntry(IceEntry.newBuilder().setItem(IceItem.newBuilder().setId(BOOK6).setType("book").build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle("Test 6").build()).build());
    d.addEntry(IceEntry.newBuilder().setItem(IceItem.newBuilder().setId(BOOK7).setType("book").build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle("Test 7").build()).build());
    d.addEntry(IceEntry.newBuilder().setItem(IceItem.newBuilder().setId(BOOK8).setType("book").build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle("Test 8").build()).build());
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
