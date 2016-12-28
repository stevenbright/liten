package liten.website.service;

import liten.website.model.IceEntryAdapter;
import liten.website.model.PaginationHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface CatalogService {

  IceEntryAdapter getEntry(long id, String userLanguage);

  PaginationHelper<IceEntryAdapter> getPaginationHelper(String userLanguage,
                                                        @Nullable String type);
}
