<%@ page defaultCodec="none" %></page>
<%@ page contentType="text/html;charset=UTF-8" %>
<% def springSecurityService %>
<%@ page import="de.iteratec.osm.report.UserspecificEventResultDashboard" %>
<g:set var="userspecificDashboardService" bean="userspecificDashboardService"/>
<html>
<head>

    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isocsi.eventResultDashboard"/></title>
    <asset:stylesheet src="rickshaw/rickshaw_custom.css"/>
    <asset:stylesheet src="eventResultDashboard/eventResultDashboard.less"/>

</head>

<body>

<h1>
    <g:message code="eventResultDashboard.label" default="Time Series"/>
    <a href="#" class="btn hidden" id="pageAggregationWithDataLink"><i class="fa fa-bar-chart"></i></a>
    <a href="#" class="btn hidden" id="distributionWithDataLink"><i class="fa fa-area-chart"></i></a>
    <g:if test="${grailsApplication.config.getProperty('grails.de.iteratec.osm.detailAnalysis.enablePersistenceOfDetailAnalysisData')?.equals("true")}">
        <a href="#" class="btn hidden" id="detailAnalysisWithDataLink"><i class="fa fa-pie-chart"></i></a>
    </g:if>
    <a href="#" class="btn hidden" id="resultListWithDataLink"><i class="fa fa-th-list"></i></a>
</h1>

<div class="row">
    <div class="col-md-12">
        <g:if test="${command}">
            <g:hasErrors bean="${command}">
                <div class="alert alert-danger">
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
    <g:if test="${request.queryString && (command && !command.hasErrors() || !command) && !eventResultValues}">
        <div class="col-md-12">
            <div class="alert alert-danger">
                <g:message code="de.iteratec.ism.no.data.on.current.selection"/>
            </div>
        </div>
    </g:if>

    <g:if test="${warnAboutExceededPointsPerGraphLimit}">
        <div class="col-md-12">
            <div class="alert alert-danger">
                <strong><g:message
                        code="de.iteratec.isr.EventResultDashboardController.warnAboutExceededPointsPerGraphLimit.title"/></strong>

                <p>
                    <g:message
                            code="de.iteratec.isr.EventResultDashboardController.warnAboutExceededPointsPerGraphLimit"/>
                </p>
            </div>
        </div>
    </g:if>

    <form method="get" action="" id="dashBoardParamsForm" data-caller="EventResult">
        <g:if test="${eventResultValues}">
            <div class="col-md-12">
                <div id="chartbox" class="card">
                    <div id="dataTableId" class="warning-ribbon" hidden="true" data-toggle="popover" aria-hidden="true"
                         title="${message([code: 'de.iteratec.osm.eventResultDashboard.hiddenFieldWarning'])}"
                         data-placement="right" data-trigger="hover"
                         data-html="true" data-content="${render(template: "hoverInfo")}"><p>!</p></div>
                    <g:render template="/highchart/chart"
                              model="[
                                      isAggregatedData             : (selectedInterval && selectedInterval != -1),
                                      chartData                    : eventResultValues,
                                      chartTitle                   : chartTitle,
                                      initialChartWidth            : chartWidth,
                                      initialChartHeight           : chartHeight,
                                      markerEnabled                : markerShouldBeEnabled,
                                      dataLabelsActivated          : labelShouldBeEnabled,
                                      highChartLabels              : highChartLabels,
                                      annotations                  : annotations,
                                      labelSummary                 : labelSummary,
                                      downloadPngLabel             : g.message(code: 'de.iteratec.ism.ui.button.save.name')
                              ]"/>
                </div>
            </div>
        </g:if>
</div>

<div class="row">
    <div class="col-md-12">
        <div class="alert alert-success renderInvisible" id="saveDashboardSuccessDiv">
            <g:message
                    code="de.iteratec.ism.ui.labels.save.success"
                    default="Successfully saved these settings as custom dashboard."/>
        </div>

        <div class="alert alert-danger renderInvisible" id="saveDashboardErrorDiv"></div>
        <g:if test="${warnAboutLongProcessingTime}">
            <div class="alert alert-warning">
                <strong><g:message
                        code="de.iteratec.isocsi.CsiDashboardController.warnAboutLongProcessingTime.title"/></strong>

                <p></p>

                <p>
                    <g:checkBox name="overwriteWarningAboutLongProcessingTime" value="${true}" checked="${true}"
                                style="display:none;"/>
                    <g:actionSubmit id="override-long-processing-time"
                                    value="${g.message(code: 'de.iteratec.isocsi.CsiDashboardController.warnAboutLongProcessingTime.checkbox.label', 'default': 'Go on')}"
                                    action="showAll" class="btn btn-warning"/>
                </p>
            </div>
        </g:if>
    </div>
