
import $ from 'jquery';

export function setUpFavoritesHandlers() {
  $(".fav-link").click(function (event) {
    event.preventDefault(); // suppress navigation

    const self = $(this);
    const toggleFavoriteUrl = self.attr("toggle-favorite-url");
    //console.log("toggleFavoriteUrl =", toggleFavoriteUrl);

    const $deferred = $.ajax({
      url: toggleFavoriteUrl,
      method: "POST"
    });

    $deferred.fail(function () {
      console.warn("Error while loading", pageUrl);
    });

    $deferred.done(function (data) {
      console.log("isFavorite =", data);

      let isFavorite = false;
      if ("value" in data) {
        isFavorite = data["value"];
      }

      // we can use toggleClass here, but add/removeClass is safer
      if (isFavorite) {
        self.addClass("fav");
      } else {
        self.removeClass("fav");
      }
    })
  });
}
