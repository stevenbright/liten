<#import "../../template/page.ftl" as page/>
<#import "../../template/catalog.ftl" as cat/>

<@page.common title="${catalogItem.defaultTitle}">

<h2>${catalogItem.defaultTitle}</h2>

<hr/>

<div class="container">

<#-- Item Details Row -->
<div class="row">
  <#if catalogItem.detailPageCoverUrl?has_content>
  <div class="col-md-4">
    <img src="${catalogItem.detailPageCoverUrl}" alt="Cover" width="240" height="320"/>
  </div>
  </#if>
  <div class="col-md-8">
    <@cat.itemDetails item=catalogItem/>
  </div>
</div><#-- Item Details Row -->

</div><#-- Container -->

<#-- Related items list (if any) -->
<div class="related-items deferred-load" deferred-load-url="${nextRightRelationEntriesUrl}">
</div>

</@page.common>
