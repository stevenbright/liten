package com.truward.dao.exception;

import org.jetbrains.annotations.NonNls;

/**
 * @author Alexander Shabanov
 */
public class ItemNotFoundException extends DaoException {
  public ItemNotFoundException(@NonNls String message) {
    super(message);
  }
}
