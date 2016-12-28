package liten.catalog.dao;

import liten.catalog.dao.model.*;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface CatalogQueryDao {

  long getNextItemId();

  IceItem getItem(long itemId);

  IceEntry getEntry(long itemId);

  List<IceEntry> getEntries(IceEntryQuery query);

  List<IceRelation> getRelations(IceRelationQuery query);
}
