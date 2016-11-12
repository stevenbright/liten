package liten.catalog.dao.model;

import liten.dao.model.BaseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.Collections.unmodifiableList;

/**
 * @author Alexander Shabanov
 */
public final class IceEntry extends BaseModel {
  private final IceItem item;
  private final List<SkuEntry> skus;

  public IceEntry(IceItem item, List<SkuEntry> skuEntries) {
    this.item = requireNonNull(item, "item");
    this.skus = unmodifiableList(new ArrayList<>(requireNonNull(skuEntries, "skuEntries")));
  }

  public String getDisplayTitle() {
    if (!skus.isEmpty()) {
      final SkuEntry skuEntry = skus.get(0);
      return skuEntry.getSku().getTitle();
    }

    return item.getDefaultTitle();
  }

  public boolean isDefaultInstancePresent() {
    return !(skus.isEmpty() || skus.get(0).getInstances().isEmpty());
  }

  public IceInstance getDefaultInstance() {
    if (!isDefaultInstancePresent()) {
      throw new IllegalStateException("Default instance is not present");
    }

    return skus.get(0).getInstances().get(0);
  }

  public IceItem getItem() {
    return item;
  }

  public List<SkuEntry> getSkuEntries() {
    return skus;
  }

  public static final class SkuEntry {
    private final IceSku sku;
    private final List<IceInstance> instances;

    public IceSku getSku() {
      return sku;
    }

    private SkuEntry(IceSku sku, List<IceInstance> instances) {
      this.sku = sku;
      this.instances = unmodifiableList(new ArrayList<>(requireNonNull(instances, "instances")));
    }

    public List<IceInstance> getInstances() {
      return instances;
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {
    private IceItem item;
    private List<SkuEntryBuilder> skuEntryBuilders = new ArrayList<>();

    public IceEntry build() {
      return new IceEntry(item,
          skuEntryBuilders.stream().map(SkuEntryBuilder::toEntry).collect(Collectors.toList()));
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
