<script type="text/x-template" id="threshold-confirm-button-vue">
<span>
    <span class="btn ">
        <g:message code="job.threshold.deleteSure" default="Sure?"/>
    </span>
    <button class="btn btn-success confirmButton"
            @click.prevent="confirmDelete(true)">
        <g:message code="job.threshold.deleteYes" default="Yes"/>
    </button>
    <button class="btn btn-danger confirmButton"
            @click.prevent="confirmDelete(false)">
        <g:message code="job.threshold.deleteNo" default="No"/>
    </button>
</span>
</script>

<asset:script type="text/javascript">
    $(window).load(function() {
        OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/job/threshold/thresholdComponents/confirmButtonVue.js"/>');
    });
</asset:script>