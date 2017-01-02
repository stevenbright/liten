<#-- Macros for adding pagination support -->

<#--
  This macro adds a comment containing next URL, enclosed in 'next' tags.
  This marker should be appended at the end of this list and then Javascript code
  should be able to parse it by looking for those 'next' tags at the end of the received page data.
  -->
<#macro nextPageMarker nextUrl>
<!-- <next>${nextUrl}</next> -->
</#macro>

<#--
  This macro adds button that triggers 'Load More' functionality.
  @param nextUrl Defines next URL that should be accessed to fetch next page
  @param targetListSelector Defines jQuery selector of the list that hosts content,
                            the content fetched from nextUrl will be appended to this list
  @param loadButtonClass Defines unique class of the buttons that can load content for that target list.
                         This is needed to trigger proper removal of all the buttons that are associated with that list
-->
<#macro loadMoreButton nextUrl targetListSelector loadButtonClass>
<#if nextUrl?has_content>
<button type="button" class="btn btn-default load-more ${loadButtonClass}" next-url="${nextUrl}" target-list="${targetListSelector}" load-button-class="${loadButtonClass}">Load More</button>
</#if>
</#macro>
