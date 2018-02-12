<script type="text/x-template" id="threshold-label-vue">
<span class="form-inline">
    <label v-if="showFront">
        <
    </label>
    <label :class="classObject">
        {{ name }}
    </label>
    <label v-if="showBehind" class="thresholdLabel">
        <
    </label>
</span>
</script>

<asset:javascript src="/job/threshold/thresholdComponents/labelVue.js"/>