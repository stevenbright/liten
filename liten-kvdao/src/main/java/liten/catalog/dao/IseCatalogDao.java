package liten.catalog.dao;

import liten.catalog.model.Ise;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface IseCatalogDao {

  Ise.Item getById(String itemId);

  List<String> getNameHints(String prefix);
}
