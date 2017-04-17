package liten.catalog.dao;

import jetbrains.exodus.env.Transaction;
import liten.catalog.model.Ise;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface IseCatalogDao {
  String AUTHOR_RELATION_NAME = "author";
  String GENRE_RELATION_NAME = "genre";
  String SERIES_RELATION_NAME = "series";

  int DEFAULT_LIMIT = 10;
  int MAX_LIMIT = 30;

  Ise.Item getById(Transaction tx, String itemId);

  @Nullable
  Ise.Item getByExternalId(Transaction tx, Ise.ExternalId externalId);

  List<String> getNameHints(Transaction tx, @Nullable String type, String prefix);

  Ise.ItemQueryResult getItems(Transaction tx, Ise.ItemQuery query);

  //
  // Update methods
  //

  String persist(Transaction tx, Ise.Item item);
}
