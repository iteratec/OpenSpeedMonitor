<script type="text/x-template" id="threshold-tab-threshold-row-vue">
<div class="thresholdBoundaries form-inline">
    <threshold-row-label name="Good"/>
    <threshold-row-input :editable="threshold.edit" :thresholdField="threshold.threshold.lowerBoundary" isUpperBoundary="false" v-on:update-boundary="updateThresholdBoundary"/>
    <threshold-row-label name="Ok" />
    <threshold-row-input :editable="threshold.edit" :thresholdField="threshold.threshold.upperBoundary" isUpperBoundary="true" v-on:update-boundary="updateThresholdBoundary"/>
    <threshold-row-label name="Bad"/>
    <threshold-row-button-positive :thresholdId="threshold.threshold.id" v-on:button-clicked="buttonClicked"/>
</div>
</script>
<g:render template="thresholdTabThresholdRowLabel"/>
<g:render template="thresholdTabThresholdRowInputVue"/>
<g:render template="thresholdTabThresholdRowButtonPositiveVue"/>
<asset:javascript src="job/thresholdTabThresholdRowVue.js"/>