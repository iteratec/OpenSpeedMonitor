<script type="text/x-template" id="threshold-button-vue">
<span v-if="!confirmDelete">
    <span class="form-inline">
        <button type="button"
                :disabled="!valid"
                :class="computedClass"
                @click="onClick(true)">{{ computedLabelPositive }}</button>
        <button type="button"
                class="thresholdButton btn btn-xs btn-danger"
                @click="onClick(false)">{{ computedLabelNegative }}</button>
    </span>
</span>
    <span v-else>
        <threshold-confirm-button v-on:delete-item="deleteThreshold"/>
    </span>
</script>

<g:render template="threshold/thresholdComponents/confirmButtonVue"/>

<asset:script type="text/javascript">
    $(window).load(function() {
        OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/job/threshold/thresholdComponents/buttonVue.js"/>');
    });
</asset:script>