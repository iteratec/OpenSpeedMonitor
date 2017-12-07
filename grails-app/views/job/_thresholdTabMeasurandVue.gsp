<script type="text/x-template" id="threshold-tab-measurand-vue">
<div>
    <div v-if="threshold.saved">
        <label class="thresholdMeasurand">{{ threshold.threshold.measurand.name }}</label>
    </div>

    <div v-else>
        <select class="thresholdMeasurand form-control thresholdSelects" id="measurand" name="measurand"
                v-model="selectedMeasurand">
            <option v-for="measurand in avaMeasurands" :value="measurand">{{measurand.name}}</option>
        </select>
    </div>
</div>
</script>

<g:render template="thresholdTabThresholdRowVue"/>
<asset:javascript src="job/thresholdTabMeasurandVue.js"/>