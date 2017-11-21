package com.truward.xodus.migration;

import jetbrains.exodus.env.Cursor;
import jetbrains.exodus.env.Store;

/**
 * Utility class that provides helpers for exodus store migration.
 */
public final class MigrationUtil {
  private MigrationUtil() {}

  public static int copyStores(Store source, Store target) {
    return source.getEnvironment().computeInTransaction(sourceTx ->
        target.getEnvironment().computeInTransaction(targetTx -> {
          final Cursor sourceCursor = source.openCursor(sourceTx);
          int transferred = 0;

          while (sourceCursor.getNext()) {
            target.add(targetTx, sourceCursor.getKey(), sourceCursor.getValue());
            ++transferred;
          }

          return transferred;
        }));
  }
}
