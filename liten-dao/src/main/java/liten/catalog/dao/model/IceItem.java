package liten.catalog.dao.model;

import liten.dao.model.ModelWithId;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class IceItem extends ModelWithId {
  private final String type;
  private final int modCounter;
  private final String alias;

  private IceItem(long id, String type, int modCounter, String alias) {
    super(id);
    this.type = Objects.requireNonNull(type, "type");
    this.modCounter = modCounter;
    this.alias = alias;
  }

  public String getType() {
    return type;
  }

  public int getModCounter() {
    return modCounter;
  }

  @Nullable
  public String getAlias() {
    return alias;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IceItem)) return false;
    if (!super.equals(o)) return false;

    IceItem iceItem = (IceItem) o;

    return type.equals(iceItem.type) && (modCounter == iceItem.modCounter) && alias.equals(iceItem.alias);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + modCounter;
    result = 31 * result + alias.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "IceItem{" +
        "id=" + getId() +
        ", type='" + type + '\'' +
        ", modCounter=" + modCounter +
        ", alias='" + alias + '\'' +
        '}';
  }

  //
  // Builder
  //

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(IceItem other) {
    return newBuilder()
        .setId(other.getId())
        .setType(other.getType())
        .setModCounter(other.getModCounter())
        .setAlias(other.getAlias());
  }

  public static final class Builder extends ModelWithId.Builder<Builder> {
    private String type;
    private int modCounter;
    private String alias;

    private Builder() {}

    public IceItem build() {
      return new IceItem(id, type, modCounter, alias);
    }

    public Builder setType(@Nullable String value) {
      this.type = value;
      return this;
    }

    public Builder setModCounter(int value) {
      this.modCounter = value;
      return this;
    }

    public Builder setAlias(@Nullable String value) {
      this.alias = value;
      return this;
    }

    @Override
    protected Builder getSelf() {
      return this;
    }
  }
}
