package liten.website.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Date;

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
    final String id = "x-" + Long.toHexString(originId) + "-" + Long.toHexString(itemId);
    return "redirect:/g/download/demo/text/" + id;
  }

  @RequestMapping("/unavailable")
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String unavailableDownload() {
    return "page/download/unavailable";
  }

  @RequestMapping("/demo/text/{id}")
  @ResponseBody
  public ResponseEntity<String> demoText(@PathVariable("id") String id) {
    final StringBuilder testContent = new StringBuilder(100);
    testContent.append("Test File\n");
    testContent.append("ID: ").append(id).append('\n');
    testContent.append("Generated at ").append(new Date()).append('\n');
    testContent.append('\n')
        .append("Lorem ipsum dolorem sit amet...\n");
    return new ResponseEntity<>(testContent.toString(), HttpStatus.OK);
  }
}