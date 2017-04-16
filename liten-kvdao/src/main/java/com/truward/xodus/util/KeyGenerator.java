package com.truward.xodus.util;

import jetbrains.exodus.env.Transaction;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Generates unique key in a form of semantic ID in the related transaction.
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface KeyGenerator {

  /**
   * Generates new key within the associated store
   *
   * @param tx Transaction, to operate within
   * @return Newly generated key represented as semantic ID
   */
  String getUniqueKey(Transaction tx);
}
