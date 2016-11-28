<#--
  Macros for building catalog UI
  -->

<#import "./favorites.ftl" as fav />

<#macro inlineItems listModel>
<#list listModel as itemModel>
  <a href="/item/${itemModel.item.id?c}" title="${itemModel.displayTitle}">${itemModel.displayTitle}</a><#if itemModel_has_next>,&nbsp</#if>
</#list>
</#macro>

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
    <#if itemModel.item.type == "book">
    <div class="col-md-5">
      <@inlineItems listModel=itemModel.authors />
    </div>
    <div class="col-md-5">
      <@inlineItems listModel=itemModel.genres />
    </div>
    <#else>
    <div class="col-md-10">
    </div>
    </#if>
  </div>
</div>
</#macro>

<#macro itemList listModel>
<#list listModel as it>
<li><@item itemModel=it /></li>
</#list>
</#macro>
