package liten.catalog.dao.support;

import com.truward.semantic.id.IdCodec;
import com.truward.semantic.id.SemanticIdCodec;
import com.truward.xodus.util.KeyGenerator;
import com.truward.xodus.util.KeyUtil;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.*;
import liten.catalog.dao.IseCatalogDao;
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
import static jetbrains.exodus.bindings.StringBinding.entryToString;
import static jetbrains.exodus.bindings.StringBinding.stringToEntry;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class DefaultIseCatalogDao implements IseCatalogDao {
  private static final IdCodec ITEM_CODEC = SemanticIdCodec.forPrefixNames("S1");

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final Store itemStore;
  private final Store externalIdStore;
  private final KeyGenerator keyGenerator;

  public DefaultIseCatalogDao(Environment environment) {
    this.itemStore = environment.computeInTransaction(tx ->
        environment.openStore("item", StoreConfig.WITHOUT_DUPLICATES, tx));
    this.externalIdStore = environment.computeInTransaction(tx ->
        environment.openStore("external-id", StoreConfig.WITH_DUPLICATES, tx));

    final Random random = new SecureRandom();
    this.keyGenerator = KeyUtil.createKeyGenerator(itemStore, ITEM_CODEC, random);
  }

  @Override
  public Ise.Item getById(Transaction tx, String id) {
    final byte[] key = ITEM_CODEC.decodeBytes(id);
    return entryToProto(itemStore.get(tx, new ArrayByteIterable(key)), Ise.Item.getDefaultInstance());
  }

  @Nullable
  @Override
  public Ise.Item getByExternalId(Transaction tx, Ise.ExternalId externalId) {
    final ByteIterable idKey = externalIdStore.get(tx, protoToEntry(externalId));
    if (idKey == null) {
      return null;
    }

    return getById(tx, entryToString(idKey));
  }

  @Override
  public List<String> getNameHints(Transaction tx, @Nullable String type, String prefix) {
    final Cursor cursor = itemStore.openCursor(tx);
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

  @Override
  public String persist(Transaction tx, Ise.Item item) {
    validateItem(item);

    // lookup for an ID
    String id = item.getId();
    ByteIterable idKey;
    boolean overrideExisting = false;
    if (StringUtils.hasLength(id)) {
      overrideExisting = true;
    } else {
      id = keyGenerator.getUniqueKey(tx);
      item = Ise.Item.newBuilder(item).setId(id).build();
    }
    idKey = KeyUtil.semanticIdAsKey(ITEM_CODEC, id);

    if (overrideExisting) {
      // cleanup external keys on old item
      final ByteIterable existingItemBytes = itemStore.get(tx, idKey);
      if (existingItemBytes != null) {
        Ise.Item oldItem = entryToProto(existingItemBytes, Ise.Item.getDefaultInstance());
        log.trace("Dropping old item with id={}", id);
        for (final Ise.ExternalId externalId : oldItem.getExternalIdsList()) {
          externalIdStore.delete(tx, protoToEntry(externalId));
        }
      }
    }

    // put item itself
    itemStore.put(tx, idKey, protoToEntry(item));

    // and add associated external IDs
    for (final Ise.ExternalId externalId : item.getExternalIdsList()) {
      externalIdStore.put(tx, protoToEntry(externalId), stringToEntry(id));
    }

    return id;
  }

  //
  // Private
  //

  private static void validateItem(Ise.Item item) {
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
