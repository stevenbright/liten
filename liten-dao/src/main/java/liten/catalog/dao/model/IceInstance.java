package liten.catalog.dao.model;

import com.truward.time.UtcTime;
import liten.dao.model.ModelWithId;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class IceInstance extends ModelWithId {
  private final UtcTime created;
  private final long originId;
  private final long downloadId;

  private IceInstance(long id, UtcTime created, long originId, long downloadId) {
    super(id);
    this.created = Objects.requireNonNull(created, "created");
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

  //
  // equals / hashCode
  //

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IceInstance)) return false;
    if (!super.equals(o)) return false;

    IceInstance that = (IceInstance) o;

    return originId == that.originId && downloadId == that.downloadId && created.equals(that.created);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + created.hashCode();
    result = 31 * result + (int) (originId ^ (originId >>> 32));
    result = 31 * result + (int) (downloadId ^ (downloadId >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "IceInstance{" +
        "id=" + getId() +
        ", created=" + created +
        ", originId=" + originId +
        ", downloadId=" + downloadId +
        '}';
  }

  //
  // Builder
  //

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
