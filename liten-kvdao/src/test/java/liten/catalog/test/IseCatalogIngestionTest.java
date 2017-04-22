package liten.catalog.test;

import com.truward.time.UtcTime;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static liten.catalog.dao.IseCatalogDao.*;
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
    final List<Ise.Item> items = createTestItemList();

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
        .getResourceAsStream("sampleItems/catalog-fixture.json")) {
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

    assertEquals(createTestItemList().size(), items.size());
  }

  //
  // Private
  //

  private static List<Ise.Item> createTestItemList() {
    return Arrays.asList(
        Ise.Item.newBuilder().setId("S1.X010").setType("language")
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("English"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Английский"))
            .addExternalIds(Ise.ExternalId.newBuilder().setIdType("alias").setIdValue("en"))
            .build(),

        Ise.Item.newBuilder().setId("S1.X020").setType("language")
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Russian"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Русский"))
            .addExternalIds(Ise.ExternalId.newBuilder().setIdType("alias").setIdValue("ru"))
            .build(),

        //
        // Genres
        //

        Ise.Item.newBuilder().setId("S1.G010").setType(GENRE)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Novel"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Новелла"))
            .build(),

        Ise.Item.newBuilder().setId("S1.G030").setType(GENRE)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Fantasy"))
            .build(),

        Ise.Item.newBuilder().setId("S1.G040").setType(GENRE)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Detective"))
            .build(),

        Ise.Item.newBuilder().setId("S1.G050").setType(GENRE)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Sci-Fi"))
            .build(),

        Ise.Item.newBuilder().setId("S1.G070").setType(GENRE)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Religion"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Религия"))
            .build(),

        //
        // Authors
        //

        Ise.Item.newBuilder().setId("S1.A010").setType(AUTHOR)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Leo Tolstoy"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Лев Николаевич Толстой"))
            .build(),

        Ise.Item.newBuilder().setId("S1.A040").setType(AUTHOR)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Arkady Strugatsky"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Аркадий Стругацкий"))
            .build(),

        Ise.Item.newBuilder().setId("S1.A050").setType(AUTHOR)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Boris Strugatsky"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Борис Стругацкий"))
            .build(),

        //
        // Books
        //

        Ise.Item.newBuilder().setId("S1.B010").setType(BOOK)
            .setExtras(Ise.ItemExtras.newBuilder().setBook(Ise.BookItemExtras.newBuilder()
                .addGenreIds("S1.G070")
                .build()))
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Holy Bible"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Библия"))
            .build(),

        Ise.Item.newBuilder().setId("S1.B210").setType(BOOK)
            .setExtras(Ise.ItemExtras.newBuilder().setBook(Ise.BookItemExtras.newBuilder()
                .addGenreIds("S1.G010")
                .addAuthorIds("S1.A010")
                .build()))
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("War and Peace")
                .addEntries(Ise.Entry.newBuilder()
                    .setCreatedTimestamp(date("2012-09-24").getTime())
                    .setDownloadInfo(Ise.DownloadInfo.newBuilder()
                        .setDownloadType("librus")
                        .setDownloadId("1013")
                        .setOriginId("15")
                        .setFileSize(1213453)
                        .build())
                    .build()))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Война и мир")
                .addEntries(Ise.Entry.newBuilder()
                    .setCreatedTimestamp(date("2012-11-05").getTime())
                    .setDownloadInfo(Ise.DownloadInfo.newBuilder()
                        .setDownloadType("librus")
                        .setDownloadId("1011")
                        .setOriginId("15")
                        .setFileSize(1532145)
                        .build())
                    .build()))
            .build(),

        Ise.Item.newBuilder().setId("S1.B250").setType(BOOK)
            .setExtras(Ise.ItemExtras.newBuilder().setBook(Ise.BookItemExtras.newBuilder()
                .addGenreIds("S1.G010").addGenreIds("S1.G050")
                .addAuthorIds("S1.A040").addAuthorIds("S1.A050")
                .build()))
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("The Land of Crimson Clouds")
                .addEntries(Ise.Entry.newBuilder()
                    .setCreatedTimestamp(date("2012-11-02").getTime())
                    .setDownloadInfo(Ise.DownloadInfo.newBuilder()
                        .setDownloadType("librus")
                        .setDownloadId("1024")
                        .setOriginId("15")
                        .setFileSize(111250)
                        .build())
                    .build()))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Страна багровых туч")
                .addEntries(Ise.Entry.newBuilder()
                    .setCreatedTimestamp(date("2012-10-24").getTime())
                    .setDownloadInfo(Ise.DownloadInfo.newBuilder()
                        .setDownloadType("librus")
                        .setDownloadId("1025")
                        .setOriginId("15")
                        .setFileSize(125460)
                        .build())
                    .build()))
            .build(),

        strugatskySciFiNovel("Отель «У Погибшего Альпиниста»", "Dead Mountaineer's Hotel"),
        strugatskySciFiNovel("Малыш", "Space Mowgli"),
        strugatskySciFiNovel("Пикник на обочине", "Roadside Picnic"),
        strugatskySciFiNovel("За миллиард лет до конца света", "Definitely Maybe"),
        strugatskySciFiNovel("Град обреченный", "The Doomed City"),
        strugatskySciFiNovel("Повесть о дружбе и недружбе", "Tale of Friendship and Non-friendship"),
        strugatskySciFiNovel("Жук в муравейнике", "Beetle in the Anthill"),
        strugatskySciFiNovel("Хромая судьба", "Limping Fate"),
        strugatskySciFiNovel("Волны гасят ветер", "The Time Wanderers"),
        strugatskySciFiNovel("Отягощённые злом", "Overburdened with Evil"),
        strugatskySciFiNovel("Второе нашествие марсиан", "The Second Invasion from Mars"),
        strugatskySciFiNovel("Сказка о Тройке", "Tale of the Troika"),
        strugatskySciFiNovel("Обитаемый остров", "Prisoners of Power"),
        strugatskySciFiNovel("Беспокойство", "Disquiet"),
        strugatskySciFiNovel("Улитка на склоне", "Snail on the Slope"),
        strugatskySciFiNovel("Попытка к бегству", "Escape Attempt"),
        strugatskySciFiNovel("Далёкая Радуга", "Far Rainbow"),
        strugatskySciFiNovel("Трудно быть богом", "Hard to Be a God"),
        strugatskySciFiNovel("Понедельник начинается в субботу", "Monday Begins on Saturday"),
        strugatskySciFiNovel("Хищные вещи века", "The Final Circle of Paradise"),
        strugatskySciFiNovel("Извне", "From Beyond"),
        strugatskySciFiNovel("Путь на Амальтею", "Destination: Amaltheia"),
        strugatskySciFiNovel("Полдень, XXII век", "Noon: 22nd Century"),
        strugatskySciFiNovel("Стажеры", "Space Apprentice"),
        strugatskySciFiNovel("Гадкие лебеди", "The Ugly Swans"),

        sampleBook("Test 4"),
        sampleBook("Test 5"),
        sampleBook("Test 6"),
        sampleBook("Test 7"),
        sampleBook("Test 8"),

        //
        // Unused (sample)
        //

        Ise.Item.newBuilder().setId("S1.G990").setType(GENRE)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("philosophy"))
            .build()

    );
  }

  private static Ise.Item strugatskySciFiNovel(String ruTitle, String enTitle) {
    return Ise.Item.newBuilder().setType(BOOK)
        .setExtras(Ise.ItemExtras.newBuilder().setBook(Ise.BookItemExtras.newBuilder()
            .addGenreIds("S1.G010").addGenreIds("S1.G050")
            .addAuthorIds("S1.A040").addAuthorIds("S1.A050")
            .build()))
        .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle(enTitle))
        .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle(ruTitle))
        .build();
  }

  private static Ise.Item sampleBook(String enTitle) {
    return Ise.Item.newBuilder().setType(BOOK)
        .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle(enTitle))
        .build();
  }

  public static UtcTime date(String strDate) {
    final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    fmt.setTimeZone(UtcTime.newUtcTimeZone());
    try {
      return UtcTime.valueOf(fmt.parse(strDate).getTime());
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
