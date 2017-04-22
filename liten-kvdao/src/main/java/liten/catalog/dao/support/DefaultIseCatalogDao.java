package liten.catalog.dao.support;

import com.truward.dao.exception.InvalidCursorException;
import com.truward.semantic.id.IdCodec;
import com.truward.semantic.id.SemanticIdCodec;
import com.truward.xodus.util.KeyGenerator;
import com.truward.xodus.util.KeyUtil;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.*;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.dao.exception.DuplicateExternalIdException;
import liten.catalog.model.Ise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.security.SecureRandom;
import java.util.*;

import static com.truward.xodus.util.ProtoEntity.entryToProto;
import static com.truward.xodus.util.ProtoEntity.protoToEntry;
import static java.util.Objects.requireNonNull;
import static jetbrains.exodus.bindings.StringBinding.entryToString;
import static jetbrains.exodus.bindings.StringBinding.stringToEntry;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class DefaultIseCatalogDao implements IseCatalogDao {
  private static final String ITEM_STORE_NAME = "item";
  private static final String EXTERNAL_ID_STORE_NAME = "external-id";
  private static final String FORWARD_RELATIONS_STORE_NAME = "forward-relations";

  private static final IdCodec ITEM_CODEC = SemanticIdCodec.forPrefixNames("S1");

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final Stores stores;
  private final KeyGenerator keyGenerator;

  private static final class Stores {
    final Store item;
    final Store externalId;
    final Store forwardRelations;

    public Stores(Environment environment, Transaction tx) {
      // bytesFromSemanticId(item.id) -> item
      this.item = environment.openStore(ITEM_STORE_NAME, StoreConfig.WITHOUT_DUPLICATES, tx);

      // ExternalId -> item.id(semantic ID)
      this.externalId = environment.openStore(EXTERNAL_ID_STORE_NAME, StoreConfig.WITHOUT_DUPLICATES, tx);

      // ForwardRelationId -> item.id(semantic ID)
      // e.g.:
      //      fromItemId=SomeAuthorId, type=author -> bookId
      this.forwardRelations = environment.openStore(FORWARD_RELATIONS_STORE_NAME, StoreConfig.WITH_DUPLICATES, tx);
    }
  }

  public DefaultIseCatalogDao(Environment environment) {
    this.stores = environment.computeInTransaction(tx -> new Stores(environment, tx));

    final Random random = new SecureRandom();
    this.keyGenerator = KeyUtil.createKeyGenerator(stores.item, ITEM_CODEC, random);
  }

  @Override
  public Ise.Item getById(Transaction tx, String id) {
    final byte[] key = ITEM_CODEC.decodeBytes(id);
    return entryToProto(stores.item.get(tx, new ArrayByteIterable(key)), Ise.Item.getDefaultInstance());
  }

  @Nullable
  @Override
  public Ise.Item getByExternalId(Transaction tx, Ise.ExternalId externalId) {
    final ByteIterable idKey = stores.externalId.get(tx, protoToEntry(externalId));
    if (idKey == null) {
      return null;
    }

    return getById(tx, entryToString(idKey));
  }

  @Override
  public List<String> getNameHints(Transaction tx, @Nullable String type, String prefix) {
    try (final Cursor cursor = stores.item.openCursor(tx)) {
      final Set<String> prefixes = new TreeSet<>(); // use for built-in sorting capabilities
      final int len = prefix.length() + 1;
      final boolean hasType = StringUtils.hasLength(type);

      // Bruteforce traverse (really bad performance)
      // TODO: indexes
      while (cursor.getNext()) {
        final Ise.Item item = entryToProto(cursor.getValue(), Ise.Item.getDefaultInstance());
        if (hasType && !type.equals(item.getType())) {
          continue;
        }

        for (final Ise.Sku sku : item.getSkusList()) {
          final String title = sku.getTitle();

          if (title.length() >= len && title.startsWith(prefix)) {
            prefixes.add(title.substring(0, len));
          }
        }
      }

      return new ArrayList<>(prefixes);
    }
  }

  @Override
  public Ise.ItemQueryResult getItems(Transaction tx, Ise.ItemQuery query) {
    if (query.getLimit() <= 0) {
      return Ise.ItemQueryResult.getDefaultInstance(); // too few results requested
    }

    try (final Cursor cursor = stores.item.openCursor(tx)) {
      if (StringUtils.hasLength(query.getCursor())) {
        final ByteIterable cursorKey = new ArrayByteIterable(ITEM_CODEC.decodeBytes(query.getCursor()));
        final ByteIterable nextCursorKey = cursor.getSearchKeyRange(cursorKey);
        if (nextCursorKey == null) {
          throw new InvalidCursorException(query.getCursor());
        }
      }

      final Ise.ItemQueryResult.Builder resultBuilder = Ise.ItemQueryResult.newBuilder();
      final int limit = Math.min(query.getLimit(), MAX_LIMIT);

      // TODO: optimize - now brute-force iteration is used
      while (cursor.getNext()) {
        final Ise.Item item = entryToProto(cursor.getValue(), Ise.Item.getDefaultInstance());

        if (StringUtils.hasLength(query.getNamePrefix())) {
          boolean matches = false;
          for (final Ise.Sku sku : item.getSkusList()) {
            if (sku.getTitle().regionMatches(true, 0, query.getNamePrefix(),
                0, query.getNamePrefix().length())) {
              matches = true;
              break;
            }
          }

          if (!matches) {
            continue;
          }
        }

        if (StringUtils.hasLength(query.getType()) && !query.getType().equals(item.getType())) {
          continue;
        }

        // item matches, insert it into the list
        resultBuilder.addItems(item);
        if (resultBuilder.getItemsCount() >= limit) {
          resultBuilder.setCursor(KeyUtil.keyAsSemanticId(ITEM_CODEC, cursor.getKey()));
          break;
        }
      }

      return resultBuilder.build();
    }
  }

  @Override
  public Ise.ItemRelationQueryResult getRelations(Transaction tx, Ise.ItemRelationQuery query) {
    if (!ITEM_CODEC.canDecode(query.getFromItemId())) {
      throw new IllegalArgumentException("fromItemId");
    }

    if (query.getLimit() <= 0) {
      return Ise.ItemRelationQueryResult.getDefaultInstance(); // too few results requested
    }

    final ByteIterable forwardRelationIdKey = protoToEntry(Ise.ForwardRelationId.newBuilder()
        .setFromItemId(query.getFromItemId())
        .setRelationType(query.getType())
        .build());

    final Ise.ItemRelationQueryResult.Builder resultBuilder = Ise.ItemRelationQueryResult.newBuilder();
    final int limit = Math.min(query.getLimit(), MAX_LIMIT);

    try (Cursor cursor = stores.forwardRelations.openCursor(tx)) {
      boolean hasNext;
      if (query.getCursor().length() > 0) {
        if (!cursor.getSearchBoth(forwardRelationIdKey, stringToEntry(query.getCursor()))) {
          throw new InvalidCursorException(query.getCursor());
        }
        hasNext = cursor.getNextDup(); // jump to the value next to cursor
      } else {
        hasNext = (cursor.getSearchKey(forwardRelationIdKey) != null); // try jump to the first value
      }

      for (; hasNext; hasNext = cursor.getNextDup()) {
        final String toItemId = entryToString(cursor.getValue());
        resultBuilder.addToItemIds(toItemId);
        if (resultBuilder.getToItemIdsCount() >= limit) {
          resultBuilder.setCursor(toItemId);
          break;
        }
      }
    }

    return resultBuilder.build();
  }

  @Override
  public String persist(Transaction tx, Ise.Item item) {
    validateItem(item);

    // lookup for an ID and find out if this is an override
    ByteIterable idKey;
    boolean overrideExisting = false;
    if (StringUtils.hasLength(item.getId())) {
      overrideExisting = true;
    } else {
      final String id = keyGenerator.getUniqueKey(tx);
      item = Ise.Item.newBuilder(item).setId(id).build();
    }
    idKey = KeyUtil.semanticIdAsKey(ITEM_CODEC, item.getId());

    // cleanup item relations if it is an override
    if (overrideExisting) {
      final ByteIterable existingItemBytes = stores.item.get(tx, idKey);
      if (existingItemBytes != null) {
        log.trace("Dropping old item with id={}", item.getId());
        cleanupItemRelations(tx, entryToProto(existingItemBytes, Ise.Item.getDefaultInstance()));
      }
    }

    // put item itself
    stores.item.put(tx, idKey, protoToEntry(item));
    setupItemRelations(tx, item);

    return item.getId();
  }

  //
  // Private
  //

  private void setupItemRelations(Transaction tx, Ise.Item item) {
    final String id = item.getId();

    // setup external IDs
    final ByteIterable itemIdEntry = stringToEntry(id);
    for (final Ise.ExternalId externalId : item.getExternalIdsList()) {
      if (!stores.externalId.add(tx, protoToEntry(externalId), itemIdEntry)) {
        throw new DuplicateExternalIdException(externalId, id,
            entryToString(requireNonNull(stores.externalId.get(tx, protoToEntry(externalId)))));
      }
    }

    // extras-specific logic
    if (item.hasExtras()) {
      final Ise.ItemExtras itemExtras = item.getExtras();

      if (itemExtras.hasBook()) {
        final Ise.BookItemExtras bookExtras = itemExtras.getBook();

        // authors->book
        setRelations(tx, AUTHOR, bookExtras.getAuthorIdsList(), id);
        // genres->book
        setRelations(tx, GENRE, bookExtras.getGenreIdsList(), id);
        // series->book
        if (StringUtils.hasLength(bookExtras.getSeriesId())) {
          setRelations(tx, SERIES, Collections.singletonList(bookExtras.getSeriesId()), id);
        }
      }
    }
  }

  private void cleanupItemRelations(Transaction tx, Ise.Item item) {
    // drop external IDs
    for (final Ise.ExternalId externalId : item.getExternalIdsList()) {
      stores.externalId.delete(tx, protoToEntry(externalId));
    }

    // drop author relations
    if (item.getExtras().hasBook()) {
      try (final Cursor forwardRelationsCursor = stores.forwardRelations.openCursor(tx)) {
        final Ise.BookItemExtras bookExtras = item.getExtras().getBook();
        dropRelations(forwardRelationsCursor, AUTHOR, bookExtras.getAuthorIdsList(), item.getId());
        dropRelations(forwardRelationsCursor, GENRE, bookExtras.getGenreIdsList(), item.getId());

        if (StringUtils.hasLength(bookExtras.getSeriesId())) {
          dropRelations(forwardRelationsCursor, SERIES,
              Collections.singletonList(bookExtras.getSeriesId()), item.getId());
        }
      }
    }
  }

  private void setRelations(Transaction tx, String type, List<String> fromItemIds, String toItemId) {
    assert ITEM_CODEC.canDecode(toItemId) && !ITEM_CODEC.canDecode(type);

    for (final String fromItemId : fromItemIds) {
      assert ITEM_CODEC.canDecode(fromItemId);

      final Ise.ForwardRelationId forwardRelationId = Ise.ForwardRelationId.newBuilder()
          .setFromItemId(fromItemId)
          .setRelationType(type)
          .build();

      if (!stores.forwardRelations.put(tx, protoToEntry(forwardRelationId), stringToEntry(toItemId))) {
        // should never happen
        log.warn("Non-overridden forward relation: fromItemId={}, type={}, toItemId={}", fromItemId, type, toItemId);
      }
    }
  }

  private void dropRelations(Cursor forwardRelationsCursor, String type, List<String> fromItemIds, String toItemId) {
    assert ITEM_CODEC.canDecode(toItemId) && !ITEM_CODEC.canDecode(type);

    for (final String fromItemId : fromItemIds) {
      assert ITEM_CODEC.canDecode(fromItemId);

      final Ise.ForwardRelationId forwardRelationId = Ise.ForwardRelationId.newBuilder()
          .setFromItemId(fromItemId)
          .setRelationType(type)
          .build();

      if (forwardRelationsCursor.getSearchBoth(protoToEntry(forwardRelationId), stringToEntry(toItemId))) {
        forwardRelationsCursor.deleteCurrent();
      }
    }
  }

  private static void validateItem(Ise.Item item) {
    KeyUtil.assertValidOptionalId(ITEM_CODEC, item.getId(), () -> "Invalid item id=" + item.getId());

    // validate extras
    if (item.hasExtras()) {
      final Ise.ItemExtras itemExtras = item.getExtras();
      if (itemExtras.hasBook()) {
        final Ise.BookItemExtras bookExtras = itemExtras.getBook();
        KeyUtil.assertValidOptionalId(ITEM_CODEC, bookExtras.getSeriesId(),
            () -> "Invalid seriesId=" + bookExtras.getSeriesId() + " for item id=" + item.getId());

        for (final String authorId : bookExtras.getAuthorIdsList()) {
          KeyUtil.assertValidId(ITEM_CODEC, authorId,
              () -> "Invalid authorId=" + authorId + " for item id=" + item.getId());
        }

        for (final String genreId : bookExtras.getGenreIdsList()) {
          KeyUtil.assertValidId(ITEM_CODEC, genreId,
              () -> "Invalid genreId=" + genreId + " for item id=" + item.getId());
        }
      }
    }

    // validate SKUs and entries within each SKU
    final Set<String> skuIds = new HashSet<>(item.getSkusCount() * 2);
    final Set<String> entryIds = new HashSet<>();
    for (final Ise.Sku sku : item.getSkusList()) {
      if (!skuIds.add(sku.getId())) {
        throw new IllegalArgumentException("Duplicate SkuId=" + sku.getId());
      }

      entryIds.clear();
      for (final Ise.Entry entry : sku.getEntriesList()) {
        if (!entryIds.add(entry.getId())) {
          throw new IllegalArgumentException("Duplicate EntryId=" + entry.getId() + " for SkuId=" + sku.getId());
        }
      }
    }
  }
}
