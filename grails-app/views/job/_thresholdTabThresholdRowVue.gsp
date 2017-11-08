<script type="text/x-template" id="threshold-tab-threshold-row-vue">
<div class="thresholdBoundaries form-inline">
    <label class="labelGood">
        Good
    </label>
    <label>
        <
    </label>
    <div class="input-group">
    <input v-if="threshold.edit" id="lowerBoundaryEdit" class="form-control" type="number"
           min="1"
           name="lowerBoundary"
           maxlength="100"
           v-model="threshold.threshold.lowerBoundary"/>
    <input v-else id="lowerBoundaryShow" class="form-control float-right" type="number"
           min="1"
           name="lowerBoundary"
           maxlength="100"
           readonly
           v-model="threshold.threshold.lowerBoundary"/>
        <span class="input-group-addon">ms</span>
    </div>
    <label>
        <
    </label>
    <label class="labelOk">
        OK
    </label>
    <label>
        <
    </label>
    <div class="input-group">
        <input v-if="threshold.edit" id="upperBoundaryEdit" class="form-control" type="number"
               min="1"
               name="upperBoundary"
               maxlength="100"
               v-model="threshold.threshold.upperBoundary"/>
        <input v-else id="upperBoundaryShow" class="form-control float-right" type="number"
               min="1"
               name="upperBoundary"
               maxlength="100"
               readonly
               v-model="threshold.threshold.upperBoundary"/>
        <span class="input-group-addon">ms</span>
    </div>
    <label>
        <
    </label>
    <label class="labelBad">
        Bad
    </label>
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

<asset:javascript src="job/thresholdTabThresholdRowVue.js"/>