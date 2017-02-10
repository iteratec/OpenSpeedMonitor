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
                %{--<input type="text" class="form-control" id="inputLocation" required>--}%
                <select id="inputLocation" class="form-control chosen-select" required>
                    <g:each in="${WebPageTestServer.findAllByActive(true)}" var="server">
                        <optgroup label="${server.label}">
                        <g:each in="${Location.findAllByWptServerAndActive(server, true)}" var="loc">
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
                %{--<input type="text" class="form-control" id="inputConnectivity" required>--}%
                <select type="text" class="form-control chosen-select" id="inputConnectivity" required>
                    <g:each in="${ConnectivityProfile.findAllByActive(true)}" var="connectivity">
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
                <a href="https://sites.google.com/a/webpagetest.org/docs/using-webpagetest/scripting" target="_blank">
                    <g:message code="de.iteratec.osm.measurement.script.wpt-dsl.link.text"
                               default="Documentation WebPagetest DSL"/>
                </a>

                <p>
                    %{--JOHANNES2DO: Write help/description text for script creation in wizard--}%
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
        <div class="col-sm-6 text-right navigationColumn">
            <a class="btn btn-default pull-left" data-toggle="modal" data-target="#cancelJobCreationDialog">
                <i class="fa fa-times" aria-hidden="true"></i>
                <g:message code="script.versionControl.cancel.button" default="Cancel"/>
            </a>
            <a data-toggle="tab" href="#createScript"
               class="btn btn-default" id="selectLocationAndConnectivityTabPreviousButton">
                <i class="fa fa-caret-left" aria-hidden="true"></i>
                <g:message code="default.paginate.prev" default="Previous"/>
            </a>
            <a data-toggle="tab" href="#createJob"
               class="btn btn-primary" id="selectLocationAndConnectivityTabNextButton">
                <g:message code="default.paginate.next" default="Next"/>
                <i class="fa fa-caret-right" aria-hidden="true"></i>
            </a>
        </div>
    </div>
</div>