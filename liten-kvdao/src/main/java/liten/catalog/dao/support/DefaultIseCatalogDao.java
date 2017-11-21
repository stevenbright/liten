package liten.catalog.dao.support;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.truward.brikar.common.log.LogLapse;
import com.truward.kvdao.exception.InvalidCursorException;
import com.truward.semantic.id.IdCodec;
import com.truward.semantic.id.SemanticIdCodec;
import com.truward.kvdao.xodus.KeyUtil;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.*;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.dao.exception.DuplicateExternalIdException;
import liten.catalog.model.Ise;
import liten.catalog.util.IseNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.security.SecureRandom;
import java.util.*;

import static com.truward.kvdao.xodus.ProtoEntity.entryToProto;
import static com.truward.kvdao.xodus.ProtoEntity.protoToEntry;
import static java.util.Objects.requireNonNull;
import static jetbrains.exodus.bindings.StringBinding.entryToString;
import static jetbrains.exodus.bindings.StringBinding.stringToEntry;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
@Repository
public final class DefaultIseCatalogDao implements IseCatalogDao {
  /**
   * Key size, used to generate smaller IDs in an environment that allows global contention control.
   */
  private static final int START_KEY_SIZE = 5;

  private static final String ITEM_STORE_NAME = "item";
  private static final String EXTERNAL_ID_STORE_NAME = "external-id";
  private static final String FORWARD_RELATIONS_STORE_NAME = "forward-relations";
  private static final String NAME_HINT_STORE_NAME = "name-hint";

  /**
   * Item encoder, "ci1" stands for Catalog Item ver. 1
   */
  private static final IdCodec ITEM_CODEC = SemanticIdCodec.forPrefixNames("ci1");

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final Environment environment;
  private final Stores stores;
  private final Random keyRandom;
  private final int startKeySize;

  private Set<String> allowedItemTypes = IseNames.ALLOWED_ITEM_TYPES;
  private Set<String> allowedRelationTypes = IseNames.ALLOWED_RELATION_TYPES;

  private static final class Stores {
    final Store item;
    final Store externalId;
    final Store forwardRelations;
    final Store nameHint;

    Stores(Environment environment, Transaction tx) {
      // bytesFromSemanticId(item.id) -> item
      this.item = environment.openStore(ITEM_STORE_NAME, StoreConfig.WITHOUT_DUPLICATES, tx);

      // ExternalId -> item.id(semantic ID)
      this.externalId = environment.openStore(EXTERNAL_ID_STORE_NAME, StoreConfig.WITHOUT_DUPLICATES, tx);

      // ForwardRelationId -> item.id(semantic ID)
      // e.g.:
      //      fromItemId=SomeAuthorId, type=author -> bookId
      this.forwardRelations = environment.openStore(FORWARD_RELATIONS_STORE_NAME, StoreConfig.WITH_DUPLICATES, tx);

      this.nameHint = environment.openStore(NAME_HINT_STORE_NAME, StoreConfig.WITHOUT_DUPLICATES, tx);
    }
  }

  public DefaultIseCatalogDao(Environment environment, Random keyRandom, int startKeySize) {
    if (startKeySize <= 0 || startKeySize > KeyUtil.DEFAULT_KEY_BYTES_SIZE) {
      throw new IllegalArgumentException("startKeySize");
    }

    this.environment = Objects.requireNonNull(environment, "environment");
    this.keyRandom = Objects.requireNonNull(keyRandom, "keyRandom");
    this.stores = environment.computeInTransaction(tx -> new Stores(environment, tx));
    this.startKeySize = startKeySize;
  }

  public DefaultIseCatalogDao(Environment environment) {
    this(environment, new SecureRandom(), START_KEY_SIZE);
  }

  @Override
  public void addItemType(String itemType) {
    this.allowedItemTypes = ImmutableSet.<String>builder().addAll(this.allowedItemTypes).add(itemType).build();
  }

  @Override
  public Environment getEnvironment() {
    return environment;
  }

  //@LogLapse("IseCatalogDao.getById") <-- too frequent
  @Override
  public Ise.Item getById(Transaction tx, String id) {
    final byte[] key = ITEM_CODEC.decodeBytes(id);
    return entryToProto(stores.item.get(tx, new ArrayByteIterable(key)), Ise.Item.getDefaultInstance());
  }

  //@LogLapse("IseCatalogDao.getMappedIdByExternalId") <-- too frequent
  @Nullable
  @Override
  public String getMappedIdByExternalId(Transaction tx, Ise.ExternalId externalId) {
    final ByteIterable idKey = stores.externalId.get(tx, protoToEntry(externalId));
    if (idKey == null) {
      return null;
    }

    return entryToString(idKey);
  }

