package com.truward.web.pagination;

import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

/**
 * Interface, that represents pagination helper.
 */
@ParametersAreNonnullByDefault
public interface PaginationHelper {

  int DEFAULT_LIMIT = 5;
  int MAX_LIMIT = 100;

  Map<String, Object> newModelWithItems(String cursor, int limit, PaginationUrlCreator urlCreator);

  default Map<String, Object> newModelWithItemsOpt(@Nullable String cursor,
                                                   @Nullable Integer limit,
                                                   PaginationUrlCreator urlCreator) {
    return newModelWithItems(StringUtils.hasLength(cursor) ? cursor : "",
        limit == null ? DEFAULT_LIMIT : limit, urlCreator);
  }
}