</div>

<div class="row">
    <div class="col-md-12">
        <div class="btn-group pull-right" id="show-button-group">
            <g:actionSubmit value="${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default': 'Show')}"
                            action="showAll" id="graphButtonHtmlId" class="btn btn-primary show-button"/>
            <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown"
                    aria-haspopup="true" aria-expanded="false">
                <span class="caret"></span>
                <span class="sr-only">Toggle Dropdown</span>
            </button>
            <ul class="dropdown-menu" id="show-button-dropdown">
                <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_SUPER_ADMIN">
                    <g:set var="dropdownHasEntries" value="true"/>
                    <li>
                        <a id="createUserspecificDashboardButton" href="#CreateUserspecifiedDashboardModal"
                           data-toggle="modal" role="button">
                            ${message(code: 'de.iteratec.ism.ui.labels.save.custom.dashboard', default: 'Save these settings as custom dashboard')}
                        </a>
                    </li>
                </sec:ifAnyGranted>
                <g:if test="${params.dashboardID}">
                    <g:if test="${userspecificDashboardService.isCurrentUserDashboardOwner(params.dashboardID)}">
                        <g:set var="dropdownHasEntries" value="true"/>
                        <li>
                            <a href="#" role="button"
                               onclick="updateCustomDashboard('${dashboardName}', '${publiclyVisible}')">${message(code: 'de.iteratec.ism.ui.labels.update.custom.dashboard', default: 'Update custom dashboard')}</a>
                            <a href="#DeleteModal" role="button" data-toggle="modal">
                                ${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.delete', default: 'Delete')}
                            </a>
                        </li>
                    </g:if>
                </g:if>
                <g:if test="${dropdownHasEntries}">
                    <li class="divider"></li>
                </g:if>
                <g:if test="${availableDashboards}">
                    <li class="dropdown-header">
                        <g:message code="de.iteratec.isocsi.dashBoardControllers.custom.select.label"
                                   default="View a custom time series"/>
                    </li>
                    <g:each in="${availableDashboards}" var="availableDashboard">
                        <li><g:link action="showAll"
                                    params="[dashboardID: availableDashboard.dashboardID]">${availableDashboard.dashboardName}</g:link></li>
                    </g:each>
                </g:if>
                <g:else>
                    <li class="dropdown-header"><g:message
                            code="de.iteratec.isocsi.dashBoardControllers.custom.select.error.noneAvailable"
                            default="No saved dashboards."/></li>
                </g:else>
            </ul>
        </div>
        <g:actionSubmit value="${message(code: 'de.iteratec.ism.ui.labels.download.csv', 'default': 'Export as CSV')}"
                        action="downloadCsv" class="btn btn-primary pull-right space-right show-button"/>


        <g:render template="/_resultSelection/hiddenWarnings"/>
        <!-- Actual tabs -->
        <ul class="nav nav-tabs card-well-tabs" id="erd-card-tabs">
            <li class="active">
                <a data-toggle="tab" href="#tabJobSelection" id="tabJobSelectionElement">
                    <g:message code="de.iteratec.sri.wptrd.time.filter.heading"
                               default="Zeitraum ausw&auml;hlen"/>
                    &amp;
                    <g:message code="de.iteratec.sri.wptrd.jobs.filter.heading"
                               default="Jobs filtern"/>
                </a>
            </li>
            <li>
                <a data-toggle="tab" href="#tabVariableSelection" id="tabVariableSelectionElement">
                    <g:message code="de.iteratec.sri.wptrd.measurement.filter.heading"
                               default="Messwerte auw&auml;hlen"/>
                </a>
            </li>
        </ul>

        <div class="tab-content card-well">
            <div class="tab-pane in active" id="tabJobSelection">
                <g:render template="/_resultSelection/selectMeasuringsAndTimeFrame"
                          model="${['selectedTimeFrameInterval'      : selectedTimeFrameInterval,
                                    'from'                           : from,
                                    'fromHour'                       : fromHour,
                                    'to'                             : to,
                                    'toHour'                         : toHour,
                                    'selectedInterval'               : selectedInterval,
                                    'dateFormat'                     : dateFormat,
                                    'weekStart'                      : weekStart,
                                    'csiAggregationIntervals'        : csiAggregationIntervals,
                                    'locationsOfBrowsers'            : locationsOfBrowsers,
                                    'eventsOfPages'                  : eventsOfPages,
                                    'folders'                        : folders,
                                    'selectedFolder'                 : selectedFolder,
                                    'pages'                          : pages,
                                    'selectedPage'                   : selectedPage,
                                    'measuredEvents'                 : measuredEvents,
                                    'selectedAllMeasuredEvents'      : selectedAllMeasuredEvents,
                                    'selectedMeasuredEvents'         : selectedMeasuredEvents,
                                    'browsers'                       : browsers,
                                    'selectedBrowsers'               : selectedBrowsers,
                                    'selectedAllBrowsers'            : selectedAllBrowsers,
                                    'locations'                      : locations,
                                    'selectedLocations'              : selectedLocations,
                                    'selectedAllLocations'           : selectedAllLocations,
                                    'connectivityProfiles'           : connectivityProfiles,
                                    'selectedConnectivityProfiles'   : selectedConnectivityProfiles,
                                    'selectedAllConnectivityProfiles': selectedAllConnectivityProfiles]}"/>
            </div>

            <div class="tab-pane" id="tabVariableSelection">
                <g:render template="/_resultSelection/selectMeasuredVariables"
                          model="${['selectedAggrGroupValuesUnCached'   : selectedAggrGroupValuesUnCached,
                                    'docCompleteTimeInMillisecsUncached': docCompleteTimeInMillisecsUncached,
                                    'aggrGroupValuesUnCached'           : aggrGroupValuesUnCached,
                                    'aggrGroupValuesCached'             : aggrGroupValuesCached,
                                    'selectedAggrGroupValuesCached'     : selectedAggrGroupValuesCached,
                                    'trimBelowLoadTimes'                : trimBelowLoadTimes,
                                    'trimAboveLoadTimes'                : trimAboveLoadTimes,
                                    'trimBelowRequestCounts'            : trimBelowRequestCounts,
                                    'trimAboveRequestCounts'            : trimAboveRequestCounts,
                                    'trimBelowRequestSizes'             : trimBelowRequestSizes,
                                    'trimAboveRequestSizes'             : trimAboveRequestSizes]}"/>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <button class="reset-result-selection btn btn-default btn-sm" type="button" title="Reset">
                        <i class="fa fa-undo"></i> Reset
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
</form>
<g:render template="/_common/modals/createUserspecifiedDashboard" model="[item: item]"/>
<g:render template="/_common/modals/chartContextMenuErrorDialog"/>
<g:if test="${params.dashboardID}">
    <g:if test="${userspecificDashboardService.isCurrentUserDashboardOwner(params.dashboardID)}">
        <g:render template="/_common/modals/deleteDialog"
                  model="[item: [id: params.dashboardID], entityName: params.dashboardID]"/>
    </g:if>
