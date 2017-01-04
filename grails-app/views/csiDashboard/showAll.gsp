<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="de.iteratec.osm.report.chart.AggregatorType" %>
<%@ page import="de.iteratec.osm.csi.CsiDashboardController" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityService" %>
<%@ page defaultCodec="none" %></page>
<% def springSecurityService %>
<%@ page import="de.iteratec.osm.report.UserspecificCsiDashboard" %>
<g:set var="userspecificDashboardService" bean="userspecificDashboardService"/>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isocsi.csiDashboard"/></title>

    <asset:stylesheet src="rickshaw/rickshaw_custom.css"/>
    <style>
    /* css for timepicker */
    .ui-timepicker-div .ui-widget-header {
        margin-bottom: 8px;
    }

    .ui-timepicker-div dl {
        text-align: left;
    }

    .ui-timepicker-div dl dt {
        height: 25px;
        margin-bottom: -25px;
    }

    .ui-timepicker-div dl dd {
        margin: 0 10px 10px 65px;
    }

    .ui-timepicker-div td {
        font-size: 90%;
    }

    .ui-tpicker-grid-label {
        background: none;
        border: none;
        margin: 0;
        padding: 0;
    }

    #csiTypeSelect {
        height: 20px;
    }


    </style>
</head>

<body>
<%-- main menu --%>
<g:render template="/layouts/mainMenu" model="${['availableDashboards': availableDashboards]}"/>

<%-- Überschrift --%>
<div class="row">
    <div class="col-md-12">
        <h3><g:message code="de.iteratec.isocsi.csi.heading" default="Kundenzufriedenheit (CSI)"/></h3>

        <p>
            <g:message code="de.iteratec.isocsi.csi.dashboard.measuring.description.short"
                       default="Den dargestellten, aggregierten Messwerten liegen jeweils die Webpagetest-Rohdaten des folgenden Intervals zugrunde."/>
        </p>

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

