<#--
  Macros for building catalog UI
  -->

<#import "./favorites.ftl" as fav />

<#macro inlineItem item>
  <a href="/g/cat/item/${item.id}" title="${item.defaultTitle}">${item.defaultTitle}</a>
</#macro>

<#macro inlineItems itemList>
<#list itemList as i>
  <@inlineItem item=i/><#if i_has_next>,&nbsp</#if>
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
      <@inlineItems itemList=itemModel.authors />
    </div>
    <div class="col-md-5">
      <@inlineItems itemList=itemModel.genres />
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
      <#if item.type == "book">
      <tr>
        <td>Authors:</td>
        <td><@inlineItems itemList=item.authors /></td>
      </tr>
      <tr>
        <td>Genres:</td>
        <td><@inlineItems itemList=item.genres /></td>
      </tr>

      <tr>
        <td>Origins:</td>
        <td><@inlineItems itemList=item.origins /></td>
      </tr>
      </#if> <#-- /if item.type == "book" -->

      <#if item.defaultSkuPresent>
      <tr>
        <td>Language:</td>
        <td><@inlineItem item=item.defaultSku.language /></td>
      </tr>
      </#if>
    </tbody>
  </table>

  <hr/>

<#-- TODO: rework
  <#if item.downloadUrl?has_content>
  <h3><a href="${item.downloadUrl}">Download&nbsp;<span class="glyphicon glyphicon-download" aria-hidden="true"></span></a></h3>
  </#if>
-->

  <#if item.defaultSkuPresent && (item.defaultSku.entries?size gt 0)>
    <h3>Download Items</h3>
    <#list item.defaultSku.entries as entry>
      <p>${entry.id}&nbsp;Created at ${entry.createdTimestamp}&nbsp;
      <#if entry.downloadInfoPresent>
        <span><a href="${entry.downloadUrl}">Download (${entry.downloadType})&nbsp;<span class="glyphicon glyphicon-download" aria-hidden="true"></span></a></span>
      <#else>
        <span>No Download Link Yet</span>
      </#if>
      </p>
    </#list>
  </#if>


  <#if item.nonDefaultSkus?size gt 0>
    <h3>Other Editions</h3>
    <ul>
    <#list item.nonDefaultSkus as sku>
      <li>
        <a href="/g/cat/item/${item.id}/${sku.id}" title="${sku.title}">${sku.title} (${sku.languageName}) &raquo;</a>
      </li>
    </#list>
    </ul>
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
