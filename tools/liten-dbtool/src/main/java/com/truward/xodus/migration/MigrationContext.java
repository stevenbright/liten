package com.truward.xodus.migration;

import jetbrains.exodus.env.Environment;

public final class MigrationContext {
  private final Environment sourceEnvironment;
  private final String fromVersion;

  private final Environment targetEnvironment;
  private final String toVersion;

  public MigrationContext(
      Environment sourceEnvironment,
      String fromVersion,
      Environment targetEnvironment,
      String toVersion) {
    this.sourceEnvironment = sourceEnvironment;
    this.fromVersion = fromVersion;
    this.targetEnvironment = targetEnvironment;
    this.toVersion = toVersion;
  }

  public Environment getSourceEnvironment() {
    return sourceEnvironment;
  }

  public String getFromVersion() {
    return fromVersion;
  }

  public Environment getTargetEnvironment() {
    return targetEnvironment;
  }

  public String getToVersion() {
    return toVersion;
  }
}
