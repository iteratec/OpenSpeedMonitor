<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<%@ page import="de.iteratec.osm.measurement.schedule.ConnectivityProfile" %>
<%@ page import="de.iteratec.osm.measurement.environment.Location" %>
<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>

<div class="col-md-6">

    <div class="row">

        <div class="form-group">
            <label for="chkbox-active" class="col-md-3 control-label">
                <g:message code="job.active.label" default="Active"/>
            </label>

            <div class="col-md-7">
                <div class="checkbox">
                    <g:checkBox name="active" value="${job?.active}" id="chkbox-active"/>
                </div>
            </div>
        </div>


        <div class="form-group" id="nameFormGroup" ${hasErrors(bean: job, field: 'label', 'has-error')} required">
            <label for="label" class="col-md-3 control-label">
                <g:message code="job.label.label" default="Name"/>
                <span class="required-indicator">*</span>
            </label>

            <div class="col-md-7">
                <input type="text" class="form-control job-label" id="inputField-JobLabel" name="label"
                       value="${job?.label}"
                       placeholder="${g.message(code: 'job.label.placeholder', 'default': 'New Job')}"
                       required >
            </div>
        </div>

        <div class="form-group ${hasErrors(bean: job, field: 'location', 'has-error')} required">
            <label class="col-md-3 control-label" for="location">
                <g:message code="job.location.label" default="Location"/>
                <span class="required-indicator">*</span>
            </label>

            <div class="col-md-7">
                <select id="location" class="form-control" name="location.id">
                    <g:each in="${WebPageTestServer.findAllByActive(true)}" var="server">
                        <optgroup label="${server.label}">
                            <g:each in="${Location.findAllByWptServerAndActive(server, true)}" var="loc">
                                <option value="${loc.id}"
                                        <g:if test="${job?.location?.id == loc.id}">selected</g:if>>${loc.uniqueIdentifierForServer ?: loc.location}</option>
                            </g:each>
                        </optgroup>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="form-group ${hasErrors(bean: job, field: 'jobGroup', 'has-error')} required">
            <label class="col-md-3 control-label" for="jobGroup">
                <g:message code="job.jobGroup.label" default="Job Group"/>
                <span class="required-indicator">*</span>
            </label>

            <div class="col-md-7">
                <g:select id="jobgroup" class="form-control chosen" name="jobGroup.id" from="${JobGroup.list()}"
                          value="${job?.jobGroup?.id}" optionValue="name" optionKey="id"/>
            </div>
            <div class="col-md-2 no-left-gutter">
                <a id="jobGroupModalLink" href="#jobGroupModal" data-toggle="modal"
                   title="Create New Job Group" role="button" class="btn btn-link col-md-2">
                    <i id="button_create_jobGroup" class="fa fa-plus" aria-hidden="true"></i>
                    <g:message code="default.button.create.new" default="Create New"/>
                </a>
            </div>
        </div>

        <div class="form-group">
            <label class="col-md-3 control-label" for="connectivityProfile"><g:message
                    code="connectivityProfile.label" default="Connection"/>
                <span class="required-indicator">*</span>
            </label>

            <div class="col-md-7">
                <g:select id="connectivityProfile" class="chosen"
                          data-placeholder="${g.message(code: 'web.gui.jquery.chosen.multiselect.placeholdermessage', 'default': 'Please chose an option')}"
                          name="connectivityProfile.id" from="${connectivites}" optionKey="id"
                          value="${job.connectivityProfile ? job.connectivityProfile.id : null}"/>
            </div>
        </div>

        <div id="connectivityProfileDetails">
            <input type="hidden" id="customConnectivityProfile" name="customConnectivityProfile"
                   value="${job?.customConnectivityProfile}"/>
            <input type="hidden" id="noTrafficShapingAtAll" name="noTrafficShapingAtAll"
                   value="${job?.noTrafficShapingAtAll}"/>

            <div class="form-group">
                <label class="col-md-3 control-label" for="customConnectivityName">
                    <g:message code="job.customConnectivityProfile.label"
                               default="Name of custom connectivity profile"/>
                </label>

                <div class="col-md-7">
                    <g:textField class="form-control" name="customConnectivityName" id="custom-connectivity-name"
                                 value="${job?.customConnectivityName}" readonly="readonly"/>
                    <label class="checkbox-inline">
                        <g:checkBox name="setCustomConnNameManually" id="setCustomConnNameManually"></g:checkBox>
                        <g:message code="job.customConnectivityProfile.setnamemanually.label"
                                   default="Set name manually"/>
                    </label>
                </div>
            </div>
            <g:each var="attribute" in="['bandwidthDown', 'bandwidthUp', 'latency', 'packetLoss']">
                <div class="form-group ${hasErrors(bean: job, field: attribute, 'has-error')}">
                    <label class="col-md-3 control-label" for="${attribute}">
                        <g:message code="connectivityProfile.${attribute}.label"
                                   default="null"/>
                    </label>

                    <div class="col-md-7">
                        <g:textField class="form-control" name="${attribute}" id="custom-${attribute}"
                                     value="${job?."$attribute"}"/>
                    </div>
                </div>
            </g:each>
        </div>

        <div class="form-group" id="executionScheduleFormGroup">
            <label for="executionSchedule" class="col-sm-3 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputCronStringLabel" default="Execution Plan"/>
            </label>

            <div class="col-sm-3">
                <select class="form-control chosen-select" id="selectExecutionSchedule">
                    <optgroup label="${message(code: 'de.iteratec.isocsi.custom', default: 'Custom')}">
                        <option value="">
                            <g:message code="de.iteratec.isocsi.custom" default="Custom"/>
                        </option>
                    </optgroup>
                    <optgroup
                            label="${message(code: 'de.iteratec.osm.result.predefined.linktext', default: 'Predefined')}">
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

            <div style="margin-left: 0px" class="col-sm-4 form-group" id="cronFormGroup">
                <input type="text" class="form-control" id="executionSchedule" name="executionSchedule"
                       value="${executionSchedule ?: "0 * * * ? *"}" data-cron="true" data-help-panel-id="cronInfoPanel" required>
                <div class="help-block with-errors"></div>
                <span id="cronInputHelpBlock" class="help-block"></span>
            </div>
        </div>
    </div>

</div>

<div class="col-md-6">
    <div class="panel panel-info hidden help-panel" id="cronInfoPanel">
        <div class="panel-heading">
            <i class="fa fa-info-circle" aria-hidden="true"></i>
            <g:message code="default.info.title" default="Information"/>
        </div>

        <div class="panel-body">
            <p>
                <g:message code="de.iteratec.osm.setupMeasurementWizard.createJob.description"
                           default="The execution plan can be assigned manually as a Cron expression."/>
            </p>
            <g:render template="/job/cronInstructions"></g:render>
        </div>
    </div>
</div>

<g:render template="/_common/modals/createJobGroupModal"/>
