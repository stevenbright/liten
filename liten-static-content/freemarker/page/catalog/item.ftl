<#import "../../template/page.ftl" as page/>
<#import "../../template/catalog.ftl" as cat/>

<@page.common title="${entry.displayTitle}">

<h2>${entry.displayTitle}</h2>

<hr/>

<div class="container">

<#-- Item Details Row -->
<div class="row">

<#if entry.detailPageCoverUrlPresent>
<div class="col-md-4">
  <img src="${entry.detailPageCoverUrl}" alt="Cover" width="240" height="320"/>
</div>
<div class="col-md-8">
  <@cat.itemDetails entryModel=entry/>
</div>
<#else>
  <#-- Include just item details information without cover image -->
  <@cat.itemDetails entryModel=entry/>
</#if>

</div><#-- Item Details Row -->
</div><#-- Container -->

<#-- Related items list (if any) -->
<div class="related-items deferred-load" deferred-load-url="${nextRightRelationEntriesUrl}">
</div>

</@page.common>
