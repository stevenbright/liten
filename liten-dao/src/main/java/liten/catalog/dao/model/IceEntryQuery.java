package liten.catalog.dao.model;

import liten.dao.model.ModelWithId;
import liten.dao.model.PageQuery;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents query that returns Item+SkuEntry+Instance combinations.
 */
@ParametersAreNonnullByDefault
public final class IceEntryQuery extends PageQuery {

  public static final IceEntryQuery NONE = IceEntryQuery.newBuilder().build();

  private final String type;
  private final String namePrefix;

  private IceEntryQuery(@Nullable String type,
                        @Nullable String namePrefix,
                        long startItemId, int limit) {
    super(startItemId, limit);
    this.type = type;
    this.namePrefix = namePrefix;
  }

  public String getType() {
    return type;
  }

  public String getNamePrefix() {
    return namePrefix;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(IceEntryQuery origin) {
    return newBuilder()
        .setType(origin.getType())
        .setNamePrefix(origin.getNamePrefix())
        .setStartItemId(origin.getStartItemId())
        .setLimit(origin.getLimit());
  }

  public static final class Builder extends PageQuery.Builder<Builder> {
    private String type;
    private String namePrefix;

    private Builder() {}

    public IceEntryQuery build() {
      return new IceEntryQuery(type, namePrefix, startItemId, limit);
    }

    public Builder setType(@Nullable String value) {
      this.type = value;
      return this;
    }

    public Builder setNamePrefix(@Nullable String value) {
      this.namePrefix = value;
      return this;
    }

    @Override
    protected Builder getSelf() {
      return this;
    }
  }
}
