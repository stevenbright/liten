package liten.tool.bm.transfer.support;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import jetbrains.exodus.env.Transaction;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.dao.exception.DuplicateExternalIdException;
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

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of data transfer service
 */
@Transactional(value = "tool.txManager", propagation = Propagation.REQUIRED)
public final class BooklibTransferService implements TransferService {

  private static final int BOOK_TRANSFER_LIMIT = 1000;

  private static final String FLIBUSTA_ID_TYPE = "flib";

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final JdbcOperations db;

  private final IseCatalogDao catalogDao;

  private Map<Long, String> genreToItem;
  private Map<Long, String> personToItem;
  private Map<Long, String> originToItem;
  private Map<Long, String> seriesToItem;

  public BooklibTransferService(JdbcOperations db,
                                IseCatalogDao catalogDao) {
    this.db = db;
    this.catalogDao = requireNonNull(catalogDao, "catalogDao");
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
    catalogDao.getEnvironment().executeInTransaction(tx -> {
      ensureLanguageAliasesExist(tx);

      this.genreToItem = insertNamedValues(tx, IseNames.GENRE, db.query("SELECT id, code FROM genre",
          new FlibustaMappers.NamedValueRowMapper("code")));

      this.personToItem = insertNamedValues(tx, IseNames.PERSON, db.query("SELECT id, f_name FROM author",
          new FlibustaMappers.NamedValueRowMapper("f_name")));

      this.originToItem = insertNamedValues(tx, IseNames.ORIGIN,
          db.query("SELECT id, code FROM book_origin", new FlibustaMappers.NamedValueRowMapper("code")));

      this.seriesToItem = insertNamedValues(tx, IseNames.SERIES,
          db.query("SELECT id, name FROM series", new FlibustaMappers.NamedValueRowMapper("name")));
    });

    return true;
  }

  @Override
  public String transferNext(String startId) {
    final int limit = BOOK_TRANSFER_LIMIT;
    final List<BookMeta> bookMetas = db.query("SELECT id, title, f_size, add_date, lang_id, origin_id " +
            "FROM book_meta WHERE ((? IS NULL) OR (id > ?)) ORDER BY id LIMIT ?",
        new FlibustaMappers.BookMetaRowMapper(),
        startId,
        startId,
        limit);

    log.info("BookMetas={}", bookMetas.stream().map(x -> x.id).collect(Collectors.toList()));

    catalogDao.getEnvironment().executeInTransaction(tx -> {
      try {
        for (final BookMeta bookMeta : bookMetas) {
          insertBook(tx, bookMeta);
        }
      } catch (DuplicateExternalIdException e) {
        final Ise.Item mappedItem = catalogDao.getById(tx, e.getMappedItemId());
        log.error("Offending MappedItem={}", mappedItem);
        throw e;
      }
    });

    if (limit > bookMetas.size()) {
      return null;
    }

    return Long.toString(bookMetas.get(bookMetas.size() - 1).id); // last ID
  }

  @Override
  public void complete() {
    final int origBookCount = db.queryForObject("SELECT COUNT(0) FROM book_meta", Integer.class);
    log.info("Transfer completed: bookCount={}", origBookCount);

    // collect garbage
    catalogDao.getEnvironment().gc();
  }

  //
  // Private
  //

  private String getLanguageAlias(Transaction tx, BookMeta book) {
    final Ise.Item languageItem = catalogDao.getByExternalId(tx, getFlibustaId(IseNames.LANGUAGE, book.langId));
    final List<Ise.ExternalId> ids = languageItem != null ? languageItem.getExternalIdsList() : ImmutableList.of();
    return ids
        .stream()
        .filter(x -> x.getIdType().equals(IseNames.ALIAS))
        .map(Ise.ExternalId::getIdValue)
        .findFirst()
        .orElse(FlibustaLanguages.UNKNOWN_LANG_ALIAS.alias);
  }

