package liten.catalog.dao.model;

import com.truward.time.UtcTime;
import liten.dao.model.BaseModel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class IceItem extends BaseModel {
  private final String type;
  private final UtcTime created;
  private final UtcTime updated;
  private final String defaultTitle;

  public IceItem(String type, UtcTime created, UtcTime updated, String defaultTitle) {
    this.type = Objects.requireNonNull(type);
    this.created = Objects.requireNonNull(created);
    this.updated = Objects.requireNonNull(updated);
    this.defaultTitle = Objects.requireNonNull(defaultTitle);
  }

  public String getType() {
    return type;
  }

  public UtcTime getCreated() {
    return created;
  }

  public UtcTime getUpdated() {
    return updated;
  }

  public String getDefaultTitle() {
    return defaultTitle;
  }

  @SuppressWarnings("SimplifiableIfStatement")
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IceItem)) return false;
    if (!super.equals(o)) return false;

    IceItem iceItem = (IceItem) o;

    if (!getType().equals(iceItem.getType())) return false;
    if (!getCreated().equals(iceItem.getCreated())) return false;
    if (!getUpdated().equals(iceItem.getUpdated())) return false;
    return getDefaultTitle().equals(iceItem.getDefaultTitle());

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + getType().hashCode();
    result = 31 * result + getCreated().hashCode();
    result = 31 * result + getUpdated().hashCode();
    result = 31 * result + getDefaultTitle().hashCode();
    return result;
  }
}
