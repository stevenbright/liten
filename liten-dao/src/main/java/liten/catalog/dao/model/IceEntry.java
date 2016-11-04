package liten.catalog.dao.model;

import java.util.Collections;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
public final class IceEntry {
  private IceItem item;
  private List<SkuEntry> skus = Collections.emptyList();

  public String getDisplayTitle() {
    if (!skus.isEmpty()) {
      final SkuEntry skuEntry = skus.get(0);
      return skuEntry.getSku().getTitle();
    }

    return item.getDefaultTitle();
  }

  public IceItem getItem() {
    return item;
  }

  public void setItem(IceItem item) {
    this.item = item;
  }

  public List<SkuEntry> getSkuEntries() {
    return skus;
  }

  public void setSkuEntries(List<SkuEntry> skus) {
    this.skus = skus;
  }

  public static final class SkuEntry {
    private IceSku sku;
    private List<IceInstance> instances = Collections.emptyList();

    public IceSku getSku() {
      return sku;
    }

    public void setSku(IceSku sku) {
      this.sku = sku;
    }

    public List<IceInstance> getInstances() {
      return instances;
    }

    public void setInstances(List<IceInstance> instances) {
      this.instances = instances;
    }
  }
}
