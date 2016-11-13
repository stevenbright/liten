package liten.catalog.dao.model;

import liten.util.CheckedCollections;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;

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
    this.languageAliases = CheckedCollections.copySet(languageAliases, "languageAliases");
    if (useLanguageFilter && this.languageAliases.isEmpty()) {
      throw new IllegalArgumentException("Language filter is set to true, but no language aliases specified");
    }
    this.useLanguageFilter = useLanguageFilter;
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

      // constant condition inspection is suppressed here to avoid bugs when this filter will be exteneded in future
      //noinspection ConstantConditions
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
