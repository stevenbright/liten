package liten.website.controller;

import com.truward.orion.user.service.spring.SecurityControllerMixin;
import liten.dao.model.ModelWithId;
import liten.website.exception.ResourceNotFoundException;
import liten.website.model.IceEntryAdapter;
import liten.website.model.PaginationHelper;
import liten.website.service.DefaultCatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
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
    final Map<String, Object> params = catalogService.getPaginationHelper(getUserLanguage(), type, namePrefix)
        .newModelWithItemsOpt(startItemId, limit);

    return new ModelAndView("part/catalog/entries", params);
  }

  @RequestMapping("/index")
  public ModelAndView index(@RequestParam(value = "limit", required = false) @Nullable Integer limit,
                            @RequestParam(value = "type", required = false) @Nullable String type,
                            @RequestParam(value = "namePrefix", required = false) @Nullable String namePrefix) {
    final String displayTitleForItemType = getDisplayTitleForItemType(type);

    final Map<String, Object> params = catalogService.getPaginationHelper(getUserLanguage(), type, namePrefix)
        .newModelWithItems(ModelWithId.INVALID_ID, limit != null ? limit : PaginationHelper.DEFAULT_LIMIT);
    params.put("itemType", displayTitleForItemType);

    return new ModelAndView("page/catalog/index", params);
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
