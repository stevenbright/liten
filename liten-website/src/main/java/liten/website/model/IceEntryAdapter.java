package liten.website.model;

import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceInstance;
import liten.catalog.dao.model.IceItem;
import liten.util.CheckedCollections;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public class IceEntryAdapter {
  private final IceEntry entry;
  private final Map<String, List<IceEntry>> fromRelations;

  public IceEntryAdapter(IceEntry entry, Map<String, List<IceEntry>> fromRelations) {
    this.entry = entry;
    this.fromRelations = CheckedCollections.copyMap(fromRelations, "fromRelations");
  }

  public IceEntryAdapter(IceEntry entry) {
    this(entry, Collections.emptyMap());
  }

  public Map<String, List<IceEntry>> getFromRelations() {
    return fromRelations;
  }

  public List<IceEntry> getFromRelations(String relationName) {
    final List<IceEntry> result = getFromRelations().get(relationName);
    return result != null ? result : Collections.emptyList();
  }

  public List<IceEntry> getAuthors() {
    return getFromRelations("author");
  }

  public List<IceEntry> getGenres() {
    return getFromRelations("genre");
  }

  public List<IceEntry> getOrigins() {
    return getFromRelations("origin");
  }

  public String getDisplayTitle() {
    return entry.getDisplayTitle();
  }

  public boolean isDefaultSkuPresent() {
    return entry.isDefaultSkuPresent();
  }

  public boolean isDefaultInstancePresent() {
    return entry.isDefaultInstancePresent();
  }

  public IceEntry.SkuEntry getDefaultSkuEntry() {
    return entry.getDefaultSkuEntry();
  }

  public IceInstance getDefaultInstance() {
    return entry.getDefaultInstance();
  }

  public IceItem getItem() {
    return entry.getItem();
  }

  public List<IceItem> getRelatedItems() {
    return entry.getRelatedItems();
  }

  public IceItem getRelatedItem(long id) {
    return entry.getRelatedItem(id);
  }

  public List<IceEntry.SkuEntry> getSkuEntries() {
    return entry.getSkuEntries();
  }
}
