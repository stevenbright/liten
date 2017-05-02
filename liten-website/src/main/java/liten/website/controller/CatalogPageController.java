package liten.website.controller;

import liten.website.exception.ResourceNotFoundException;
import liten.website.model.deprecated.IseItemAdapter;
import com.truward.web.pagination.PaginationHelper;
import com.truward.web.pagination.PaginationUrlCreator;
import liten.website.service.CatalogService;
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
      @RequestParam(value = "cursor", required = false) @Nullable String cursor,
      @RequestParam(value = "limit", required = false) @Nullable Integer limit,
      @RequestParam(value = "type", required = false) @Nullable String type,
      @RequestParam(value = "namePrefix", required = false) @Nullable String namePrefix) {
    // normalize string parameters
    final Map<String, Object> params = catalogService.getPaginationHelper(getUserLanguage(), type, namePrefix)
        .newModelWithItemsOpt(cursor, limit, getCatalogEntriesUrlCreator(type, namePrefix));

    return new ModelAndView("part/catalog/entries", params);
  }

  private PaginationUrlCreator getCatalogEntriesUrlCreator(@Nullable String type, @Nullable String namePrefix) {
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

  @RequestMapping("/part/{itemId}/right")
  public ModelAndView rightRelationsPart(
      @PathVariable("itemId") String itemId,
      @RequestParam(value = "cursor", required = false) @Nullable String cursor,
      @RequestParam(value = "limit", required = false) @Nullable Integer limit
  ) {
    final Map<String, Object> params = catalogService.getRightRelationEntries(itemId,
        getUserLanguage()).newModelWithItemsOpt(cursor, limit, getRightRelationsUrlCreator(itemId));

    return new ModelAndView("part/catalog/entries", params);
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

  @RequestMapping("/part/{itemId}/right/container")
  public ModelAndView rightRelationsPartContainer(@PathVariable("itemId") String itemId) {
    final Map<String, Object> params = catalogService.getRightRelationEntries(itemId,
        getUserLanguage()).newModelWithItemsOpt(
          "",
          PaginationHelper.DEFAULT_LIMIT,
          getRightRelationsUrlCreator(itemId));

    return new ModelAndView("part/catalog/rightEntriesContainer", params);
  }

  @RequestMapping("/index")
  public ModelAndView index(
      @RequestParam(value = "limit", required = false) @Nullable Integer limit,
      @RequestParam(value = "type", required = false) @Nullable String type,
      @RequestParam(value = "namePrefix", required = false) @Nullable String namePrefix
  ) throws IOException {
    // normalize string parameters
    final String displayTitleForItemType = getDisplayTitleForItemType(type);

    final Map<String, Object> params = catalogService.getPaginationHelper(getUserLanguage(), type, namePrefix)
        .newModelWithItems(
            "",
            limit != null ? limit : PaginationHelper.DEFAULT_LIMIT,
            getCatalogEntriesUrlCreator(type, namePrefix));
    params.put("displayItemTypeTitle", displayTitleForItemType);

    return new ModelAndView("page/catalog/index", params);
  }

  @RequestMapping("/hints")
  public String hints(
      @RequestParam(value = "type", required = false) @Nullable String type,
      @RequestParam(value = "namePrefix", required = false) @Nullable String namePrefix,
      Model model) throws IOException {
    // normalize string parameters
    if (namePrefix != null && namePrefix.length() >= 3) {
      return getIndexRedirectDirective(namePrefix, type);
    }

    final String displayTitleForItemType = getDisplayTitleForItemType(type);

    final List<String> namePrefixList = catalogService.getSkuNameHints(type, namePrefix);
    model.addAttribute("type", type != null ? type : "");
    model.addAttribute("displayItemTypeTitle", displayTitleForItemType);
    model.addAttribute("namePrefixList", namePrefixList);

    return "page/catalog/hints";
  }

  @RequestMapping("/item/{id:.+}")
  public ModelAndView detailPage(@PathVariable("id") String id) {
    final IseItemAdapter entry = catalogService.getDetailedEntry(id, getUserLanguage());
    log.trace("entry={}", entry);

    final Map<String, Object> params = new HashMap<>();
    params.put("currentTime", System.currentTimeMillis());
    params.put("entry", entry);
    params.put("nextRightRelationEntriesUrl", "/g/cat/part/" + entry.getId() + "/right/container");

    return new ModelAndView("page/catalog/item", params);
  }

  //
  // Private
  //

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

  private String getDisplayTitleForItemType(@Nullable String type) {
    // TODO: return localized string!

    if (type == null) {
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
