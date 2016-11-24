<#import "../../template/page.ftl" as page/>

<@page.common title="Catalog Index">
<@page.heading/>

<div>
  <ul>
  <#list entries as entry>
    <li><a href="/g/cat/item/${entry.item.id}">${entry.displayTitle}</a></li>
  </#list>
  </ul>
</div>

<#if hasNext>
  <p><a href="/g/cat/index?startItemId=${startItemId}&limit=${limit}">Next</a></p>
</#if>

<script type="text/javascript" src="/assets/js/bundle.js"></script>
</@page.common>
