<#import "../template/page.ftl" as page/>

<@page.common title="Index">
<@page.heading/>

<div id="javascript-alert">
<div class="alert alert-danger">
  <strong>Enable Javascript!</strong> If you see this warning, it means that either you or your system administrator disabled javascript or your browser is very old.
</div>
<p>If you did enable javascript prior to getting this page, please, update your browser.</p>
</div>

<div>
  <#list 0..9 as x>
    <span><img src="/demo/media/image" width="64" height="64"/></span>
  </#list>
</div>

<script type="text/javascript" src="/assets/js/bundle.js"></script>
</@page.common>
