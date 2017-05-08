package liten.website.service.catalog.support;

import com.truward.web.pagination.PaginationHelper;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.model.Ise;
import liten.website.model.catalog.CatalogEntry;
import liten.website.model.catalog.CatalogItem;
import liten.website.model.catalog.CatalogSku;
import liten.website.model.catalog.support.GenericCatalogItem;
import liten.website.model.catalog.support.GenericCatalogSku;
import liten.website.service.catalog.CatalogService2;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link CatalogService2}.
 */
@ParametersAreNonnullByDefault
public final class DefaultCatalogService2 implements CatalogService2 {
  private final IseCatalogDao catalogDao;

  public DefaultCatalogService2(IseCatalogDao catalogDao) {
    this.catalogDao = Objects.requireNonNull(catalogDao, "catalogDao");
  }

  @Override
  public List<String> getSkuNameHints(@Nullable String type, @Nullable String namePrefix) {
    return null;
  }

  @Override
  public CatalogItem getDetailedEntry(String id, String userLanguage) {
    return catalogDao.getEnvironment().computeInTransaction(tx -> {
      final Ise.Item item = catalogDao.getById(tx, id);
      return getCatalogItem(item, userLanguage);
    });
  }

  @Override
  public PaginationHelper getPaginationHelper(String userLanguage, @Nullable String type, @Nullable String namePrefix) {
    return null;
  }

  @Override
  public PaginationHelper getRightRelationEntries(String id, String userLanguage) {
    return null;
  }

  //
  // Private
  //

  private CatalogItem getCatalogItem(Ise.Item item, String userLanguage) {
    final List<CatalogSku> catalogSkus = new ArrayList<>(item.getSkusCount());
    final int defaultSkuIndex = getDefaultSkuIndex(item, userLanguage);

    for (int i = 0; i < item.getSkusCount(); ++i) {
      final boolean isDefault = (i == defaultSkuIndex);
      final Ise.Sku sku = item.getSkus(i);
      final CatalogItem languageItem = null;
      final List<CatalogEntry> entries = Collections.emptyList();

      catalogSkus.add(new GenericCatalogSku(sku, isDefault, languageItem, entries));
    }

    return new GenericCatalogItem(item, catalogSkus);
  }

  private static int getDefaultSkuIndex(Ise.Item item, String userLanguage) {
    int result = -1;

    for (int i = 0; i < item.getSkusCount(); ++i) {
      final Ise.Sku sku = item.getSkus(i);

      if (i == 0 || sku.getLanguage().equals(userLanguage)) {
        result = i;
      }
    }

    return result;
  }
}
