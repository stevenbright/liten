package liten.website.service;

import liten.catalog.dao.CatalogQueryDao;
import liten.catalog.dao.CatalogUpdaterDao;
import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceEntryFilter;
import liten.catalog.dao.model.IceRelation;
import liten.catalog.dao.model.IceRelationQuery;
import liten.website.model.IceEntryAdapter;
import liten.website.model.PaginationHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Exposes access to the catalog.
 */
@ParametersAreNonnullByDefault
public final class DefaultCatalogService {
  private final CatalogQueryDao queryDao;
  private final CatalogUpdaterDao updaterDao;

  public DefaultCatalogService(CatalogQueryDao queryDao, CatalogUpdaterDao updaterDao) {
    this.queryDao = Objects.requireNonNull(queryDao, "queryDao");
    this.updaterDao = Objects.requireNonNull(updaterDao, "updaterDao");
  }

  public PaginationHelper<IceEntryAdapter> getPaginationHelper(String userLanguage) {
    return new IceEntryPaginationHelper(queryDao, userLanguage);
  }

  public IceEntryAdapter getEntry(long id, String userLanguage) {
    final IceEntry entry = queryDao.getEntry(id);
    return getEntryAdapter(queryDao, userLanguage, entry);
  }

  //
  // Private
  //

  private static IceEntryAdapter getEntryAdapter(CatalogQueryDao queryDao,
                                                 String userLanguage,
                                                 IceEntry entry) {
    List<IceEntry> relatedEntries = new ArrayList<>();
    List<String> preferredLanguages = Collections.singletonList(userLanguage);
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

    return new IceEntryAdapter(entry, relatedEntries, preferredLanguages, fromRelations);
  }

  private static final class IceEntryPaginationHelper extends PaginationHelper<IceEntryAdapter> {
    private final CatalogQueryDao queryDao;
    private final String userLanguage;

    IceEntryPaginationHelper(CatalogQueryDao queryDao, String userLanguage) {
      this.queryDao = queryDao;
      this.userLanguage = userLanguage;
    }

    @Override
    protected List<IceEntryAdapter> getItemList(long startItemId, int limit) {
      final List<IceEntry> entries = queryDao.getEntries(
          IceEntryFilter.forLanguages(true, userLanguage), startItemId, limit);

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
      return String.format("/g/cat/part/entries?startItemId=%s&limit=%s", startItemId, limit);
    }
  }
}
