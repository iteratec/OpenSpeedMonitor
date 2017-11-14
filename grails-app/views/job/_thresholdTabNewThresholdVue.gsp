<script type="text/x-template" id="threshold-tab-new-threshold-vue">
<div data-fv-framework="bootstrap">
    <div>
        <select id="measuredEvent" name="measuredEvent" class="form-control thresholdSelects"
                v-model="newThreshold.measuredEvent">
            <option v-for="measuredEvent in measuredEvents"
                    :value="measuredEvent">{{measuredEvent.name}}</option>
        </select>
    </div>
    <select class="thresholdMeasurand form-control thresholdSelects" id="measurand" name="measurand"
            v-model="newThreshold.measurand">
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
                   v-model="newThreshold.lowerBoundary"
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
                   v-model="newThreshold.upperBoundary"/>
            <span class="input-group-addon">ms</span>
        </div>
        <label>
            ms <
        </label>
        <label class="labelBad">
            Bad
        </label>
        <button type="button" class="margins btn btn-default"
                @click="createThreshold(newThreshold)">
            <i class="fa text-success fa-lg fa-plus"></i>
        </button>
    </div>
</div>
</script>

<asset:javascript src="job/thresholdTabNewThresholdVue.js"/>