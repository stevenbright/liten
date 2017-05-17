<#import "../../template/page.ftl" as page/>
<#import "../../template/catalog.ftl" as cat/>

<!-- ${catalogItem.detailPageCoverUrl} -->

<@page.common title="${catalogItem.defaultTitle}">

<h2>${catalogItem.defaultTitle}</h2>

<hr/>

<div class="container">

<#-- Item Details Row -->
<div class="row">


</div><#-- Item Details Row -->

</div><#-- Container -->

<#-- Related items list (if any) -->
<div class="related-items deferred-load" deferred-load-url="${nextRightRelationEntriesUrl}">
</div>

</@page.common>
