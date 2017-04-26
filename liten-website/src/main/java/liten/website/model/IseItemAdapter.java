package liten.website.model;

import com.truward.time.UtcTime;
import liten.catalog.dao.IseCatalogDao;
import liten.catalog.model.Ise;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Alexander Shabanov
 */
@SuppressWarnings({"unused", "WeakerAccess"}) // referred from freemarker templates
@ParametersAreNonnullByDefault
public final class IseItemAdapter {
  private final Ise.Item item;
  private final List<Ise.Item> relatedItems;
  private final Ise.Sku defaultSkuEntry;
  private final Map<String, List<Ise.Item>> fromRelations;

  public IseItemAdapter(Ise.Item item,
                        List<Ise.Item> relatedItems,
                        @Nullable Ise.Sku defaultSkuEntry,
                        Map<String, List<Ise.Item>> fromRelations) {
    this.item = item;
    this.relatedItems = relatedItems;
    this.defaultSkuEntry = defaultSkuEntry;
    this.fromRelations = fromRelations;
  }

  public String getId() {
    return item.getId();
  }

  private Map<String, List<Ise.Item>> getFromRelations() {
    return fromRelations;
  }

  public boolean isDetailPageCoverUrlPresent() {
    return item.getType().equals(IseCatalogDao.BOOK);
  }

  public final String getDetailPageCoverUrl() {
    return "/demo/media/image?type=cover";
  }

  public final String getCreatedDate() {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(UtcTime.newUtcTimeZone());
    return dateFormat.format(new Date(getDefaultInstance().getCreatedTimestamp()));
  }

  public final boolean isDownloadUrlPresent() {
    return isDefaultInstancePresent() && getDefaultInstance().hasDownloadInfo();
  }

  public final String getDownloadUrl() {
    if (!isDownloadUrlPresent()) {
      throw new IllegalStateException("downloadUrl is missing");
    }

    final Ise.Entry inst = getDefaultInstance();
    final Ise.DownloadInfo downloadInfo = inst.getDownloadInfo();
    return "/g/download/origin/" + downloadInfo.getOriginId() + "/item/" + downloadInfo.getDownloadId();
  }

  public final int getFileSize() {
    return 1000;
  }

  public final List<IseItemRef> getLanguages() {
//    if (isDefaultSkuPresent()) {
//      final Ise.Sku sku = getDefaultSkuEntry();
//      for (final Ise.Item relatedEntry : relatedItems) {
//        if (relatedEntry.getId() )
//
//        if (relatedEntry.getItem().getId() == sku.getSku().getLanguageId()) {
//          return Collections.singletonList(toEntityRef(relatedEntry));
//        }
//      }
//    }

    return Collections.emptyList();
  }

  public final List<IseItemRef> getAuthors() {
    return getFromRelations("author");
  }

  public final List<IseItemRef> getGenres() {
    return getFromRelations("genre");
  }

  public final List<IseItemRef> getOrigins() {
    return getFromRelations("origin");
  }

  public final String getDisplayTitle() {
    String title = null;

    if (isDefaultSkuPresent()) {
      title = getDefaultSkuEntry().getTitle();
    }

    return StringUtils.hasLength(title) ? title : getDefaultTitle(item);
  }

  public final boolean isDefaultSkuPresent() {
    return defaultSkuEntry != null;
  }

  public final boolean isDefaultInstancePresent() {
    return !(item.getSkusCount() == 0 || item.getSkus(0).getEntriesCount() == 0);
  }

  public final Ise.Sku getDefaultSkuEntry() {
    if (defaultSkuEntry != null) {
      return defaultSkuEntry;
    }

    throw new IllegalStateException("No default SKU entry for Ise.Item{id=" + item.getId() + "}");
  }

  public final Ise.Entry getDefaultInstance() {
    if (isDefaultSkuPresent()) {
      final Ise.Sku skuEntry = getDefaultSkuEntry();
      if (skuEntry.getEntriesCount() > 0) {
        return skuEntry.getEntries(0);
      }
    }

    throw new IllegalStateException("No default instance for Ise.Item{id=" + item.getId() + "}");
  }

  public final Ise.Item getItem() {
    return item;
  }

  public final List<Ise.Sku> getSkuEntries() {
    return item.getSkusList();
  }

  //
  // Private
  //

  private IseItemRef toEntityRef(Ise.Item item) {
    // now pick up SKU with the same title
    String displayTitle = null;

    if (isDefaultSkuPresent()) {
      final String languageId = getDefaultSkuEntry().getLanguage();

      for (final Ise.Sku sku : item.getSkusList()) {
        if (sku.getLanguage().compareToIgnoreCase(languageId) == 0) {
          displayTitle = sku.getTitle();
          break;
        }
      }
    }

    return new IseItemRef(item.getId(), StringUtils.hasLength(displayTitle) ? displayTitle : getDefaultTitle(item));
  }

  private static String getDefaultTitle(Ise.Item item) {
    return item.getId();
  }

  private List<IseItemRef> getFromRelations(String relationName) {
    final List<Ise.Item> result = getFromRelations().get(relationName);
    return result != null ?
        result.stream().map(this::toEntityRef).collect(Collectors.toList()) :
        Collections.emptyList();
  }

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
