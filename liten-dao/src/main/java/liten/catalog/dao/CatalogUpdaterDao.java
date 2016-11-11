package liten.catalog.dao;

import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceRelation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface CatalogUpdaterDao {

  void addEntries(List<IceEntry> entries);

  void setRelation(long leftItemId, long rightItemId, String type);

  default void addEntry(IceEntry entry) {
    addEntries(Collections.singletonList(entry));
  }

  void deleteEntry(long id);
}
