<#--
  Macros for building catalog UI
  -->

<#import "./favorites.ftl" as fav />

<#macro inlineItems listModel>
<#list listModel as itemModel>
  <a href="/g/cat/item/${itemModel.id}" title="${itemModel.defaultTitle}">${itemModel.defaultTitle}</a><#if itemModel_has_next>,&nbsp</#if>
</#list>
</#macro>

<#macro item itemModel>
<div class="container">
  <#-- Title -->
  <div class="row">
    <div class="col-md-12">
      <h3><a href="/g/cat/item/${itemModel.id}" title="test"><small>${itemModel.id}</small>&nbsp;${itemModel.defaultTitle}</a></h3>
    </div>
  </div>
  <#-- Authors, Genres, Related Books, ... etc. -->
  <div class="row">
    <div class="col-md-2">
      <@fav.star isFavorite=false toggleFavoriteUrl="/api/p13n/v1/favs/${itemModel.id}/toggle" />
    </div>
    <#if itemModel.type == "book">
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

<#macro itemDetails item>
  <table class="item-info">
    <tbody>
      <tr>
        <td>ID:</td>
        <td>${item.id}</td>
      </tr>
  <#if item.type == "book">
      <tr>
        <td>Authors:</td>
        <td><@inlineItems listModel=item.authors /></td>
      </tr>
      <tr>
        <td>Genres:</td>
        <td><@inlineItems listModel=item.genres /></td>
      </tr>
  <#if entry.defaultInstancePresent>
      <tr>
        <td>File Size:</td>
        <td>${item.fileSize?c} byte(s)</td>
      </tr>
      <tr>
        <td>Add Date:</td>
        <td>${item.createdDate}</td>
      </tr>
  </#if> <#-- /if entry.defaultInstancePresent -->
      <tr>
        <td>Language:</td>
        <td><@inlineItems listModel=item.languages /></td>
      </tr>
      <tr>
        <td>Origin:</td>
        <td><@inlineItems listModel=item.origins /></td>
      </tr>
  </#if> <#-- /if entry.item.type == "book" -->
    </tbody>
  </table>

  <hr/>

  <#if entry.downloadUrlPresent>
  <h3><a href="${item.downloadUrl}">Download&nbsp;<span class="glyphicon glyphicon-download" aria-hidden="true"></span></a></h3>
  </#if>
</#macro><#-- macro itemDetails -->

<#macro filterButtonGroup>
  <div class="btn-group">
    <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
      Filter by Type <span class="caret"></span>
    </button>
    <ul class="dropdown-menu">
      <li><a href="/g/cat/index?type=author">Authors</a></li>
      <li><a href="/g/cat/index?type=genre">Genres</a></li>
      <li><a href="/g/cat/index?type=book">Book</a></li>
      <li><a href="/g/cat/index?type=origin">Origins</a></li>
      <li role="separator" class="divider"></li>
      <li><a href="/g/cat/index">No Type Filter</a></li>
    </ul>
  </div>
</#macro><#-- macro itemListControls -->
