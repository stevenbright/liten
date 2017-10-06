<#import "../template/page.ftl" as page/>

<@page.common title="Error">
<h2>Ooops! Something went wrong...</h2>
<p>${errorCode} ${reasonPhrase}</p>
</@page.common>
