<script type="text/x-template" id="threshold-tab-measured-event-vue">
    <div>
        <label class="measuredEventLabel">{{ thresholds.measuredEvent.name }}</label>

        <div v-for="threshold in thresholds.thresholdList">
            <threshold-measurand :measurand="threshold.threshold.measurand"></threshold-measurand>
            <threshold-row :threshold="threshold"></threshold-row>
        </div>
    </div>
</script>
<g:render template="thresholdTabThresholdRowVue"/>
<g:render template="thresholdTabMeasurandVue"/>

<asset:javascript src="job/thresholdTabMeasuredEventVue.js"/>