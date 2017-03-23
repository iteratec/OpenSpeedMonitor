<div class="row form-horizontal">
    <div class="col-sm-6">
        <div class="form-group">
            <label for="inputJobName" class="col-sm-2 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputJobLabel" default="Job Name"/>
            </label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="inputJobName" name="job.label" required>
            </div>
        </div>

        <div class="form-group">
            <g:hiddenField id="executionSchedule" name="job.executionSchedule" value="${job?.executionSchedule}"/>
            <label for="inputCronString" class="col-sm-2 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputCronStringLabel" default="Execution Plan"/>
            </label>

            <div class="col-sm-5">
                <select class="form-control chosen-select" id="selectExecutionSchedule">
                    <option value="">
                        <g:message code="de.iteratec.isocsi.custom" default="Custom"/>
                    </option>
                    <option value="0 0/30 * 1/1 * ? *">
                        <g:message code="de.iteratec.osm.setupMeasurementWizard.selectExecutionSchedule.halfHourly" default="Every half an hour"/>
                    </option>
                    <option value="0 0 0/1 1/1 * ? *">
                        <g:message code="de.iteratec.osm.setupMeasurementWizard.selectExecutionSchedule.hourly" default="Every hour"/>
                    </option>
                    <option value="0 0/15 * 1/1 * ? *">
                        <g:message code="de.iteratec.osm.setupMeasurementWizard.selectExecutionSchedule.15min" default="Every 15 minutes"/>
                    </option>
                    <option value="0 0 15 1/1 * ? *">
                        <g:message code="de.iteratec.osm.setupMeasurementWizard.selectExecutionSchedule.daily" default="Daily at 3pm"/>
                    </option>
                </select>
            </div>

            <div class="col-sm-5">
                <div class="input-group">
                    <input type="text" class="form-control hidden" id="inputCronString" required
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
                    With the execution plan you can define when and how often your job will be started.
                </p>
            </div>
        </div>
    </div>
</div>

<div class="row navigationRow">
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
                <g:message code="de.iteratec.osm.setupMeasurementWizard.createJobStartMeasurement"
                           default="Create Job and Start Measurement"/>
            </button>
        </div>
    </div>
</div>