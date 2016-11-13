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
  private final String alias;

  private IceItem(long id, String type, String alias) {
    super(id);
    this.type = Objects.requireNonNull(type, "type");
    this.alias = alias;
  }

  public String getType() {
    return type;
  }

  @Nullable
  public String getAlias() {
    return alias;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(IceItem other) {
    return newBuilder()
        .setId(other.getId())
        .setType(other.getType())
        .setAlias(other.getAlias());
  }

  public static final class Builder extends ModelWithId.Builder<Builder> {
    private String type;
    private String alias;

    private Builder() {}

    public IceItem build() {
      return new IceItem(id, type, alias);
    }

    public Builder setType(@Nullable String value) {
      this.type = value;
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
