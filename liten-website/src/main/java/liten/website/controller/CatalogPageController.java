package liten.website.controller;

import com.truward.orion.user.service.spring.SecurityControllerMixin;
import liten.catalog.dao.CatalogQueryDao;
import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceEntryFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/g/cat")
public final class CatalogPageController implements SecurityControllerMixin, LocaleControllerMixin {
  private static final int MAX_LIMIT = 25;

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Resource
  private CatalogQueryDao queryDao;

  @RequestMapping("/index")
  public ModelAndView index(@RequestParam(value = "limit", defaultValue = "10") int limit,
                            @RequestParam(value = "startItemId", defaultValue = "0") long startItemId) {
    if (limit < 0 || limit > MAX_LIMIT) {
      throw new IllegalArgumentException("limit");
    }

    final List<IceEntry> entries = queryDao.getEntries(IceEntryFilter.forLanguages(getUserLanguage()),
        startItemId, limit + 1);
    log.trace("entries={}", entries);

    final Map<String, Object> params = new HashMap<>();
    params.put("currentTime", System.currentTimeMillis());
    params.put("entries", entries);

    boolean hasNext = entries.size() > limit;
    params.put("hasNext", hasNext);
    if (hasNext) {
      params.put("startItemId", entries.get(limit).getItem().getId());
      params.put("limit", limit);
    }

    return new ModelAndView("page/catalog/index", params);
  }

  @RequestMapping("/item/{id}")
  public ModelAndView detailPage(@PathVariable("id") long id) {
    final IceEntry entry = queryDao.getEntry(id, IceEntryFilter.NONE);
    log.trace("entry={}", entry);

    final Map<String, Object> params = new HashMap<>();
    params.put("currentTime", System.currentTimeMillis());
    params.put("entry", entry);

    return new ModelAndView("page/catalog/item", params);
  }
}
