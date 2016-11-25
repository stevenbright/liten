<#--
  Macros for building catalog UI
  -->

<#import "./favorites.ftl" as fav />

<#macro item itemModel>
<div class="container">
  <#-- Title -->
  <div class="row">
    <div class="col-md-12">
      <h3><a href="/g/cat/item/${itemModel.item.id?c}" title="test"><small>${itemModel.item.id?c}</small>&nbsp;${itemModel.displayTitle}</a></h3>
    </div>
  </div>
  <#-- Authors, Genres, Related Books, ... etc. -->
  <div class="row">
    <div class="col-md-2">
      <@fav.star isFavorite=false toggleFavoriteUrl="/api/p13n/v1/favs/${itemModel.item.id?c}/toggle" />
    </div>
    <div class="col-md-5">
      <#list 0..3 as author>
        <a href="/item/${author}" title="${author}">${author}</a><#if author_has_next>,&nbsp</#if>
      </#list>
    </div>
    <div class="col-md-5">
      <#list 5..7 as genre>
        <a href="/item/${genre}" title="${genre}">${genre}</a><#if genre_has_next>,&nbsp</#if>
      </#list>
    </div>
  </div>
</div>
</#macro>


<#macro itemList listModel>
<#list listModel as it>
<li><@item itemModel=it /></li>
</#list>
</#macro>
