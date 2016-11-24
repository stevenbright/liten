<#import "../../template/page.ftl" as page/>

<@page.common title="Catalog">

<div>
  <h1>${entry.displayTitle}</h1>
  <small>${entry.item.id}</small>
<#if entry.defaultInstancePresent>
  <p>Instance.Created = ${entry.instance.created}</p>
  <p>Instance.OriginId = ${entry.instance.originId}</p>
</#if>
  <p>TBD</p>
</div>



<script type="text/javascript" src="/assets/js/bundle.js"></script>
</@page.common>
