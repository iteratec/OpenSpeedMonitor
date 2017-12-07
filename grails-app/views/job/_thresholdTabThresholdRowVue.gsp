<script type="text/x-template" id="threshold-tab-threshold-row-vue">
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

<div class="thresholdBoundaries form-inline">
    <threshold-row-label name="Good"/>
    <threshold-row-input :editable="threshold.edit" :thresholdField="threshold.threshold.lowerBoundary"
                         :fieldName="lowerField" v-on:update-boundary="updateThresholdBoundary"/>
    <threshold-row-label name="Ok"/>
    <threshold-row-input :editable="threshold.edit" :thresholdField="threshold.threshold.upperBoundary"
                         :fieldName="upperField" v-on:update-boundary="updateThresholdBoundary"/>
    <threshold-row-label name="Bad"/>
    <threshold-row-button :saved="threshold.saved" :editable="threshold.edit" v-on:button-clicked="buttonClicked"/>
</div>
</div>
</script>
<g:render template="thresholdTabThresholdRowLabel"/>
<g:render template="thresholdTabThresholdRowInputVue"/>
<g:render template="thresholdTabThresholdRowButtonVue"/>
<asset:javascript src="job/thresholdTabThresholdRowVue.js"/>