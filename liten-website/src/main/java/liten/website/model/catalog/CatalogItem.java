package liten.website.model.catalog;

import com.google.common.collect.ImmutableList;
import liten.catalog.util.IseNames;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public class CatalogItem {
  private final CatalogItemRef userLanguage;
  private final String id;
  private final String type;
  private final List<CatalogSku> skus;
  private final List<CatalogItemRef> authors;
  private final List<CatalogItemRef> genres;
  private final List<CatalogItemRef> origins;
  private final CatalogItemRef series;
  private final int seriesPos;

  private CatalogItem(
      CatalogItemRef userLanguage,
      String id,
      String type,
      List<CatalogSku> skus,
      List<CatalogItemRef> authors,
      List<CatalogItemRef> genres,
      List<CatalogItemRef> origins,
      @Nullable CatalogItemRef series,
      int seriesPos) {
    this.userLanguage = requireNonNull(userLanguage, "userLanguage");
    this.id = requireNonNull(id, "id");
    this.type = requireNonNull(type, "type");
    this.skus = requireNonNull(skus, "skus");
    this.authors = requireNonNull(authors, "authors");
    this.genres = requireNonNull(genres, "genres");
    this.origins = requireNonNull(origins, "origins");
    this.series = series;
    this.seriesPos = seriesPos;
  }

  public CatalogItemRef getUserLanguage() {
    return userLanguage;
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
    return ImmutableList.of(userLanguage);
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
    return authors;
  }

  public List<CatalogItemRef> getGenres() {
    return genres;
  }

  public List<CatalogItemRef> getOrigins() {
    return origins;
  }

  @Nullable
  public CatalogItemRef getSeries() {
    return series;
  }

  public int getSeriesPos() {
    return seriesPos;
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
    private CatalogItemRef userLanguage;
    private String id = "";
    private String type;
    private List<CatalogSku> skus = ImmutableList.of();
    private List<CatalogItemRef> authors = ImmutableList.of();
    private List<CatalogItemRef> genres = ImmutableList.of();
    private List<CatalogItemRef> origins = ImmutableList.of();
    private CatalogItemRef series;
    private int seriesPos = -1;

    public CatalogItem build() {
      return new CatalogItem(userLanguage, id, type, skus, authors, genres, origins, series, seriesPos);
    }

    public Builder setUserLanguage(CatalogItemRef userLanguage) {
      this.userLanguage = userLanguage;
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

    public Builder setAuthors(List<CatalogItemRef> authors) {
      this.authors = ImmutableList.copyOf(authors);
      return this;
    }

    public Builder setGenres(List<CatalogItemRef> genres) {
      this.genres = ImmutableList.copyOf(genres);
      return this;
    }

    public Builder setOrigins(List<CatalogItemRef> origins) {
      this.origins = ImmutableList.copyOf(origins);
      return this;
    }

    public Builder setSeries(CatalogItemRef series) {
      this.series = series;
      return this;
    }

    public Builder setSeriesPos(int seriesPos) {
      this.seriesPos = seriesPos;
      return this;
    }
  }
}
