<script type="text/x-template" id="threshold-tab-threshold-row-input-vue">
<span class="form-inline">
    <div class="input-group thresholdInput">
        <input class="form-control" type="number"
               min="1"
               size="40"
               onClick="this.select()"
               maxlength="12"
               :readonly="!editable"
               v-model="value"/>
        <span class="input-group-addon">{{ computedUnit }}</span>
    </div>
</span>
</script>

<asset:javascript src="thresholdTabThresholdRowInput.js"/>