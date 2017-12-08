<script type="text/x-template" id="threshold-tab-threshold-vue">
<div>
    <div v-if="thresholdItem.saved">
        <label class="thresholdMeasurand">{{ thresholdItem.threshold.measurand.name }}</label>
    </div>

    <div v-else>
        <threshold-measurand
                :availableMeasurands="availableMeasurands"
                :thresholdMeasurand="thresholdItem.threshold.measurand"
                v-on:update-measurand="updateMeasurand"/>
    </div>

    <div class="thresholdBoundaries form-inline">
        <threshold-label name="Good"/>
        <threshold-input :editable="thresholdItem.edit" :threshold-field="thresholdItem.threshold.lowerBoundary"
                             :threshold-measurand="thresholdItem.threshold.measurand"
                             :fieldName="lowerField" v-on:update-boundary="updateThresholdBoundary"/>
        <threshold-label name="Ok"/>
        <threshold-input :editable="thresholdItem.edit" :threshold-field="thresholdItem.threshold.upperBoundary"
                             :threshold-measurand="thresholdItem.threshold.measurand"
                             :fieldName="upperField" v-on:update-boundary="updateThresholdBoundary"/>
        <threshold-label name="Bad"/>
        <threshold-button :saved="thresholdItem.saved" :editable="thresholdItem.edit" v-on:button-clicked="buttonClicked"/>
    </div>
</div>
</script>
<g:render template="thresholdTabThresholdLabel"/>
<g:render template="thresholdTabThresholdInputVue"/>
<g:render template="thresholdTabThresholdButtonVue"/>
<g:render template="thresholdTabMeasurandVue"/>

<asset:javascript src="thresholdTabThresholdVue.js"/>