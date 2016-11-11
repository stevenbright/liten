package liten.catalog.dao.model;

import liten.dao.model.ModelWithId;

/**
 * @author Alexander Shabanov
 */
public final class IceSku extends ModelWithId {
  private final String title;
  private final IceItem language;
  private final Long languageId; // shall be set when SKU is updated

  public IceSku(long id, String title, IceItem language, Long languageId) {
    super(id);
    this.title = title;
    this.language = language;
    this.languageId = languageId;
  }

  public String getTitle() {
    return title;
  }

  public IceItem getLanguage() {
    return language;
  }

  public Long getLanguageId() {
    return languageId;
  }
}
