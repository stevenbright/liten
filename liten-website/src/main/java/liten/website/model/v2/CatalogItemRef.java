package liten.website.model.v2;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface CatalogItemRef {

  String getId();

  String getDefaultTitle();
}
