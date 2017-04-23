package com.truward.web.pagination;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface to callback that creates next URLs.
 */
@ParametersAreNonnullByDefault
public interface PaginationUrlCreator {

  String createUrl(String cursor, int limit);
}
