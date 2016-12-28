package liten.catalog.dao.model;

import liten.dao.model.ModelWithId;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents query that returns Item+SkuEntry+Instance combinations.
 */
@ParametersAreNonnullByDefault
public final class IceEntryQuery {
  public static final int DEFAULT_LIMIT = 10;

  public static final IceEntryQuery NONE = new IceEntryQuery(null, ModelWithId.INVALID_ID, DEFAULT_LIMIT);

  private final String type;
  private final long startItemId;
  private final int limit;

  private IceEntryQuery(@Nullable String type, long startItemId, int limit) {
    this.type = type;
    this.startItemId = startItemId;
    this.limit = limit;
  }

  public String getType() {
    return type;
  }

  public long getStartItemId() {
    return startItemId;
  }

  public int getLimit() {
    return limit;
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

  public static final class Builder {
    private String type;
    private long startItemId;
    private int limit;

    private Builder() {}

    public IceEntryQuery build() {
      return new IceEntryQuery(type, startItemId, limit);
    }

    public Builder setType(String value) {
      this.type = value;
      return this;
    }

    public Builder setStartItemId(long value) {
      this.startItemId = value;
      return this;
    }

    public Builder setLimit(int value) {
      this.limit = value;
      return this;
    }
  }
}
