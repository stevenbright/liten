package liten.catalog.dao;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface CatalogDao {

  Object getById(String id);

  List<String> getNameHints(String prefix);
}
