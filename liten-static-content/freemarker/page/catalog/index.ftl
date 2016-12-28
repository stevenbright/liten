<#import "../../template/page.ftl" as page/>
<#import "../../template/catalog.ftl" as cat />
<#import "../../template/pagination.ftl" as p />

<@page.common title="Catalog Index">

<h2>Catalog Index</h2>

<#-- Filtering labels -->
<div class="container">
  <div class="row">
    <div class="col-md-6">
      <@p.loadMoreButton nextUrl=nextUrl targetListSelector="#catalog-items" />
    </div>
    <div class="col-md-6">
      <div class="btn-group">
        <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
          Filter by Type <span class="caret"></span>
        </button>
        <ul class="dropdown-menu">
          <li><a href="/g/cat/index?type=author">Authors</a></li>
          <li><a href="/g/cat/index?type=genre">Genres</a></li>
          <li><a href="/g/cat/index?type=book">Book</a></li>
          <li><a href="/g/cat/index?type=origin">Origins</a></li>
          <li role="separator" class="divider"></li>
          <li><a href="/g/cat/index">No Type Filter</a></li>
        </ul>
      </div>
    </div>
  </div>
</div>
<br/>

<#-- Main Content -->
<div>
  <ul id="catalog-items" class="catalog-list">
  <@cat.itemList listModel=items />
  </ul>
</div>

</@page.common>
