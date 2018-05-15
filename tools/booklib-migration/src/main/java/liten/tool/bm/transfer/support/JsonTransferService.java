package liten.tool.bm.transfer.support;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.truward.protobuf.jackson.ProtobufJacksonUtil;
import liten.catalog.model.Ise;
import liten.catalog.util.IseNames;
import liten.tool.bm.transfer.TransferService;
import liten.tool.bm.transfer.support.model.BookMeta;
import liten.tool.bm.transfer.support.model.NamedValue;
import liten.tool.bm.transfer.support.model.SeriesPos;
import liten.tool.bm.transfer.util.flibusta.FlibustaLanguages;
import liten.tool.bm.transfer.util.flibusta.FlibustaMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.stream.Collectors;

/**
 * Transfers data from the legacy DB into JSON file.
 */
@Transactional(value = "tool.txManager", propagation = Propagation.REQUIRED)
public final class JsonTransferService implements TransferService {
  private static final int BOOK_TRANSFER_LIMIT = 1000;

  private static final JsonFactory DEFAULT_JSON_FACTORY = new JsonFactory();

  private static final String FLIBUSTA_ID_TYPE = "flib";

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final JdbcOperations db;

  private final String outputFileName;
  private Ise.Dump.Builder target;


  public JsonTransferService(JdbcOperations db, String outputFileName) {
    this.db = db;
    this.outputFileName = outputFileName;
  }

  @Override
  public boolean prepare() {
    log.info("Preparing transfer...");

    try {
      final int count = db.queryForObject("SELECT COUNT(0) FROM book_meta", Integer.class);
      log.info("Number of items about to be migrated: {}", count);
    } catch (DataAccessException ignored) {
      log.warn("There is no items table, schema is invalid, returning");
      return false;
    }

    // Create ID mappings
    this.target = Ise.Dump.newBuilder();

    ensureLanguageAliasesExist();

    insertNamedValues(IseNames.GENRE, "SELECT id, code FROM genre", "code");
    insertNamedValues(IseNames.PERSON, "SELECT id, f_name FROM author", "f_name");
    insertNamedValues(IseNames.ORIGIN, "SELECT id, code FROM book_origin", "code");
    insertNamedValues(IseNames.SERIES,"SELECT id, name FROM series", "name");

    return true;
  }

  @Override
  public String transferNext(String startId) {
    final List<BookMeta> bookMetas = db.query("SELECT id, title, f_size, add_date, lang_id, origin_id " +
            "FROM book_meta WHERE ((? IS NULL) OR (id > ?)) ORDER BY id LIMIT ?",
        new FlibustaMappers.BookMetaRowMapper(),
        startId,
        startId,
        BOOK_TRANSFER_LIMIT);

    log.info("BookMetas={}", bookMetas.stream().map(x -> x.id).collect(Collectors.toList()));

    for (final BookMeta bookMeta : bookMetas) {
      insertBook(bookMeta);
    }
    if (bookMetas.isEmpty()) {
      return null;
    }

    return Long.toString(bookMetas.get(bookMetas.size() - 1).id); // last ID
  }

  @Override
  public void complete() {
    log.info("Starting to write to file {}", outputFileName);
    // at this point we should have all books appropriately put on their respective places
    // so, save the resultant JSON
    try (final OutputStream outputStream = new FileOutputStream(outputFileName)) {
      try (final JsonGenerator jg = DEFAULT_JSON_FACTORY.createGenerator(outputStream)) {
        ProtobufJacksonUtil.writeJson(this.target.build(), jg);
      }
    } catch (IOException e) {
      throw new RuntimeException("unable to save output file", e);
    }

    log.info("Transfer completed: itemCount={}", this.target.getItemsCount());
  }

  //
  // Private
  //

  private static String getInlineFlibExternalId(String itemType, long value) {
    return "external:" + FLIBUSTA_ID_TYPE + "/" + itemType + "-" + value;
  }

