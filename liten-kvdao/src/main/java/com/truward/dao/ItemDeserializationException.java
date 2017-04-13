package com.truward.dao;

import org.jetbrains.annotations.NonNls;

/**
 * @author Alexander Shabanov
 */
public class ItemDeserializationException extends DaoException {
  public ItemDeserializationException() {
  }

  public ItemDeserializationException(@NonNls String message) {
    super(message);
  }

  public ItemDeserializationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ItemDeserializationException(Throwable cause) {
    super(cause);
  }
}
