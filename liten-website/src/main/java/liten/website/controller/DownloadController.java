package liten.website.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Responds to download requests.
 */
@Controller
@RequestMapping("/g/download")
@ParametersAreNonnullByDefault
public final class DownloadController {

  @RequestMapping("/origin/{originId}/item/{itemId}")
  public String getDownloadUrl(@RequestParam("originId") long originId,
                               @RequestParam("itemId") long itemId) {
    return "redirect:/g/download/typ";
  }

  @RequestMapping
  public String getTemporarilyNotAvailablePage() {
    return "page/download/";
  }
}
