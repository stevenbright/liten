package liten.catalog.dao.model;

import liten.dao.model.ModelWithId;
import liten.dao.model.PageQuery;
import liten.util.CheckedCollections;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Filter for ICE item relations
 */
public final class IceRelationQuery extends PageQuery {
  public enum Direction {
    LEFT,
    RIGHT
  }

  public static final int DEFAULT_LIMIT = 10;

  private final Direction direction;
  private final Set<String> relationTypes;
  private final long relatedItemId;

  private IceRelationQuery(Direction direction,
                           Set<String> relationTypes,
                           long relatedItemId,
                           long startItemId,
                           int limit) {
    super(startItemId, limit);
    this.direction = requireNonNull(direction, "direction");
    this.relationTypes = CheckedCollections.copySet(relationTypes, "relationTypes");
    this.relatedItemId = ModelWithId.requireValidId(relatedItemId, "relatedItemId");
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

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder extends PageQuery.Builder<Builder> {
    private Direction direction = Direction.LEFT;
    private Set<String> relationTypes = new HashSet<>();
    private long relatedItemId = ModelWithId.INVALID_ID;

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

    @Override
    protected Builder getSelf() {
      return this;
    }
  }
}
