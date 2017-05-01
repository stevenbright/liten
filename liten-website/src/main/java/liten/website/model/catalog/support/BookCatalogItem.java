package liten.website.model.catalog.support;

import liten.catalog.model.Ise;
import liten.website.model.catalog.CatalogItemBook;
import liten.website.model.catalog.CatalogItemRef;
import liten.website.model.catalog.CatalogSku;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
public final class BookCatalogItem extends GenericCatalogItem implements CatalogItemBook {
  public BookCatalogItem(Ise.Item item, List<CatalogSku> skus) {
    super(item, skus);
  }

  @Override
  public List<CatalogItemRef> getLanguages() {
    return Collections.emptyList();
  }

  @Override
  public List<CatalogItemRef> getAuthors() {
    return Collections.emptyList();
  }

  @Override
  public List<CatalogItemRef> getGenres() {
    return Collections.emptyList();
  }

  @Override
  public List<CatalogItemRef> getOrigins() {
    return Collections.emptyList();
  }

  @Nullable
  @Override
  public CatalogItemRef getSeries() {
    return null;
  }

  @Override
  public int getSeriesPos() {
    return 0;
  }
}
