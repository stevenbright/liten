package com.truward.dao.exception;

import org.jetbrains.annotations.NonNls;

/**
 * An exception thrown when cursor is no longer valid or stale.
 *
 * @author Alexander Shabanov
 */
public class InvalidCursorException extends DaoException {

  public InvalidCursorException(@NonNls String cursor) {
    super("Cursor=" + cursor + " is stale or not valid");
  }
}
