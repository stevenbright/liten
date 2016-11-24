<#import "../../template/catalog/entries.ftl" as cat />
<#import "../../template/pagination.ftl" as p />

<@cat.entryList entryListModel=items />
<@p.nextPageMarker nextUrl=nextUrl />