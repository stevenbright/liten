package liten.catalog.dao;

import liten.catalog.dao.model.IceEntry;

/**
 * @author Alexander Shabanov
 */
public interface CatalogQueryDao {

  IceEntry getEntry(long itemId, String language);
}
