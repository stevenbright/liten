package liten.website.model.catalog;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public abstract class CatalogItem implements CatalogItemRef {

  public boolean hasFavoriteFlag() {
    return false;
  }

  public boolean isFavorite() {
    return false;
  }

  public abstract String getDetailPageCoverUrl();

  public abstract List<CatalogSku> getSkus();

  public boolean isDefaultSkuPresent() {
    return getDefaultSku() != null;
  }

  @Nullable
  public CatalogSku getDefaultSku() {
    for (final CatalogSku sku : getSkus()) {
      if (sku.isDefault()) {
        return sku;
      }
    }

    return null;
  }

  public abstract String getType();

  public List<CatalogItemRef> getAuthors() {
    return Collections.emptyList();
  }

  public List<CatalogItemRef> getGenres() {
    return Collections.emptyList();
  }

  public List<CatalogItemRef> getLanguages() {
    return Collections.emptyList();
  }

  public List<CatalogItemRef> getOrigins() {
    return Collections.emptyList();
  }

  @Nullable
  public CatalogItemRef getSeries() {
    return null;
  }

  public int getSeriesPos() {
    return 0;
  }

  public String getDefaultTitle() {
    // TODO: localize default title
    return getTitle(() -> "UnknownItem#" + getId());
  }

  public int getFileSize() {
    return 0;
  }

  public String getCreatedDate() {
    return "";
  }

  public String getDownloadUrl() {
    return "";
  }

  @Nullable
  public String getTitle(Supplier<String> defaultTitleSupplier) {
    final CatalogSku sku = getDefaultSku();
    return sku != null ? sku.getTitle() : defaultTitleSupplier.get();
  }
}
