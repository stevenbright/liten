package liten.website.util;

import org.springframework.util.StringUtils;

import javax.annotation.Nullable;

/**
 * Utility class for working with HTTP request parameters.
 *
 * @author Alexander Shabanov
 */
public final class RequestParams {
  private RequestParams() {}

  @Nullable
  public static String getEmptyAsNull(@Nullable String value) {
    return StringUtils.hasLength(value) ? value : null;
  }
}
