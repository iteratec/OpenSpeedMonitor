<script type="text/x-template" id="threshold-tab-threshold-row-vue">
<div class="thresholdBoundaries form-inline">
    <threshold-row-label name="Good"/>
    <threshold-row-input :editable="threshold.edit" :thresholdField="threshold.threshold.lowerBoundary" isUpperBoundary="false" v-on:update-boundary="updateThresholdBoundary"/>
    <threshold-row-label name="Ok" />
    <threshold-row-input :editable="threshold.edit" :thresholdField="threshold.threshold.upperBoundary" isUpperBoundary="true" v-on:update-boundary="updateThresholdBoundary"/>
    <threshold-row-label name="Bad"/>
    <span v-if="threshold.edit">
        <button type="button"
                class="thresholdButton margins btn btn-primary btn-xs"
                @click="updateThreshold(threshold)">submit</button>
        <button type="button"
                class="thresholdButton btn btn-danger btn-xs"
                @click="changeEditMode(threshold, false)">discard</button>
    </span>
    <span v-else>
        <button type="button"
                class="thresholdButton margins btn btn-primary btn-xs"
                @click="changeEditMode(threshold, true)">edit</button>
        <button type="button"
                class="thresholdButton btn btn-danger btn-xs"
                @click="deleteThreshold(threshold)">delete</button>
    </span>
</div>
</script>
<g:render template="thresholdTabThresholdRowLabel"/>
<g:render template="thresholdTabThresholdRowInputVue"/>
<asset:javascript src="job/thresholdTabThresholdRowVue.js"/>