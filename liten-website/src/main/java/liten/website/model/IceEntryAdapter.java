package liten.website.model;

import com.truward.time.UtcTime;
import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceInstance;
import liten.catalog.dao.model.IceItem;
import liten.dao.model.ModelWithId;
import liten.util.CheckedCollections;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public abstract class IceEntryAdapter {
  private final IceEntry entry;

  public IceEntryAdapter(IceEntry entry) {
    this.entry = entry;
  }

  public static IceEntryAdapter fromEntry(IceEntry entry) {
    return new StandardEntryAdapter(entry);
  }

  public static IceEntryAdapter fromBook(IceEntry entry, Map<String, List<IceEntry>> fromRelations) {
    return new BookEntryAdapter(entry, fromRelations);
  }

  public Map<String, List<IceEntry>> getFromRelations() {
    throw new UnsupportedOperationException();
  }

  public final List<IceEntry> getFromRelations(String relationName) {
    final List<IceEntry> result = getFromRelations().get(relationName);
    return result != null ? result : Collections.emptyList();
  }

  public final String getCreatedDate() {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(UtcTime.newUtcTimeZone());
    return dateFormat.format(entry.getDefaultInstance().getCreated().asDate());
  }

  public final boolean isDownloadUrlPresent() {
    if (!entry.isDefaultInstancePresent()) {
      return false;
    }
    final IceInstance inst = entry.getDefaultInstance();
    return ModelWithId.isValidId(inst.getDownloadId()) && ModelWithId.isValidId(inst.getOriginId());
  }

  public final String getDownloadUrl() {
    if (!isDownloadUrlPresent()) {
      throw new IllegalStateException("downloadUrl is missing");
    }

    final IceInstance inst = entry.getDefaultInstance();
    return "/g/download/origin/" + inst.getOriginId() + "/item/" + inst.getDownloadId();
  }

  public final int getFileSize() {
    return 1000;
  }

  public final List<IceEntry> getLanguages() {
    return getFromRelations("language");
  }

  public final List<IceEntry> getAuthors() {
    return getFromRelations("author");
  }

  public final List<IceEntry> getGenres() {
    return getFromRelations("genre");
  }

  public final List<IceEntry> getOrigins() {
    return getFromRelations("origin");
  }

  public final String getDisplayTitle() {
    return entry.getDisplayTitle();
  }

  public final boolean isDefaultSkuPresent() {
    return entry.isDefaultSkuPresent();
  }

  public final boolean isDefaultInstancePresent() {
    return entry.isDefaultInstancePresent();
  }

  public final IceEntry.SkuEntry getDefaultSkuEntry() {
    return entry.getDefaultSkuEntry();
  }

  public final IceInstance getDefaultInstance() {
    return entry.getDefaultInstance();
  }

  public final IceItem getItem() {
    return entry.getItem();
  }

  public final List<IceItem> getRelatedItems() {
    return entry.getRelatedItems();
  }

  public final IceItem getRelatedItem(long id) {
    return entry.getRelatedItem(id);
  }

  public final List<IceEntry.SkuEntry> getSkuEntries() {
    return entry.getSkuEntries();
  }

  //
  // Private
  //

  private static final class StandardEntryAdapter extends IceEntryAdapter {

    StandardEntryAdapter(IceEntry entry) {
      super(entry);
    }
  }

  private static final class BookEntryAdapter extends IceEntryAdapter {
    private final Map<String, List<IceEntry>> fromRelations;

    BookEntryAdapter(IceEntry entry, Map<String, List<IceEntry>> fromRelations) {
      super(entry);
      this.fromRelations = CheckedCollections.copyMap(fromRelations, "fromRelations");
    }

    @Override
    public Map<String, List<IceEntry>> getFromRelations() {
      return fromRelations;
    }
  }
}
