<div class="row form-horizontal">
    <div class="col-sm-6">
        <div class="form-group" id="jobGroupFormGroup">
            <label for="inputJobGroup" class="col-sm-2 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputJobGroupLabel" default="Job Group"/>
            </label>

            <div class="col-sm-5">
                <g:hiddenField id="jobGroupName" name="jobGroup.name" value="${jobGroup?.name}"/>
                <select class="form-control chosen-select" id="jobGroupSelect">
                    <optgroup label="${message(code: 'default.button.create.label', default: 'Create')}">
                        <option value="">
                            <g:message code="default.button.create.new" default="Create New"/>
                        </option>
                    </optgroup>
                    <g:if test="${allJobGroups}">
                        <optgroup label="${message(code: 'de.iteratec.isocsi.existing', default: 'Exsiting')}">
                            <g:each var="currentJobGroup" in="${allJobGroups}">
                                <option>
                                    ${currentJobGroup.name}
                                </option>
                            </g:each>
                        </optgroup>
                    </g:if>
                </select>
            </div>

            <div class="col-sm-5">
                <div class="input-group">
                    <input type="text" class="form-control hidden" id="inputNewJobGroupName"  aria-describedby="jobGroupNameHelpBlock" >
                    <span id="jobGroupNameHelpBlock" class="help-block hidden"><g:message code="de.iteratec.osm.measurement.schedule.JobGroup.name.unique" default="Already Exists"/></span>
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
                %{--JOHANNES2DO: Define help text Job Group Creation--}%
                <p>You will find the results of your measurement under the here created Job Group.</p>

                <p>The job group can be selected in the result views via a select box you can see below:</p>
                <img class="infoImage thumbnail" id="jobGroupSelectionImg"
                     src="${resource(dir: 'images', file: 'jobGroupSelection.png')}"
                     alt="Job Group Selection"/>
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
            <a data-toggle="tab" href="#createScript" class="btn btn-primary" id="setJobGroubTabNextButton">
                <g:message code="default.paginate.next" default="Next"/>
                <i class="fa fa-caret-right" aria-hidden="true"></i>
            </a>
        </div>
    </div>
</div>