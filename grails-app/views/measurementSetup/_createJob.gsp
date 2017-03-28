<div class="row form-horizontal">
    <div class="col-sm-6">
        <div class="form-group" id="jobNameFormGroup">
            <label for="inputJobName" class="col-sm-2 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputJobLabel" default="Job Name"/>
            </label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="inputJobName" name="job.label" required>
                <span id="jobNameHelpBlock" class="help-block hidden"><g:message code="de.iteratec.osm.measurement.schedule.Job.label.unique" default="Already Exists"/></span>
            </div>
        </div>

        <div class="form-group" id="executionScheduleFormGroup">
            <label for="executionSchedule" class="col-sm-2 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputCronStringLabel" default="Execution Plan"/>
            </label>

            <div class="col-sm-5">
                <select class="form-control chosen-select" id="selectExecutionSchedule">
                    <optgroup label="${message(code: 'de.iteratec.isocsi.custom', default: 'Custom')}">
                        <option value="">
                            <g:message code="de.iteratec.isocsi.custom" default="Custom"/>
                        </option>
                    </optgroup>
                    <optgroup label="${message(code: 'de.iteratec.osm.result.predefined.linktext', default: 'Predefined')}">
                        <option value="0 * * * ? *" selected>
                            <g:message code="de.iteratec.osm.setupMeasurementWizard.selectExecutionSchedule.hourly"
                                       default="Every hour"/>
                        </option>
                        <option value="0/30 * * * ? *">
                            <g:message code="de.iteratec.osm.setupMeasurementWizard.selectExecutionSchedule.halfHourly"
                                       default="Every half an hour"/>
                        </option>
                        <option value="0/15 * * * ? *">
                            <g:message code="de.iteratec.osm.setupMeasurementWizard.selectExecutionSchedule.15min"
                                       default="Every 15 minutes"/>
                        </option>
                        <option value="0 15 * * ? *">
                            <g:message code="de.iteratec.osm.setupMeasurementWizard.selectExecutionSchedule.daily"
                                       default="Daily at 3pm"/>
                        </option>
                    </optgroup>
                </select>
            </div>

            <div class="col-sm-5 form-group">
                <input type="text" class="form-control" id="executionSchedule" name="job.executionSchedule" value="${job?.executionSchedule}">
                <span id="cronInputHelpBlock" class="help-block"></span>
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
                <p><g:message code="de.iteratec.osm.setupMeasurementWizard.createJob.description" encodeAs="raw" args="[
                        link(url: 'http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html', target: '_blank') { message(code:'job.cronInstructions.moreInformation', default:'Quartz Doku')}
                ]"/></p>
                <g:render template="/job/cronInstructions"></g:render>
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
            <button data-toggle="tab" href="#selectLocationAndConnectivity"
               class="btn btn-default" id="createJobTabPreviousButton">
                <i class="fa fa-caret-left" aria-hidden="true"></i>
                <g:message code="default.paginate.prev" default="Previous"/>
            </button>
            <button type="submit" name="_action_save" class="btn btn-primary" id="createJobTabCreationButton" disabled>
                <i class='fa fa-plus' aria-hidden='true'></i>
                <g:message code="de.iteratec.osm.setupMeasurementWizard.createJobStartMeasurement"
                           default="Create Job and Start Measurement"/>
            </button>
        </div>
    </div>
</div>
