<#import "../../template/page.ftl" as page/>
<#import "../../template/catalog.ftl" as cat />
<#import "../../template/pagination.ftl" as p />

<@page.common title="Catalog Index">
<@page.heading/>

<div>
  <ul id="catalog-items" class="catalog-list">
  <@cat.itemList listModel=items />
  </ul>
</div>
<div>
  <@p.loadMoreButton nextUrl=nextUrl targetListSelector="#catalog-items" />
</div>

<script type="text/javascript" src="/assets/js/bundle.js"></script>
</@page.common>
