package liten.website.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/g/")
public final class PublicPageController extends BaseHtmlController {

  @RequestMapping("/login")
  public ModelAndView login(@RequestParam(value = "error", required = false) String loginError) {
    final Map<String, Object> params = new HashMap<>();
    params.put("loginError", loginError);
    params.put("currentTime", System.currentTimeMillis());
    return new ModelAndView("page/login", params);
  }

  @RequestMapping("/index")
  public ModelAndView index() {
    return new ModelAndView("page/index", "currentTime", System.currentTimeMillis());
  }

  @RequestMapping("/about")
  public ModelAndView about() {
    final Map<String, Object> params = newMapWithAccount();
    params.put("userId", hasUserAccount() ? getUserId() : -1L);
    params.put("currentTime", System.currentTimeMillis());
    return new ModelAndView("page/about", params);
  }

  @RequestMapping("/pub/error/{errorCode}")
  public ModelAndView errorPage(@PathVariable("errorCode") int errorCode) {
    final HttpStatus status = HttpStatus.valueOf(errorCode);
    final Map<String, Object> model = new HashMap<>();
    model.put("errorCode", errorCode);
    model.put("reasonPhrase", status.getReasonPhrase());
    return new ModelAndView("page/error", model);
  }
}
