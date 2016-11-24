<#import "../../template/page.ftl" as page/>
<#import "../../template/catalog/entries.ftl" as cat />

<@page.common title="Catalog Index">
<@page.heading/>

<div>
  <ul id="catalog-list">
  <@cat.entryList entryListModel=items />
  </ul>
</div>

<#if nextUrl?has_content>
<div>
  <button class="load-more" next-url="${nextUrl}" target-list="#catalog-list">Load More</button>
</div>
</#if>

<script type="text/javascript" src="/assets/js/bundle.js"></script>
</@page.common>
