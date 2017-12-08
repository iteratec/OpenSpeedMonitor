<script type="text/x-template" id="threshold-button-vue">
<span class="form-inline">
    <button type="button"
            :disabled="!valid"
            :class="computedClass"
            @click="onClick(true)">{{computedLabelPositive}}</button>
    <button type="button"
            class="thresholdButton margins btn btn-xs btn-danger"
            @click="onClick(false)">{{computedLabelNegative}}</button>
</span>
</script>

<asset:javascript src="/job/threshold/thresholdComponents/buttonVue.js"/>