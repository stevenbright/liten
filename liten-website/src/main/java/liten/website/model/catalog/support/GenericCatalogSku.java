package liten.website.model.catalog.support;

import liten.catalog.model.Ise;
import liten.website.model.catalog.CatalogEntry;
import liten.website.model.catalog.CatalogItem;
import liten.website.model.catalog.CatalogSku;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
public class GenericCatalogSku implements CatalogSku {
  private final Ise.Sku sku;
  private final boolean isDefault;
  private final CatalogItem language;
  private final List<CatalogEntry> entries;

  public GenericCatalogSku(
      Ise.Sku sku,
      boolean isDefault,
      @Nullable CatalogItem language,
      List<CatalogEntry> entries) {
    this.sku = sku;
    this.isDefault = isDefault;
    this.language = language;
    this.entries = entries;
  }

  @Override
  public String getId() {
    return sku.getId();
  }

  @Override
  public String getLanguageName() {
    return language != null ? language.getTitle(sku::getLanguage) : sku.getLanguage();
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
