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

  List<Long> addEntries(List<IceEntry> entries);

  void setRelation(long leftItemId, long rightItemId, String type);

  default Long addEntry(IceEntry entry) {
    return addEntries(Collections.singletonList(entry)).get(0);
  }

  void deleteEntry(long id);
}
