package liten.catalog.dao;

import jetbrains.exodus.env.Environment;
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

  int DEFAULT_LIMIT = 10;
  int MAX_LIMIT = 30;

  Environment getEnvironment();

  Ise.Item getById(Transaction tx, String itemId);

  @Nullable
  Ise.Item getByExternalId(Transaction tx, Ise.ExternalId externalId);

  List<String> getNameHints(Transaction tx, @Nullable String type, String prefix);

  Ise.ItemQueryResult getItems(Transaction tx, Ise.ItemQuery query);

  Ise.ItemRelationQueryResult getRelations(Transaction tx, Ise.ItemRelationQuery query);

  //
  // Update methods
  //

  String persist(Transaction tx, Ise.Item item);
}
