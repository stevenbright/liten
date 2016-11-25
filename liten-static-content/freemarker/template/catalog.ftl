
<#macro entry entryModel>
<a href="/g/cat/item/${entryModel.item.id?c}">${entryModel.displayTitle}</a>
</#macro>

<#macro entry2 entryModel>
<a href="/g/cat/item/${entryModel.item.id?c}">${entryModel.displayTitle}</a>
</#macro>


<#macro entryList entryListModel>
<#list entryListModel as e>
<li><@entry entryModel=e /></li>
</#list>
</#macro>
