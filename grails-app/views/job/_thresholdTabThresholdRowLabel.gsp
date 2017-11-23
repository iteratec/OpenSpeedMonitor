<script type="text/x-template" id="threshold-tab-threshold-row-label-vue">
<span class="form-inline">
    <label v-if="showFront">
        <
    </label>
    <label :class="classObject">
        {{ name }}
    </label>
    <label v-if="showBehind">
        <
    </label>
</span>
</script>

<asset:javascript src="thresholdTabThresholdRowLabel.js"/>