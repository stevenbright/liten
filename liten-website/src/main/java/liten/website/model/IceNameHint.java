package liten.website.model;

/**
 * Model for catalog/page/hints list
 *
 * @author Alexander Shabanov
 */
public final class IceNameHint {
  private final String prefix;
  private final String moreHintsUrl;
  private final String itemsFilterUrl;

  public IceNameHint(String prefix, String moreHintsUrl, String itemsFilterUrl) {
    this.prefix = prefix;
    this.moreHintsUrl = moreHintsUrl;
    this.itemsFilterUrl = itemsFilterUrl;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getMoreHintsUrl() {
    return moreHintsUrl;
  }

  public String getItemsFilterUrl() {
    return itemsFilterUrl;
  }
}
