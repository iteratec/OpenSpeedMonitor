<script type="text/x-template" id="threshold-tab-measurand-vue">
<div>
    <div v-if="threshold.saved">
        <label class="thresholdMeasurand">{{ threshold.threshold.measurand.name }}</label>
    </div>

    <div v-else>
        <select class="thresholdMeasurand form-control thresholdSelects" id="measurand" name="measurand"
                v-model="threshold.threshold.measurand">
            <option v-for="measurand in avaMeasurands" :value="measurand">{{measurand.name}}</option>
        </select>
    </div>

    <threshold-row :threshold="threshold"
                   v-on:delete-threshold="deleteThreshold"
                   v-on:update-threshold="updateThreshold"
                   v-on:create-threshold="createThreshold"
                   v-on:remove-new-threshold="removeNewThreshold">
    </threshold-row>
</div>
</script>

<g:render template="thresholdTabThresholdRowVue"/>
<asset:javascript src="job/thresholdTabMeasurandVue.js"/>