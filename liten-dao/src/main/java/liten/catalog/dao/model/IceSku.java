package liten.catalog.dao.model;

import liten.dao.model.BaseModel;

/**
 * @author Alexander Shabanov
 */
public final class IceSku extends BaseModel {
  private String title;
  private IceItem language;
  private Long languageId; // shall be set when SKU is updated

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public IceItem getLanguage() {
    return language;
  }

  public void setLanguage(IceItem language) {
    this.language = language;
  }

  public Long getLanguageId() {
    return languageId;
  }

  public void setLanguageId(Long languageId) {
    this.languageId = languageId;
  }
}
