<%@ page defaultCodec="none" %></page>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isocsi.detailAnalysis"/></title>
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
        <g:if test="${errorList && !errorList.empty}">
            <div class="alert alert-error">
                <strong><g:message code="de.iteratec.isocsi.CsiDashboardController.selectionErrors.title"/></strong>
                <ul>
                    <g:each in="${errorList}">
                        <li><g:message error = "${it}"/></li>
                    </g:each>
                </ul>
            </div>
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
                            <g:render template="../eventResultDashboard/selectMeasurings"
                                      model="${['locationsOfBrowsers'             : locationsOfBrowsers,
                                                'eventsOfPages'                   : eventsOfPages,
                                                'folders'                         : folders,
                                                'selectedFolder'                  : selectedFolder,
                                                'pages'                           : pages,
                                                'selectedPage'                    : selectedPage,
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
                        <div class="accordion-group">
                            <div class="accordion-heading accordion-custom-heading">
                                <div class="row">
                                    <div class="span12">
                                        <div class="row">
                                            <div class="span3">
                                                <a class="accordion-toggle accordion-link fa fa-chevron-up"
                                                   data-toggle="collapse" data-parent="#accordion2"
                                                   href="#collapseThree">
                                                    <g:message code="de.iteratec.sri.wptrd.measurement.filter.heading"
                                                               default="Messwerte auw&auml;hlen"/>
                                                </a>
                                            </div>

                                            <div class="span2 accordion-info text-right">
                                                <g:message code="job.firstView.label" default="First View"/>:<br>
                                                <g:message code="job.repeatedView.label" default="Repeated View"/>:<br>
                                            </div>

                                            <div class="span7 accordion-info" id="accordion-info-measurements"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <g:if test="${request.queryString}"><div id="collapseThree" class="accordion-body collapse"></g:if>
                            <g:else><div id="collapseThree" class="accordion-body collapse in"></g:else>
                            <div class="accordion-inner">
                                <div class="span4">
                                    <label for="selectAggregatorUncachedHtmlId"><g:message
                                            code="de.iteratec.isr.wptrd.labels.filterFirstView"
                                            default="First View:"/></label>
                                    <g:if test="${selectedAggrGroupValuesUnCached.size() == 0}"><g:set
                                            var="selectedAggrGroupValuesUnCached"
                                            value="${['docCompleteTimeInMillisecsUncached']}"/></g:if>
                                    <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}"
                                                               id="selectAggregatorUncachedHtmlId"
                                                               class="iteratec-element-select-higher"
                                                               name="selectedAggrGroupValuesUnCached"
                                                               optionKey="value" optionValue="value"
                                                               multiple="true"
                                                               value="${selectedAggrGroupValuesUnCached}"/>
                                </div>

                                <div class="span4">
                                    <label for="selectAggregatorCachedHtmlId"><g:message
                                            code="de.iteratec.isr.wptrd.labels.filterRepeatedView"
                                            default="Repeated View:"/></label>
                                    <iteratec:optGroupedSelect id="selectAggregatorCachedHtmlId"
                                                               dataMap="${aggrGroupValuesCached}"
                                                               multiple="true" id="selectAggregatorCachedHtmlId"
                                                               class="iteratec-element-select-higher"
                                                               name="selectedAggrGroupValuesCached" optionKey="value"
                                                               optionValue="value"
                                                               value="${selectedAggrGroupValuesCached}"/>
                                </div>
                            </div>
                        </div>
                            <g:if test="${osmDetailAnalysisRequest}">
                                <div>
                                    <iframe id="osmDetailAnalysisRsultIFrame" src="${osmDetailAnalysisRequest}" style="width: 100%; height: 800px;overflow:hidden;" name="internal"
                                            scrolling="yes" marginwidth="0" marginheight="0" frameborder="0" vspace="0" hspace="0"></iframe>
                                </div>

                            </g:if>
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
        </form>
    </div>

    <g:if test="${request.queryString && command && !command.hasErrors() && !eventResultValues}">
        <div class="span12">
            <div class="alert alert-danger">
                <strong><g:message code="de.iteratec.ism.no.data.on.current.selection.heading"/></strong>
                <g:message code="de.iteratec.ism.no.data.on.current.selection"/>
            </div>
        </div>
    </g:if>
</div>

<content tag="include.bottom">
    <asset:javascript src="eventresultdashboard/eventResultDashboard.js"/>
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

        $(document).ready(function () {

        initSelectMeasuringsControls(pagesToEvents, browserToLocation, allMeasuredEventElements, allBrowsers, allLocations);

        doOnDomReady(
                '${dateFormat}',
        ${weekStart},
                    '${g.message(code: 'web.gui.jquery.chosen.multiselect.noresultstext', 'default': 'Keine Eintr&auml;ge gefunden f&uuml;r ')}'
            );
        });

    </asset:script>

</content>

</body>
</html>
