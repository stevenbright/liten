package liten.catalog.dao.model;

import liten.dao.model.ModelWithId;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class IceSku extends ModelWithId {
  private final String title;
  private final IceItem language;
  private final long languageId; // shall be set when SKU is updated

  private IceSku(long id, String title, @Nullable IceItem language, long languageId) {
    super(id);
    if (isValidId(languageId)) {
      if (language != null && languageId != language.getId()) {
        throw new IllegalArgumentException("Language ID mismatch");
      }
    } else {
      if (language == null) {
        throw new IllegalArgumentException("Both language and languageId are missing");
      } else {
        // languageId is invalid, but language is - so set languageId
        languageId = language.getId();
      }
    }

    if (!isValidId(languageId)) {
      throw new IllegalArgumentException("Invalid language ID");
    }

    this.title = requireNonNull(title, "title");
    this.language = language;
    this.languageId = languageId;
  }

  public String getTitle() {
    return title;
  }

  @Nullable
  public IceItem getLanguage() {
    return language;
  }

  public long getLanguageId() {
    return languageId;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(IceSku other) {
    return newBuilder()
        .setId(other.getId())
        .setLanguageId(other.getLanguageId())
        .setLanguage(other.getLanguage())
        .setTitle(other.getTitle());
  }

  public static final class Builder extends ModelWithId.Builder<Builder> {
    private String title;
    private IceItem language;
    private long languageId = INVALID_ID;

    private Builder() {}

    public IceSku build() {
      return new IceSku(id, title, language, languageId);
    }

    public Builder setTitle(@Nullable String value) {
      this.title = value;
      return this;
    }

    public Builder setLanguage(@Nullable IceItem value) {
      this.language = value;
      return this;
    }

    public Builder setLanguageId(long value) {
      this.languageId = value;
      return this;
    }

    @Override
    protected Builder getSelf() {
      return this;
    }
  }
}
