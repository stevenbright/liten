package liten.website.service.catalog;

import com.truward.web.pagination.PaginationHelper;
import liten.website.model.catalog.CatalogItem;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
public interface CatalogService2 {

  List<String> getSkuNameHints(@Nullable String type, @Nullable String namePrefix);

  CatalogItem getDetailedEntry(String id, String userLanguage);

  PaginationHelper getPaginationHelper(
      String userLanguage,
      @Nullable String type,
      @Nullable String namePrefix);

  // e.g. author's books
  PaginationHelper getRightRelationEntries(String id, String userLanguage);
}
