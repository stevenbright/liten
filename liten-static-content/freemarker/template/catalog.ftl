<#--
  Macros for building catalog UI
  -->

<#import "./favorites.ftl" as fav />

<#macro inlineItems listModel>
<#list listModel as itemModel>
  <a href="/g/cat/item/${itemModel.item.id?c}" title="${itemModel.displayTitle}">${itemModel.displayTitle}</a><#if itemModel_has_next>,&nbsp</#if>
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

<#macro itemDetails entryModel>
<table class="item-info">
  <tbody>
    <tr>
      <td>ID:</td>
      <td>${entryModel.item.id?c}</td>
    </tr>
<#if entryModel.item.type == "book">
    <tr>
      <td>Authors:</td>
      <td><@inlineItems listModel=entryModel.authors /></td>
    </tr>
    <tr>
      <td>Genres:</td>
      <td><@inlineItems listModel=entryModel.genres /></td>
    </tr>
<#if entry.defaultInstancePresent>
    <tr>
      <td>File Size:</td>
      <td>${entryModel.fileSize?c} byte(s)</td>
    </tr>
    <tr>
      <td>Add Date:</td>
      <td>${entryModel.createdDate}</td>
    </tr>
</#if> <#-- /if entry.defaultInstancePresent -->
    <tr>
      <td>Language:</td>
      <td><@inlineItems listModel=entryModel.languages /></td>
    </tr>
    <tr>
      <td>Origin:</td>
      <td><@inlineItems listModel=entryModel.origins /></td>
    </tr>
</#if> <#-- /if entry.item.type == "book" -->
  </tbody>
</table>

<hr/>

<#if entry.downloadUrlPresent>
<h3><a href="${entryModel.downloadUrl}">Download&nbsp;<span class="glyphicon glyphicon-download" aria-hidden="true"></span></a></h3>
</#if>
</#macro>
