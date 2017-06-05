package liten.website.service.catalog;

import com.truward.web.pagination.PageResult;
import liten.website.model.catalog.CatalogItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface CatalogService2 {

  List<String> getSkuNameHints(String type, String namePrefix);

  CatalogItem getDetailedEntry(String id, String userLanguage);

  PageResult<CatalogItem> getItems(
      String userLanguage,
      String type,
      String namePrefix);

  // e.g. author's books
  PageResult<CatalogItem> getRightRelationEntries(String id, String userLanguage);
}