</g:if>

<content tag="include.bottom">
    <asset:javascript src="eventresultdashboard/eventResultDashboard.js"/>
    <asset:javascript src="iteratecChartRickshaw.js"/>
    <asset:javascript src="chartSwitch"/>
    <asset:script type="text/javascript">

        var chartContextMenuI18N = ${i18n};

        function setAdjustments() {
            var chartTitle = "${chartTitle}",
                chartWidth = "${chartWidth}",
                chartHeight = "${chartHeight}",
                loadTimeMinimum = "${loadTimeMinimum}",
                loadTimeMaximum = "${loadTimeMaximum}",
                showDataMarkers = "${showDataMarkers}",
                showDataLabels = "${showDataLabels}",
                graphNameAliases = ${graphNameAliases},
                graphColors = ${graphColors};
            $("#dia-title").val(chartTitle);
            $("#dia-width").val(chartWidth < 0 ? "auto" : chartWidth);
            $("#dia-height").val(chartHeight < 0 ? "auto" : chartHeight);
            $("#dia-y-axis-max").val(loadTimeMaximum);
            $("#dia-y-axis-min").val(loadTimeMinimum);
            initGraphNameAliases(graphNameAliases);
            initGraphColors(graphColors);

            if (eval(showDataMarkers)) {
                $("#to-enable-marker").click();
            }
            if (eval(showDataLabels)) {
                $("#to-enable-label").click();
            }
        }

        $(document).ready(function () {

            doOnDomReady(
                '${g.message(code: 'web.gui.jquery.chosen.multiselect.noresultstext', 'default': 'Keine Eintr&auml;ge gefunden f&uuml;r ')}'
            );

            if (navigator.userAgent.indexOf('MSIE') === -1 && navigator.appVersion.indexOf('Trident/') <= 0 && navigator.appVersion.indexOf('Edge/') <= 0) {
                $("#download-dropdown").removeClass("hidden");
            }
            setAdjustments();
        });

        $(window).load(function() {
            if (!$("#graph_container").data("isAggregatedData")) {
                OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="charts/chartContextUtilities.js"/>')
            }
            OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/resultSelection.js"/>')
            OpenSpeedMonitor.ChartModules.UrlHandling.ChartSwitch.updateUrls(true);
        });

    </asset:script>
</content>

</body>
</html>
