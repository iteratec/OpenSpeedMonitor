<%@ page contentType="text/html;charset=UTF-8" %>
<% def springSecurityService %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isocsi.detailAnalysisDashboard"/></title>

    <asset:stylesheet src="rickshaw/rickshaw_custom.css"/>
    <asset:stylesheet src="dc.min.css" />

    <style>
    %{--Overwrite dc.min.css--}%
    .dc-chart .pie-slice {
        fill: #fff;
        font-size: 10px;
        font-weight: bold;
        cursor: pointer;
    }
    </style>

</head>

<body>

<%-- main menu --%>
<g:render template="/layouts/mainMenu"/>

<div class="row">
    <div class="span12">
        <g:if test="${command}">
            <g:hasErrors bean="${command}">
                <div class="alert alert-error">
                    <strong><g:message code="de.iteratec.isocsi.CsiDashboardController.selectionErrors.title"/></strong>
                    <ul>
                        <g:eachError var="eachError" bean="${command}">
                            <li><g:message error="${eachError}"/></li>
                        </g:eachError>
                    </ul>
                </div>
            </g:hasErrors>
        </g:if>
    </div>
</div>

<div class="row">
    <div class="span12">
        <form method="get" action="" id="dashBoardParamsForm">
            <div class="accordion">
                <div class="accordion-group">
                    <div class="accordion-heading accordion-custom-heading">
                        <div class="row">
                            <div class="span12">
                                <div class="row">
                                    <div class="span3">
                                        <a class="accordion-toggle accordion-link fa fa-chevron-up"
                                           data-toggle="collapse" data-parent="#accordion2" href="#collapseOne">
                                            <g:message code="de.iteratec.sri.wptrd.time.filter.heading"
                                                       default="Zeitraum ausw&auml;hlen"/>
                                        </a>
                                    </div>

                                    <div class="span2 accordion-info text-right">
                                        <g:message code="de.iteratec.isocsi.csi.timeframe.heading"
                                                   default="Timeframe"/>:
                                    </div>

                                    <div class="span7 accordion-info" id="accordion-info-date"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <g:if test="${request.queryString}"><div id="collapseOne" class="accordion-body collapse"></g:if>
                    <g:else><div id="collapseOne" class="accordion-body collapse in"></g:else>
                    <div class="accordion-inner" id="accordion-inner-date">

                        <div class="row">
                            <div class="span5">
                                <g:render template="/dateSelection/startAndEnddateSelection"
                                          model="${['selectedTimeFrameInterval': selectedTimeFrameInterval, 'from': from, 'fromHour': fromHour, 'to': to, 'toHour': toHour]}"/>
                            </div>
                        </div>
                    </div>
                </div>
                </div>
                    <div class="accordion-group">
                        <div class="accordion-heading accordion-custom-heading">
                            <div class="row">
                                <div class="span12">
                                    <div class="row">
                                        <div class="span3">
                                            <a class="accordion-toggle accordion-link fa fa-chevron-up"
                                               data-toggle="collapse" data-parent="#accordion2" href="#collapseTwo">
                                                <g:message code="de.iteratec.sri.wptrd.jobs.filter.heading"
                                                           default="Jobs filtern"/>
                                            </a>
                                        </div>

                                        <div class="span2 accordion-info text-right">
                                            <g:message code="de.iteratec.isr.wptrd.labels.filterFolder"
                                                       default="Job Group"/>:<br>
                                            <g:message code="de.iteratec.osm.result.page.label"
                                                       default="Page"/>&nbsp;|&nbsp;<g:message
                                                    code="de.iteratec.osm.result.measured-event.label"
                                                    default="Measured step"/>:<br>
                                            <g:message code="browser.label" default="Browser"/>&nbsp;|&nbsp;<g:message
                                                    code="job.location.label" default="Location"/>:<br>
                                            <g:message code="de.iteratec.osm.result.connectivity.label"
                                                       default="Connectivity"/>:<br>
                                        </div>

                                        <div class="span7 accordion-info" id="accordion-info-jobs"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <g:if test="${request.queryString}"><div id="collapseTwo" class="accordion-body collapse"></g:if>
                        <g:else><div id="collapseTwo" class="accordion-body collapse in"></g:else>
                        <div class="accordion-inner" style="margin: 0px; padding: 4px;">
                            <g:render template="/eventResultDashboard/selectMeasurings"
                                      model="${['locationsOfBrowsers'             : locationsOfBrowsers,
                                                'eventsOfPages'                   : eventsOfPages,
                                                'folders'                         : folders,
                                                'selectedFolder'                  : selectedFolder,
                                                'pages'                           : pages,
                                                'selectedPasge'                   : selectedPage,
                                                'measuredEvents'                  : measuredEvents,
                                                'selectedAllMeasuredEvents'       : selectedAllMeasuredEvents,
                                                'selectedMeasuredEvents'          : selectedMeasuredEvents,
                                                'browsers'                        : browsers,
                                                'selectedBrowsers'                : selectedBrowsers,
                                                'selectedAllBrowsers'             : selectedAllBrowsers,
                                                'locations'                       : locations,
                                                'selectedLocations'               : selectedLocations,
                                                'selectedAllLocations'            : selectedAllLocations,
                                                'connectivityProfiles'            : connectivityProfiles,
                                                'selectedConnectivityProfiles'    : selectedConnectivityProfiles,
                                                'selectedAllConnectivityProfiles' : selectedAllConnectivityProfiles,
                                                'showExtendedConnectivitySettings': true]}"/>
                        </div>
                    </div>
                    </div>
                    </div>

                    <div class="row">
                        <div class="span12" id="bottomCommitButtons">
                            <g:actionSubmit
                                    value="${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default': 'Show')}"
                                    action="show"
                                    id="graphButtonHtmlId" class="btn btn-primary"
                                    style="margin-top: 16px;"/>
                        </div>

                    </div>
                </div>
            </div>
        </form>

    <g:if test="${graphData}">
        <g:render template="/detailAnalysisDashboard/detailAnalysisChart" model="${[
        ]}"/>
    </g:if>
    <g:else>
        <g:if test="${request.queryString}">
            <g:if test="${!warnAboutLongProcessingTime}">
                <div class="span12">
                    <strong><g:message
                            code="de.iteratec.isocsi.CsiDashboardController.no.data.on.current.selection"/></strong>
                </div>
            </g:if>
        </g:if>
    </g:else>
    </div>
