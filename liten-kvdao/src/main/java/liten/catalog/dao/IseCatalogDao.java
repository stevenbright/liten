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

  Ise.Item getById(Transaction tx, String itemId);

  String persist(Transaction tx, Ise.Item item);

  List<String> getNameHints(Transaction tx, @Nullable String type, String prefix);
}
