package liten.website.service;

import liten.catalog.dao.CatalogQueryDao;
import liten.catalog.dao.CatalogUpdaterDao;
import liten.catalog.dao.model.*;
import liten.website.model.IceEntryAdapter;
import liten.website.model.PaginationHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Exposes access to the catalog.
 */
@ParametersAreNonnullByDefault
public final class DefaultCatalogService implements CatalogService {
  private final CatalogQueryDao queryDao;
  private final CatalogUpdaterDao updaterDao;

  public DefaultCatalogService(CatalogQueryDao queryDao, CatalogUpdaterDao updaterDao) {
    this.queryDao = Objects.requireNonNull(queryDao, "queryDao");
    this.updaterDao = Objects.requireNonNull(updaterDao, "updaterDao");
  }

  @Override
  public PaginationHelper<IceEntryAdapter> getPaginationHelper(String userLanguage,
                                                               @Nullable String type,
                                                               @Nullable String namePrefix) {
    return new IceEntryPaginationHelper(queryDao, userLanguage, type, namePrefix);
  }

  @Override
  public IceEntryAdapter getEntry(long id, String userLanguage) {
    final IceEntry entry = queryDao.getEntry(id);
    return getEntryAdapter(queryDao, userLanguage, entry);
  }

  public List<String> getSkuNameHints(@Nullable String type,
                                      @Nullable String namePrefix) {
    return queryDao.getSkuNameHints(type, namePrefix);
  }

  /*
  TODO: use cache below

  LoadingCache<Key, Graph> graphs = CacheBuilder.newBuilder()
    .concurrencyLevel(4)
    .weakKeys()
    .maximumSize(10000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build(
        new CacheLoader<Key, Graph>() {
          public Graph load(Key key) throws AnyException {
            return createExpensiveGraph(key);
          }
        });
   */

  //
  // Private
  //

  private static IceEntryAdapter getEntryAdapter(CatalogQueryDao queryDao,
                                                 String userLanguage,
                                                 IceEntry entry) {
    List<IceEntry> relatedEntries = new ArrayList<>();
    Map<String, List<IceEntry>> fromRelations = new HashMap<>();

    if (entry.getItem().getType().equals("book")) {
      // get relations
      final List<IceRelation> relations = queryDao.getRelations(IceRelationQuery.newBuilder()
          .setLimit(100)
          .setRelatedItemId(entry.getItem().getId())
          .setDirection(IceRelationQuery.Direction.LEFT)
          .build());
      for (final IceRelation relation : relations) {
        final List<IceEntry> e = fromRelations.computeIfAbsent(relation.getType(), k -> new ArrayList<>());
        final IceEntry relatedEntry = queryDao.getEntry(relation.getRelatedItemId());
        e.add(relatedEntry);
      }
    }

    // add language and try to find default entry
    IceEntry.SkuEntry defaultSkuEntry = null;
    for (final IceEntry.SkuEntry skuEntry : entry.getSkuEntries()) {
      final IceEntry language = queryDao.getEntry(skuEntry.getSku().getLanguageId());
      relatedEntries.add(language);

      // find default SKU entry by matching language alias,
      // also try to fallback to English if nothing found
      if (userLanguage.equals(language.getItem().getAlias()) ||
          (defaultSkuEntry == null && "en".equals(language.getItem().getAlias()))) {
        // found preferred user language
        defaultSkuEntry = skuEntry;
      }
    }

    if (defaultSkuEntry == null && !entry.getSkuEntries().isEmpty()) {
      // no default entry - fallback to something
      defaultSkuEntry = entry.getSkuEntries().get(0);
    }

    return new IceEntryAdapter(entry, relatedEntries, defaultSkuEntry, fromRelations);
  }

  private static final class IceEntryPaginationHelper extends PaginationHelper<IceEntryAdapter> {
    private final CatalogQueryDao queryDao;
    private final String userLanguage;
    private final String type;
    private final String namePrefix;

    IceEntryPaginationHelper(CatalogQueryDao queryDao,
                             String userLanguage,
                             @Nullable String type,
                             @Nullable String namePrefix) {
      this.queryDao = queryDao;
      this.userLanguage = userLanguage;
      this.type = type;
      this.namePrefix = namePrefix;
    }

    @Override
    protected List<IceEntryAdapter> getItemList(long startItemId, int limit) {
      final List<IceEntry> entries = queryDao.getEntries(IceEntryQuery.newBuilder()
          .setStartItemId(startItemId)
          .setLimit(limit)
          .setType(type)
          .setNamePrefix(namePrefix)
          .build());

      return entries.stream()
          .map(e -> getEntryAdapter(queryDao, userLanguage, e))
          .collect(Collectors.toList());
    }

    @Override
    protected long getItemId(IceEntryAdapter item) {
      return item.getItem().getId();
    }

    @Override
    protected String createNextUrl(long startItemId, int limit) {
      try {
        final StringBuilder builder = new StringBuilder(100);
        builder.append("/g/cat/part/entries?startItemId=").append(startItemId);
        builder.append("&limit=").append(limit);
        if (type != null) {
            builder.append("&type=").append(URLEncoder.encode(type, StandardCharsets.UTF_8.name()));
        }
        if (namePrefix != null) {
          builder.append("&namePrefix=").append(URLEncoder.encode(namePrefix, StandardCharsets.UTF_8.name()));
        }

        return builder.toString();
      } catch (UnsupportedEncodingException e) {
        throw new IllegalStateException(e); // should not happen
      }
    }
  }
}
