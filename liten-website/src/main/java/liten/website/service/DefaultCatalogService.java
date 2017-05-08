package liten.website.service;

import jetbrains.exodus.env.Transaction;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.model.Ise;
import liten.website.model.deprecated.IseItemAdapter;
import com.truward.web.pagination.AbstractPaginationHelper;
import com.truward.web.pagination.PaginationHelper;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Exposes access to the catalog.
 */
@Deprecated
@ParametersAreNonnullByDefault
public final class DefaultCatalogService implements CatalogService {
  private final IseCatalogDao catalogDao;

  public DefaultCatalogService(IseCatalogDao catalogDao) {
    this.catalogDao = Objects.requireNonNull(catalogDao, "catalogDao");
  }

  @Override
  public PaginationHelper getPaginationHelper(String userLanguage,
                                              @Nullable String type,
                                              @Nullable String namePrefix) {
    return new ItemPagination(catalogDao, userLanguage, type, namePrefix);
  }

  @Override
  public IseItemAdapter getDetailedEntry(String id, String userLanguage) {
    return catalogDao.getEnvironment().computeInTransaction(tx -> {
      final Ise.Item item = catalogDao.getById(tx, id);
      return getItemAdapter(tx, catalogDao, userLanguage, item);
    });
  }

  @Override
  public PaginationHelper getRightRelationEntries(String id, String userLanguage) {
    return new ForwardRelationsPagination(catalogDao, userLanguage, id);
  }

  public List<String> getSkuNameHints(@Nullable String type,
                                      @Nullable String namePrefix) {
    return catalogDao.getEnvironment().computeInTransaction(tx ->
        catalogDao.getNameHints(tx, type, namePrefix != null ? namePrefix : ""));
  }

  //
  // Private
  //

  private static IseItemAdapter getItemAdapter(Transaction tx,
                                               IseCatalogDao catalogDao,
                                               String userLanguage,
                                               Ise.Item item) {
    // try to find matching entry
    Ise.Sku defaultSku = null;
    for (final Ise.Sku sku : item.getSkusList()) {
      if (defaultSku == null) {
        defaultSku = sku;
      }

      if (userLanguage.compareToIgnoreCase(sku.getLanguage()) == 0) {
        defaultSku = sku;
        break;
      }
    }

    return new IseItemAdapter(item, Collections.emptyList(), defaultSku, Collections.emptyMap());
//    final List<IceEntry> relatedEntries = new ArrayList<>();
//    final Map<String, List<IceEntry>> fromRelations = new HashMap<>();
//    final String itemType = entry.getItem().getType();
//
//    if (itemType.equals("book")) {
//      // get incoming relations2
//      final List<IceRelation> relations = queryDao.getRelations(IceRelationQuery.newBuilder()
//          .setLimit(100)
//          .setRelatedItemId(entry.getItem().getId())
//          .setDirection(IceRelationQuery.Direction.LEFT)
//          .build());
//      for (final IceRelation relation : relations) {
//        final List<IceEntry> e = fromRelations.computeIfAbsent(relation.getType(), k -> new ArrayList<>());
//        final IceEntry relatedEntry = queryDao.getEntry(relation.getRelatedItemId());
//        e.add(relatedEntry);
//      }
//    }
//
//    // add language and try to find default entry
//    IceEntry.SkuEntry defaultSkuEntry = null;
//    for (final IceEntry.SkuEntry skuEntry : entry.getSkuEntries()) {
//      final IceEntry language = queryDao.getEntry(skuEntry.getSku().getLanguageId());
//      relatedEntries.add(language);
//
//      // find default SKU entry by matching language alias,
//      // also try to fallback to English if nothing found
//      if (userLanguage.equals(language.getItem().getAlias()) ||
//          (defaultSkuEntry == null && "en".equals(language.getItem().getAlias()))) {
//        // found preferred user language
//        defaultSkuEntry = skuEntry;
//      }
//    }
//
//    if (defaultSkuEntry == null && !entry.getSkuEntries().isEmpty()) {
//      // no default entry - fallback to something
//      defaultSkuEntry = entry.getSkuEntries().get(0);
//    }
//
//    return new IseItemAdapter(entry, relatedEntries, defaultSkuEntry, fromRelations);
  }

  private static final class ForwardRelationsPagination
      extends AbstractPaginationHelper<IseItemAdapter, Ise.ItemRelationQueryResult> {
    private final IseCatalogDao catalogDao;
    private final String userLanguage;
    private final String itemId;

    public ForwardRelationsPagination(IseCatalogDao catalogDao, String userLanguage, String itemId) {
      if (!StringUtils.hasLength(itemId)) {
        throw new IllegalArgumentException("itemId");
      }

      this.catalogDao = catalogDao;
      this.userLanguage = userLanguage;
      this.itemId = itemId;
    }

    @Override
    protected String getCursor(Ise.ItemRelationQueryResult itemRelationQueryResult) {
      return itemRelationQueryResult.getCursor();
    }

    @Override
    protected List<IseItemAdapter> getItemList(Ise.ItemRelationQueryResult itemRelationQueryResult) {
      final List<String> itemIds = itemRelationQueryResult.getToItemIdsList();

      return catalogDao.getEnvironment().computeInTransaction(tx -> {
        final List<IseItemAdapter> result = new ArrayList<>(itemIds.size());
        for (final String itemId : itemIds) {
          result.add(getItemAdapter(tx, catalogDao, userLanguage, catalogDao.getById(tx, itemId)));
        }
        return result;
      });
    }

    @Override
    protected Ise.ItemRelationQueryResult query(String cursor, int limit) {
      return catalogDao.getEnvironment().computeInTransaction(tx -> catalogDao.getRelations(tx,
          Ise.ItemRelationQuery.newBuilder()
              .setFromItemId(itemId)
              .setCursor(cursor)
              .setLimit(limit)
              .build()));
    }
  }

  private static final class ItemPagination extends AbstractPaginationHelper<IseItemAdapter, Ise.ItemQueryResult> {
    private final IseCatalogDao catalogDao;
    private final String userLanguage;
    private final String type;
    private final String namePrefix;

    ItemPagination(IseCatalogDao catalogDao,
                   String userLanguage,
                   @Nullable String type,
                   @Nullable String namePrefix) {
      this.catalogDao = catalogDao;
      this.userLanguage = userLanguage;
      this.type = type;
      this.namePrefix = namePrefix;
    }


    @Override
    protected String getCursor(Ise.ItemQueryResult itemQueryResult) {
      return itemQueryResult.getCursor();
    }

    @Override
    protected List<IseItemAdapter> getItemList(Ise.ItemQueryResult itemQueryResult) {
      return catalogDao.getEnvironment().computeInTransaction(tx -> itemQueryResult.getItemsList()
          .stream()
          .map(item -> getItemAdapter(tx, catalogDao, userLanguage, item))
          .collect(Collectors.toList()));
    }

    @Override
    protected Ise.ItemQueryResult query(String cursor, int limit) {
      return catalogDao.getEnvironment().computeInTransaction(tx -> catalogDao.getItems(tx, Ise.ItemQuery.newBuilder()
          .setCursor(cursor)
          .setNamePrefix(StringUtils.hasLength(namePrefix) ? namePrefix : "")
          .setType(StringUtils.hasLength(type) ? type : "")
          .setLimit(limit)
          .build()));
    }
  }
}
