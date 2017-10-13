<script type="text/x-template" id="threshold-tab-measured-event-vue">
    <div>
        <label class="measuredEventLabel">{{ thresholds.measuredEvent.name }}</label>

        <div v-for="threshold in thresholds.thresholdList">
            <threshold-measurand :threshold="threshold" v-on:delete-threshold="deleteThreshold"></threshold-measurand>
        </div>
    </div>
</script>
<g:render template="thresholdTabMeasurandVue"/>

<asset:javascript src="job/thresholdTabMeasuredEventVue.js"/>