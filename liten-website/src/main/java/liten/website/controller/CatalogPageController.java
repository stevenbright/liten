package liten.website.controller;

import com.truward.orion.user.service.spring.SecurityControllerMixin;
import liten.dao.model.ModelWithId;
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
import javax.annotation.Resource;
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

  @Resource
  private DefaultCatalogService catalogService;

  @RequestMapping("/part/entries")
  public ModelAndView entriesPart(
      @RequestParam(value = "startItemId", required = false) @Nullable Long startItemId,
      @RequestParam(value = "limit", required = false) @Nullable Integer limit) {
    final Map<String, Object> params = catalogService.getPaginationHelper(getUserLanguage())
        .newModelWithItemsOpt(startItemId, limit);

    return new ModelAndView("part/catalog/entries", params);
  }

  @RequestMapping("/index")
  public ModelAndView index(@RequestParam(value = "limit", required = false) @Nullable Integer limit) {
    final Map<String, Object> params = catalogService.getPaginationHelper(getUserLanguage())
        .newModelWithItems(ModelWithId.INVALID_ID, limit != null ? limit : PaginationHelper.DEFAULT_LIMIT);

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
}
