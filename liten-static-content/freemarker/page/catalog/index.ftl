<#import "../../template/page.ftl" as page/>

<@page.common title="Catalog">
<@page.heading/>

<div>
  <ul>
  <#list entries as entry>
    <li><a href="/g/cat/item/${entry.item.id}">${entry.displayTitle}</a></li>
  </#list>
  </ul>
</div>

<script type="text/javascript" src="/assets/js/bundle.js"></script>
</@page.common>
