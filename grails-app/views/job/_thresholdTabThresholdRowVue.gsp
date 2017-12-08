<script type="text/x-template" id="threshold-tab-threshold-row-vue">
<div>
    <div v-if="threshold.saved">
        <label class="thresholdMeasurand">{{ threshold.threshold.measurand.name }}</label>
    </div>

    <div v-else>
        <threshold-measurand
                :availableMeasurands="availableMeasurands"
                :thresholdMeasurand="threshold.threshold.measurand"
                v-on:update-measurand="updateMeasurand"/>
    </div>

    <div class="thresholdBoundaries form-inline">
        <threshold-row-label name="Good"/>
        <threshold-row-input :editable="threshold.edit" :thresholdField="threshold.threshold.lowerBoundary"
                             :thresholdMeasurand="threshold.threshold.measurand"
                             :fieldName="lowerField" v-on:update-boundary="updateThresholdBoundary"/>
        <threshold-row-label name="Ok"/>
        <threshold-row-input :editable="threshold.edit" :thresholdField="threshold.threshold.upperBoundary"
                             :thresholdMeasurand="threshold.threshold.measurand"
                             :fieldName="upperField" v-on:update-boundary="updateThresholdBoundary"/>
        <threshold-row-label name="Bad"/>
        <threshold-row-button :saved="threshold.saved" :editable="threshold.edit" v-on:button-clicked="buttonClicked"/>
    </div>
</div>
</script>
<g:render template="thresholdTabThresholdRowLabel"/>
<g:render template="thresholdTabThresholdRowInputVue"/>
<g:render template="thresholdTabThresholdRowButtonVue"/>
<g:render template="thresholdTabMeasurandVue"/>

<asset:javascript src="job/thresholdTabThresholdRowVue.js"/>