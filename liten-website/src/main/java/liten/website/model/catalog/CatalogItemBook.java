package liten.website.model.catalog;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface CatalogItemBook extends CatalogItem {

  List<CatalogItemRef> getLanguages();

  List<CatalogItemRef> getAuthors();

  List<CatalogItemRef> getGenres();

  List<CatalogItemRef> getOrigins();

  @Nullable
  CatalogItemRef getSeries();

  int getSeriesPos();
}
