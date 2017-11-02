<script type="text/x-template" id="threshold-tab-generate-script-vue">
    <div class="container">
        <div class="row form-group">
            <div class="col-md-2 col-md-offset-2">
                <label class="control-label">Gesamtlaufzeit</label>
            </div>
            <div class="col-md-2 col-md-offset-1">
                <input class="form-control"
                       type="number"
                       v-model="runtime">
            </div>
        </div>
        <div class="row form-group">
            <div class="col-md-2 col-md-offset-2">
                <label class="control-label">Intervallzyklus</label>
            </div>
            <div class="col-md-2 col-md-offset-1">
                <input class="form-control"
                       type="number"
                       v-model="intervalTime">
            </div>
        </div>
        <div class="row form-group">
            <div class="col-md-2 col-md-offset-2">
                <label class="control-label">Anzahl Intervalle</label>
            </div>
            <div class="col-md-2 col-md-offset-1">
                <input class="form-control"
                       type="number"
                       v-model="calcIntervals"
                       readonly>
            </div>
        </div>
        <div class="row from-group">
            <span class="col-md-8 col-md-offset-2">
                <span class="col-md-4">
                    <input id="allGood"
                           type="radio"
                           name="proof"
                           value="allGood"
                           v-model="picked">
                    <label for="allGood">Alle Messwerte gut</label>
                </span>
                <span class="col-md-4">
                    <input id="noneBad"
                           type="radio"
                           name="proof"
                           value="noneBad"
                           v-model="picked">
                    <label for="noneBad">Kein Messwert schlecht</label>
                </span>
            </span>
        </div>
        <div class="row">
            <div class="col-md-4 col-md-offset-4">
                <button type="button"
                        class="btn btn-md">Skript erstellen</button>
            </div>
        </div>
    </div>
</script>

<asset:javascript src="job/thresholdTabGenerateScriptVue.js"/>