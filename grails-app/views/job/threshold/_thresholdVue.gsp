<script type="text/x-template" id="threshold-vue">
<div>
    <div v-if="thresholdItem.saved">
        <label class="thresholdMeasurand">{{ thresholdItem.threshold.measurand.name }}</label>
    </div>

    <div v-else>
        <measurand-select
                :availableMeasurands="availableMeasurands"
                :thresholdMeasurand="thresholdItem.threshold.measurand"
                v-on:update-measurand="updateMeasurand"></measurand-select>
    </div>

    <div class="thresholdBoundaries form-inline">
        <threshold-label name="Good"></threshold-label>
        <threshold-input :editable="thresholdItem.edit" :threshold-field="thresholdItem.threshold.lowerBoundary"
                             :threshold-measurand="thresholdItem.threshold.measurand"
                             :fieldName="lowerField" v-on:update-boundary="updateThresholdBoundary"></threshold-input>
        <threshold-label name="Ok"></threshold-label>
        <threshold-input :editable="thresholdItem.edit" :threshold-field="thresholdItem.threshold.upperBoundary"
                             :threshold-measurand="thresholdItem.threshold.measurand"
                             :fieldName="upperField" v-on:update-boundary="updateThresholdBoundary"></threshold-input>
        <threshold-label name="Bad"></threshold-label>
        <threshold-button :saved="thresholdItem.saved"
                          :editable="thresholdItem.edit"
                          :valid="validThreshold"
                          v-on:button-clicked="buttonClicked">
        </threshold-button>
    </div>
</div>
</script>
<g:render template="threshold/thresholdComponents/labelVue"/>
<g:render template="threshold/thresholdComponents/inputVue"/>
<g:render template="threshold/thresholdComponents/buttonVue"/>
<g:render template="threshold/thresholdComponents/measurandSelect"/>

<asset:javascript src="/job/threshold/thresholdVue.js"/>