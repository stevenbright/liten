package liten.website.service;

import liten.website.model.IseItemAdapter;
import com.truward.web.pagination.PaginationHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface CatalogService {

  List<String> getSkuNameHints(@Nullable String type, @Nullable String namePrefix);

  IseItemAdapter getDetailedEntry(String id, String userLanguage);

  PaginationHelper getPaginationHelper(
      String userLanguage,
      @Nullable String type,
      @Nullable String namePrefix);

  // e.g. author's books
  PaginationHelper getRightRelationEntries(String id, String userLanguage);
}
