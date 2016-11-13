package liten.catalog.dao.model;

import liten.dao.model.ModelWithId;
import liten.util.CheckedCollections;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Filter for ICE item relations
 */
public final class IceRelationQuery {
  public enum Direction {
    LEFT,
    RIGHT
  }

  public static final int DEFAULT_LIMIT = 10;

  private final Direction direction;
  private final Set<String> relationTypes;
  private final long relatedItemId;
  private final long startItemId;
  private final int limit;

  private IceRelationQuery(Direction direction,
                           Set<String> relationTypes,
                           long relatedItemId,
                           long startItemId,
                           int limit) {
    this.direction = requireNonNull(direction, "direction");
    this.relationTypes = CheckedCollections.copySet(relationTypes, "relationTypes");
    this.relatedItemId = ModelWithId.requireValidId(relatedItemId, "relatedItemId");
    this.startItemId = startItemId;
    this.limit = limit;
  }

  public Direction getDirection() {
    return direction;
  }

  public Set<String> getRelationTypes() {
    return relationTypes;
  }

  public long getRelatedItemId() {
    return relatedItemId;
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

  public static final class Builder {
    private Direction direction = Direction.LEFT;
    private Set<String> relationTypes = new HashSet<>();
    private long relatedItemId = ModelWithId.INVALID_ID;
    private long startItemId = ModelWithId.INVALID_ID;
    private int limit = DEFAULT_LIMIT;

    private Builder() {}

    public IceRelationQuery build() {
      return new IceRelationQuery(direction, relationTypes, relatedItemId, startItemId, limit);
    }

    public Builder setDirection(Direction value) {
      this.direction = value;
      return this;
    }

    public Builder addRelationType(String relationType) {
      this.relationTypes.add(relationType);
      return this;
    }

    public Builder setRelatedItemId(long value) {
      this.relatedItemId = value;
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
