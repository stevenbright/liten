package liten.website.model.catalog;

import liten.catalog.model.Ise;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
public class CatalogSku {
  private final Ise.Sku sku;
  private final CatalogItemRef language;
  private final List<CatalogEntry> entries;

  public CatalogSku(
      Ise.Sku sku,
      CatalogItemRef language,
      List<CatalogEntry> entries) {
    this.sku = sku;
    this.language = language;
    this.entries = entries;
  }

  public String getId() {
    return sku.getId();
  }

  public String getLanguageCode() {
    return sku.getLanguage();
  }

  public CatalogItemRef getLanguage() {
    return language;
  }

  public String getLanguageName() {
    return getLanguage().getDefaultTitle();
  }

  public String getTitle() {
    return sku.getTitle();
  }

  public List<CatalogEntry> getEntries() {
    return entries;
  }
}
