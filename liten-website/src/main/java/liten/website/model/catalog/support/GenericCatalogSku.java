package liten.website.model.catalog.support;

import liten.catalog.model.Ise;
import liten.website.model.catalog.CatalogEntry;
import liten.website.model.catalog.CatalogSku;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
public class GenericCatalogSku implements CatalogSku {
  private final Ise.Sku sku;
  private final boolean isDefault;
  private final String languageName;
  private final List<CatalogEntry> entries;

  public GenericCatalogSku(
      Ise.Sku sku,
      boolean isDefault,
      @Nullable String languageName,
      List<CatalogEntry> entries) {
    this.sku = sku;
    this.isDefault = isDefault;
    this.languageName = languageName;
    this.entries = entries;
  }

  @Override
  public String getId() {
    return sku.getId();
  }

  @Override
  public String getLanguageName() {
    return languageName != null ? languageName : sku.getLanguage();
  }

  @Override
  public String getTitle() {
    return sku.getTitle();
  }

  @Override
  public boolean isDefault() {
    return isDefault;
  }

  @Override
  public List<CatalogEntry> getEntries() {
    return entries;
  }
}
