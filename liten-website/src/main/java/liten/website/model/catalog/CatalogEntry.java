package liten.website.model.catalog;

import liten.catalog.model.Ise;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

@ParametersAreNonnullByDefault
public final class CatalogEntry {
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

    final Ise.DownloadInfo di = entry.getDownloadInfo();

    //noinspection StringBufferReplaceableByString
    return new StringBuilder(50)
        .append("/g/cat/download/")
        .append(di.getDownloadType())
        .append("/")
        .append(di.getOriginId())
        .append("/")
        .append(di.getDownloadId())
        .toString();
  }
}
