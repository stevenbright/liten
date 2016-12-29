<#import "../../template/page.ftl" as page/>
<#import "../../template/catalog.ftl" as cat />
<#import "../../template/pagination.ftl" as p />

<@page.common title="Catalog Hints">

<h2>Catalog Hints<small>&nbsp;&nbsp;&raquo; ${itemType}</small></h2>

<#-- Filtering labels -->
<div class="container">
  <div class="row">
    <div class="col-md-12">
      <div class="btn-group">
        <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
          Filter by Type <span class="caret"></span>
        </button>
        <ul class="dropdown-menu">
          <li><a href="/g/cat/hints?type=author">Authors</a></li>
          <li><a href="/g/cat/hints?type=genre">Genres</a></li>
          <li><a href="/g/cat/hints?type=book">Book</a></li>
          <li><a href="/g/cat/hints?type=origin">Origins</a></li>
          <li role="separator" class="divider"></li>
          <li><a href="/g/cat/hints">No Type Filter</a></li>
        </ul>
      </div><#-- btn-group -->
    </div><#-- column -->
  </div><#-- row -->

  <div class="row">
    <div class="col-md-12">
      <div>
        <#list nameHintList as nameHint>
          <div class="btn-group">
            <a class="btn btn-default" href="${nameHint.moreHintsUrl}">${nameHint.prefix}&nbsp;&hellip;</a>
            <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
              <span class="caret"></span>
              <span class="sr-only">Toggle Dropdown</span>
            </button>
            <ul class="dropdown-menu">
              <li><a href="${nameHint.itemsFilterUrl}">Show items starting with '${nameHint.prefix}'</a></li>
            </ul>
          </div>
        </#list>
      </div><#-- namePrefix container -->
    </div><#-- column -->
  </div><#-- row -->
</div><#-- container -->

</@page.common>
