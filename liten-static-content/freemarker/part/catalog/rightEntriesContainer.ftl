<#import "../../template/catalog.ftl" as cat />
<#import "../../template/pagination.ftl" as p />

<div>
<#if items?size != 0>
  <h3>Related Items</h3>
  <div>
    <@p.loadMoreButton nextUrl=nextUrl targetListSelector="#catalog-items" loadButtonClass="btn-catalog-pagination" />
  </div>
  <div>
    <ul id="catalog-items" class="catalog-list">
    <@cat.itemList listModel=items />
    </ul>
  </div>
</#if>
</div>
