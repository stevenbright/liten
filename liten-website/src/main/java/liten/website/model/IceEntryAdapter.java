package liten.website.model;

import com.truward.time.UtcTime;
import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceInstance;
import liten.catalog.dao.model.IceItem;
import liten.catalog.dao.model.IceSku;
import liten.dao.model.ModelWithId;
import liten.util.CheckedCollections;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class IceEntryAdapter {
  private final IceEntry entry;
  private final List<IceEntry> relatedEntries;
  private final List<String> preferredUserLanguages;
  private final Map<String, List<IceEntry>> fromRelations;

  public IceEntryAdapter(IceEntry entry,
                         List<IceEntry> relatedEntries,
                         List<String> preferredUserLanguages,
                         Map<String, List<IceEntry>> fromRelations) {
    this.entry = entry;
    this.relatedEntries = CheckedCollections.copyList(relatedEntries, "relatedEntries");
    this.preferredUserLanguages = CheckedCollections.copyList(preferredUserLanguages, "preferredUserLanguages");
    this.fromRelations = CheckedCollections.copyMap(fromRelations, "fromRelations");
  }

  public Map<String, List<IceEntry>> getFromRelations() {
    return fromRelations;
  }

  public final List<IceEntry> getFromRelations(String relationName) {
    final List<IceEntry> result = getFromRelations().get(relationName);
    return result != null ? result : Collections.emptyList();
  }

  public final String getCreatedDate() {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(UtcTime.newUtcTimeZone());
    return dateFormat.format(getDefaultInstance().getCreated().asDate());
  }

  public final boolean isDownloadUrlPresent() {
    if (!isDefaultInstancePresent()) {
      return false;
    }
    final IceInstance inst = getDefaultInstance();
    return ModelWithId.isValidId(inst.getDownloadId()) && ModelWithId.isValidId(inst.getOriginId());
  }

  public final String getDownloadUrl() {
    if (!isDownloadUrlPresent()) {
      throw new IllegalStateException("downloadUrl is missing");
    }

    final IceInstance inst = getDefaultInstance();
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
    return entry.getSkuEntries().size() > 0;
  }

  public final boolean isDefaultInstancePresent() {
    return (entry.getSkuEntries().size() > 0) && (entry.getSkuEntries().get(0).getInstances().size() > 0);
  }

  public final IceEntry.SkuEntry getDefaultSkuEntry() {
    if (entry.getSkuEntries().size() > 0) {
      return entry.getSkuEntries().get(0);
    }

    throw new IllegalStateException("No default SKU for IceEntry{id=" + entry.getItem().getId() + "}");
  }

  public final IceInstance getDefaultInstance() {
    if (entry.getSkuEntries().size() > 0) {
      final IceEntry.SkuEntry skuEntry = entry.getSkuEntries().get(0);
      if (skuEntry.getInstances().size() > 0) {
        return skuEntry.getInstances().get(0);
      }
    }

    throw new IllegalStateException("No default instance for IceEntry{id=" + entry.getItem().getId() + "}");
  }

  public final IceItem getItem() {
    return entry.getItem();
  }

  public final List<IceEntry.SkuEntry> getSkuEntries() {
    return entry.getSkuEntries();
  }

  //
  // Private
  //

  // == WIP ==
//  @Nullable
//  private List<IceSku> getPreferredSkuByLanguage() {
//    final List<IceSku> result = new ArrayList<>();
//
//    for (final String preferredLanguage : this.preferredUserLanguages) {
//      for (final IceEntry.SkuEntry skuEntry : this.entry.getSkuEntries()) {
//        if (skuEntry.getSku().getLanguageId())
//      }
//    }
//
//    return result;
//  }
//
//  @Nullable
//  private List<IceInstance> getPreferredInstanceByLanguage() {
//
//    for (final String preferredLanguage : this.preferredUserLanguages) {
//
//    }
//  }
}
