package liten.website.controller.rest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/rest/p13n")
public final class P13nRestController extends BaseRestController {

  @RequestMapping("/v1/favs/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void addToFavorite(@PathVariable("id") String id) {
  }
}
