<#import "../../template/page.ftl" as page/>

<@page.common title="Catalog">

<div>
  <h1>${entry.displayTitle}</h1>
  <small>${entry.item.id}</small>
<#if entry.defaultInstancePresent>
  <p>Created = ${entry.createdDate}</p>
  <#if entry.downloadUrlPresent>
  <p><a href="${entry.downloadUrl}">Download</a></p>
  </#if>
</#if>
  <p>TBD</p>
</div>



<script type="text/javascript" src="/assets/js/bundle.js"></script>
</@page.common>
