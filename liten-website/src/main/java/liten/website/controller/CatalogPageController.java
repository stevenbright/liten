package liten.website.controller;

import com.truward.orion.user.service.spring.SecurityControllerMixin;
import liten.catalog.dao.CatalogQueryDao;
import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceEntryFilter;
import liten.dao.model.ModelWithId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/g/cat")
public final class CatalogPageController implements SecurityControllerMixin {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Resource
  private CatalogQueryDao queryDao;

  @RequestMapping("/index")
  public ModelAndView index() {
    final Locale locale = LocaleContextHolder.getLocale();

    final List<IceEntry> entries = queryDao.getEntries(IceEntryFilter.forLanguages(locale.getLanguage()),
        ModelWithId.INVALID_ID, 10);
    log.trace("entries={}", entries);

    final Map<String, Object> params = new HashMap<>();
    params.put("currentTime", System.currentTimeMillis());
    params.put("entries", entries);

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
