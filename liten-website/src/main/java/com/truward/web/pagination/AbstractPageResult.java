package com.truward.web.pagination;

import org.springframework.util.StringUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

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
public abstract class AbstractPageResult<TItem, TQueryResult> implements PageResult<TItem> {

  @Override
  public final Page<TItem> getPage(String cursor, int limit, PaginationUrlCreator urlCreator) {
    if (limit == 0) {
      // edge case - specified limit is empty, so page should be empty and next one unavailable
      return Page.empty();
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
    final String nextUrl = StringUtils.hasLength(nextCursor) ? urlCreator.createUrl(nextCursor, limit) : "";
    return new Page<>(nextUrl, items);
  }

  protected abstract String getCursor(TQueryResult queryResult);

  protected abstract List<TItem> getItemList(TQueryResult queryResult);

  protected abstract TQueryResult query(String cursor, int limit);
}
