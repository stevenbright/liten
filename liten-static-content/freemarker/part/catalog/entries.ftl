<#import "../../template/catalog.ftl" as cat />
<#import "../../template/pagination.ftl" as p />

<@cat.itemList listModel=items />
<@p.nextPageMarker nextUrl=nextUrl />