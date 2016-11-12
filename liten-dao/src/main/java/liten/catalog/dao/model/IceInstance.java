package liten.catalog.dao.model;

import com.truward.time.UtcTime;
import liten.dao.model.ModelWithId;

import javax.annotation.Nullable;

/**
 * @author Alexander Shabanov
 */
public final class IceInstance extends ModelWithId {
  private final UtcTime created;
  private final long originId;
  private final long downloadId;

  private IceInstance(long id, UtcTime created, long originId, long downloadId) {
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

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder extends ModelWithId.Builder<Builder> {
    private UtcTime created;
    private long originId;
    private long downloadId;

    private Builder() {}

    public IceInstance build() {
      return new IceInstance(id, created, originId, downloadId);
    }

    public Builder setCreated(@Nullable UtcTime value) {
      this.created = value;
      return this;
    }

    public Builder setOriginId(long value) {
      this.originId = value;
      return this;
    }

    public Builder setDownloadId(long value) {
      this.downloadId = value;
      return this;
    }

    @Override
    protected Builder getSelf() {
      return this;
    }
  }
}
