package liten.website.controller;

import com.truward.orion.user.service.spring.SecurityControllerMixin;
import liten.catalog.dao.CatalogQueryDao;
import liten.catalog.dao.model.IceEntry;
import liten.catalog.dao.model.IceEntryFilter;
import liten.dao.model.ModelWithId;
import liten.website.util.PaginationHelper;
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

  @Resource
  private CatalogQueryDao queryDao;

  @RequestMapping("/part/entries")
  public ModelAndView entriesPart(
      @RequestParam(value = "startItemId", required = false) @Nullable Long startItemId,
      @RequestParam(value = "limit", required = false) @Nullable Integer limit) {
    final Map<String, Object> params = newEntryPaginationHelper()
        .newModelWithItemsOpt(startItemId, limit);

    return new ModelAndView("part/catalog/entries", params);
  }

  @RequestMapping("/index")
  public ModelAndView index(@RequestParam(value = "limit", required = false) @Nullable Integer limit) {
    final Map<String, Object> params = newEntryPaginationHelper()
        .newModelWithItems(ModelWithId.INVALID_ID, limit != null ? limit : PaginationHelper.DEFAULT_LIMIT);

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

  //
  // Private
  //

  private PaginationHelper<IceEntry> newEntryPaginationHelper() {
    return new IceEntryPaginationHelper(queryDao, getUserLanguage());
  }

  private static final class IceEntryPaginationHelper extends PaginationHelper<IceEntry> {
    private final CatalogQueryDao queryDao;
    private final String userLanguage;

    public IceEntryPaginationHelper(CatalogQueryDao queryDao, String userLanguage) {
      this.queryDao = queryDao;
      this.userLanguage = userLanguage;
    }

    @Override
    protected List<IceEntry> getItemList(long startItemId, int limit) {
      return queryDao.getEntries(IceEntryFilter.forLanguages(userLanguage), startItemId, limit);
    }

    @Override
    protected long getItemId(IceEntry item) {
      return item.getItem().getId();
    }

    @Override
    protected String createNextUrl(long startItemId, int limit) {
      return String.format("/g/cat/part/entries?startItemId=%s&limit=%s", startItemId, limit);
    }
  }
}
