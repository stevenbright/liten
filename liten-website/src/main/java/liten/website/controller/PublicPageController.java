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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("/g/")
public final class PublicPageController implements SecurityControllerMixin {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Resource
  private CatalogQueryDao queryDao;

  @RequestMapping("/login")
  public ModelAndView login(@RequestParam(value = "error", required = false) String loginError) {
    final Map<String, Object> params = new HashMap<>();
    params.put("loginError", loginError);
    params.put("currentTime", System.currentTimeMillis());
    return new ModelAndView("page/login", params);
  }

  @RequestMapping("/index")
  public ModelAndView index() {
    final List<IceEntry> entries = queryDao.getEntries(IceEntryFilter.NONE, ModelWithId.INVALID_ID, 10);

    final Locale locale = LocaleContextHolder.getLocale();
    final Object[] intro = {
        locale.getISO3Language(),
        locale.getLanguage(),
        locale.getCountry()
    };
    log.trace("intro={}", intro);

    final Map<String, Object> params = new HashMap<>();
    params.put("time", System.currentTimeMillis());
    params.put("entries", entries);

    return new ModelAndView("page/index", params);
  }

  @RequestMapping("/about")
  public ModelAndView about() {
    final Map<String, Object> params = newMapWithAccount();
    params.put("userId", hasUserAccount() ? getUserId() : -1L);
    params.put("currentTime", System.currentTimeMillis());
    return new ModelAndView("page/about", params);
  }
}
