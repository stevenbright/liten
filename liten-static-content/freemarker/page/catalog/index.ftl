<#import "../../template/page.ftl" as page/>
<#import "../../template/catalog.ftl" as cat />
<#import "../../template/pagination.ftl" as p />

<@page.common title="Catalog Index">

<h2>Catalog Index<small>&nbsp;&nbsp;&raquo; ${displayItemTypeTitle}</small></h2>

<div class="container">
  <div class="row">
    <#-- Filtering labels -->
    <div class="col-md-6">
      <@p.loadMoreButton nextUrl=nextUrl targetListSelector="#catalog-items" loadButtonClass="btn-catalog-pagination" />
    </div>
    <div class="col-md-6">
      <@cat.filterButtonGroup/>
    </div>
  </div><#-- row (upper controls) -->
  <div class="row">
    <div class="col-md-12">
      <ul id="catalog-items" class="catalog-list" effect="fade-in scroll">
      <@cat.itemList listModel=items />
      </ul>
    </div>
  </div><#-- row (main content) -->
  <div class="row">
    <div class="col-md-6">
      <@p.loadMoreButton nextUrl=nextUrl targetListSelector="#catalog-items" loadButtonClass="btn-catalog-pagination" />
    </div>
    <div class="col-md-6">
      <@cat.filterButtonGroup/>
    </div>
  </div><#-- row (bottom controls) -->
</div>

</@page.common>
