package liten.website.service;

import liten.website.model.deprecated.IseItemAdapter;
import com.truward.web.pagination.PageResult;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@Deprecated
@ParametersAreNonnullByDefault
public interface CatalogService {

  List<String> getSkuNameHints(@Nullable String type, @Nullable String namePrefix);

  IseItemAdapter getDetailedEntry(String id, String userLanguage);

  PageResult getPaginationHelper(
      String userLanguage,
      @Nullable String type,
      @Nullable String namePrefix);

  // e.g. author's books
  PageResult getRightRelationEntries(String id, String userLanguage);
}
