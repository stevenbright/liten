package liten.catalog.dao.support;

import com.truward.time.UtcTime;
import liten.catalog.dao.CatalogUpdaterDao;
import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceInstance;
import liten.catalog.dao.model.IceItem;
import liten.catalog.dao.model.IceSku;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Defines fixture data for catalog
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class SampleCatalogFixture {
  private SampleCatalogFixture() {}

  private static final long EN          = 101L;
  private static final long RU          = 100L;

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

  private static long CUSTOM_ENTITY_LAST = 1000;

  public static void addSampleData(CatalogUpdaterDao d) {
    addItem(d, EN, "en", "language", "English", "Английский");
    addItem(d, RU, "ru", "language", "Russian", "Русский");

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
        .addInstance(1, IceInstance.newBuilder()
            .setCreated(date("2012-11-05")).setDownloadId(1011L).setOriginId(15L).build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle("War and Peace").build())
        .addInstance(2, IceInstance.newBuilder()
            .setCreated(date("2012-09-24")).setDownloadId(1013L).setOriginId(15L).build())
        .build());
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
        .addInstance(1, IceInstance.newBuilder()
            .setCreated(date("2012-10-24")).setDownloadId(1024L).setOriginId(15L).build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle("The Land of Crimson Clouds").build())
        .addInstance(2, IceInstance.newBuilder()
            .setCreated(date("2012-11-02")).setDownloadId(1025L).setOriginId(15L).build())
        .build());
    d.setRelation(BOOK3, ORIGIN2, "origin");
    d.setRelation(BOOK3, NOVEL, "genre");
    d.setRelation(BOOK3, SCIFI, "genre");
    d.setRelation(BOOK3, AUTHOR2, "author");
    d.setRelation(BOOK3, AUTHOR3, "author");

    addSciFiStrugatskyNovel(d, "Отель «У Погибшего Альпиниста»", "Dead Mountaineer's Hotel");
    addSciFiStrugatskyNovel(d, "Малыш", "Space Mowgli");
    addSciFiStrugatskyNovel(d, "Пикник на обочине", "Roadside Picnic");
    addSciFiStrugatskyNovel(d, "За миллиард лет до конца света", "Definitely Maybe");
    addSciFiStrugatskyNovel(d, "Град обреченный", "The Doomed City");
    addSciFiStrugatskyNovel(d, "Повесть о дружбе и недружбе", "Tale of Friendship and Non-friendship");
    addSciFiStrugatskyNovel(d, "Жук в муравейнике", "Beetle in the Anthill");
    addSciFiStrugatskyNovel(d, "Хромая судьба", "Limping Fate");
    addSciFiStrugatskyNovel(d, "Волны гасят ветер", "The Time Wanderers");
    addSciFiStrugatskyNovel(d, "Отягощённые злом", "Overburdened with Evil");
    addSciFiStrugatskyNovel(d, "Второе нашествие марсиан", "The Second Invasion from Mars");
    addSciFiStrugatskyNovel(d, "Сказка о Тройке", "Tale of the Troika");
    addSciFiStrugatskyNovel(d, "Обитаемый остров", "Prisoners of Power");
    addSciFiStrugatskyNovel(d, "Беспокойство", "Disquiet");
    addSciFiStrugatskyNovel(d, "Улитка на склоне", "Snail on the Slope");
    addSciFiStrugatskyNovel(d, "Попытка к бегству", "Escape Attempt");
    addSciFiStrugatskyNovel(d, "Далёкая Радуга", "Far Rainbow");
    addSciFiStrugatskyNovel(d, "Трудно быть богом", "Hard to Be a God");
    addSciFiStrugatskyNovel(d, "Понедельник начинается в субботу", "Monday Begins on Saturday");
    addSciFiStrugatskyNovel(d, "Хищные вещи века", "The Final Circle of Paradise");
    addSciFiStrugatskyNovel(d, "Извне", "From Beyond");
    addSciFiStrugatskyNovel(d, "Путь на Амальтею", "Destination: Amaltheia");
    addSciFiStrugatskyNovel(d, "Полдень, XXII век", "Noon: 22nd Century");
    addSciFiStrugatskyNovel(d, "Стажеры", "Space Apprentice");
    addSciFiStrugatskyNovel(d, "Гадкие лебеди", "The Ugly Swans");

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

  private static void addSciFiStrugatskyNovel(CatalogUpdaterDao d, String ruTitle, String enTitle) {
    final long id = CUSTOM_ENTITY_LAST + 1;
    CUSTOM_ENTITY_LAST = id + ThreadLocalRandom.current().nextInt(50) + 1;

    d.addEntry(IceEntry.newBuilder().setItem(IceItem.newBuilder().setId(id).setType("book").build())
        .addSku(IceSku.newBuilder().setId(1).setLanguageId(RU)
            .setTitle(ruTitle).build())
        .addInstance(1, IceInstance.newBuilder()
            .setCreated(date("2012-10-24")).build())
        .addSku(IceSku.newBuilder().setId(2).setLanguageId(EN)
            .setTitle(enTitle).build())
        .addInstance(2, IceInstance.newBuilder()
            .setCreated(date("2012-11-02")).build())
        .build());
    d.setRelation(id, ORIGIN2, "origin");
    d.setRelation(id, NOVEL, "genre");
    d.setRelation(id, SCIFI, "genre");
    d.setRelation(id, AUTHOR2, "author");
    d.setRelation(id, AUTHOR3, "author");
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
