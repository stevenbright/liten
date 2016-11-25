
<#--
  Favorite star icon.
  Takes initial state (isFavorite) and URL that should be POSTed in order to toggle favorite status.
  That POST request should respond with favorite status (true/false).
  -->
<#macro star isFavorite toggleFavoriteUrl>
<a class="fav-link <#if isFavorite>fav</#if>" href="#" toggle-favorite-url="${toggleFavoriteUrl}">
  <span class="star"><span class="glyphicon glyphicon glyphicon-star" aria-hidden="true"></span>&nbsp;Unstar</span>
  <span class="unstar"><span class="glyphicon glyphicon glyphicon-star-empty" aria-hidden="true"></span>&nbsp;Star</span>
</a>
</#macro>