  private void insertBook(BookMeta book) {
    final Ise.ExternalId bookFlibustaId = getFlibustaId(IseNames.BOOK, book.id);

    final Ise.BookItemExtras.Builder bookExtrasBuilder = Ise.BookItemExtras.newBuilder();

    // save genres relations
    final List<Long> genreIds = db.queryForList("SELECT genre_id FROM book_genre WHERE book_id=?",
        Long.class, book.id);
    for (final Long flibGenreId : genreIds) {
      bookExtrasBuilder.addGenreIds(getInlineFlibExternalId("genre", flibGenreId));
    }

    // save authors
    final List<Long> authorIds = db.queryForList("SELECT author_id FROM book_author WHERE book_id=?",
        Long.class, book.id);
    for (final Long flibAuthorId : authorIds) {
      bookExtrasBuilder.addAuthorIds(getInlineFlibExternalId("author", flibAuthorId));
    }

    // save series relations
    final List<SeriesPos> seriesPosList = getSeriesPos(book.id);
    if (seriesPosList.size() == 1) {
      final SeriesPos sp = seriesPosList.get(0);
      bookExtrasBuilder.setSeriesPos(sp.pos);
      bookExtrasBuilder.setSeriesId(getInlineFlibExternalId("series", sp.seriesId));
    } else if (seriesPosList.size() > 1) {
      throw new IllegalStateException("Book is a part of more than two series, flibBookId=" + book.id);
    }

    final Ise.Item.Builder builder = Ise.Item.newBuilder()
        .addExternalIds(bookFlibustaId)
        .setExtras(Ise.ItemExtras.newBuilder().setBook(bookExtrasBuilder))
        .setType(IseNames.BOOK)
        .addNotes("flibLanguage=" + book.langId)
        .addSkus(Ise.Sku.newBuilder()
            .setTitle(book.title)
            .addEntries(Ise.Entry.newBuilder()
                .setCreatedTimestamp(book.dateAdded.getTime())
                .setDownloadInfo(Ise.DownloadInfo.newBuilder()
                    .setDownloadType("fb2")
                    .setOriginId(getInlineFlibExternalId("origin", book.originId))
                    .setDownloadId(Long.toString(book.id))
                    .setFileSize(book.fileSize)
                    .build())));

    this.target.addItems(builder.build());
  }

  private void ensureLanguageAliasesExist() {
    // now check that all the languages are covered
    final List<NamedValue> languages =
        db.query("SELECT id, code FROM lang_code", new FlibustaMappers.NamedValueRowMapper("code"));
    for (final NamedValue lang : languages) {
      final Ise.ExternalId flibustaId = getFlibustaId(IseNames.LANGUAGE, lang.id);

      // match locale and detect other values
      final Locale curLocale;
      final String localeAlias;
      final int dashIndex = lang.name.indexOf('-');
      if (dashIndex > 0) {
        final String languageCode = lang.name.substring(0, dashIndex).toLowerCase();
        final String country = lang.name.substring(dashIndex + 1).toUpperCase();
        curLocale = new Locale(languageCode, country);
        localeAlias = languageCode + '-' + country;
      } else {
        curLocale = new Locale(lang.name);
        localeAlias = lang.name.toLowerCase();
      }

      Ise.LangItemExtras.Builder langItemExtraBuilder = Ise.LangItemExtras.newBuilder();
      if (!StringUtils.isEmpty(curLocale.getLanguage())) {
        langItemExtraBuilder.setLanguageCode(curLocale.getLanguage());
      }
      if (!StringUtils.isEmpty(curLocale.getCountry())) {
        langItemExtraBuilder.setCountryCode(curLocale.getCountry());
      }

      final Ise.ExternalId langAliasId = IseNames.newAlias(localeAlias);

      String iso3Language = null;
      try {
        iso3Language = curLocale.getISO3Language();
      } catch (final MissingResourceException ignored) {
        // ignore
        log.trace("Unknown language code");
      }

      // insert a new one
      final Ise.Item.Builder langItemBuilder = Ise.Item.newBuilder()
          .setExtras(Ise.ItemExtras.newBuilder().setLang(langItemExtraBuilder.build()))
          .addExternalIds(flibustaId);
      if (!StringUtils.isEmpty(iso3Language)) {
        for (int i = 0; i < FlibustaLanguages.KNOWN_LANG_ALIASES.size(); ++i) {
          final FlibustaLanguages.LangAlias langAlias = FlibustaLanguages.KNOWN_LANG_ALIASES.get(i);
          final String languageName = curLocale.getDisplayLanguage(langAlias.locale);
          langItemBuilder.addSkus(Ise.Sku.newBuilder()
              .setId(Integer.toString(i + 1)).setTitle(languageName).setLanguage(langAlias.alias));
        }
      } else {
        // unknown language
        langItemBuilder
            .addExternalIds(langAliasId)
            .addSkus(Ise.Sku.newBuilder().setId("1").setTitle(lang.name));
      }

      this.target.addItems(langItemBuilder.build());
    }

    log.info("Language existence ensured");
  }

  private void insertNamedValues(String itemType, String query, String nameField) {
    final List<NamedValue> values = db.query(query, new FlibustaMappers.NamedValueRowMapper(nameField));
    for (final NamedValue v : values) {
      this.target.addItems(Ise.Item.newBuilder()
          .addExternalIds(getFlibustaId(itemType, v.id))
          .setType(itemType)
          .addSkus(Ise.Sku.newBuilder()
              .setId("1")
              .setTitle(v.name)
              .build())
          .build());
    }
  }

  private static Ise.ExternalId getFlibustaId(String type, long flibId) {
    return Ise.ExternalId
        .newBuilder()
        .setIdType(FLIBUSTA_ID_TYPE)
        .setIdValue(type + "-" + Long.toString(flibId))
        .build();
  }

  private List<SeriesPos> getSeriesPos(Long bookId) {
    return db.query("SELECT series_id, pos FROM book_series WHERE book_id=?", (rs, i) -> {
      final SeriesPos result = new SeriesPos();
      result.seriesId = rs.getLong("series_id");
      result.pos = rs.getInt("pos");

      return result;
    }, bookId);
  }
}
