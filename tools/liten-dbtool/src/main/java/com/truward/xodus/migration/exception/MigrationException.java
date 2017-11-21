package com.truward.xodus.migration.exception;

public final class MigrationException extends RuntimeException {

  public MigrationException(String message) {
    super(message);
  }

  public MigrationException(Throwable cause) {
    super(cause);
  }
}
