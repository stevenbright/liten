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
      <h3><a href="/g/cat/item/${itemModel.id}" title="${itemModel.id} - ${itemModel.defaultTitle}">${itemModel.defaultTitle}</a></h3>
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

      <#-- TODO: use language from book-extras -->
      <#if item.defaultSkuPresent>
      <tr>
        <td>Language:</td>
        <td><@inlineItem item=item.defaultSku.language /></td>
      </tr>
      </#if>
      </#if> <#-- /if item.type == "book" -->


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
    <table class="table-download-links">
      <tbody>
      <#list item.defaultSku.entries as entry>
        <tr>
          <td>${entry.id}</td>
          <td>${entry.createdTimestamp}</td>
          <#if entry.downloadInfoPresent>
            <td>${entry.fileSize} bytes</td>
            <td><a href="${entry.downloadUrl}">Download (${entry.downloadType})&nbsp;<span class="glyphicon glyphicon-download" aria-hidden="true"></span></a></td>
          <#else>
            <td colspan="2">No Content Yet</td>
          </#if>
        </tr>
      </#list>
      </tbody>
    </table>
  </#if>


  <#if item.nonDefaultSkus?size gt 0>
    <h3>Other Editions</h3>
    <ol>
    <#list item.nonDefaultSkus as sku>
      <li>
        <a href="/g/cat/item/${item.id}/${sku.id}" title="${sku.title}">${sku.title} (${sku.languageName}) &raquo;</a>
      </li>
    </#list>
    </ol>
  </#if>

</#macro><#-- macro itemDetails -->

<#macro filterButtonGroup>
  <div class="btn-group">
    <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
      Filter by Type <span class="caret"></span>
    </button>
    <ul class="dropdown-menu">
      <li><a href="/g/cat/index?type=book">Book</a></li>
      <li><a href="/g/cat/index?type=genre">Genres</a></li>
      <li><a href="/g/cat/index?type=language">Languages</a></li>
      <li><a href="/g/cat/index?type=origin">Origins</a></li>
      <li><a href="/g/cat/index?type=person">Persons</a></li>
      <li><a href="/g/cat/index?type=series">Series</a></li>
      <li role="separator" class="divider"></li>
      <li><a href="/g/cat/index">No Type Filter</a></li>
    </ul>
  </div>
</#macro><#-- macro itemListControls -->
