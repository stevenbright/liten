package liten.website.model.v2;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface CatalogItem extends CatalogItemRef {

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
}
