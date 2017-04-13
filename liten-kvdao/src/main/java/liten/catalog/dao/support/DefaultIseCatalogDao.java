package liten.catalog.dao.support;

import com.truward.semantic.id.IdCodec;
import com.truward.semantic.id.SemanticIdCodec;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.*;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.model.Ise;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.security.SecureRandom;
import java.util.*;

import static com.truward.xodus.util.ProtoEntity.entryToProto;
import static com.truward.xodus.util.ProtoEntity.protoToEntry;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class DefaultIseCatalogDao implements IseCatalogDao {
  private static final IdCodec ITEM_CODEC = SemanticIdCodec.forPrefixNames("S1");

  private final Random random;
  private final Store itemStore;
  private final Store externalIdStore;

  public DefaultIseCatalogDao(Environment environment) {
    this.random = new SecureRandom();
    this.itemStore = environment.computeInTransaction(tx ->
        environment.openStore("item", StoreConfig.WITHOUT_DUPLICATES, tx));
    this.externalIdStore = environment.computeInTransaction(tx ->
        environment.openStore("external-id", StoreConfig.WITH_DUPLICATES, tx));
  }

  @Override
  public Ise.Item getById(Transaction tx, String id) {
    final byte[] key = ITEM_CODEC.decodeBytes(id);
    return entryToProto(itemStore.get(tx, new ArrayByteIterable(key)), Ise.Item.getDefaultInstance());
  }

  @Override
  public String persist(Transaction tx, Ise.Item item) {
    // try to find by an external ID
    for (final Ise.ExternalId externalId : item.getExternalIdsList()) {
      final ByteIterable idCandidate = externalIdStore.get(tx, protoToEntry(externalId));

    }

    // lookup for an ID
    String id = item.getId();
    byte[] key;
    if (StringUtils.hasLength(id)) {
      key = ITEM_CODEC.decodeBytes(id);
    } else {
      // get unique key
      int keySize = 4;
      do {
        key = new byte[keySize];
        random.nextBytes(key);
      } while (itemStore.get(tx, new ArrayByteIterable(key)) != null);
      id = ITEM_CODEC.encodeBytes(key);
      item = Ise.Item.newBuilder(item).setId(id).build();
    }

    itemStore.put(tx, new ArrayByteIterable(key), protoToEntry(item));
    return id;
  }

  @Override
  public List<String> getNameHints(Transaction tx, @Nullable String type, String prefix) {
    final Cursor cursor = itemStore.openCursor(tx);
    final Set<String> prefixes = new HashSet<>();
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
