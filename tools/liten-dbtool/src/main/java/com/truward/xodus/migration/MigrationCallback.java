package com.truward.xodus.migration;

public interface MigrationCallback {
  void migrate(MigrationContext context);
}
