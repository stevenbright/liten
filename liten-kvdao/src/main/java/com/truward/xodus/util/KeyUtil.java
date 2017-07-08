package com.truward.xodus.util;

import com.google.protobuf.Message;
import com.truward.semantic.id.IdCodec;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.truward.xodus.util.ProtoEntity.protoToEntry;

/**
 * Utility class for operating on Xodus Keys.
 *
 * @author Alexander Shabanov
 */
public final class KeyUtil {

  /**
   * Key size, used to generate smaller IDs in an environment that allows global contention control.
   */
  public static final int START_KEY_SIZE = 5;

  /**
   * Default size of byte array backing the key.
   * The value is picked up to exclude potential clashes.
   */
  public static final int DEFAULT_KEY_BYTES_SIZE = 16;

  private KeyUtil() {}

  public static ByteIterable semanticIdAsKey(IdCodec codec, String id) {
    return new ArrayByteIterable(codec.decodeBytes(id));
  }

  public static String keyAsSemanticId(IdCodec codec, ByteIterable key) {
    byte[] keyBytes = key.getBytesUnsafe();
    if (keyBytes.length > key.getLength()) {
      keyBytes = Arrays.copyOf(keyBytes, key.getLength());
    }
    return codec.encodeBytes(keyBytes);
  }

  public static void assertValidId(IdCodec codec, String id, Supplier<String> messageSupplier) {
    if (codec.canDecode(id)) {
      return;
    }

    throw new IllegalArgumentException(messageSupplier.get());
  }

  public static void assertValidOptionalId(IdCodec codec, @Nullable String id, Supplier<String> messageSupplier) {
    if (StringUtils.hasLength(id)) {
      assertValidId(codec, id, messageSupplier);
    }
  }

  public static <T extends Message> T addUniqueEntry(
      Transaction tx,
      Store entryStore,
      T entry,
      BiFunction<T, String, T> idApplier,
      IdCodec entryKeyCodec,
      int startKeySize,
      Random keyRandom) {
    assert startKeySize > 0 && startKeySize < KeyUtil.DEFAULT_KEY_BYTES_SIZE;

    for (int keySize = startKeySize; keySize < KeyUtil.DEFAULT_KEY_BYTES_SIZE; ++keySize) {
      final byte[] keyBytes = new byte[keySize];
      keyRandom.nextBytes(keyBytes);

      final String id = entryKeyCodec.encodeBytes(keyBytes);
      entry = idApplier.apply(entry, id);

      // try adding as new item
      // TODO: this needs to be much smarter in multi-threaded environment,
      // TODO:    e.g. it requires cluster ID at the beginning of small IDs
      final ByteIterable idKey = new ArrayByteIterable(keyBytes);
      if (entryStore.add(tx, idKey, protoToEntry(entry))) {
        break;
      }
    }

    return entry;
  }
}
