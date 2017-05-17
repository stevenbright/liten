package liten.website.model.catalog;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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

  @Nullable
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

  public String getDefaultTitle() {
    return getTitle(() -> "UnknownItem#" + getId());
  }

  @Nullable
  public String getTitle(Supplier<String> defaultTitleSupplier) {
    final CatalogSku sku = getDefaultSku();
    return sku != null ? sku.getTitle() : defaultTitleSupplier.get();
  }
}