<form method="get" action="" id="dashBoardParamsForm">
    <g:if test="${wptCustomerSatisfactionValues}">
        <div class="row">
            <div class="col-md-12">
                <div id="chart-table-toggle" class="btn-group" data-toggle="buttons" id="job-filter-toggle">
                    <button type="button" class="btn btn-sm btn-default active" id="chart-toggle"><g:message
                            code="de.iteratec.isocsi.csi.button.graphView" default="Kurvendarstellung"/></button>
                    <button type="button" class="btn btn-sm btn-default" id="table-toggle"><g:message
                            code="de.iteratec.isocsi..csi.button.tableView" default="Tabellendarstellung"/></button>
                </div>
            </div>
        </div>

        <a name="chart-table"></a>

        <div class="row">
            <%-- chart --%>
            <g:set var="openDataPointLinksInNewWindow" value="false"/>
            <g:if test="${aggrGroupAndInterval == CsiDashboardController.HOURLY_MEASURED_EVENT}">
                <g:set var="openDataPointLinksInNewWindow" value="true"/>
            </g:if>

            %{-- chart title--}%
            <g:if test="${!chartTitle}">
                <g:if test="${aggrGroupAndInterval == CsiDashboardController.HOURLY_MEASURED_EVENT}">
                    <g:set var="chartTitle"
                           value="${g.message(code: 'de.iteratec.isocsi.CsiDashboardController.chart.measured_steps.title')}"/>
                    <g:set var="openDataPointLinksInNewWindow" value="true"/>
                </g:if>
                <g:elseif
                        test="${aggrGroupAndInterval == CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE || aggrGroupAndInterval == CsiDashboardController.DAILY_AGGR_GROUP_PAGE}">
                    <g:set var="chartTitle"
                           value="${g.message(code: 'de.iteratec.isocsi.CsiDashboardController.chart.pages.title')}"/>
                </g:elseif>
                <g:elseif
                        test="${aggrGroupAndInterval == CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP || aggrGroupAndInterval == CsiDashboardController.DAILY_AGGR_GROUP_SHOP}">
                    <g:set var="chartTitle"
                           value="${g.message(code: 'de.iteratec.isocsi.CsiDashboardController.chart.shops.title')}"/>
                </g:elseif>
                <g:elseif
                        test="${aggrGroupAndInterval == CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM || aggrGroupAndInterval == CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM}">
                    <g:set var="chartTitle"
                           value="${g.message(code: 'de.iteratec.isocsi.CsiDashboardController.chart.csiSystem.title')}"/>
                </g:elseif>
            </g:if>
            <div class="col-md-12 section">
                <div id="chartbox">
                    <g:render template="/highchart/chart"
                              model="[
                                      singleYAxis                  : 'false',
                                      chartData                    : wptCustomerSatisfactionValues,
                                      chartTitle                   : chartTitle,
                                      yAxisLabel                   : g.message(code: 'de.iteratec.isocsi.CsiDashboardController.chart.yType.label'),
                                      initialChartWidth            : chartWidth,
                                      initialChartHeight           : chartHeight,
                                      chartUnit                    : '%',
                                      globalLineWidth              : '2',
                                      xAxisMin                     : fromTimestampForHighChart,
                                      xAxisMax                     : toTimestampForHighChart,
                                      markerEnabled                : markerShouldBeEnabled,
                                      dataLabelsActivated          : labelShouldBeEnabled,
                                      yAxisScalable                : 'false',
                                      optimizeForExport            : 'false',
                                      openDataPointLinksInNewWindow: openDataPointLinksInNewWindow,
                                      annotations                  : annotations,
                                      labelSummary                 : labelSummary,
                                      downloadPngLabel             : null,
                                      downloadCsvSubmitButton      : g.actionSubmit([value: g.message(code: 'de.iteratec.ism.ui.labels.download.csv', 'default': 'Download CSV'), action: 'csiValuesCsv'])
                              ]"/>
                </div>
            </div>
            <%-- table --%>
            <div id="csi-table" class="col-md-12" style="display: none;">
                <g:if test="${flash.tableDataError}">
                    <div class="alert alert-danger">
                        <g:message error="${flash.tableDataError}"/>
                    </div>
                </g:if>
                <g:if test="${wptCustomerSatisfactionValuesForTable != null}">
                    <table class="table table-bordered">
                        <tbody>
                        <tr>
                            <td class="text-info">
                                ${wptCustomerSatisfactionValuesForTable.replace(';', '</td><td class="text-info">').replace('\n', '</td></tr><tr><td class="text-info">')}
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </g:if>
            </div>
        </div>
    </g:if>
    <g:else>
        <g:if test="${request.queryString}">
            <g:if test="${!warnAboutLongProcessingTime}">
                <div class="alert alert-info text-center" id="noDataForCurrentSelectionWarning">
                    <g:message code="de.iteratec.isocsi.CsiDashboardController.no.data.on.current.selection"/>
                </div>
            </g:if>
        </g:if>
    </g:else>
    <div class="row">
        <div class="col-md-12">
            <div class="alert alert-success renderInvisible" id="saveDashboardSuccessDiv"><g:message
                    code="de.iteratec.ism.ui.labels.save.success"
                    default="Successfully saved these settings as custom dashboard."/></div>

            <div class="alert alert-danger renderInvisible" id="saveDashboardErrorDiv"></div>
            <g:if test="${warnAboutLongProcessingTime}">
                <div class="alert alert-warning">
                    <strong><g:message
                            code="de.iteratec.isocsi.CsiDashboardController.warnAboutLongProcessingTime.title"/></strong>

                    <p>
                        <g:message
                                code="de.iteratec.isocsi.CsiDashboardController.warnAboutLongProcessingTime.message"/>
                    </p>

                    <p>
                        <g:checkBox name="overwriteWarningAboutLongProcessingTime" value="${true}" checked="${true}"
                                    style="display:none;"/>
                        <g:actionSubmit id="override-long-processing-time"
                                        value="${g.message(code: 'de.iteratec.isocsi.CsiDashboardController.warnAboutLongProcessingTime.checkbox.label', 'default': 'Go on')}"
                                        action="showAll" class="btn btn-warning"/>
                    </p>
                </div>
            </g:if>
            <div class="action-row">
                <div class="col-md-12">
                    <div class="btn-group pull-right">
                        <g:actionSubmit id="chart-submit"
                                        value="${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default': 'Show')}"
                                        action="showAll" class="btn btn-primary"/>
                        <sec:ifLoggedIn>
                            <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown"
                                    aria-haspopup="true" aria-expanded="false" id="chart-action-dropdown">
                                <span class="caret"></span>
                                <span class="sr-only">Toggle Dropdown</span>
                            </button>
                            <ul class="dropdown-menu">
                                <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_SUPER_ADMIN">
                                    <li class="separator"></li>
                                    <li>
                                        <a href="#CreateUserspecifiedDashboardModal" role="button" data-toggle="modal">
                                            ${message(code: 'de.iteratec.ism.ui.labels.save.custom.dashboard', default: 'Save these settings as custom dashboard')}
                                        </a>
                                    </li>
                                </sec:ifAnyGranted>
                                <g:if test="${params.dashboardID}">
                                    <g:if test="${userspecificDashboardService.isCurrentUserDashboardOwner(params.dashboardID)}">
                                        <li>
                                            <a href="#" role="button"
                                               onclick="updateCustomDashboard('${dashboardName}', '${publiclyVisible}')">
                                                ${message(code: 'de.iteratec.ism.ui.labels.update.custom.dashboard', default: 'Update custom dashboard')}
                                            </a>
                                            <a href="#DeleteModal" role="button" data-toggle="modal">
                                                ${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.delete', default: 'Delete')}
                                            </a>
                                        </li>
                                    </g:if>
                                </g:if>
                            </ul>
                        </sec:ifLoggedIn>
                    </div>
                </div>
            </div>

            <div class="row card-well">
                <div class="col-md-4">
                    <div class="card">
                        <fieldset>
                            <legend>
                                <g:message code="de.iteratec.isocsi.csi.aggreator.heading"
                                           default="Aggregator"/>
                            </legend>

                            <div id="aggregationRadioButtons">
                                <g:radioGroup name="aggrGroupAndInterval" labels="${aggrGroupLabels}"
                                              values="${aggrGroupValues}"
                                              value="${aggrGroupAndInterval}">
                                    <p>${it.radio} <g:message code="${it.label}"/></p>
                                </g:radioGroup>
                            </div>
                        </fieldset>
                    </div>
                    <g:render template="/_resultSelection/selectIntervalTimeframeCard"
                              model="${['selectedTimeFrameInterval': selectedTimeFrameInterval, 'from': from,
                                        'fromHour'                 : fromHour, 'to': to, 'toHour': toHour, 'showIncludeInterval': true,
                                        'includeInterval'          : includeInterval, 'dateFormat': dateFormat,
                                        'weekStart'                : weekStart]}"/>
                </div>

                <div class="col-md-3">
                    <div class="card" id="filter-navtab-csiSystem">
                        <legend>
                            <g:message code="de.iteratec.isr.wptrd.labels.filterCsiSystem" default="CSI System"/>
                        </legend>
                        <g:select id="folderSelectCsiSystem" class="form-control"
                                  name="selectedCsiSystems" from="${csiSystems}" optionKey="id"
                                  optionValue="label" value="${selectedCsiSystems}" multiple="true"/>
                    </div>

                    <div id="filter-navtab-jobGroup">
                        <g:render template="/_resultSelection/selectJobGroupCard"
                                  model="['folders'             : folders, 'selectedFolder': selectedFolder,
                                          'tagToJobGroupNameMap': tagToJobGroupNameMap, 'noAutoUpdate': true]"/>
                    </div>

                    <div class="card">
                        <legend>
                            <g:message code="de.iteratec.osm.csi.type.heading" default="CSI Type"/>
                        </legend>

                        <div class="checkbox">
                            <label for="csiTypeDocComplete">
                                <input type="checkbox" name="csiTypeDocComplete" id="csiTypeDocComplete" <g:if
                                        test="${csiTypeDocComplete || (!csiTypeDocComplete && !csiTypeVisuallyComplete)}">
                                    checked
                                </g:if>/>
                                &nbsp;${message(code: "de.iteratec.osm.csi.type.byDocComplete.label", default: "Doc Complete")}
                            </label>
                        </div>

                        <div class="checkbox">
                            <label for="csiTypeVisuallyComplete">
                                <input type="checkbox" name="csiTypeVisuallyComplete"
                                       id="csiTypeVisuallyComplete" <g:if test="${csiTypeVisuallyComplete}">
                                    checked
                                </g:if>/>
                                &nbsp;${message(code: "de.iteratec.osm.csi.type.byVisuallyComplete.label", default: "Visually Complete")}
                            </label>
                        </div>
                    </div>
                </div>
                %{--the rest----------------------------------------------------------------------------------------------}%
                <div id="filter-complete-tabbable" class="col-md-5">
                    <g:render template="/_resultSelection/selectPageLocationConnectivityCard"
                              model="['locationsOfBrowsers'            : locationsOfBrowsers,
                                      'eventsOfPages'                  : eventsOfPages,
                                      'pages'                          : pages,
                                      'selectedPages'                  : selectedPages,
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
                                      'selectedAllConnectivityProfiles': selectedAllConnectivityProfiles,
                                      'noAutoUpdate'                   : true]"/>
                </div>
            </div>
            <g:if test="${exceedsTimeframeBoundary}">
                <g:if test="${selectedInterval.intervalInMinutes == 60}">
                    <p class="text-danger"><g:message
                            code="de.iteratec.isocsi.csi.timeframe.boundary.hourly.exceeded"
                            default="Gew&auml;hlter Zeitraum zu gro&szlig;"/></p>
                </g:if>
                <g:elseif test="${selectedInterval.intervalInMinutes == 60 * 24}">
                    <p class="text-danger"><g:message code="de.iteratec.isocsi.csi.timeframe.boundary.daily.exceeded"
                                                      default="Gew&auml;hlter Zeitraum zu gro&szlig;"/></p>
                </g:elseif>
                <g:elseif test="${selectedInterval.intervalInMinutes == 60 * 24 * 7}">
                    <p class="text-danger"><g:message
                            code="de.iteratec.isocsi.csi.timeframe.boundary.weekly.exceeded"
                            default="Gew&auml;hlter Zeitraum zu gro&szlig;"/></p>
                </g:elseif>
                <g:else>
                    <p class="text-danger"><g:message code="de.iteratec.isocsi.csi.timeframe.boundary.exceeded"
                                                      default="Gew&auml;hlter Zeitraum zu gro&szlig;"/></p>
                </g:else>
            </g:if>
        </div>
    </div>
