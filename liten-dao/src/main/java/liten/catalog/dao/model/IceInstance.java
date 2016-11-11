package liten.catalog.dao.model;

import com.truward.time.UtcTime;
import liten.dao.model.ModelWithId;

/**
 * @author Alexander Shabanov
 */
public final class IceInstance extends ModelWithId {
  private final UtcTime created;
  private final long originId;
  private final long downloadId;

  public IceInstance(long id, UtcTime created, long originId, long downloadId) {
    super(id);
    this.created = created;
    this.originId = originId;
    this.downloadId = downloadId;
  }

  public UtcTime getCreated() {
    return created;
  }

  public long getOriginId() {
    return originId;
  }

  public long getDownloadId() {
    return downloadId;
  }
}
