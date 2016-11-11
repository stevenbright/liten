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
  private final String defaultTitle;

  private IceItem(long id, String type, String defaultTitle) {
    super(id);
    this.type = Objects.requireNonNull(type, "type");
    this.defaultTitle = Objects.requireNonNull(defaultTitle, "defaultTitle");
  }

  public String getType() {
    return type;
  }

  public String getDefaultTitle() {
    return defaultTitle;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(IceItem other) {
    return newBuilder()
        .setId(other.getId())
        .setType(other.getType())
        .setDefaultTitle(other.getDefaultTitle());
  }

  public static final class Builder extends ModelWithId.Builder<Builder> {
    private String type;
    private String defaultTitle = "";

    private Builder() {}

    public IceItem build() {
      return new IceItem(id, type, defaultTitle);
    }

    public Builder setType(@Nullable String value) {
      this.type = value;
      return this;
    }

    public Builder setDefaultTitle(@Nullable String value) {
      this.defaultTitle = value;
      return this;
    }

    @Override
    protected Builder getSelf() {
      return this;
    }
  }
}
