<script type="text/x-template" id="threshold-label-vue">
<span class="form-inline">
    <label v-if="showFront">
        <
    </label>
    <label :class="classObject">
        {{ name }}
    </label>
    <label v-if="showBehind" class="thresholdLabel">
        <
    </label>
</span>
</script>

<asset:script type="text/javascript">
    $(window).load(function() {
        OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/job/threshold/thresholdComponents/labelVue.js"/>');
    });
</asset:script>