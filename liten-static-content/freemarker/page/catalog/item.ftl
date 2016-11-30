<#import "../../template/page.ftl" as page/>
<#import "../../template/catalog.ftl" as cat/>

<@page.common title="Catalog">

<h2>${entry.displayTitle}</h2>

<hr/>

<!-- Information -->
<table class="item-info">
  <tbody>
    <tr>
      <td>ID:</td>
      <td>${entry.item.id?c}</td>
    </tr>
<#if entry.item.type == "book">
    <tr>
      <td>Authors:</td>
      <td><@cat.inlineItems listModel=entry.authors /></td>
    </tr>
    <tr>
      <td>Genres:</td>
      <td><@cat.inlineItems listModel=entry.genres /></td>
    </tr>
<#if entry.defaultInstancePresent>
    <tr>
      <td>File Size:</td>
      <td>${entry.fileSize?c} byte(s)</td>
    </tr>
    <tr>
      <td>Add Date:</td>
      <td>${entry.createdDate}</td>
    </tr>
</#if>
    <tr>
      <td>Language:</td>
      <td><@cat.inlineItems listModel=entry.languages /></td>
    </tr>
    <tr>
      <td>Origin:</td>
      <td><@cat.inlineItems listModel=entry.origins /></td>
    </tr>
</#if> <#-- /entry.item.type == "book" -->
  </tbody>
</table>

<hr/>

<#if entry.downloadUrlPresent>
<h3><a href="${entry.downloadUrl}">Download&nbsp;<span class="glyphicon glyphicon-download" aria-hidden="true"></span></a></h3>
</#if>



<script type="text/javascript" src="/assets/js/bundle.js"></script>
</@page.common>
