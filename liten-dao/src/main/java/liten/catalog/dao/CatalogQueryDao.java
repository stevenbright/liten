package liten.catalog.dao;

import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceItem;
import liten.catalog.dao.model.IceRelation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface CatalogQueryDao {

  IceItem getItem(long itemId);

  IceEntry getEntry(long itemId, String language);

  List<IceEntry> getEntries(long startItemId, int limit);

  List<IceRelation> getLeftRelations(long rightItemId, @Nullable String type, long startItemId, int limit);

  List<IceRelation> getRightRelations(long rightItemId, @Nullable String type, long startItemId, int limit);
}
