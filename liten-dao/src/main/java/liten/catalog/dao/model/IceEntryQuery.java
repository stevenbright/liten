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

  public static final IceEntryQuery NONE = new IceEntryQuery(null, ModelWithId.INVALID_ID, DEFAULT_LIMIT);

  private final String type;

  private IceEntryQuery(@Nullable String type, long startItemId, int limit) {
    super(startItemId, limit);
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(IceEntryQuery origin) {
    return newBuilder()
        .setType(origin.getType())
        .setStartItemId(origin.getStartItemId())
        .setLimit(origin.getLimit());
  }

  public static final class Builder extends PageQuery.Builder<Builder> {
    private String type;

    private Builder() {}

    public IceEntryQuery build() {
      return new IceEntryQuery(type, startItemId, limit);
    }

    public Builder setType(String value) {
      this.type = value;
      return this;
    }

    @Override
    protected Builder getSelf() {
      return this;
    }
  }
}
