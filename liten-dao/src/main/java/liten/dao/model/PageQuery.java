package liten.dao.model;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Base class for pagination queries
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public abstract class PageQuery {
  public static final int DEFAULT_LIMIT = 10;

  private final long startItemId;
  private final int limit;

  protected PageQuery(long startItemId, int limit) {
    if (limit < 0) {
      throw new IllegalArgumentException("limit");
    }

    this.startItemId = startItemId;
    this.limit = limit;
  }

  public long getStartItemId() {
    return startItemId;
  }

  public int getLimit() {
    return limit;
  }

  public static abstract class Builder<TSelf> {
    protected long startItemId = ModelWithId.INVALID_ID;
    protected int limit = DEFAULT_LIMIT;

    protected Builder() {}

    public TSelf setStartItemId(long value) {
      this.startItemId = value;
      return getSelf();
    }

    public TSelf setLimit(int value) {
      this.limit = value;
      return getSelf();
    }

    protected abstract TSelf getSelf();
  }
}