</div>

<content tag="include.bottom">
    <asset:javascript src="detailanalysisdashboard/detailAnalysisDashboard.js"/>
    <asset:javascript src="detailanalysisdashboard/detailAnalysisGraph.js" />
        <asset:script type="text/javascript">
        var pagesToEvents = [];
        <g:each var="page" in="${pages}">
            <g:if test="${eventsOfPages[page.id] != null}">
                pagesToEvents[${page.id}] = [<g:each var="event" in="${eventsOfPages[page.id]}">${event},</g:each>];
            </g:if>
        </g:each>

        var browserToLocation = [];
        <g:each var="browser" in="${browsers}">
            <g:if test="${locationsOfBrowsers[browser.id] != null}">
                browserToLocation[${browser.id}] = [<g:each var="location"
                                                            in="${locationsOfBrowsers[browser.id]}">${location},</g:each>];
            </g:if>
        </g:each>

        var selectedCsiSystems = [];
        <g:each var="csiSystem" in="${selectedCsiSystems}">
            selectedCsiSystems.push(${csiSystem});
        </g:each>

        $(document).ready(function(){

             initSelectMeasuringsControls(pagesToEvents, browserToLocation, allMeasuredEventElements, allBrowsers, allLocations);

             doOnDomReady(
                    '${dateFormat}',
                    ${weekStart},
                    '${g.message(code: 'web.gui.jquery.chosen.multiselect.noresultstext', 'default': 'Keine Eintr&auml;ge gefunden f&uuml;r ')}'
                );

                <g:if test="${graphData}">
                    drawDcGraph(${graphData}, ${labelAliases}, ${from.getTime()}, ${to.getTime()}, 'dcChart');
                </g:if>

            });
    </asset:script>
</content>

</body>
</html>
