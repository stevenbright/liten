package com.truward.dao.exception;

import org.jetbrains.annotations.NonNls;

/**
 * Base class for data access object exceptions
 *
 * @author Alexander Shabanov
 */
public abstract class DaoException extends RuntimeException {

  public DaoException() {
  }

  public DaoException(@NonNls String message) {
    super(message);
  }

  public DaoException(String message, Throwable cause) {
    super(message, cause);
  }

  public DaoException(Throwable cause) {
    super(cause);
  }
}
