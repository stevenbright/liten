package liten.website.model.catalog;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
public interface CatalogSku {

  String getId();

  String getLanguageName();

  String getTitle();

  boolean isDefault();

  List<CatalogEntry> getEntries();

  @Nullable
  default CatalogEntry getDefaultEntry() {
    for (final CatalogEntry entry : getEntries()) {
      if (entry.isDefault()) {
        return entry;
      }
    }

    return null;
  }
}
