package liten.website.controller;

import com.truward.orion.user.service.spring.SecurityControllerMixin;
import liten.dao.model.ModelWithId;
import liten.website.exception.ResourceNotFoundException;
import liten.website.model.IceEntryAdapter;
import liten.website.model.PaginationHelper;
import liten.website.service.DefaultCatalogService;
import liten.website.util.RequestParams;
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
public final class CatalogPageController implements SecurityControllerMixin, LocaleControllerMixin {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final DefaultCatalogService catalogService;

  public CatalogPageController(DefaultCatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @RequestMapping("/part/entries")
  public ModelAndView entriesPart(
      @RequestParam(value = "startItemId", required = false) @Nullable Long startItemId,
      @RequestParam(value = "limit", required = false) @Nullable Integer limit,
      @RequestParam(value = "type", required = false) @Nullable String type,
      @RequestParam(value = "namePrefix", required = false) @Nullable String namePrefix) {
    // normalize string parameters
    type = RequestParams.getEmptyAsNull(type);
    namePrefix = RequestParams.getEmptyAsNull(namePrefix);

    final Map<String, Object> params = catalogService.getPaginationHelper(getUserLanguage(), type, namePrefix)
        .newModelWithItemsOpt(startItemId, limit);

    return new ModelAndView("part/catalog/entries", params);
  }

  @RequestMapping("/index")
  public ModelAndView index(
      @RequestParam(value = "limit", required = false) @Nullable Integer limit,
      @RequestParam(value = "type", required = false) @Nullable String type,
      @RequestParam(value = "namePrefix", required = false) @Nullable String namePrefix
  ) throws IOException {
    // normalize string parameters
    type = RequestParams.getEmptyAsNull(type);
    namePrefix = RequestParams.getEmptyAsNull(namePrefix);

    final String displayTitleForItemType = getDisplayTitleForItemType(type);

    final Map<String, Object> params = catalogService.getPaginationHelper(getUserLanguage(), type, namePrefix)
        .newModelWithItems(ModelWithId.INVALID_ID, limit != null ? limit : PaginationHelper.DEFAULT_LIMIT);
    params.put("displayItemTypeTitle", displayTitleForItemType);

    return new ModelAndView("page/catalog/index", params);
  }

  @RequestMapping("/hints")
  public String hints(
      @RequestParam(value = "type", required = false) @Nullable String type,
      @RequestParam(value = "namePrefix", required = false) @Nullable String namePrefix,
      Model model
  ) throws IOException {
    // normalize string parameters
    type = RequestParams.getEmptyAsNull(type);
    namePrefix = RequestParams.getEmptyAsNull(namePrefix);

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

  @RequestMapping("/item/{id}")
  public ModelAndView detailPage(@PathVariable("id") long id) {
    final IceEntryAdapter entry = catalogService.getEntry(id, getUserLanguage());
    log.trace("entry={}", entry);

    final Map<String, Object> params = new HashMap<>();
    params.put("currentTime", System.currentTimeMillis());
    params.put("entry", entry);

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
