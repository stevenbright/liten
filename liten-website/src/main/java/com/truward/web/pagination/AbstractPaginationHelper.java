package com.truward.web.pagination;

import org.springframework.util.StringUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for building models for paginated UI.
 * The newly produced model WILL include the following parameters:
 * <ul>
 *   <li><code>items</code> a list of items that constitute the requested data page</li>
 *   <li><code>nextUrl</code> a URL, that points to the next page; empty for last pages</li>
 * </ul>
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public abstract class AbstractPaginationHelper<TItem, TQueryResult> implements PaginationHelper {

  @Override
  public final Map<String, Object> newModelWithItems(String cursor, int limit, PaginationUrlCreator urlCreator) {
    if (limit == 0) {
      // edge case - specified limit is empty, so page should be empty and next one unavailable
      final Map<String, Object> params = new HashMap<>();
      params.put(ITEMS, Collections.emptyList());
      params.put(NEXT_URL, ""); // no next URL as it can't be retrieved
      return params;
    }

    if (limit < 0) {
      throw new IllegalArgumentException("limit");
    }

    if (limit > MAX_LIMIT) {
      // silently refuse to serve more than this many items
      limit = MAX_LIMIT;
    }

    final TQueryResult queryResult = query(cursor, limit);
    final List<TItem> items = getItemList(queryResult);
    final String nextCursor = getCursor(queryResult);

    final Map<String, Object> params = new HashMap<>(4);

    params.put(ITEMS, items);

    final String nextUrl = StringUtils.hasLength(nextCursor) ? urlCreator.createUrl(nextCursor, limit) : "";
    params.put(NEXT_URL, nextUrl);

    return params;
  }

  protected abstract String getCursor(TQueryResult queryResult);

  protected abstract List<TItem> getItemList(TQueryResult queryResult);

  protected abstract TQueryResult query(String cursor, int limit);
}
