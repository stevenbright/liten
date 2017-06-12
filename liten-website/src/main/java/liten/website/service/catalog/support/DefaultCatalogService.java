package liten.website.service.catalog.support;

import com.truward.web.pagination.AbstractPageResult;
import com.truward.web.pagination.PageResult;
import jetbrains.exodus.env.Transaction;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.model.Ise;
import liten.catalog.util.IseNames;
import liten.website.model.catalog.CatalogEntry;
import liten.website.model.catalog.CatalogItem;
import liten.website.model.catalog.CatalogSku;
import liten.website.service.catalog.CatalogService;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link CatalogService}.
 */
@ParametersAreNonnullByDefault
public final class DefaultCatalogService implements CatalogService {
  private final IseCatalogDao catalogDao;

  // TODO: cache
  //private final Map<Ise.ExternalId, CatalogItem> cache = new ConcurrentHashMap<>(10000);

  // TODO: cache
  private final Map<String, Optional<Ise.Item>> languageAliasItems = new ConcurrentHashMap<>(100);

  public DefaultCatalogService(IseCatalogDao catalogDao) {
    this.catalogDao = Objects.requireNonNull(catalogDao, "catalogDao");
  }

  @Override
  public List<String> getSkuNameHints(String type, String namePrefix) {
    return catalogDao.getEnvironment().computeInTransaction(tx -> catalogDao.getNameHints(tx, type, namePrefix));
  }

  @Override
  public CatalogItem getDetailedEntry(String id, String userLanguage) {
    return catalogDao.getEnvironment().computeInTransaction(tx -> {
      final Ise.Item item = catalogDao.getById(tx, id);
      return getCatalogItem(tx, item, userLanguage);
    });
  }

  @Override
  public PageResult<CatalogItem> getItems(String userLanguage, String type, String namePrefix) {
    return new ItemPageResult(userLanguage, type, namePrefix);
  }

  @Override
  public PageResult<CatalogItem> getRightRelationEntries(String id, String userLanguage) {
    return PageResult.empty();
  }

  //
  // Private
  //

  private final class ItemPageResult extends AbstractPageResult<CatalogItem, Ise.ItemQueryResult> {
    private final String userLanguage;
    private final String type;
    private final String namePrefix;

    ItemPageResult(String userLanguage,
                   String type,
                   String namePrefix) {
      this.userLanguage = userLanguage;
      this.type = type;
      this.namePrefix = namePrefix;
    }

    @Override
    protected String getCursor(Ise.ItemQueryResult itemQueryResult) {
      return itemQueryResult.getCursor();
    }

    @Override
    protected List<CatalogItem> getItemList(Ise.ItemQueryResult itemQueryResult) {
      return catalogDao.getEnvironment().computeInTransaction(tx -> itemQueryResult.getItemsList()
          .stream()
          .map(item -> getCatalogItem(tx, item, userLanguage))
          .collect(Collectors.toList()));
    }

    @Override
    protected Ise.ItemQueryResult query(String cursor, int limit) {
      return catalogDao.getEnvironment().computeInTransaction(tx -> catalogDao.getItems(tx, Ise.ItemQuery.newBuilder()
          .setCursor(cursor)
          .setNamePrefix(StringUtils.hasLength(namePrefix) ? namePrefix : "")
          .setType(type)
          .setLimit(limit)
          .build()));
    }
  }

  //private final class RightRelationsPageResult implements

  private CatalogItem getCatalogItem(Transaction tx, Ise.Item item, String userLanguageCode) {
    final List<CatalogSku> catalogSkus = new ArrayList<>(item.getSkusCount());
    final int defaultSkuIndex = getDefaultSkuIndex(item, userLanguageCode);

    for (int i = 0; i < item.getSkusCount(); ++i) {
      final boolean isDefault = (i == defaultSkuIndex);
      final Ise.Sku sku = item.getSkus(i);
      final List<CatalogEntry> entries = Collections.emptyList();

      catalogSkus.add(new CatalogSku(
          sku,
          isDefault,
          getLanguageName(tx, sku.getLanguage(), userLanguageCode),
          entries));
    }

    return CatalogItem.newBuilder()
        .setUserLanguageCode(userLanguageCode)
        .setId(item.getId())
        .setType(item.getType())
        .setSkus(catalogSkus)
        .build();
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

  @Nullable
  private String getLanguageName(Transaction tx, String alias, String userLanguage) {
    final Optional<Ise.Item> langItem = languageAliasItems.computeIfAbsent(alias,
        (k) -> Optional.ofNullable(catalogDao.getByExternalId(tx, IseNames.newAlias(alias))));

    if (langItem.isPresent()) {
      for (final Ise.Sku sku : langItem.get().getSkusList()) {
        if (userLanguage.equals(sku.getLanguage())) {
          return sku.getTitle();
        }
      }
    }

    return null;
  }
}
