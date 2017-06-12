package liten.website.model.catalog;

import liten.catalog.model.Ise;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
public class CatalogSku {
  private final Ise.Sku sku;
  private final boolean isDefault;
  private final String languageName;
  private final List<CatalogEntry> entries;

  public CatalogSku(
      Ise.Sku sku,
      boolean isDefault,
      @Nullable String languageName,
      List<CatalogEntry> entries) {
    this.sku = sku;
    this.isDefault = isDefault;
    this.languageName = languageName;
    this.entries = entries;
  }

  public String getId() {
    return sku.getId();
  }

  public String getLanguageName() {
    return languageName != null ? languageName : sku.getLanguage();
  }

  public String getTitle() {
    return sku.getTitle();
  }

  public boolean isDefault() {
    return isDefault;
  }

  public List<CatalogEntry> getEntries() {
    return entries;
  }
}
