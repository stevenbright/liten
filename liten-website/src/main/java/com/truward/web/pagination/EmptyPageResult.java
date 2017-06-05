package com.truward.web.pagination;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Empty page result.
 */
@ParametersAreNonnullByDefault
public final class EmptyPageResult<T> implements PageResult<T> {
  private static final EmptyPageResult<?> INSTANCE = new EmptyPageResult<>();

  @SuppressWarnings("unchecked")
  public static <T> EmptyPageResult<T> instance() {
    return (EmptyPageResult<T>) INSTANCE;
  }

  private EmptyPageResult() {}

  @Override
  public Page<T> getPage(String cursor, int limit, PaginationUrlCreator urlCreator) {
    return Page.empty();
  }
}
