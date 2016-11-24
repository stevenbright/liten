package liten.website.util;

import liten.dao.model.ModelWithId;

import javax.annotation.Nullable;
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
public abstract class PaginationHelper<T> {
  public static final int DEFAULT_LIMIT = 5;
  public static final int MAX_LIMIT = 100;

  public Map<String, Object> newModelWithItemsOpt(@Nullable Long startItemId, @Nullable Integer limit) {
    return newModelWithItems(startItemId == null ? ModelWithId.INVALID_ID : startItemId,
        limit == null ? DEFAULT_LIMIT : limit);
  }

  public Map<String, Object> newModelWithItems(long startItemId, int limit) {
    if (limit == 0) {
      // edge case - specified limit is empty, so page should be empty and next one unavailable
      final Map<String, Object> params = new HashMap<>();
      params.put("items", Collections.emptyList());
      params.put("nextUrl", ""); // no next URL as it can't be retrieved
      return params;
    }

    if (limit < 0) {
      throw new IllegalArgumentException("limit");
    }

    if (limit > MAX_LIMIT) {
      // silently refuse to serve more than this many item
      limit = MAX_LIMIT;
    }

    final List<T> items = getItemList(startItemId, limit + 1);

    final Map<String, Object> params = new HashMap<>();

    final String nextUrl;
    if (items.size() > limit) {
      // exclude last element which indicates a presence of list continuation
      params.put("items", items.subList(0, limit));

      final long nextItemId = getItemId(items.get(limit - 1));
      // TODO: proper URL construction
      nextUrl = createNextUrl(nextItemId, limit);
    } else {
      nextUrl = "";
      params.put("items", items);
    }

    params.put("nextUrl", nextUrl);

    return params;
  }

  protected abstract List<T> getItemList(long startItemId, int limit);

  protected abstract long getItemId(T item);

  protected abstract String createNextUrl(long startItemId, int limit);
}
