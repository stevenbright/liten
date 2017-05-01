package liten.website.model.catalog;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface CatalogItem extends CatalogItemRef {

  default boolean hasFavoriteFlag() {
    return false;
  }

  default boolean isFavorite() {
    return false;
  }

  @Nullable
  String getDetailPageCoverUrl();

  List<CatalogSku> getSkus();

  default boolean isDefaultSkuPresent() {
    return getDefaultSku() != null;
  }

  @Nullable
  default CatalogSku getDefaultSku() {
    for (final CatalogSku sku : getSkus()) {
      if (sku.isDefault()) {
        return sku;
      }
    }

    return null;
  }

  default String getDefaultTitle() {
    return getTitle(() -> "UnknownItem#" + getId());
  }

  @Nullable
  default String getTitle(Supplier<String> defaultTitleSupplier) {
    final CatalogSku sku = getDefaultSku();
    return sku != null ? sku.getTitle() : defaultTitleSupplier.get();
  }
}
