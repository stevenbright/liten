package liten.catalog.dao.model;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

/**
 * Represents query that returns Item+SkuEntry+Instance combinations.
 */
@ParametersAreNonnullByDefault
public final class IceEntryFilter {
  public static final IceEntryFilter NONE = new IceEntryFilter(false, emptySet());

  public static IceEntryFilter forLanguages(String... languageAliases) {
    final IceEntryFilter.Builder builder = IceEntryFilter.newBuilder();
    for (final String alias : languageAliases) {
      builder.addLanguageAlias(alias);
    }
    return builder.build();
  }

  private final boolean useLanguageFilter;
  private final Set<String> languageAliases;

  private IceEntryFilter(boolean useLanguageFilter, Collection<String> languageAliases) {
    if (useLanguageFilter) {
      if (languageAliases.isEmpty()) {
        throw new IllegalArgumentException("Language filter is set to true, but no language aliases specified");
      }

      this.useLanguageFilter = true;
      this.languageAliases = unmodifiableSet(new HashSet<>(languageAliases));
    } else {
      this.useLanguageFilter = false;
      this.languageAliases = emptySet();
    }

  }

  public boolean isUseLanguageFilter() {
    return useLanguageFilter;
  }

  public Set<String> getLanguageAliases() {
    if (!isUseLanguageFilter()) {
      throw new IllegalStateException("Language filter is not used");
    }
    return languageAliases;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {
    private boolean useLanguageFilter = false;
    private Set<String> languageAliases = new HashSet<>();

    private Builder() {}

    public IceEntryFilter build() {
      if (!useLanguageFilter) {
        return NONE;
      }

      return new IceEntryFilter(useLanguageFilter, languageAliases);
    }

    public Builder setUseLanguageFilter(boolean value) {
      this.useLanguageFilter = value;
      return this;
    }

    public Builder addLanguageAlias(String value) {
      this.languageAliases.add(value);
      return setUseLanguageFilter(true);
    }
  }
}
