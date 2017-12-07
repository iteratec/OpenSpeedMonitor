<script type="text/x-template" id="threshold-tab-measurand-vue">
<div>
    <select class="thresholdMeasurand form-control thresholdSelects" id="measurand" name="measurand"
            v-model="selectedMeasurand">
        <option v-for="measurand in avaMeasurands" :value="measurand">{{measurand.name}}</option>
    </select>
</div>
</script>

<asset:javascript src="job/thresholdTabMeasurandVue.js"/>