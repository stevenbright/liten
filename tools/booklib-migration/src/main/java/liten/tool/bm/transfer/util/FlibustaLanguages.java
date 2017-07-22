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

  public static final LangAlias UNKNOWN_LANG_ALIAS = new LangAlias("?", ImmutableList.of(Ise.Sku.newBuilder()
      .setId("1")
      .setTitle("Unknown")
      .setLanguage("en")
      .build(), Ise.Sku.newBuilder()
      .setId("2")
      .setTitle("неизвестный")
      .setLanguage("ru")
      .build(), Ise.Sku.newBuilder()
      .setId("3")
      .setTitle("desconocida")
      .setLanguage("es")
      .build()));

  private static final LangAlias EN_LANG_ALIAS = new LangAlias("en", ImmutableList.of(Ise.Sku.newBuilder()
      .setId("1")
      .setTitle("English")
      .setLanguage("en")
      .build(), Ise.Sku.newBuilder()
      .setId("2")
      .setTitle("aнглийский")
      .setLanguage("ru")
      .build(), Ise.Sku.newBuilder()
      .setId("3")
      .setTitle("ingles")
      .setLanguage("es")
      .build()));

  private static final LangAlias RU_LANG_ALIAS = new LangAlias("ru", ImmutableList.of(Ise.Sku.newBuilder()
      .setId("1")
      .setTitle("Russian")
      .setLanguage("en")
      .build(), Ise.Sku.newBuilder()
      .setId("2")
      .setTitle("русский")
      .setLanguage("ru")
      .build(), Ise.Sku.newBuilder()
      .setId("3")
      .setTitle("ruso")
      .setLanguage("es")
      .build()));

  private static final LangAlias ES_LANG_ALIAS = new LangAlias("es", ImmutableList.of(Ise.Sku.newBuilder()
      .setId("1")
      .setTitle("Spanish")
      .setLanguage("en")
      .build(), Ise.Sku.newBuilder()
      .setId("2")
      .setTitle("испанский")
      .setLanguage("ru")
      .build(), Ise.Sku.newBuilder()
      .setId("3")
      .setTitle("español")
      .setLanguage("es")
      .build()));


  /**
   * Flibusta language code to alias mapping.
   */
  public static final Map<String, LangAlias> FLIBUSTA_CODE_TO_ALIAS = ImmutableMap.<String, LangAlias>builder()
      .put("en", EN_LANG_ALIAS)
      .put("ru", RU_LANG_ALIAS)
      .put("es", ES_LANG_ALIAS)
      .put("?", UNKNOWN_LANG_ALIAS)
      .build();
}
