package liten.website.model.catalog;

import com.google.common.collect.ImmutableList;
import liten.catalog.util.IseNames;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Shabanov
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@ParametersAreNonnullByDefault
public class CatalogItem {
  private final String id;
  private final String type;
  private final List<CatalogSku> skus;
  private final List<CatalogItemRef> authors;
  private final List<CatalogItemRef> genres;
  private final CatalogItemRef series;
  private final int seriesPos;

  private CatalogItem(
      String id,
      String type,
      List<CatalogSku> skus,
      List<CatalogItemRef> authors,
      List<CatalogItemRef> genres,
      @Nullable CatalogItemRef series,
      int seriesPos) {
    this.id = requireNonNull(id, "id");
    this.type = requireNonNull(type, "type");
    this.skus = requireNonNull(skus, "skus");
    this.authors = requireNonNull(authors, "authors");
    this.genres = requireNonNull(genres, "genres");
    this.series = series;
    this.seriesPos = seriesPos;
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

  public List<CatalogItemRef> getAuthors() {
    return authors;
  }

  public List<CatalogItemRef> getGenres() {
    return genres;
  }

  @Nullable
  public CatalogItemRef getSeries() {
    return series;
  }

  public int getSeriesPos() {
    return seriesPos;
  }

  public String getDefaultTitle() {
    if (isDefaultSkuPresent()) {
      return getDefaultSku().getTitle();
    }

    // TODO: use localization
    return "UnnamedItem#" + getId();
  }

  public boolean isDefaultSkuPresent() {
    return !getSkus().isEmpty();
  }

  public CatalogSku getDefaultSku() {
    if (!isDefaultSkuPresent()) {
      throw new UnsupportedOperationException("Default SKU is not present on the item " + getId());
    }

    return getSkus().get(0);
  }

  public List<CatalogSku> getNonDefaultSkus() {
    final int skuCount = getSkus().size();
    if (skuCount > 0) {
      return getSkus().subList(1, skuCount);
    }

    return ImmutableList.of();
  }

  //
  // Builder
  //

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {
    private String id = "";
    private String type;
    private List<CatalogSku> skus = ImmutableList.of();
    private List<CatalogItemRef> authors = ImmutableList.of();
    private List<CatalogItemRef> genres = ImmutableList.of();
    private CatalogItemRef series;
    private int seriesPos = -1;

    public CatalogItem build() {
      return new CatalogItem(id, type, skus, authors, genres, series, seriesPos);
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
