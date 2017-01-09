<#import "../../template/catalog.ftl" as cat />
<#import "../../template/pagination.ftl" as p />

<div class="container">
<#if items?size != 0>
  <div class="row">
    <div class="col-md-12">
      <h3>Related Items</h3>
      <@p.loadMoreButton nextUrl=nextUrl targetListSelector="#catalog-items" loadButtonClass="btn-catalog-pagination" />
    </div>
  </div>

  <div class="row">
    <div class="col-md-12">
      <ul id="catalog-items" class="catalog-list">
      <@cat.itemList listModel=items />
      </ul>
    </div>
  </div>

  <div class="row">
    <div class="col-md-12">
      <@p.loadMoreButton nextUrl=nextUrl targetListSelector="#catalog-items" loadButtonClass="btn-catalog-pagination" />
    </div>
  </div>
</#if>
</div>
