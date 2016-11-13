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
  private final long languageId;

  private IceSku(long id, String title, long languageId) {
    super(id);
    this.title = requireNonNull(title, "title");
    this.languageId = ModelWithId.requireValidId(languageId, "languageId");
  }

  public String getTitle() {
    return title;
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
        .setTitle(other.getTitle());
  }

  public static final class Builder extends ModelWithId.Builder<Builder> {
    private String title;
    private long languageId = INVALID_ID;

    private Builder() {}

    public IceSku build() {
      return new IceSku(id, title, languageId);
    }

    public Builder setTitle(@Nullable String value) {
      this.title = value;
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
