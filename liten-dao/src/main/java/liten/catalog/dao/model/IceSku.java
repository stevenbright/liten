package liten.catalog.dao.model;

import com.truward.time.UtcTime;
import liten.dao.model.BaseModel;
import liten.dao.model.ModelWithId;

/**
 * @author Alexander Shabanov
 */
public final class IceSku extends BaseModel {
  private String type;
  private String title;
  private IceItem language;
  private Long languageId; // shall be set when SKU is updated

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

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
