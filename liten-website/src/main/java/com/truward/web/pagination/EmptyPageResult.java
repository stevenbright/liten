package com.truward.web.pagination;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Empty .
 */
@ParametersAreNonnullByDefault
public final class EmptyPageResult<T> implements PageResult<T> {
  public static <T> EmptyPageResult<T> instance() {
    // TODO: singleton
    return new EmptyPageResult<>();
  }

  private EmptyPageResult() {}

  @Override
  public Page<T> getPage(String cursor, int limit, PaginationUrlCreator urlCreator) {
    return Page.empty();
  }
}
