<script type="text/x-template" id="threshold-tab-threshold-row-vue">
<div class="thresholdBoundaries form-inline">
    <label class="labelGood">
        Good
    </label>
    <label>
        <
    </label>
    <input v-if="threshold.edit" id="lowerBoundaryEdit" class="form-control" type="number"
           min="1"
           name="lowerBoundary" cols="30"
           rows="5"
           maxlength="150"
           v-model="threshold.threshold.lowerBoundary"/>
    <input v-else id="lowerBoundaryShow" class="form-control float-right" type="number"
           min="1"
           name="lowerBoundary" cols="30"
           rows="5"
           maxlength="150"
           readonly
           v-model="threshold.threshold.lowerBoundary"/>
    <label>
        ms <
    </label>
    <label class="labelOk">
        OK
    </label>
    <label>
        <
    </label>
    <input v-if="threshold.edit" id="upperBoundaryEdit" class="form-control" type="number"
           min="1"
           name="upperBoundary" cols="40"
           rows="5"
           maxlength="150"
           v-model="threshold.threshold.upperBoundary"/>
    <input v-else id="upperBoundaryShow" class="form-control float-right" type="number"
           min="1"
           name="upperBoundary" cols="40"
           rows="5"
           maxlength="150"
           readonly
           v-model="threshold.threshold.upperBoundary"/>
    <label>
        ms <
    </label>
    <label class="labelBad">
        Bad
    </label>
    <span v-if="threshold.edit">
        <button type="button"
                class="thresholdButton margins btn btn-primary btn-xs"
                @click="updateThreshold(threshold, '${g.createLink([controller: 'threshold', action: 'updateAsync'])}')">submit</button>
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