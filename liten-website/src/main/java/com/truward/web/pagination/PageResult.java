package com.truward.web.pagination;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * Interface, that represents pagination helper.
 */
@ParametersAreNonnullByDefault
public interface PageResult<T> {

  /**
   * Name, which is used to identify item list in the returned pagination map.
   */
  String ITEMS = "items";

  /**
   * Name, which is used to identify next URL in the returned pagination map.
   */
  String NEXT_URL = "nextUrl";

  /**
   * Default cursor, with meaning that this value is missing.
   */
  String DEFAULT_CURSOR = "";

  /**
   * Default limit values, as integer and as string (the latter is to use in Spring MVC defaultValue annotation param).
   */
  int DEFAULT_LIMIT = 5;
  String DEFAULT_LIMIT_STR = "" + DEFAULT_LIMIT;

  int MAX_LIMIT = 100;

  Page<T> getPage(String cursor, int limit, PaginationUrlCreator urlCreator);

  default Page<T> getPageWithDefaults(
      @Nullable String cursor,
      @Nullable Integer limit,
      PaginationUrlCreator urlCreator) {
    return getPage(cursor != null && cursor.length() > 0 ? cursor : "",
        limit == null ? DEFAULT_LIMIT : limit, urlCreator);
  }

  static <T> PageResult<T> empty() {
    return EmptyPageResult.instance();
  }

  /** Represents a paginated result */
  final class Page<T> {
    static final Page<?> EMPTY = new Page<>("", ImmutableList.of());

    @SuppressWarnings("unchecked")
    public static <T> Page<T> empty() {
      return (Page<T>) EMPTY;
    }

    private final String nextUrl;
    private final List<T> items;

    public Page(String nextUrl, List<T> items) {
      this.nextUrl = Objects.requireNonNull(nextUrl);
      this.items = ImmutableList.copyOf(items);
    }

    public String getNextUrl() {
      return nextUrl;
    }

    public List<T> getItems() {
      return items;
    }

    public Map<String, Object> toModelMap() {
      final Map<String, Object> result = new HashMap<>(4);
      result.put(ITEMS, getItems());
      result.put(NEXT_URL, getNextUrl());
      return result;
    }
  }
}
