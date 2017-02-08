<div class="row form-horizontal">
    <div class="col-sm-6">
        <div class="form-group">
            <label for="inputJobName" class="col-sm-2 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputJobLabel" default="Job"/>
            </label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="inputJobName" required>
            </div>
        </div>

        <div class="form-group">
            <label for="inputCronString" class="col-sm-2 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputCronStringLabel" default="Execution Plan"/>
            </label>

            <div class="col-sm-5">
                <select class="form-control chosen-select" id="selectExecutionSchedule">
                    <option value="0/30 * * * ? *">Every half an hour</option>
                    <option value="0 * * * ? *">Hourly</option>
                    <option value="">...</option>
                </select>
            </div>

            <div class="col-sm-5">
                <div class="input-group">
                    <span class="input-group-btn" data-toggle="buttons">
                        <label class="btn btn-default">
                            <input type="checkbox" id="inputCustomCronString">Change Cron String
                        </label>
                    </span>
                    <input type="text" class="form-control" id="inputCronString" disabled required
                           value="0/30 * * * ? *">
                </div>
            </div>
        </div>
    </div>

    <div class="col-sm-offset-1 col-sm-4">
        <div class="panel panel-info">
            <div class="panel-heading">
                <i class="fa fa-info-circle" aria-hidden="true"></i>
                <g:message code="default.info.title" default="Information"/>
            </div>
            <div class="panel-body">
                %{--JOHANNES2DO: Write help text for Job tab in wizard--}%
                <p>
                    Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                    Est excepturi officiis placeat qui quibusdam?
                    Aliquid commodi delectus deleniti dolorem eaque error,
                    et id impedit maxime neque qui velit voluptas voluptatem!
                    Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                    Est excepturi officiis placeat qui quibusdam?
                    Aliquid commodi delectus deleniti dolorem eaque error,
                    et id impedit maxime neque qui velit voluptas voluptatem!
                </p>
            </div>
        </div>
    </div>
</div>

<div class="row">
    <div class="form-group">
        <div class="col-sm-6 text-right">
            <a class="btn btn-default pull-left" data-toggle="modal" data-target="#cancelJobCreationDialog">
                <i class="fa fa-times" aria-hidden="true"></i>
                <g:message code="script.versionControl.cancel.button" default="Cancel"/>
            </a>
            <a data-toggle="tab" href="#selectLocationAndConnectivity"
               class="btn btn-default" id="createJobTabPreviousButton">
                <i class="fa fa-caret-left" aria-hidden="true"></i>
                <g:message code="default.paginate.prev" default="Previous"/>
            </a>
            <button type="submit" name="_action_save" class="btn btn-primary" id="createJobTabCreationButton">
                <i class='fa fa-plus' aria-hidden='true'></i>
                <g:message code="de.iteratec.osm.setupMeasurementWizard.createJobStartMeasurement" default="Create Job and Start Measurement"/>
            </button>
        </div>
    </div>
</div>

<content tag="include.bottom">
    <asset:script type="text/javascript">
        function preselectCronStrings() {
            var selectExecutionSchedule = document.querySelector("#selectExecutionSchedule");
            var inputCronString = document.querySelector("#inputCronString");

            selectExecutionSchedule.addEventListener("change", function (e) {
                inputCronString.value = e.target.value;
            });
        }

        function customCronString() {
            var inputCustomCronString = document.querySelector("#inputCustomCronString");
            var selectExecutionSchedule = document.querySelector("#selectExecutionSchedule");
            var inputCronString = document.querySelector("#inputCronString");

            // JOHANNES2DO: Why does the jQuery version work, but the JavaScript version not
            // inputCustomCronString.addEventListener("change", function (e) {
            $(inputCustomCronString).on("change", function (e) {
                if (e.target.checked) {
                    selectExecutionSchedule.disabled = true;
                    inputCronString.disabled = false;
                } else {
                    selectExecutionSchedule.disabled = false;
                    inputCronString.disabled = true;
                }
            });
        }

        $(document).ready( function () {
            preselectCronStrings();
            customCronString();
        });
    </asset:script>
</content>