  @LogLapse("IseCatalogDao.getNameHints")
  @Override
  public List<String> getNameHints(Transaction tx, @Nullable String type, String prefix) {
    prefix = prefix.toUpperCase();

    if (prefix.length() > 0) {
      // use indices
      final ByteIterable nameHintValue = this.stores.nameHint.get(tx, stringToEntry(prefix));
      if (nameHintValue == null) {
        return ImmutableList.of();
      }

      final Ise.NameHint nameHint = entryToProto(nameHintValue, Ise.NameHint.getDefaultInstance());
      final List<String> prefixes = new ArrayList<>(nameHint.getNameReferences().getPrefixesCount());

      for (final Ise.TypedNameReference nameReference : nameHint.getNameReferences().getPrefixesList()) {
        if (!Strings.isNullOrEmpty(type) && !nameReference.getTypesList().contains(type)) {
          continue;
        }

        prefixes.add(nameReference.getPrefix());
      }

      return prefixes;
    }

    // TODO: use root index
    try (final Cursor cursor = stores.item.openCursor(tx)) {
      final Set<String> prefixes = new TreeSet<>(); // use for built-in sorting capabilities
      final int len = prefix.length() + 1;
      final boolean hasType = !Strings.isNullOrEmpty(type);

      // Bruteforce traverse (really bad performance)
      // TODO: indexes
      while (cursor.getNext()) {
        final Ise.Item item = entryToProto(cursor.getValue(), Ise.Item.getDefaultInstance());
        if (hasType && !type.equals(item.getType())) {
          continue;
        }

        for (final Ise.Sku sku : item.getSkusList()) {
          final String title = sku.getTitle().toUpperCase();

          if (title.length() >= len && title.startsWith(prefix)) {
            prefixes.add(title.substring(0, len));
          }
        }
      }

      return new ArrayList<>(prefixes);
    }
  }

