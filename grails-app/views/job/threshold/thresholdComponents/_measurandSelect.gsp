<script type="text/x-template" id="measurand-select-vue">
<div>
    <select class="thresholdMeasurand form-control thresholdSelects " id="measurand" name="measurand"
            v-model="selectedMeasurand">
        <option v-for="measurand in avaMeasurands" :value="measurand">
            {{ measurand.translatedName }}
        </option>
    </select>
</div>
</script>

<asset:script type="text/javascript">
    $(window).load(function() {
        OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/job/threshold/thresholdComponents/measurandSelectVue.js"/>');
    });
</asset:script>