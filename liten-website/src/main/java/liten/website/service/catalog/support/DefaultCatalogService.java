package liten.website.service.catalog.support;

import com.google.common.collect.ImmutableList;
import com.truward.web.pagination.AbstractPageResult;
import com.truward.web.pagination.PageResult;
import jetbrains.exodus.env.Transaction;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.model.Ise;
import liten.catalog.util.IseNames;
import liten.website.exception.ResourceNotFoundException;
import liten.website.model.catalog.CatalogEntry;
import liten.website.model.catalog.CatalogItem;
import liten.website.model.catalog.CatalogItemRef;
import liten.website.model.catalog.CatalogSku;
import liten.website.service.catalog.CatalogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link CatalogService}.
 */
@Service
@ParametersAreNonnullByDefault
public final class DefaultCatalogService implements CatalogService {
  private final IseCatalogDao catalogDao;

  // TODO: cache
  //private final Map<Ise.ExternalId, CatalogItem> cache = new ConcurrentHashMap<>(10000);

  // TODO: cache
  //private final Map<String, Optional<Ise.Item>> languageAliasItems = new ConcurrentHashMap<>(100);

  public DefaultCatalogService(IseCatalogDao catalogDao) {
    this.catalogDao = Objects.requireNonNull(catalogDao, "catalogDao");
  }

  @Override
  public List<String> getSkuNameHints(String type, String namePrefix) {
    return catalogDao.getEnvironment().computeInTransaction(tx -> catalogDao.getNameHints(tx, type, namePrefix));
  }

  @Override
  public CatalogItem getDetailedEntry(String itemId, @Nullable String skuId, String userLanguage) {
    return catalogDao.getEnvironment().computeInTransaction(tx -> {
      final Ise.Item item = catalogDao.getById(tx, itemId);
      return getCatalogItem(tx, item, userLanguage, skuId);
    });
  }

  @Override
  public PageResult<CatalogItem> getItems(String userLanguage, String type, String namePrefix) {
    return new ItemPageResult(userLanguage, type, namePrefix);
  }

  @Override
  public PageResult<CatalogItem> getRightRelationEntries(String itemId, String userLanguage) {
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
          .map(item -> getCatalogItem(tx, item, userLanguage, null))
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

  private List<CatalogItemRef> getItemRefs(Transaction tx, List<String> ids, String userLanguageCode) {
    final List<CatalogItemRef> result = new ArrayList<>();
    for (final String id : ids) {
      result.add(getCatalogItemRef(catalogDao.getById(tx, id), userLanguageCode));
    }
    return result;
  }

  private CatalogItemRef getCatalogItemRef(Ise.Item item, String userLanguageCode) {
    String title = userLanguageCode;
    for (final Ise.Sku sku : item.getSkusList()) {
      if (sku.getLanguage().equals(userLanguageCode)) {
        title = sku.getTitle();
        break;
      }
    }

    return new CatalogItemRef(item.getId(), title, userLanguageCode);
  }

  private CatalogSku getCatalogSku(Transaction tx, Ise.Sku sku, String userLanguageCode) {
    final List<CatalogEntry> entries = new ArrayList<>(sku.getEntriesCount());
    for (final Ise.Entry e : sku.getEntriesList()) {
      entries.add(new CatalogEntry(e));
    }

    return new CatalogSku(
        sku,
        getUserLanguage(tx, sku.getLanguage(), userLanguageCode),
        entries);
  }

  private List<CatalogSku> getOrderedCatalogSkus(
      Transaction tx,
      String userLanguageCode,
      Ise.Item item,
      @Nullable String skuId) {
    if (item.getSkusCount() == 0) {
      return ImmutableList.of();
    }

    final int defaultSkuIndex = getDefaultSkuIndex(item, skuId, userLanguageCode);
    final List<CatalogSku> skus = new ArrayList<>(item.getSkusCount());

    if (defaultSkuIndex >= 0) {
      // add default indexed item first
      skus.add(getCatalogSku(tx, item.getSkus(defaultSkuIndex), userLanguageCode));
    }

    // add remaining items
    for (int i = 0; i < item.getSkusCount(); ++i) {
      if (i == defaultSkuIndex) {
        continue;
      }
      skus.add(getCatalogSku(tx, item.getSkus(i), userLanguageCode));
    }

    return skus;
  }

  private static int getDefaultSkuIndex(Ise.Item item, @Nullable String skuId, String userLanguage) {
    final int skuCount = item.getSkusCount();

    if (skuId != null) {
      skuId = skuId.toLowerCase();
      for (int i = 0; i < skuCount; ++i) {
        final Ise.Sku sku = item.getSkus(i);
        if (sku.getId().equals(skuId)) {
          return i;
        }
      }

      // sku is specified, but not found, throw an error
      throw new ResourceNotFoundException("Missing SKU=" + skuId + " for item ID=" + item.getId());
    }

    int result = -1;
    for (int i = 0; i < skuCount; ++i) {
      final Ise.Sku sku = item.getSkus(i);

      if (i == 0 || sku.getLanguage().equals(userLanguage)) {
        result = i;
      }
    }

    return result;
  }

  private CatalogItemRef getUserLanguage(Transaction tx, String languageCode, String userLanguageCode) {
    // TODO: cache
    final Ise.Item languageItem = catalogDao.getByExternalId(tx, IseNames.newAlias(languageCode));
    final CatalogItemRef languageItemRef;
    if (languageItem == null) {
      languageItemRef = new CatalogItemRef("", languageCode, languageCode);
    } else {
      languageItemRef = getCatalogItemRef(languageItem, userLanguageCode);
    }

    return languageItemRef;
  }

  private CatalogItem getCatalogItem(Transaction tx, Ise.Item item, String userLanguageCode, @Nullable String skuId) {
    userLanguageCode = userLanguageCode.toLowerCase();

    final List<CatalogSku> catalogSkus = getOrderedCatalogSkus(tx, userLanguageCode, item, skuId);

    final CatalogItem.Builder builder = CatalogItem.newBuilder()
        .setId(item.getId())
        .setType(item.getType())
        .setSkus(catalogSkus);

    if (item.hasExtras()) {
      if (item.getExtras().hasBook()) {
        final Ise.BookItemExtras bookExtras = item.getExtras().getBook();
        builder
            .setAuthors(getItemRefs(tx, bookExtras.getAuthorIdsList(), userLanguageCode))
            .setGenres(getItemRefs(tx, bookExtras.getGenreIdsList(), userLanguageCode));
      }
    }

    return builder.build();
  }
}
