<#-- Macros for adding pagination support -->

<#--
  This macro adds a comment containing next URL, enclosed in 'next' tags.
  This marker should be appended at the end of this list and then Javascript code
  should be able to parse it by looking for those 'next' tags at the end of the received page data.
  -->
<#macro nextPageMarker nextUrl>
<!-- <next>${nextUrl}</next> -->
</#macro>