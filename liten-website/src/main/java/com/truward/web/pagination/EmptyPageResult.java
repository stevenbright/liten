package com.truward.web.pagination;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Empty .
 */
@ParametersAreNonnullByDefault
public final class EmptyPageResult implements PageResult {
  public static final EmptyPageResult INSTANCE = new EmptyPageResult();

  private EmptyPageResult() {}

  private static final Map<String, Object> EMPTY_RESULT;

  static {
    final Map<String, Object> r = new HashMap<>();
    r.put(ITEMS, Collections.emptyList());
    r.put(NEXT_URL, "");
    EMPTY_RESULT = Collections.unmodifiableMap(r);
  }

  @Override
  public Map<String, Object> newModelWithItems(String cursor, int limit, PaginationUrlCreator urlCreator) {
    return EMPTY_RESULT;
  }
}
