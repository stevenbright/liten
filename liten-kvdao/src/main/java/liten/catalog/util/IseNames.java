package liten.catalog.util;

import com.google.common.collect.ImmutableSet;
import liten.catalog.model.Ise;

import java.util.Set;

/**
 * Utility class that defines names for ISE entities.
 */
public final class IseNames {
  private IseNames() {} // hidden

  // Type or relation name:
  public static final String PERSON = "person";
  public static final String GENRE = "genre";
  public static final String SERIES = "series";
  public static final String BOOK = "book";
  public static final String ORIGIN = "origin";
  public static final String LANGUAGE = "language";

  public static final String AUTHOR = "author";

  public static final Set<String> ALLOWED_ITEM_TYPES = ImmutableSet.of(
      PERSON,
      GENRE,
      SERIES,
      BOOK,
      ORIGIN,
      LANGUAGE
  );

  public static final Set<String> ALLOWED_RELATION_TYPES = ImmutableSet.of(
      AUTHOR,
      GENRE,
      SERIES
  );

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
