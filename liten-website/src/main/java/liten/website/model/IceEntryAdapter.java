package liten.website.model;

import com.truward.time.UtcTime;
import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceInstance;
import liten.catalog.dao.model.IceItem;
import liten.dao.model.ModelWithId;
import liten.util.CheckedCollections;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Alexander Shabanov
 */
@SuppressWarnings("unused") // referred from freemarker templates
@ParametersAreNonnullByDefault
public final class IceEntryAdapter {
  private final IceEntry entry;
  private final List<IceEntry> relatedEntries;
  private final IceEntry.SkuEntry defaultSkuEntry;
  private final Map<String, List<IceEntry>> fromRelations;

  public IceEntryAdapter(IceEntry entry,
                         List<IceEntry> relatedEntries,
                         @Nullable IceEntry.SkuEntry defaultSkuEntry,
                         Map<String, List<IceEntry>> fromRelations) {
    this.entry = entry;
    this.relatedEntries = CheckedCollections.copyList(relatedEntries, "relatedEntries");
    this.defaultSkuEntry = defaultSkuEntry;
    this.fromRelations = CheckedCollections.copyMap(fromRelations, "fromRelations");
  }

  public long getId() {
    return entry.getItem().getId();
  }

  public Map<String, List<IceEntry>> getFromRelations() {
    return fromRelations;
  }

  public final List<IceEntryRef> getFromRelations(String relationName) {
    final List<IceEntry> result = getFromRelations().get(relationName);
    return result != null ?
        result.stream().map(this::toEntityRef).collect(Collectors.toList()) :
        Collections.emptyList();
  }

  public boolean isDetailPageCoverUrlPresent() {
    return entry.getItem().getType().equals("book");
  }

  public final String getDetailPageCoverUrl() {
    return "/demo/media/image?type=cover";
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

  public final List<IceEntryRef> getLanguages() {
    if (isDefaultSkuPresent()) {
      final IceEntry.SkuEntry sku = getDefaultSkuEntry();
      for (final IceEntry relatedEntry : relatedEntries) {
        if (relatedEntry.getItem().getId() == sku.getSku().getLanguageId()) {
          return Collections.singletonList(toEntityRef(relatedEntry));
        }
      }
    }

    return Collections.emptyList();
  }

  private IceEntryRef toEntityRef(IceEntry otherEntry) {
    // now pick up SKU with the same title
    String displayTitle = otherEntry.getItem().getAlias();
    if (displayTitle == null) {
      displayTitle = "?";
    }

    if (isDefaultSkuPresent()) {
      final long languageId = getDefaultSkuEntry().getSku().getLanguageId();

      for (final IceEntry.SkuEntry skuEntry : otherEntry.getSkuEntries()) {
        if (skuEntry.getSku().getLanguageId() == languageId) {
          displayTitle = skuEntry.getSku().getTitle();
        }
      }
    }

    return new IceEntryRef(otherEntry.getItem().getId(), displayTitle);
  }

  public final List<IceEntryRef> getAuthors() {
    return getFromRelations("author");
  }

  public final List<IceEntryRef> getGenres() {
    return getFromRelations("genre");
  }

  public final List<IceEntryRef> getOrigins() {
    return getFromRelations("origin");
  }

  public final String getDisplayTitle() {
    String title = entry.getItem().getAlias();
    if (isDefaultSkuPresent()) {
      title = getDefaultSkuEntry().getSku().getTitle();
    }

    return title != null ? title : "???";
  }

  public final boolean isDefaultSkuPresent() {
    return defaultSkuEntry != null;
  }

  public final boolean isDefaultInstancePresent() {
    return !(entry.getSkuEntries().isEmpty() || entry.getSkuEntries().get(0).getInstances().isEmpty());
  }

  public final IceEntry.SkuEntry getDefaultSkuEntry() {
    if (defaultSkuEntry != null) {
      return defaultSkuEntry;
    }

    throw new IllegalStateException("No default SKU entry for IceEntry{id=" + entry.getItem().getId() + "}");
  }

  public final IceInstance getDefaultInstance() {
    if (isDefaultSkuPresent()) {
      final IceEntry.SkuEntry skuEntry = getDefaultSkuEntry();
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
