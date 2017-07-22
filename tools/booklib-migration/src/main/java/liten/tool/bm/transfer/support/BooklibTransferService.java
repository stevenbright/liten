package liten.tool.bm.transfer.support;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import jetbrains.exodus.env.Transaction;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.model.Ise;
import liten.catalog.util.IseNames;
import liten.tool.bm.transfer.TransferService;
import liten.tool.bm.transfer.support.model.BookMeta;
import liten.tool.bm.transfer.support.model.NamedValue;
import liten.tool.bm.transfer.support.model.SeriesPos;
import liten.tool.bm.transfer.util.FlibustaLanguages;
import liten.tool.bm.transfer.util.FlibustaMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
  private Map<Long, String> langToItem;
  private Map<Long, String> seriesToItem;

  public BooklibTransferService(JdbcOperations db,
                                IseCatalogDao catalogDao) {
    this.db = db;
    this.catalogDao = Objects.requireNonNull(catalogDao, "catalogDao");
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

      this.originToItem = insertNamedValues(tx, "origin",
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
            "FROM book_meta WHERE ((? IS NULL) OR (id > ?)) ORDER BY id LIMIT ?", new FlibustaMappers.BookMetaRowMapper(),
        startId, startId, limit);
    log.info("BookMetas={}", bookMetas);

//    for (final BookMeta bookMeta : bookMetas) {
//      final Long itemId = addItem(bookMeta.getTitle(), bookTypeId);
//      log.trace("Book {}->{}", bookMeta.getId(), itemId);
//
//      // save origin and language relations
//      insertRelation(itemId, originToItem.get(bookMeta.getOriginId()), originTypeId);
//      insertRelation(itemId, langToItem.get(bookMeta.getLangId()), languageTypeId);
//
//      // save genres relations
//      final List<Long> genreIds = db.queryForList("SELECT genre_id FROM book_genre WHERE book_id=?",
//          Long.class, bookMeta.getId());
//      insertCodedRelations(itemId, genreIds, genreToItem, genreTypeId);
//
//      // save authors relations
//      final List<Long> authorIds = db.queryForList("SELECT author_id FROM book_author WHERE book_id=?",
//          Long.class, bookMeta.getId());
//      insertCodedRelations(itemId, authorIds, personToItem, authorTypeId);
//
//      // save series relations
//      final List<SeriesPos> seriesPosList = getSeriesPos(bookMeta.getId());
//      Integer pos = null;
//      for (SeriesPos seriesPos : seriesPosList) {
//        // should be only one
//        insertRelation(itemId, seriesToItem.get(seriesPos.getSeriesId()), seriesTypeId);
//        pos = seriesPos.getPos() > 0 ? seriesPos.getPos() : null;
//      }
//
//      // create metadata with series position and known file size
//      final EolaireModel.Metadata.Builder metadataBuilder = EolaireModel.Metadata.newBuilder();
//      if (pos != null) {
//        metadataBuilder.addEntries(EolaireModel.MetadataEntry.newBuilder()
//            .setKey("seriesPos").setValue(EolaireModel.VariantValue.newBuilder().setIntValue(pos))
//            .setType(EolaireModel.VariantType.INT32)
//            .build());
//      }
//
//      metadataBuilder.addEntries(EolaireModel.MetadataEntry.newBuilder().setKey("fileSize")
//          .setType(EolaireModel.VariantType.INT32)
//          .setValue(EolaireModel.VariantValue.newBuilder().setIntValue(bookMeta.getFileSize())));
//
//      insertBookProfile(itemId, 1, metadataBuilder.build());
//    }

    if (limit > bookMetas.size()) {
      return null;
    }

    return Long.toString(bookMetas.get(bookMetas.size() - 1).id); // last ID
  }

  @Override
  public void complete() {
    final int origBookCount = db.queryForObject("SELECT COUNT(0) FROM book_meta", Integer.class);
    log.info("Transfer completed: bookCount={}", origBookCount);
  }

  //
  // Private
  //

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

    Locale enLoc = new Locale("en");
    Locale ruLoc = new Locale("ru");

    // now check that all the languages are covered
    final List<String> unsupportedLanguages = new ArrayList<>();
    final List<NamedValue> languages =
        db.query("SELECT id, code FROM lang_code", new FlibustaMappers.NamedValueRowMapper("code"));
    //String languageCode = lang.name;
    //String country = null;
    for (final NamedValue lang : languages) {

      final Locale curLocale;
      final int dashIndex = lang.name.indexOf('-');
      if (dashIndex > 0) {
        final String languageCode = lang.name.substring(0, dashIndex);
        final String country = lang.name.substring(dashIndex + 1);
        curLocale = new Locale(languageCode, country);
      } else {
        curLocale = new Locale(lang.name);
      }

      String iso3Language = null;
      try {
        iso3Language = curLocale.getISO3Language();
      } catch (final MissingResourceException ignored) {
        // ignore
      }

      //final Ise.Item existingLangItem = catalogDao.getByExternalId(IseNames.newAlias());

      String enLangName = curLocale.getDisplayLanguage(enLoc);
      String ruLangName = curLocale.getDisplayLanguage(ruLoc);
      log.info("iso3={}, country={}, locale={}, enName={}, ruName={}",
          iso3Language,
          curLocale.getCountry(),
          lang.name,
          enLangName,
          ruLangName);

      if (!FlibustaLanguages.FLIBUSTA_CODE_TO_ALIAS.containsKey(lang.name)) {
        unsupportedLanguages.add(lang.name);
      }
    }

    if (!unsupportedLanguages.isEmpty()) {
      throw new UnsupportedOperationException("Unsupported languages=" + unsupportedLanguages);
    }

    log.info("Language existence ensured");
  }

  private static Ise.ExternalId getFlibustaId(long flibId) {
    return Ise.ExternalId.newBuilder().setIdType(FLIBUSTA_ID_TYPE).setIdValue(Long.toString(flibId)).build();
  }

  private String getOrAddItem(Transaction tx, String itemName, String itemType, Long flibustaId) {
    final Ise.ExternalId flibustaExternalId = getFlibustaId(flibustaId);
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

  private Long getEntityTypeId(String n) {
    throw new UnsupportedOperationException("TODO: refactor - this needs to be replaced w/ something else");
  }

  private void insertCodedRelations(Long lhs, List<Long> codedRhsList, Map<Long, Long> map, Long typeId) {
    for (final Long codedRhs : codedRhsList) {
      insertRelation(lhs, map.get(codedRhs), typeId);
    }
  }

  private void insertRelation(Long lhs, Long rhs, Long typeId) {
    assert lhs != null && rhs != null && typeId != null;
    db.update("INSERT INTO item_relation (lhs, rhs, type_id) VALUES (?, ?, ?)", lhs, rhs, typeId);
  }

  private List<SeriesPos> getSeriesPos(Long bookId) {
    return db.query("SELECT series_id, pos FROM book_series WHERE book_id=?", (rs, i) -> {
      final SeriesPos result = new SeriesPos();
      result.seriesId = rs.getLong("series_id");
      result.pos = rs.getInt("pos");

      return result;
    }, bookId);
  }

  private Long getNextEntityTypeId() {
    return db.queryForObject("SELECT seq_entity_type.nextval", Long.class);
  }
}
