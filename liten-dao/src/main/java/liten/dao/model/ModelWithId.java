package liten.dao.model;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public abstract class ModelWithId extends BaseModel {
  public static final long INVALID_ID = 0;

  public static boolean isValidId(long id) {
    return id > 0;
  }

  private final long id;

  protected ModelWithId(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public long getValidId() {
    if (!isValidId()) {
      throw new IllegalStateException("Valid ID expected for object=" + this.toString());
    }

    return getId();
  }

  public boolean isValidId() {
    return isValidId(id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ModelWithId)) return false;

    ModelWithId that = (ModelWithId) o;

    return getId() == that.getId();

  }

  @Override
  public int hashCode() {
    return (int) (getId() ^ (getId() >>> 32));
  }

  @Override
  public String toString() {
    @SuppressWarnings("StringBufferReplaceableByString")
    final StringBuilder builder = new StringBuilder(50);

    builder
        .append(getClass().getSimpleName())
        .append("#{id=")
        .append(getId())
        .append('}');

    return builder.toString();
  }

  public static abstract class Builder<TSelf> {
    protected long id = INVALID_ID;

    protected Builder() {}

    public TSelf setId(long value) {
      this.id = value;
      return getSelf();
    }

    protected abstract TSelf getSelf();
  }
}
