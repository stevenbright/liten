package liten.catalog.dao.model;

import com.truward.time.UtcTime;
import liten.dao.model.BaseModel;

/**
 * @author Alexander Shabanov
 */
public final class IceInstance extends BaseModel {
  private UtcTime created;
  private long originId;
  private long downloadId;

  public UtcTime getCreated() {
    return created;
  }

  public void setCreated(UtcTime created) {
    this.created = created;
  }

  public long getOriginId() {
    return originId;
  }

  public void setOriginId(long originId) {
    this.originId = originId;
  }

  public long getDownloadId() {
    return downloadId;
  }

  public void setDownloadId(long downloadId) {
    this.downloadId = downloadId;
  }
}
