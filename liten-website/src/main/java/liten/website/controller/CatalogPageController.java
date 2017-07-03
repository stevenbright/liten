package liten.website.controller;

import com.truward.web.pagination.PageResult;
import com.truward.web.pagination.PaginationUrlCreator;
import liten.website.exception.ResourceNotFoundException;
import liten.website.model.catalog.CatalogItem;
import liten.website.service.catalog.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/g/cat")
@ParametersAreNonnullByDefault
public final class CatalogPageController extends BaseHtmlController {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final CatalogService catalogService;

  public CatalogPageController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @RequestMapping("/part/entries")
  public ModelAndView entriesPart(
      @RequestParam(value = "cursor", defaultValue = PageResult.DEFAULT_CURSOR) String cursor,
      @RequestParam(value = "limit", defaultValue = PageResult.DEFAULT_LIMIT_STR) int limit,
      @RequestParam(value = "type", defaultValue = "") String type,
      @RequestParam(value = "namePrefix", defaultValue = "") String namePrefix) {
    // normalize string parameters
    final Map<String, Object> params = catalogService.getItems(getUserLanguageCode(), type, namePrefix)
        .getPage(cursor, limit, getCatalogEntriesUrlCreator(type, namePrefix))
        .toModelMap();

    return new ModelAndView("part/catalog/entries", params);
  }

  @RequestMapping("/part/{itemId}/right")
  public ModelAndView rightRelationsPart(
      @PathVariable("itemId") String itemId,
      @RequestParam(value = "cursor", defaultValue = PageResult.DEFAULT_CURSOR) String cursor,
      @RequestParam(value = "limit", defaultValue = PageResult.DEFAULT_LIMIT_STR) int limit
  ) {
    final Map<String, Object> params = catalogService.getRightRelationEntries(itemId,
        getUserLanguageCode()).getPage(cursor, limit, getRightRelationsUrlCreator(itemId)).toModelMap();

    return new ModelAndView("part/catalog/entries", params);
  }

  @RequestMapping("/part/{itemId}/right/container")
  public ModelAndView rightRelationsPartContainer(@PathVariable("itemId") String itemId) {
    final Map<String, Object> params = catalogService.getRightRelationEntries(itemId, getUserLanguageCode())
        .getPage(PageResult.DEFAULT_CURSOR, PageResult.DEFAULT_LIMIT, getRightRelationsUrlCreator(itemId))
        .toModelMap();

    return new ModelAndView("part/catalog/rightEntriesContainer", params);
  }

  @RequestMapping("/index")
  public ModelAndView index(
      @RequestParam(value = "limit", defaultValue = PageResult.DEFAULT_LIMIT_STR) int limit,
      @RequestParam(value = "type", defaultValue = "") String type,
      @RequestParam(value = "namePrefix", defaultValue = "") String namePrefix
  ) throws IOException {
    // normalize string parameters
    final String displayTitleForItemType = getDisplayTitleForItemType(type);

    final Map<String, Object> params = catalogService.getItems(getUserLanguageCode(), type, namePrefix)
        .getPage(
            PageResult.DEFAULT_CURSOR,
            limit,
            getCatalogEntriesUrlCreator(type, namePrefix)).toModelMap();
    params.put("displayItemTypeTitle", displayTitleForItemType);

    return new ModelAndView("page/catalog/index", params);
  }

  @RequestMapping("/hints")
  public String hints(
      @RequestParam(value = "type", defaultValue = "") String type,
      @RequestParam(value = "namePrefix", defaultValue = "") String namePrefix,
      Model model) throws IOException {
    // normalize string parameters
    if (namePrefix.length() >= 3) {
      return getIndexRedirectDirective(namePrefix, type);
    }

    final String displayTitleForItemType = getDisplayTitleForItemType(type);

    final List<String> namePrefixList = catalogService.getSkuNameHints(type, namePrefix);
    model.addAttribute("type", type);
    model.addAttribute("displayItemTypeTitle", displayTitleForItemType);
    model.addAttribute("namePrefixList", namePrefixList);

    return "page/catalog/hints";
  }

  @RequestMapping("/item/{itemId}")
  public ModelAndView detailPage(@PathVariable("itemId") String itemId) {
    return getDetailPage(itemId, null);
  }

  @RequestMapping("/item/{itemId}/{skuId}")
  public ModelAndView detailPage(
      @PathVariable("itemId") String itemId,
      @PathVariable("skuId") String skuId) {
    return getDetailPage(itemId, skuId);
  }

  //
  // Private
  //

  private PaginationUrlCreator getCatalogEntriesUrlCreator(String type, String namePrefix) {
    return (cursor, limit) -> {
      final String charset = StandardCharsets.UTF_8.name();
      try {
        final StringBuilder builder = new StringBuilder(100);
        builder.append("/g/cat/part/entries?cursor=").append(URLEncoder.encode(cursor, charset));
        builder.append("&limit=").append(limit);
        if (StringUtils.hasLength(type)) {
          builder.append("&type=").append(URLEncoder.encode(type, charset));
        }
        if (StringUtils.hasLength(namePrefix)) {
          builder.append("&namePrefix=").append(URLEncoder.encode(namePrefix, charset));
        }

        return builder.toString();
      } catch (UnsupportedEncodingException e) {
        throw new IllegalStateException(e); // should not happen
      }
    };
  }

  private PaginationUrlCreator getRightRelationsUrlCreator(String itemId) {
    return ((cursor, limit) -> {
      //noinspection StringBufferReplaceableByString
      final StringBuilder builder = new StringBuilder(100);
      builder.append("/g/cat/part/").append(itemId);
      builder.append("/right?cursor=").append(cursor);
      builder.append("&limit=").append(limit);
      return builder.toString();
    });
  }

  private ModelAndView getDetailPage(String itemId, @Nullable String skuId) {
    final String userLanguageCode = getUserLanguageCode();

    final CatalogItem catalogItem = catalogService.getDetailedEntry(itemId, skuId, userLanguageCode);
    log.trace("catalogItem={}", catalogItem);

    final Map<String, Object> params = new HashMap<>();
    params.put("currentTime", System.currentTimeMillis());
    params.put("catalogItem", catalogItem);
    params.put("nextRightRelationEntriesUrl", "/g/cat/part/" + catalogItem.getId() + "/right/container");

    return new ModelAndView("page/catalog/item", params);
  }

  private static String getIndexRedirectDirective(String namePrefix, @Nullable String type) throws IOException {
    final StringBuilder redirectBuilder = new StringBuilder(100);
    redirectBuilder.append("redirect:/g/cat/index?namePrefix=");
    redirectBuilder.append(URLEncoder.encode(namePrefix, StandardCharsets.UTF_8.name()));
    if (type != null) {
      redirectBuilder.append("&type=");
      redirectBuilder.append(URLEncoder.encode(type, StandardCharsets.UTF_8.name()));
    }
    return redirectBuilder.toString();
  }

  private String getDisplayTitleForItemType(String type) {
    // TODO: return localized string!
    if (!StringUtils.hasLength(type)) {
      return "All";
    }

    switch (type) {
      case "book":
        return "Books";
      case "genre":
        return "Genres";
      case "origin":
        return "Origins";
      case "author":
        return "Authors";
      case "persons":
        return "Persons";
      case "movie":
        return "Movie";
      case "series":
        return "Series";

      default:
        throw new ResourceNotFoundException("Unknown item type");
    }
  }
}
