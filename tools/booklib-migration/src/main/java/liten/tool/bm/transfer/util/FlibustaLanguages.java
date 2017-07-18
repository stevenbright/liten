package liten.tool.bm.transfer.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import liten.catalog.model.Ise;

import java.util.List;
import java.util.Map;

/**
 * Flibusta languages.
 */
public final class FlibustaLanguages {
  private FlibustaLanguages() {}



  public static final class LangAlias {
    public final String alias;
    public final List<Ise.Sku> skus;

    LangAlias(String alias, List<Ise.Sku> skus) {
      this.alias = alias;
      this.skus = ImmutableList.copyOf(skus);
    }
  }


  private static final LangAlias EN_LANG_ALIAS = new LangAlias("en", ImmutableList.of(Ise.Sku.newBuilder()
      .setId("1")
      .setTitle("English")
      .setLanguage("en")
      .build(), Ise.Sku.newBuilder()
      .setId("2")
      .setTitle("Английский")
      .setLanguage("ru")
      .build()));

  private static final LangAlias RU_LANG_ALIAS = new LangAlias("ru", ImmutableList.of(Ise.Sku.newBuilder()
      .setId("1")
      .setTitle("Russian")
      .setLanguage("en")
      .build(), Ise.Sku.newBuilder()
      .setId("2")
      .setTitle("Русский")
      .setLanguage("ru")
      .build()));

  public static final Map<String, LangAlias> FLIBUSTA_LANG_NAME_TO_ALIAS = ImmutableMap.<String, LangAlias>builder()
      .put("en", EN_LANG_ALIAS)
      .put("ru", RU_LANG_ALIAS)
      .build();
}
