package liten.tool.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Cursor;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class XodusTextDatabaseExport {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final Environment environment;
  private final Map<String, Message> storeMappings;

  public  XodusTextDatabaseExport(
      Environment environment,
      Map<String, Message> storeMappings) {
    this.environment = requireNonNull(environment, "environment");
    this.storeMappings = ImmutableMap.copyOf(requireNonNull(storeMappings, "storeMappings"));
  }

  public void export() throws IOException {
    final Set<String> targetStores = getIntersectStores(
        ImmutableSet.copyOf(this.environment.computeInReadonlyTransaction(this.environment::getAllStoreNames)),
        storeMappings.keySet());

  }

  protected void exportStore(Store store, Appendable output) throws IOException {
    this.environment.executeInExclusiveTransaction(tx -> {
      try (Cursor cursor = store.openCursor(tx)) {
        while (cursor.getNext()) {
          try {
            output.append("---\n");
            output.append(encodeKey(cursor.getKey()));
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }

          cursor.getKey();
        }
      }
    });
  }

  //
  // Private
  //

  private String encodeKey(ByteIterable byteIterableKey) {
    return Base64.getEncoder().encodeToString(
        Arrays.copyOf(byteIterableKey.getBytesUnsafe(), byteIterableKey.getLength()));
  }

  private Set<String> getIntersectStores(Set<String> environmentStores, Set<String> requestedStores) {
    final Set<String> result = Sets.intersection(environmentStores, requestedStores);
    if (log.isInfoEnabled()) {
      log.info(
          "Intersect stores={}, unaccounted stores={}, disregarded stores={}",
          result,
          Sets.difference(environmentStores, result),
          Sets.difference(requestedStores, result));
    }
    return result;
  }
}
