<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>
<%@ page import="de.iteratec.osm.measurement.environment.Location" %>
<%@ page import="de.iteratec.osm.measurement.schedule.ConnectivityProfile" %>

<div class="row form-horizontal">
    <div class="col-sm-6">
        <div class="form-group">
            <label for="inputLocation" class="col-sm-2 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputLocationLabel" default="Location"/>
            </label>

            <div class="col-sm-10">
                <select id="inputLocation" class="form-control chosen-select" name="location" required>
                    <g:each in="${wptServersWithLocations}" var="server">
                        <optgroup label="${server.key}">
                        <g:each in="${server.value}" var="loc">
                            <option value="${loc.id}" <g:if test="${job?.location?.id == loc.id}"> selected </g:if>>
                                ${loc.uniqueIdentifierForServer ?: loc.location}
                            </option>
                        </g:each>
                        </optgroup>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="form-group">
            <label for="inputConnectivity" class="col-sm-2 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputConnectivityLabel" default="Connectivity"/>
            </label>

            <div class="col-sm-10">
                <select type="text" class="form-control chosen-select" id="inputConnectivity" name="connectivity"
                        required data-default-option="DSL 6.000">
                    <g:each in="${connectivityProfiles}" var="connectivity">
                        <option value="${connectivity.name}">
                            ${connectivity.name}
                        </option>
                    </g:each>
                </select>
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
                <p><g:message code="de.iteratec.osm.setupMeasurementWizard.locationAndConnectivity.description" /></p>
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
            <button data-toggle="tab" href="#createScript"
               class="btn btn-default" id="selectLocationAndConnectivityTabPreviousButton">
                <i class="fa fa-caret-left" aria-hidden="true"></i>
                <g:message code="default.paginate.prev" default="Previous"/>
            </button>
            <button data-toggle="tab" href="#createJob"
               class="btn btn-primary" id="selectLocationAndConnectivityTabNextButton">
                <g:message code="default.paginate.next" default="Next"/>
                <i class="fa fa-caret-right" aria-hidden="true"></i>
            </button>
        </div>
    </div>
</div>
