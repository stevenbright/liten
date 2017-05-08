package liten.website.model.catalog.support;

import liten.catalog.model.Ise;
import liten.catalog.util.IseNames;
import liten.website.model.catalog.CatalogItem;
import liten.website.model.catalog.CatalogSku;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class GenericCatalogItem implements CatalogItem {
  private final Ise.Item item;
  private final List<CatalogSku> skus;

  public GenericCatalogItem(Ise.Item item, List<CatalogSku> skus) {
    this.item = item;
    this.skus = skus;
  }

  @Override
  public String getId() {
    return item.getId();
  }

  @Nullable
  @Override
  public String getDetailPageCoverUrl() {
    if (IseNames.BOOK.equals(item.getType())) {
      return null;
    }

    return "/demo/media/image?type=cover";
  }

  @Override
  public List<CatalogSku> getSkus() {
    return skus;
  }
}
