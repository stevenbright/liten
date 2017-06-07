package liten.website.model.catalog.support;

import com.google.common.collect.ImmutableList;
import liten.catalog.model.Ise;
import liten.catalog.util.IseNames;
import liten.website.model.catalog.CatalogItem;
import liten.website.model.catalog.CatalogItemRef;
import liten.website.model.catalog.CatalogSku;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
public class GenericCatalogItem extends CatalogItem {
  protected final String userLanguageCode;
  protected final Ise.Item item;
  protected final List<CatalogSku> skus;

  public GenericCatalogItem(String userLanguageCode, Ise.Item item, List<CatalogSku> skus) {
    this.item = item;
    this.skus = skus;
    this.userLanguageCode = userLanguageCode;
  }

  @Override
  public String getId() {
    return item.getId();
  }

  @Override
  public String getType() {
    return item.getType();
  }

  @Override
  public String getDetailPageCoverUrl() {
    if (!IseNames.BOOK.equals(item.getType())) {
      return "";
    }

    return "/demo/media/image?type=cover";
  }

  @Override
  public List<CatalogSku> getSkus() {
    return skus;
  }

  public List<CatalogItemRef> getLanguages() {
    return ImmutableList.of();
//    final CatalogSku sku = getDefaultSku();
//    if (sku == null) {
//      return ImmutableList.of();
//    }
//
//    return ;
  }
}
