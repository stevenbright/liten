package liten.catalog.dao;

import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceItem;

import java.util.List;

/**
 * @author Alexander Shabanov
 */
public interface CatalogUpdaterDao {

  List<Long> addEntries(List<IceEntry> entries);

  void deleteEntry(long id);
}
