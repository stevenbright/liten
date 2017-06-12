package liten.website.model.catalog;

import com.google.common.base.Strings;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

/**
 * Lightweight representation of a particular catalog item.
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class CatalogItemRef {
  private final String id;
  private final String defaultTitle;
  private final String langCode;

  public CatalogItemRef(String id, String defaultTitle, String langCode) {
    this.id = Objects.requireNonNull(id);
    this.defaultTitle = Objects.requireNonNull(defaultTitle);
    this.langCode = Objects.requireNonNull(langCode);
  }

  public final String getId() {
    return id;
  }

  public final boolean hasId() {
    return !Strings.isNullOrEmpty(id);
  }

  public final String getDefaultTitle() {
    return defaultTitle;
  }

  public final String getLangCode() {
    return langCode;
  }
}
