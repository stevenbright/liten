package liten.website.service;

import com.truward.time.UtcTime;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.model.Ise;
import liten.catalog.util.IseNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import static liten.catalog.util.IseNames.AUTHOR;
import static liten.catalog.util.IseNames.BOOK;
import static liten.catalog.util.IseNames.GENRE;

/**
 * @author Alexander Shabanov
 */
public class DemoInitializer {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Resource
  private IseCatalogDao catalogDao;

  @PostConstruct
  public void init() {
    log.info("Initializing demo data");

    catalogDao.getEnvironment().executeInTransaction(tx -> {
      for (final Ise.Item item : createTestItemList()) {
        catalogDao.persist(tx, item);
      }
    });

    log.info("Demo data added");
  }

  private static List<Ise.Item> createTestItemList() {
    return Arrays.asList(

        //
        // Languages
        //

        Ise.Item.newBuilder().setId("ci1-x010").setType("language")
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("English"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Английский"))
            .addExternalIds(IseNames.newAlias("en")) // alias:en
            .build(),

        Ise.Item.newBuilder().setId("ci1-x020").setType("language")
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Russian"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Русский"))
            .addExternalIds(IseNames.newAlias("ru")) // alias:ru
            .build(),

        //
        // Genres
        //

        Ise.Item.newBuilder().setId("ci1-g010").setType(GENRE)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Novel"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Новелла"))
            .build(),

        Ise.Item.newBuilder().setId("ci1-g030").setType(GENRE)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Fantasy"))
            .build(),

        Ise.Item.newBuilder().setId("ci1-g040").setType(GENRE)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Detective"))
            .build(),

        Ise.Item.newBuilder().setId("ci1-g050").setType(GENRE)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Sci-Fi"))
            .build(),

        Ise.Item.newBuilder().setId("ci1-g070").setType(GENRE)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Religion"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Религия"))
            .build(),

        //
        // Authors
        //

        Ise.Item.newBuilder().setId("ci1-a010").setType(AUTHOR)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Leo Tolstoy"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Лев Николаевич Толстой"))
            .build(),

        Ise.Item.newBuilder().setId("ci1-a040").setType(AUTHOR)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Arkady Strugatsky"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Аркадий Стругацкий"))
            .build(),

        Ise.Item.newBuilder().setId("ci1-a050").setType(AUTHOR)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Boris Strugatsky"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Борис Стругацкий"))
            .build(),

        //
        // Books
        //

        Ise.Item.newBuilder().setId("ci1-b010").setType(BOOK)
            .setExtras(Ise.ItemExtras.newBuilder().setBook(Ise.BookItemExtras.newBuilder()
                .addGenreIds("ci1-g070")
                .build()))
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("Holy Bible"))
            .addSkus(Ise.Sku.newBuilder().setId("2").setLanguage("ru").setTitle("Библия"))
            .build(),

        Ise.Item.newBuilder().setId("ci1-b210").setType(BOOK)
            .setExtras(Ise.ItemExtras.newBuilder().setBook(Ise.BookItemExtras.newBuilder()
                .addGenreIds("ci1-g010")
                .addAuthorIds("ci1-a010")
                .build()))
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("War and Peace")
                .addEntries(Ise.Entry.newBuilder()
                    .setId("1")
                    .setCreatedTimestamp(date("2012-09-24").getTime())
                    .setDownloadInfo(Ise.DownloadInfo.newBuilder()
                        .setDownloadType("librus")
                        .setDownloadId("1013")
                        .setOriginId("15")
                        .setFileSize(1213453)
                        .build())
                    .build())
                .addEntries(Ise.Entry.newBuilder()
                    .setId("2")
                    .setCreatedTimestamp(date("2012-09-25").getTime())
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

        Ise.Item.newBuilder().setId("ci1-B250").setType(BOOK)
            .setExtras(Ise.ItemExtras.newBuilder().setBook(Ise.BookItemExtras.newBuilder()
                .addGenreIds("ci1-g010").addGenreIds("ci1-g050")
                .addAuthorIds("ci1-a040").addAuthorIds("ci1-a050")
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

        Ise.Item.newBuilder().setId("ci1-g990").setType(GENRE)
            .addSkus(Ise.Sku.newBuilder().setId("1").setLanguage("en").setTitle("philosophy"))
            .build()

    );
  }

  private static Ise.Item strugatskySciFiNovel(String ruTitle, String enTitle) {
    return Ise.Item.newBuilder().setType(BOOK)
        .setExtras(Ise.ItemExtras.newBuilder().setBook(Ise.BookItemExtras.newBuilder()
            .addGenreIds("ci1-g010").addGenreIds("ci1-g050")
            .addAuthorIds("ci1-a040").addAuthorIds("ci1-a050")
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

  private static UtcTime date(String strDate) {
    final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    fmt.setTimeZone(UtcTime.newUtcTimeZone());
    try {
      return UtcTime.valueOf(fmt.parse(strDate).getTime());
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
