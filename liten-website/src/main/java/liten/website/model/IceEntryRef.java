package liten.website.model;

/**
 * @author Alexander Shabanov
 */
@SuppressWarnings("unused") // referred from freemarker templates
public final class IceEntryRef {
  public final long id;
  public final String displayTitle;

  public IceEntryRef(long id, String displayTitle) {
    this.id = id;
    this.displayTitle = displayTitle;
  }

  public long getId() {
    return id;
  }

  public String getDisplayTitle() {
    return displayTitle;
  }
}