</form>
<g:render template="/_common/modals/createUserspecifiedDashboard" model="[item: item]"/>
<g:if test="${params.dashboardID}">
    <g:if test="${userspecificDashboardService.isCurrentUserDashboardOwner(params.dashboardID)}">
        <g:render template="/_common/modals/deleteDialog"
                  model="[item: [id: params.dashboardID], entityName: params.dashboardID]"/>
    </g:if>
</g:if>

<content tag="include.bottom">
    <asset:javascript src="csidashboard/showAll.js"/>
    <asset:script type="text/javascript">

        var selectedCsiSystems = [];
        <g:each var="csiSystem" in="${selectedCsiSystems}">
            selectedCsiSystems.push(${csiSystem});
        </g:each>

        function setAdjustments() {
            var chartTitle = "${chartTitle}";
            var chartWidth = "${chartWidth}";
            var chartHeight = "${chartHeight}";
            var loadTimeMinimum = "${loadTimeMinimum}";
            var loadTimeMaximum = "${loadTimeMaximum}";
            var showDataMarkers = "${showDataMarkers}";
            var showDataLabels = "${showDataLabels}";
            var optimizeForWideScreen = "${showDataLabels}";
            var graphNameAliases = ${graphNameAliases};
            var graphColors = ${graphColors};
            $("#dia-title").val(chartTitle);
            $("#dia-width").val(chartWidth < 0 ? "auto" : chartWidth);
            $("#dia-height").val(chartHeight < 0 ? "auto" : chartHeight);
            $(".dia-y-axis-max").val(loadTimeMaximum);
            $(".dia-y-axis-min").val(loadTimeMinimum);
            if (eval(showDataMarkers)) {
                $("#to-enable-marker").click();
            }
            if (eval(showDataLabels)) {
                $("#to-enable-label").click();
            }
            initGraphNameAliases(graphNameAliases);
            initGraphColors(graphColors);
        }

        $(document).ready(function () {

            doOnDomReady(
                    '${g.message(code: 'web.gui.jquery.chosen.multiselect.noresultstext', 'default': 'Keine Eintr&auml;ge gefunden f&uuml;r ')}'
            )

            if (navigator.userAgent.indexOf('MSIE') === -1 && navigator.appVersion.indexOf('Trident/') <= 0 && navigator.appVersion.indexOf('Edge/') <= 0) {
                $("#download-dropdown").removeClass("hidden");
            }
            setAdjustments();

        });
        $(window).load(function() {
           OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/resultSelection.js"
                                                                    absolute="true"/>')
        });

    </asset:script>
</content>

</body>

</html>
