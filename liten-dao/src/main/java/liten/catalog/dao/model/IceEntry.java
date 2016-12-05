package liten.catalog.dao.model;

import liten.dao.model.BaseModel;
import liten.util.CheckedCollections;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Shabanov
 */
public final class IceEntry extends BaseModel {
  public static final String DEFAULT_DISPLAY_TITLE = "?";

  private final IceItem item;
  private final List<SkuEntry> skus;

  public IceEntry(IceItem item, List<SkuEntry> skuEntries) {
    this.item = requireNonNull(item, "item");
    this.skus = CheckedCollections.copyList(skuEntries, "skuEntries");
  }

  public String getDisplayTitle() {
    String result = item.getAlias();
    if (!skus.isEmpty()) {
      final SkuEntry skuEntry = skus.get(0);
      result = skuEntry.getSku().getTitle();
    }

    return result != null ? result : DEFAULT_DISPLAY_TITLE;
  }

  public IceItem getItem() {
    return item;
  }

  public List<SkuEntry> getSkuEntries() {
    return skus;
  }

  /**
   * Represents SKU entry along with the related instances
   */
  public static final class SkuEntry {
    private final IceSku sku;
    private final List<IceInstance> instances;

    public IceSku getSku() {
      return sku;
    }

    private SkuEntry(IceSku sku, List<IceInstance> instances) {
      this.sku = sku;
      this.instances = CheckedCollections.copyList(instances, "instances");
    }

    public List<IceInstance> getInstances() {
      return instances;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof SkuEntry)) return false;

      SkuEntry skuEntry = (SkuEntry) o;

      return sku.equals(skuEntry.sku) && instances.equals(skuEntry.instances);

    }

    @Override
    public int hashCode() {
      int result = sku.hashCode();
      result = 31 * result + instances.hashCode();
      return result;
    }

    @Override
    public String toString() {
      return "SkuEntry{" +
          "sku=" + sku +
          ", instances=" + instances +
          '}';
    }
  }

  //
  // hashCode / equals
  //

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IceEntry)) return false;

    IceEntry entry = (IceEntry) o;

    return item.equals(entry.item) && skus.equals(entry.skus);

  }

  @Override
  public int hashCode() {
    int result = item.hashCode();
    result = 31 * result + skus.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "IceEntry{" +
        "item=" + item +
        ", skus=" + skus +
        '}';
  }

  //
  // Builder
  //

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {
    private IceItem item;
    private List<SkuEntryBuilder> skuEntryBuilders = new ArrayList<>();

    public IceEntry build() {
      return new IceEntry(item, skuEntryBuilders.stream().map(SkuEntryBuilder::toEntry).collect(Collectors.toList()));
    }

    private Builder() {}

    public Builder setItem(IceItem item) {
      this.item = item;
      return this;
    }

    public Builder addSku(IceSku value) {
      this.skuEntryBuilders.add(new SkuEntryBuilder(value));
      return this;
    }

    public Builder addInstance(long skuId, IceInstance instance) {
      SkuEntryBuilder entryBuilder = null;
      for (final SkuEntryBuilder b : skuEntryBuilders) {
        if (b.sku.getId() == skuId) {
          if (entryBuilder != null) {
            throw new IllegalStateException("Duplicate skuId=" + skuId + " found while constructing sku entry");
          }
          entryBuilder = b;
        }
      }

      if (entryBuilder == null) {
        throw new IllegalArgumentException("Missing entry for skuId=" + skuId);
      }

      entryBuilder.instances.add(instance);
      return this;
    }

    private static final class SkuEntryBuilder {
      private final IceSku sku;
      private List<IceInstance> instances = new ArrayList<>();

      public SkuEntryBuilder(IceSku sku) {
        this.sku = sku;
      }

      private SkuEntry toEntry() {
        return new SkuEntry(sku, instances);
      }
    }
  }
}
