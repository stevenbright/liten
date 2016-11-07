package liten.catalog.dao.model;

import liten.dao.model.BaseModel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class IceRelation extends BaseModel {
  private final String type;
  private final long relatedItemId;

  public IceRelation(String type, long relatedItemId) {
    this.type = Objects.requireNonNull(type, "type");
    this.relatedItemId = relatedItemId;
  }

  public String getType() {
    return type;
  }

  public long getRelatedItemId() {
    return relatedItemId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IceRelation)) return false;

    IceRelation that = (IceRelation) o;

    return relatedItemId == that.relatedItemId && type.equals(that.type);

  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + (int) (relatedItemId ^ (relatedItemId >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "IceRelation{" +
        "type='" + type + '\'' +
        ", relatedItemId=" + relatedItemId +
        '}';
  }
}
