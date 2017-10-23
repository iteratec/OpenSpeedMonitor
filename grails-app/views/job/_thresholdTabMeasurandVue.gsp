<script type="text/x-template" id="threshold-tab-measurand-vue">
<div>
    <label class="thresholdMeasurand">{{ threshold.threshold.measurand.name }}</label>
    <threshold-row :threshold="threshold"
                   v-on:delete-threshold="deleteThreshold"
                   v-on:update-threshold="updateThreshold"></threshold-row>
</div>
</script>

<g:render template="thresholdTabThresholdRowVue"/>
<asset:javascript src="job/thresholdTabMeasurandVue.js"/>