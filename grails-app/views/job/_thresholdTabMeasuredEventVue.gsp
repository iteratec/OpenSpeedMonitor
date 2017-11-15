<script type="text/x-template" id="threshold-tab-measured-event-vue">
    <div>
        <div>
        <label class="measuredEventLabel">{{ thresholds.measuredEvent.name }}</label>
            <button type="button" @click="addMetric()">Add</button>
        </div>

        <div v-for="threshold in thresholds.thresholdList">
            <div>
                <threshold-measurand v-if="threshold.saved"
                                     :threshold="threshold"
                                     v-on:delete-threshold="deleteThreshold"
                                     v-on:update-threshold="updateThreshold"></threshold-measurand>
                <threshold-new-threshold v-else
                                         :measurands="measurands"
                                         :threshold="threshold"
                                         v-on:create-threshold="createThreshold"
                                         v-on:remove-new-threshold="removeNewThreshold"></threshold-new-threshold>
            </div>
        </div>
    </div>
</script>

<g:render template="thresholdTabMeasurandVue"/>
<g:render template="thresholdTabNewThresholdVue"/>

<asset:javascript src="job/thresholdTabMeasuredEventVue.js"/>