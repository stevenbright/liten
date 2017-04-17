package com.truward.xodus.util;

import com.truward.semantic.id.IdCodec;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Utility class for operating on Xodus Keys.
 *
 * @author Alexander Shabanov
 */
public final class KeyUtil {
  private KeyUtil() {}

  public static KeyGenerator createKeyGenerator(Store store, IdCodec codec, Random random) {
    return new DefaultKeyGenerator(store, codec, random);
  }

  public static ByteIterable semanticIdAsKey(IdCodec codec, String id) {
    return new ArrayByteIterable(codec.decodeBytes(id));
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

  //
  // Private
  //

  /**
   * Helper class that generates unique key trying to create smaller keys first then incrementing key length as more
   * and more collisions occur.
   *
   * @author Alexander Shabanov
   */
  @ParametersAreNonnullByDefault
  final static class DefaultKeyGenerator implements KeyGenerator {
    private static final int DEFAULT_START_KEY_LENGTH = 3;

    private final Store store;
    private final Random random;
    private final IdCodec codec;
    private volatile int startKeyLength;

    DefaultKeyGenerator(Store store, IdCodec codec, Random random) {
      this.store = store;
      this.codec = codec;
      this.random = random;
      this.startKeyLength = DEFAULT_START_KEY_LENGTH;
    }

    @Override
    public String getUniqueKey(Transaction tx) {
      byte[] keyBytes;

      for (int keySize = startKeyLength; ;++keySize) {
        keyBytes = new byte[keySize];
        random.nextBytes(keyBytes);

        if (store.get(tx, new ArrayByteIterable(keyBytes)) == null) {
          break;
        }

        // once at least one collision detected increment start key length to operate on bigger ID space
        if (keySize == this.startKeyLength) {
          // NOTE: even though this code intended for multithreaded environment, we don't have any synchronization
          //       as we don't need to be precise about values controlled by this logic.
          ++this.startKeyLength;
        }
      }

      return codec.encodeBytes(keyBytes);
    }
  }

}
