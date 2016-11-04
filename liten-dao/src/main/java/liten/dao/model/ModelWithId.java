package liten.dao.model;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class ModelWithId<TValue> extends BaseModel {
  public static final long INVALID_ID = 0;

  private final long id;
  private final TValue value;

  private ModelWithId(long id, TValue value) {
    this.id = id;
    this.value = Objects.requireNonNull(value, "value");
  }

  public static <T> ModelWithId<T> from(long id, T value) {
    return new ModelWithId<T>(id, value);
  }

  public static <T> ModelWithId<T> from(T value) {
    return from(INVALID_ID, value);
  }

  public long getId() {
    return id;
  }

  public boolean isValidId() {
    return isValidId(id);
  }

  public TValue getValue() {
    return value;
  }

  public static boolean isValidId(long id) {
    return id > 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ModelWithId)) return false;

    ModelWithId<?> that = (ModelWithId<?>) o;

    if (getId() != that.getId()) return false;
    return getValue().equals(that.getValue());

  }

  @Override
  public int hashCode() {
    int result = (int) (getId() ^ (getId() >>> 32));
    result = 31 * result + getValue().hashCode();
    return result;
  }
}
