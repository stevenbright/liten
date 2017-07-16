package liten.catalog.util;

import liten.catalog.model.Ise;

/**
 * Utility class that defines names for ISE entities.
 */
public final class IseNames {
  private IseNames() {} // hidden

  // Type or relation name:
  public static final String PERSON = "person";
  public static final String AUTHOR = "author";
  public static final String GENRE = "genre";
  public static final String SERIES = "series";
  public static final String BOOK = "book";

  //
  // Standard naming for external ID prefixes
  //

  /**
   * Type of external ID that are aliases to entities.
   */
  public static final String ALIAS = "alias";

  public static Ise.ExternalId newAlias(String aliasId) {
    return Ise.ExternalId.newBuilder()
        .setIdType(ALIAS)
        .setIdValue(aliasId)
        .build();
  }
}
