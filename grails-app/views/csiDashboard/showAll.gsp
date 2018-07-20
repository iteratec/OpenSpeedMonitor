<%@ page contentType="text/html;charset=UTF-8" %>
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
    <asset:stylesheet src="csiDashboard/csiDashboard.less"/>
</head>

<body>
<h1>
    <g:message code="de.iteratec.isocsi.csi.heading" default="Customer Satisfaction Index (CSI)"/>
</h1>

<p>
    <g:message code="de.iteratec.isocsi.csi.dashboard.measuring.description.short"
               default="Den dargestellten, aggregierten Messwerten liegen jeweils die Webpagetest-Rohdaten des folgenden Intervals zugrunde."/>
</p>

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

<form method="get" action="" id="dashBoardParamsForm" data-caller="CsiAggregation">
    <g:if test="${wptCustomerSatisfactionValues}">
        <ul class="nav nav-tabs card-tabs">
            <li class="active">
                <a data-toggle="tab" href="#chart-graph" id="chart-toggle">
                    <g:message code="de.iteratec.isocsi.csi.button.graphView" default="Kurvendarstellung"/>
                </a>
            </li>
            <li>
                <a data-toggle="tab" href="#csi-table" id="tabVariableSelectionElement" id="table-toggle">
                    <g:message code="de.iteratec.isocsi..csi.button.tableView" default="Tabellendarstellung"/>
                </a>
            </li>
        </ul>

        <div class="tab-content card">
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
            <div class="tab-pane in active" id="chart-graph">
                <div id="chartbox">
                    <g:render template="/highchart/chart"
                              model="[
                                      isAggregatedData             : true,
                                      chartData                    : wptCustomerSatisfactionValues,
                                      chartTitle                   : chartTitle,
                                      initialChartWidth            : chartWidth,
                                      initialChartHeight           : chartHeight,
                                      showDataMarkers              : showDataMarkers,
                                      showDataLabels               : showDataLabels,
                                      highChartLabels              : highChartLabels,
                                      annotations                  : annotations,
                                      labelSummary                 : labelSummary,
                                      downloadPngLabel             : g.message(code: 'de.iteratec.ism.ui.button.save.name')
                              ]"/>
                </div>
            </div>
            <%-- table --%>
            <div id="csi-table" class="tab-pane">
                <g:if test="${flash.tableDataError}">
                    <div class="alert alert-danger">
                        <g:message error="${flash.tableDataError}"/>
                    </div>
                </g:if>
                <g:if test="${wptCustomerSatisfactionValuesForTable != null}">
                    <table class="table table-striped">
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
                                        action="showAll" class="btn btn-primary show-button"/>
                        <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown"
                                aria-haspopup="true" aria-expanded="false" id="chart-action-dropdown">
                            <span class="caret"></span>
                            <span class="sr-only">Toggle Dropdown</span>
                        </button>
                        <ul class="dropdown-menu chart-action-dropdown-menu" id="show-button-dropdown">
                            <sec:ifLoggedIn>
                                <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_SUPER_ADMIN">
                                    <g:set var="dropdownHasEntries" value="true"/>
                                    <li>
                                        <a href="#CreateUserspecifiedDashboardModal" role="button" data-toggle="modal"
                                           class="show-button">
                                            ${message(code: 'de.iteratec.ism.ui.labels.save.custom.dashboard', default: 'Save these settings as custom dashboard')}
                                        </a>
                                    </li>
                                </sec:ifAnyGranted>
                                <g:if test="${params.dashboardID}">
                                    <g:if test="${userspecificDashboardService.isCurrentUserDashboardOwner(params.dashboardID)}">
                                        <g:set var="dropdownHasEntries" value="true"/>
                                        <li>
                                            <a href="#" role="button" class="show-button"
                                               onclick="updateCustomDashboard('${dashboardName}', '${publiclyVisible}')">
                                                ${message(code: 'de.iteratec.ism.ui.labels.update.custom.dashboard', default: 'Update custom dashboard')}
                                            </a>
                                            <a href="#DeleteModal" role="button" data-toggle="modal">
                                                ${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.delete', default: 'Delete')}
                                            </a>
                                        </li>
                                    </g:if>
                                </g:if>
                            </sec:ifLoggedIn>
                            <g:if test="${dropdownHasEntries}">
                                <li class="divider"></li>
                            </g:if>
                            <g:if test="${availableDashboards}">
                                <li class="dropdown-header">
                                    <g:message code="de.iteratec.isocsi.dashBoardControllers.custom.select.label"
                                               default="View a custom time series"/>
                                </li>
                                <g:each in="${availableDashboards}" var="availableDashboard">
                                    <li><g:link controller="${affectedController}" action="showAll"
                                                class="custom-dashboard"
                                                params="[dashboardID: availableDashboard.dashboardID]">${availableDashboard.dashboardName}</g:link></li>
                                </g:each>
                            </g:if>
                            <g:else>
                                <li class="dropdown-header"><g:message
                                        code="de.iteratec.isocsi.dashBoardControllers.custom.select.error.noneAvailable"
                                        default="None available"/></li>
                            </g:else>
                        </ul>
                    </div>
                    <g:actionSubmit value="${message(code: 'de.iteratec.ism.ui.labels.download.csv', 'default': 'Export as CSV')}"
                                    action="csiValuesCsv" class="btn btn-primary pull-right space-right show-button"/>

                    <g:render template="/_resultSelection/hiddenWarnings"/>
                </div>
            </div>

            <div class="row card-well">
                <div class="col-md-4">
                    <div class="card">
                        <fieldset>
                            <h2>
                                <g:message code="de.iteratec.isocsi.csi.aggreator.heading"
                                           default="Aggregator"/>
                            </h2>

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
                        <h2>
                            <g:message code="de.iteratec.isr.wptrd.labels.filterCsiSystem" default="CSI System"/>
                        </h2>
                        <g:select id="folderSelectCsiSystem" class="form-control"
                                  name="selectedCsiSystems" from="${csiSystems}" optionKey="id"
                                  optionValue="label" value="${selectedCsiSystems}" multiple="true"/>
                    </div>

                    <div id="filter-navtab-jobGroup">
                        <g:render template="/_resultSelection/selectJobGroupCard"
                                  model="['folders'             : folders, 'selectedFolder': selectedFolder,
                                          'tagToJobGroupNameMap': tagToJobGroupNameMap]"/>
                    </div>

                    <div class="card">
                        <h2>
                            <g:message code="de.iteratec.osm.csi.type.heading" default="CSI Type"/>
                        </h2>

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
                                      avaiableConnectivities           : avaiableConnectivities,
                                      'selectedConnectivityProfiles'   : selectedConnectivityProfiles,
                                      'selectedAllConnectivityProfiles': selectedAllConnectivityProfiles]"/>
                </div>
            </div>
        </div>
    </div>
</form>
<button class="reset-result-selection btn btn-default btn-sm" type="button" title="Reset">
    <i class="fas fa-undo"></i> Reset
</button>
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
        $(window).on('load', function() {
           OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/resultSelection.js" />', 'resultSelection');
        });

    </asset:script>
</content>

</body>

</html>
