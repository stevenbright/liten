package liten.website.model.catalog;

import com.truward.semantic.id.IdCodec;
import com.truward.semantic.id.SemanticIdCodec;
import liten.catalog.model.Ise;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Note on suppressions: this class is accessed from templating engine
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@ParametersAreNonnullByDefault
public final class CatalogEntry {
  public static final IdCodec DOWNLOAD_PARAMETERS_CODEC = SemanticIdCodec.forPrefixNames("dp1");

  private final Ise.Entry entry;

  public CatalogEntry(Ise.Entry entry) {
    this.entry = Objects.requireNonNull(entry, "entry");
  }

  public String getId() {
    return entry.getId();
  }

  public String getCreatedTimestamp() {
    //final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(TimeZone.getTimeZone("UTC"));
    return format.format(new Date(entry.getCreatedTimestamp()));
  }

  public boolean isDownloadInfoPresent() {
    return entry.hasDownloadInfo();
  }

  public int getFileSize() {
    return entry.getDownloadInfo().getFileSize();
  }

  public String getDownloadType() {
    return entry.getDownloadInfo().getDownloadType();
  }

  public String getDownloadUrl() {
    if (!isDownloadInfoPresent()) {
      return "";
    }

    //noinspection StringBufferReplaceableByString
    return new StringBuilder(50)
        .append("/g/download/item/")
        .append(DOWNLOAD_PARAMETERS_CODEC.encodeBytes(entry.getDownloadInfo().toByteArray()))
        .toString();
  }
}
