package liten.website.model;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
@SuppressWarnings({"unused", "WeakerAccess"}) // referred from freemarker templates
public final class IseItemRef {
  public final String id;
  public final String displayTitle;

  public IseItemRef(String id, String displayTitle) {
    this.id = id;
    this.displayTitle = displayTitle;
  }

  public String getId() {
    return id;
  }

  public String getDisplayTitle() {
    return displayTitle;
  }
}
