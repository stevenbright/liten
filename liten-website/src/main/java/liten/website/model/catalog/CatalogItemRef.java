package liten.website.model.catalog;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface CatalogItemRef {

  String getId();

  String getDefaultTitle();
}
