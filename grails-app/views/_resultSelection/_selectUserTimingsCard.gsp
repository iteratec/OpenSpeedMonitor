
<%--
A card with controls to select a user timing
--%>
<h4>User Timings</h4>
<div id="select-usertimings-card${name}" data-no-auto-update="${(boolean) noAutoUpdate}">

    <g:select id="userTimingsSelectHtmlId${name}"
              class="form-control"
              name="selectedUserTimings${name}" from="${userTimings}" optionKey="${it}"
              optionValue="${it}" multiple="true"/>

    </div>
<asset:script type="text/javascript">
    $(window).load(function() {
        OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/selectUserTimingsCard.js" />');
    });
</asset:script>