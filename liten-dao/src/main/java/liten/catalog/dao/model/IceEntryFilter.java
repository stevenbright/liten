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
  public static final IceEntryFilter NONE = new IceEntryFilter(false, emptySet(), false);

  public static IceEntryFilter forLanguages(boolean includeInstances, String... languageAliases) {
    final IceEntryFilter.Builder builder = IceEntryFilter.newBuilder().setIncludeInstances(includeInstances);
    for (final String alias : languageAliases) {
      builder.addLanguageAlias(alias);
    }
    return builder.build();
  }

  private final boolean useLanguageFilter;
  private final Set<String> languageAliases;
  private final boolean includeInstances;

  private IceEntryFilter(boolean useLanguageFilter,
                         Collection<String> languageAliases,
                         boolean includeInstances) {
    this.languageAliases = CheckedCollections.copySet(languageAliases, "languageAliases");
    if (useLanguageFilter && this.languageAliases.isEmpty()) {
      throw new IllegalArgumentException("Language filter is set to true, but no language aliases specified");
    }
    this.useLanguageFilter = useLanguageFilter;
    this.includeInstances = includeInstances;
  }

  public boolean isIncludeInstances() {
    return includeInstances;
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

  public static Builder newBuilder(IceEntryFilter origin) {
    return newBuilder()
        .setUseLanguageFilter(origin.isUseLanguageFilter())
        .setIncludeInstances(origin.isIncludeInstances())
        .addLanguageAliases(origin.getLanguageAliases());
  }

  public static final class Builder {
    private boolean useLanguageFilter = false;
    private Set<String> languageAliases = new HashSet<>();
    private boolean includeInstances = false;

    private Builder() {}

    public IceEntryFilter build() {
      return new IceEntryFilter(useLanguageFilter, languageAliases, includeInstances);
    }

    public Builder setIncludeInstances(boolean value) {
      this.includeInstances = value;
      return this;
    }

    public Builder setUseLanguageFilter(boolean value) {
      this.useLanguageFilter = value;
      return this;
    }

    public Builder addLanguageAlias(String value) {
      this.languageAliases.add(value);
      return setUseLanguageFilter(true);
    }

    public Builder addLanguageAliases(Iterable<String> values) {
      for (final String value : values) {
        addLanguageAlias(value);
      }
      return this;
    }
  }
}
