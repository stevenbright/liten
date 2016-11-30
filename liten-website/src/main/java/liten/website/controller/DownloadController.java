package liten.website.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Responds to download requests.
 */
@Controller
@RequestMapping("/g/download")
@ParametersAreNonnullByDefault
public final class DownloadController {

  @RequestMapping("/origin/{originId}/item/{itemId}")
  public String getDownloadUrl(@PathVariable("originId") long originId,
                               @PathVariable("itemId") long itemId) {
    return "redirect:/g/download/unavailable";
  }

  @RequestMapping("/unavailable")
  public String thankYouPage() {
    return "page/download/unavailable";
  }
}
