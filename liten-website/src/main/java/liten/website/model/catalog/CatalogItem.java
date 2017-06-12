package liten.website.model.catalog;

import com.google.common.collect.ImmutableList;
import liten.catalog.util.IseNames;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public class CatalogItem {
  private final String userLanguageCode;
  private final String id;
  private final String type;
  private final List<CatalogSku> skus;

  private CatalogItem(String userLanguageCode, String id, String type, List<CatalogSku> skus) {
    this.userLanguageCode = Objects.requireNonNull(userLanguageCode, "userLanguageCode");
    this.id = Objects.requireNonNull(id, "id");
    this.type = Objects.requireNonNull(type, "type");
    this.skus = Objects.requireNonNull(skus, "skus");
  }

  public String getId() {
    return id;
  }

  public final String getType() {
    return type;
  }

  public String getDetailPageCoverUrl() {
    if (!IseNames.BOOK.equals(getType())) {
      return "";
    }

    return "/demo/media/image?type=cover";
  }

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

  public boolean hasFavoriteFlag() {
    return false;
  }

  public boolean isFavorite() {
    return false;
  }

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

  public List<CatalogItemRef> getAuthors() {
    return Collections.emptyList();
  }

  public List<CatalogItemRef> getGenres() {
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

  //
  // Builder
  //

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {
    private String userLanguageCode;
    private String id = "";
    private String type;
    private List<CatalogSku> skus = ImmutableList.of();

    public CatalogItem build() {
      return new CatalogItem(userLanguageCode, id, type, skus);
    }

    public Builder setUserLanguageCode(String userLanguageCode) {
      this.userLanguageCode = userLanguageCode;
      return this;
    }

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    public Builder setType(String type) {
      this.type = type;
      return this;
    }

    public Builder setSkus(List<CatalogSku> skus) {
      this.skus = ImmutableList.copyOf(skus);
      return this;
    }
  }
}
