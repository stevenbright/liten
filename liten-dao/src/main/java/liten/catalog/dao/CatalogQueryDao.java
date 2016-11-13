package liten.catalog.dao;

import liten.catalog.dao.model.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface CatalogQueryDao {

  long getNextItemId();

  IceItem getItem(long itemId);

  IceEntry getEntry(long itemId, IceEntryFilter filter);

  List<IceEntry> getEntries(IceEntryFilter filter, long startItemId, int limit);

  List<IceRelation> getRelations(IceRelationQuery query);

  List<IceRelation> getLeftRelations(long relatedItemId, String type, long startItemId, int limit);

  List<IceRelation> getRightRelations(long relatedItemId, String type, long startItemId, int limit);
}
