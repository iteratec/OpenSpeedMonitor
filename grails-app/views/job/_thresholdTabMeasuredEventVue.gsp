<script type="text/x-template" id="threshold-tab-measured-event-vue">
<div>
    <div v-if="Object.keys(thresholds.measuredEvent).length">
        <label class="measuredEventLabel">{{ thresholds.measuredEvent.name }}</label>
        <button type="button" class="margins btn btn-default"
                @click="addMetric()">
            <i class="fa text-success fa-lg fa-plus"></i>
        </button>
    </div>
    <div v-else>
        <select class="form-control thresholdSelects" v-model="thresholds.measuredEvent">
            <option v-for="measuredEvent in test" :value="measuredEvent">{{measuredEvent.name}}</option>
        </select>
    </div>


    <div v-for="threshold in thresholds.thresholdList">
        <div>
            <threshold-row :threshold="threshold"
                           :availableMeasurands="availableMeasurands"
                           v-on:delete-threshold="deleteThreshold"
                           v-on:update-threshold="updateThreshold"
                           v-on:create-threshold="createThreshold"
                           v-on:remove-new-threshold="removeNewThreshold">
            </threshold-row>
        </div>
    </div>
</div>
</script>

<g:render template="thresholdTabThresholdRowVue"/>

<asset:javascript src="job/thresholdTabMeasuredEventVue.js"/>