  private void insertBook(Transaction tx, BookMeta book) {
    final Ise.ExternalId bookFlibustaId = getFlibustaId(IseNames.BOOK, book.id);
    if (catalogDao.getMappedIdByExternalId(tx, bookFlibustaId) != null) {
      log.debug("Item with id={} has been inserted", book.id);
    }

    final Ise.BookItemExtras.Builder bookExtrasBuilder = Ise.BookItemExtras.newBuilder();

    // save genres relations
    final List<Long> genreIds = db.queryForList("SELECT genre_id FROM book_genre WHERE book_id=?",
        Long.class, book.id);
    for (final Long flibGenreId : genreIds) {
      bookExtrasBuilder.addGenreIds(requireNonNull(genreToItem.get(flibGenreId), "genre"));
    }

    // save authors
    final List<Long> authorIds = db.queryForList("SELECT author_id FROM book_author WHERE book_id=?",
        Long.class, book.id);
    for (final Long flibAuthorId : authorIds) {
      bookExtrasBuilder.addAuthorIds(requireNonNull(personToItem.get(flibAuthorId), "author"));
    }

    // save series relations
    final List<SeriesPos> seriesPosList = getSeriesPos(book.id);
    if (seriesPosList.size() == 1) {
      final SeriesPos sp = seriesPosList.get(0);
      bookExtrasBuilder.setSeriesPos(sp.pos);
      bookExtrasBuilder.setSeriesId(requireNonNull(seriesToItem.get(sp.seriesId)));
    } else if (seriesPosList.size() > 1) {
      throw new IllegalStateException("Book is a part of more than two series, flibBookId=" + book.id);
    }

    final String originId = requireNonNull(originToItem.get(book.originId), "origin");

    final Ise.Item.Builder builder = Ise.Item.newBuilder()
        .addExternalIds(bookFlibustaId)
        .setExtras(Ise.ItemExtras.newBuilder().setBook(bookExtrasBuilder))
        .setType(IseNames.BOOK)
        .addSkus(Ise.Sku.newBuilder()
            .setTitle(book.title)
            .addEntries(Ise.Entry.newBuilder()
                .setCreatedTimestamp(book.dateAdded.getTime())
                .setDownloadInfo(Ise.DownloadInfo.newBuilder()
                    .setDownloadType("fb2")
                    .setOriginId(originId)
                    .setDownloadId(Long.toString(book.id))
                    .setFileSize(book.fileSize)
                    .build()))
            .setLanguage(getLanguageAlias(tx, book)));

    catalogDao.persist(tx, builder.build());
  }

  private void ensureLanguageAliasesExist(Transaction tx) {
    for (final FlibustaLanguages.LangAlias alias : FlibustaLanguages.FLIBUSTA_CODE_TO_ALIAS.values()) {
      if (catalogDao.getMappedIdByExternalId(tx, IseNames.newAlias(alias.alias)) != null) {
        continue; // ok, item present
      }

      // make sure language exists
      catalogDao.persist(tx, Ise.Item.newBuilder()
          .setType(IseNames.LANGUAGE)
          .addExternalIds(IseNames.newAlias(alias.alias))
          .addAllSkus(alias.skus)
          .build());
    }

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
      final Ise.Item existingLangItem = catalogDao.getByExternalId(tx, langAliasId);
      if (existingLangItem != null) {
        // item has been inserted already, check if we can assign language extras
        Ise.Item.Builder newItemBuilder = Ise.Item.newBuilder(existingLangItem)
            .setExtras(Ise.ItemExtras.newBuilder().setLang(langItemExtraBuilder.build()));
        if (!existingLangItem.getExternalIdsList().contains(flibustaId)) {
          newItemBuilder.addExternalIds(flibustaId);
        }

        catalogDao.persist(tx, newItemBuilder.build());
        continue;
      }

      String iso3Language = null;
      try {
        iso3Language = curLocale.getISO3Language();
      } catch (final MissingResourceException ignored) {
        // ignore
        log.trace("Unknown language code");
      }

      // there is no matching language in ISE DB, insert a new one
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
            .addAllSkus(FlibustaLanguages.UNKNOWN_LANG_ALIAS.skus);
      }
    }

    log.info("Language existence ensured");
  }

  private static Ise.ExternalId getFlibustaId(String type, long flibId) {
    return Ise.ExternalId
        .newBuilder()
        .setIdType(FLIBUSTA_ID_TYPE)
        .setIdValue(type + "-" + Long.toString(flibId))
        .build();
  }

  private String getOrAddItem(Transaction tx, String itemName, String itemType, Long flibustaId) {
    final Ise.ExternalId flibustaExternalId = getFlibustaId(itemType, flibustaId);
    final String existingItemId = catalogDao.getMappedIdByExternalId(tx, flibustaExternalId);
    if (!Strings.isNullOrEmpty(existingItemId)) {
      return existingItemId;
    }

    return addItem(tx, itemName, itemType, ImmutableList.of(flibustaExternalId));
  }

  private String addItem(Transaction tx, String itemName, String itemType, List<Ise.ExternalId> externalIds) {
    final String itemId = catalogDao.persist(tx, Ise.Item.newBuilder()
        .setType(itemType)
        .addAllExternalIds(externalIds)
        .addSkus(Ise.Sku.newBuilder()
            .setLanguage("en")
            .setTitle(itemName))
        .build());

    log.trace("Added IseItem: id={}", itemId);
    return itemId;
  }

  private Map<Long, String> insertNamedValues(Transaction tx, String typeName, List<NamedValue> values) {
    final Map<Long, String> result = new HashMap<>(values.size() * 2);

    for (final NamedValue value : values) {
      final String itemId = getOrAddItem(tx, value.name, typeName, value.id);
      result.put(value.id, itemId);
    }

    return result;
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
