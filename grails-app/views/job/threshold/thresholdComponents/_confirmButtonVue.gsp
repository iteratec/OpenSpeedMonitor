<script type="text/x-template" id="threshold-confirm-button-vue">
<span>
    <span class="btn ">
        Sure?
    </span>
    <button class="btn btn-success confirmButton"
            @click.prevent="confirmDelete(true)">
        Yes
    </button>
    <button class="btn btn-danger confirmButton"
            @click.prevent="confirmDelete(false)">
        No
    </button>
</span>
</script>

<asset:javascript src="/job/threshold/thresholdComponents/confirmButtonVue.js"/>