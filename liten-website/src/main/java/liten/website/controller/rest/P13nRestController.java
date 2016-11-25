package liten.website.controller.rest;

import com.google.protobuf.BoolValue;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/api/p13n/v1")
public final class P13nRestController extends BaseRestController {
  private final Set<Long> favorites = new HashSet<>();

  @RequestMapping(value = "/favs/{id}/toggle", method = RequestMethod.POST)
  @ResponseBody
  public BoolValue toggleFavorite(@PathVariable("id") long id) {
    final boolean isFavorite;
    if (favorites.remove(id)) {
      isFavorite = false;
    } else {
      isFavorite = true;
      favorites.add(id);
    }
    return BoolValue.newBuilder().setValue(isFavorite).build();
  }
}
