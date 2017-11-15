<script type="text/x-template" id="threshold-tab-new-threshold-vue">
<div data-fv-framework="bootstrap">
    <select class="thresholdMeasurand form-control thresholdSelects" id="measurand" name="measurand"
            v-model="threshold.threshold.measurand">
        <option v-for="measurand in measurands" :value="measurand">{{measurand.name}}</option>
    </select>

    <div class="thresholdBoundaries form-inline thresholdSelects">
        <label class="labelGood">
            Good
        </label>
        <label>
            <
        </label>

        <div class="input-group thresholdInput">
            <input id="lowerBoundary" class="form-control" type="number" min="1"
                   name="lowerBoundary" cols="30"
                   rows="5"
                   maxlength="100"
                   v-model="threshold.threshold.lowerBoundary"
            required/>
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

        <div class="input-group thresholdInput">
            <input id="upperBoundary" class="form-control" type="number" min="1"
                   name="upperBoundary" cols="40"
                   rows="5"
                   maxlength="100"
                   v-model="threshold.threshold.upperBoundary"/>
            <span class="input-group-addon">ms</span>
        </div>
        <label>
            <
        </label>
        <label class="labelBad">
            Bad
        </label>
        <span>
            <button type="button"
                    class="thresholdButton margins btn btn-success btn-xs"
                    @click="createThreshold(threshold)">save</button>
            <button type="button"
                    class="thresholdButton btn btn-danger btn-xs"
                    @click="removeThreshold(threshold)">remove</button>
        </span>
    </div>
</div>
</script>

<asset:javascript src="job/thresholdTabNewThresholdVue.js"/>