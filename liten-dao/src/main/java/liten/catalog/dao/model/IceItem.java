package liten.catalog.dao.model;

import liten.dao.model.BaseModel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class IceItem extends BaseModel {
  private final String type;
  private final String defaultTitle;

  public IceItem(String type, String defaultTitle) {
    this.type = Objects.requireNonNull(type);
    this.defaultTitle = Objects.requireNonNull(defaultTitle);
  }

  public String getType() {
    return type;
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
    return getDefaultTitle().equals(iceItem.getDefaultTitle());

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + getType().hashCode();
    result = 31 * result + getDefaultTitle().hashCode();
    return result;
  }
}