  @LogLapse("IseCatalogDao.getItems")
  @Override
  public Ise.ItemQueryResult getItems(Transaction tx, Ise.ItemQuery query) {
    if (query.getLimit() <= 0) {
      return Ise.ItemQueryResult.getDefaultInstance(); // too few results requested
    }

    try (final Cursor cursor = stores.item.openCursor(tx)) {
      if (!Strings.isNullOrEmpty(query.getCursor())) {
        final ByteIterable cursorKey = new ArrayByteIterable(ITEM_CODEC.decodeBytes(query.getCursor()));
        final ByteIterable nextCursorKey = cursor.getSearchKeyRange(cursorKey);
        if (nextCursorKey == null) {
          throw new InvalidCursorException(query.getCursor());
        }
      }

      final Ise.ItemQueryResult.Builder resultBuilder = Ise.ItemQueryResult.newBuilder();
      final int limit = Math.min(query.getLimit(), IseCatalogDao.MAX_LIMIT);

      // TODO: optimize - now brute-force iteration is used
      while (cursor.getNext()) {
        final Ise.Item item = entryToProto(cursor.getValue(), Ise.Item.getDefaultInstance());

        if (!Strings.isNullOrEmpty(query.getNamePrefix())) {
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

        if (!Strings.isNullOrEmpty(query.getType()) && !query.getType().equals(item.getType())) {
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

  @LogLapse("IseCatalogDao.getRelations")
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
    final int limit = Math.min(query.getLimit(), IseCatalogDao.MAX_LIMIT);

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

  @LogLapse("IseCatalogDao.persist")
  @Override
  public String persist(Transaction tx, Ise.Item item) {
    validateItem(item);

    // lookup for an ID and find out if this is an override
    if (!Strings.isNullOrEmpty(item.getId())) {
      // drop existing item
      final ByteIterable idKey = KeyUtil.semanticIdAsKey(ITEM_CODEC, item.getId());
      final ByteIterable existingItemBytes = stores.item.get(tx, idKey);
      if (existingItemBytes != null) {
        log.trace("Dropping old item with id={}", item.getId());
        cleanupItemRelations(tx, entryToProto(existingItemBytes, Ise.Item.getDefaultInstance()));
      }

      // put updated version of item
      stores.item.put(tx, idKey, protoToEntry(item));
    } else {
      item = KeyUtil.addUniqueEntry(
          tx,
          stores.item,
          item,
          (it, id) -> Ise.Item.newBuilder(it).setId(id).build(),
          ITEM_CODEC,
          this.startKeySize,
          this.keyRandom
      );
    }

    setupItemRelations(tx, item);
    setupItemNameHints(tx, item);

    return item.getId();
  }

  //
  // Private
  //

  private void setupItemNameHints(Transaction tx, Ise.Item item) {
    for (final Ise.Sku sku : item.getSkusList()) {
      setupSkuNameHints(tx, item.getType(), item.getId(), sku);
    }
  }

  private void setupSkuNameHints(Transaction tx, String type, String itemId, Ise.Sku sku) {
    final String title = sku.getTitle();

    final StringBuilder prefixBuilder = new StringBuilder(3);
    final int count = Math.min(3, title.length());

    for (int i = 0; i < count; ++i) {
      prefixBuilder.append(Character.toUpperCase(title.charAt(i)));
      final ByteIterable nameKey = stringToEntry(prefixBuilder.toString());
      final ByteIterable nameValue = this.stores.nameHint.get(tx, nameKey);
      final Ise.NameHint.Builder nameHint = Ise.NameHint.newBuilder();
      if (nameValue != null) {
        nameHint.mergeFrom(entryToProto(nameValue, Ise.NameHint.getDefaultInstance()));
      }

      // insert either next name prefix or item link
      if (i < (count - 1)) {
        final Ise.TypedNameReference typedNameReference = Ise.TypedNameReference.newBuilder()
            .setPrefix(title.substring(0, i + 2).toUpperCase())
            .addTypes(type)
            .build();

        final int insertIndex = Collections.binarySearch(
            nameHint.getNameReferences().getPrefixesList(),
            typedNameReference,
            Comparator.comparing(Ise.TypedNameReference::getPrefix)
        );

        final List<Ise.TypedNameReference> namePrefixes = new ArrayList<>(
            nameHint.getNameReferences().getPrefixesList());
        if (insertIndex < 0) {
          namePrefixes.add(-1 - insertIndex, typedNameReference);
        } else {
          namePrefixes.set(
              insertIndex,
              Ise.TypedNameReference.newBuilder()
                  .setPrefix(typedNameReference.getPrefix())
                  .addAllTypes(ImmutableSet.<String>builder()
                      .addAll(namePrefixes.get(insertIndex).getTypesList())
                      .add(type)
                      .build())
                  .build());
        }

        nameHint.setNameReferences(Ise.NameReferences.newBuilder().addAllPrefixes(namePrefixes));
      } else {
        // last entry, so insert link
        final Ise.ItemLink newItemLink = Ise.ItemLink.newBuilder()
            .setItemId(itemId)
            .setItemType(type)
            .setSkuId(sku.getId())
            .setSkuTitle(title)
            .build();

        final int insertIndex = Collections.binarySearch(
            nameHint.getItemLinks().getLinksList(),
            newItemLink,
            (l, r) -> {
              final int cmp = l.getSkuTitle().compareTo(r.getSkuTitle());
              if (cmp != 0) {
                return 0;
              }
              return l.getItemId().compareTo(r.getItemId());
            });

        if (insertIndex < 0) {
          final List<Ise.ItemLink> links = new ArrayList<>(nameHint.getItemLinks().getLinksList());
          links.add(-1 - insertIndex, newItemLink);
          nameHint.setItemLinks(Ise.ItemLinks.newBuilder().addAllLinks(links));
        }
      }

      this.stores.nameHint.put(tx, nameKey, protoToEntry(nameHint.build()));
    }
  }

  private void setupItemRelations(Transaction tx, Ise.Item item) {
    final String id = item.getId();

    // setup external IDs
    final ByteIterable itemIdEntry = stringToEntry(id);
    for (final Ise.ExternalId externalId : item.getExternalIdsList()) {
      if (!stores.externalId.add(tx, protoToEntry(externalId), itemIdEntry)) {
        final ByteIterable otherKey = stores.externalId.get(tx, protoToEntry(externalId));
        throw new DuplicateExternalIdException(externalId, id, entryToString(requireNonNull(otherKey)));
      }
    }

    // extras-specific logic
    if (item.hasExtras()) {
      final Ise.ItemExtras itemExtras = item.getExtras();

      if (itemExtras.hasBook()) {
        final Ise.BookItemExtras bookExtras = itemExtras.getBook();

        // authors->book
        setRelations(tx, IseNames.AUTHOR, bookExtras.getAuthorIdsList(), id);
        // genres->book
        setRelations(tx, IseNames.GENRE, bookExtras.getGenreIdsList(), id);
        // series->book
        if (!Strings.isNullOrEmpty(bookExtras.getSeriesId())) {
          setRelations(tx, IseNames.SERIES, Collections.singletonList(bookExtras.getSeriesId()), id);
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
        dropRelations(forwardRelationsCursor, IseNames.AUTHOR, bookExtras.getAuthorIdsList(), item.getId());
        dropRelations(forwardRelationsCursor, IseNames.GENRE, bookExtras.getGenreIdsList(), item.getId());

        if (!Strings.isNullOrEmpty(bookExtras.getSeriesId())) {
          dropRelations(forwardRelationsCursor, IseNames.SERIES,
              Collections.singletonList(bookExtras.getSeriesId()), item.getId());
        }
      }
    }
  }

  private void setRelations(Transaction tx, String type, List<String> fromItemIds, String toItemId) {
    assert ITEM_CODEC.canDecode(toItemId) && !ITEM_CODEC.canDecode(type);

    if (Strings.isNullOrEmpty(type)) {
      throw new IllegalArgumentException("Missing relation type");
    }

    if (!allowedRelationTypes.contains(type)) {
      throw new IllegalArgumentException("Unknown relation type=" + type);
    }

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

  private void validateItem(Ise.Item item) {
    KeyUtil.assertValidOptionalId(ITEM_CODEC, item.getId(), () -> "Invalid item id=" + item.getId());

    // validate type
    if (Strings.isNullOrEmpty(item.getType())) {
      throw new IllegalArgumentException("Missing type, item id=" + item.getId());
    }

    if (!allowedItemTypes.contains(item.getType())) {
      throw new IllegalArgumentException("Unsupported type=" + item.getType() + ", item id=" + item.getId());
    }

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
