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
<g:render template="/layouts/mainMenu"/>
<%-- user specific dashboards --%>
<div class="row">
    <div class="col-md-12">
        <div class="btn-group">
            <a class="btn btn-primary btn-small dropdown-toggle" data-toggle="dropdown" href="#">
                <g:message code="de.iteratec.isocsi.dashBoardControllers.custom.select.label"
                           default="Dashboard-Ansicht ausw&auml;hlen"/>
                <span class="caret"></span>
            </a>
            <ul class="dropdown-menu">

                <g:set var="availableDashboards"
                       value="${userspecificDashboardService.getListOfAvailableCsiDashboards()}"/>

                <g:if test="${availableDashboards.size() > 0}">
                    <g:each in="${availableDashboards}" var="availableDashboard">
                        <li><g:link action="showAll"
                                    params="[dashboardID: availableDashboard.dashboardID]">${availableDashboard.dashboardName}</g:link></li>
                    </g:each>
                </g:if>
                <g:else>
                    <g:set var="anchorDashboardCreation" value="#"/>
                    <sec:ifLoggedIn>
                        <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_SUPER_ADMIN">
                            <g:set var="anchorDashboardCreation" value="#bottomCommitButtons"/>
                        </sec:ifAnyGranted>
                    </sec:ifLoggedIn>
                    <li><a href="${anchorDashboardCreation}"><g:message
                            code="de.iteratec.isocsi.dashBoardControllers.custom.select.error.noneAvailable"
                            default="Es sind keine verf&uuml;gbar - bitte legen Sie eine an!"/></a></li>
                </g:else>
            </ul>
        </div>
    </div>
</div>

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

        <form method="get" action="" id="dashBoardParamsForm">
            <div class="alert alert-success renderInvisible" id="saveDashboardSuccessDiv"><g:message
                    code="de.iteratec.ism.ui.labels.save.success"
                    default="Successfully saved these settings as custom dashboard."/></div>

            <div class="alert alert-error renderInvisible" id="saveDashboardErrorDiv"></div>
            <g:if test="${warnAboutLongProcessingTime}">
                <div class="alert">
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


            <div class="row">
                <div class="col-md-6">
                    <fieldset>
                        <legend><g:message code="de.iteratec.isocsi.csi.aggreator.heading"
                                           default="Aggregator"/></legend>

                        <div>
                            <g:radioGroup name="aggrGroupAndInterval" labels="${aggrGroupLabels}"
                                          values="${aggrGroupValues}"
                                          value="${aggrGroupAndInterval}">
                                <p>${it.radio} <g:message code="${it.label}"/></p>
                            </g:radioGroup>
                        </div>
                    </fieldset>
                </div>

                <div class="col-md-6">
                    <g:render template="/dateSelection/startAndEnddateSelection"
                              model="${['selectedTimeFrameInterval': selectedTimeFrameInterval, 'from': from, 'fromHour': fromHour, 'to': to, 'toHour': toHour, 'includeInterval': includeInterval]}"/>

                    <fieldset id="includeInterval">
                        <div class="col-md-3">
                            <label class="checkbox inline">
                                <g:checkBox name="includeInterval" id="includeInterval" checked="${includeInterval}"/>
                                &nbsp;<g:message code="de.iteratec.isocsi.csi.includeInterval.label"
                                                 default="auch&nbsp;aktuelles&nbsp;Intervall&nbsp;anzeigen"/>
                            </label>
                        </div>
                    </fieldset>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <legend>
                        <g:message code="de.iteratec.isocsi.csi.filter.heading" default="Filter"/>
                    </legend>
                </div>
            </div>

            <div class="row">
                <div class="col-md-4" id="filter-navtab-csiSystem">
                    <div style="padding-top: 60px;"></div>
                    <label for="folderSelectCsiSystem">
                        <strong>
                            <g:message code="de.iteratec.isr.wptrd.labels.filterCsiSystem" default="CSI System"/>:
                        </strong>
                    </label>
                    <g:select id="folderSelectCsiSystem" class="iteratec-element-select"
                              name="selectedCsiSystems" from="${csiSystems}" optionKey="id"
                              optionValue="label" value="${selectedCsiSystems}"
                              multiple="true"/>
                </div>
            </div>
            <g:render template="/eventResultDashboard/selectMeasurings"
                      model="[
                              'locationsOfBrowsers'             : locationsOfBrowsers,
                              'eventsOfPages'                   : eventsOfPages,
                              'folders'                         : folders,
                              'selectedFolder'                  : selectedFolder,
                              'pages'                           : pages,
                              'selectedPages'                   : selectedPages,
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
                              'showExtendedConnectivitySettings': false]"/>
            <div style="clear:both;"></div>

            <div class="row">
                <div class="col-md-12">
                    <legend>
                        <g:message code="de.iteratec.osm.csi.type.heading" default="CSI Type"/>
                    </legend>

                    <div class="col-md-12">
                        <div class="control-group">
                            <div class="controls">
                                <label class="checkbox" for="csiTypeDocComplete">
                                    <input type="checkbox" name="csiTypeDocComplete" id="csiTypeDocComplete" <g:if
                                            test="${csiTypeDocComplete || (!csiTypeDocComplete && !csiTypeVisuallyComplete)}">
                                        checked
                                    </g:if>/>
                                    &nbsp;${message(code: "de.iteratec.osm.csi.type.byDocComplete.label", default: "Doc Complete")}
                                </label>
                                <label class="checkbox" for="csiTypeVisuallyComplete">
                                    <input type="checkbox" name="csiTypeVisuallyComplete"
                                           id="csiTypeVisuallyComplete" <g:if test="${csiTypeVisuallyComplete}">
                                        checked
                                    </g:if>/>
                                    &nbsp;${message(code: "de.iteratec.osm.csi.type.byVisuallyComplete.label", default: "Visually Complete")}
                                </label>
                            </div>
                        </div>
                    </div>
                </div>

                <p>
                    <g:actionSubmit id="chart-submit"
                                    value="${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default': 'Show')}"
                                    action="showAll" class="btn btn-primary" style="margin-top: 16px;"/>
                    <g:actionSubmit
                            value="${g.message(code: 'de.iteratec.ism.ui.labels.download.csv', 'default': 'As CSV')}"
                            action="csiValuesCsv" class="btn btn-primary" style="margin-top: 16px;"/>
                    <sec:ifLoggedIn>
                        <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_SUPER_ADMIN">
                            <a href="#CreateUserspecifiedDashboardModal" role="button" class="btn btn-primary"
                               data-toggle="modal"
                               style="margin-top: 16px;">${message(code: 'de.iteratec.ism.ui.labels.save.custom.dashboard', default: 'Save these settings as custom dashboard')}</a>
                        </sec:ifAnyGranted>
                    </sec:ifLoggedIn>
                    <g:if test="${params.dashboardID}">
                        <g:if test="${userspecificDashboardService.isCurrentUserDashboardOwner(params.dashboardID)}">
                            <a href="#" role="button" class="btn btn-primary"
                               style="margin-top: 16px;"
                               onclick="updateCustomDashboard('${dashboardName}', '${publiclyVisible}')">${message(code: 'de.iteratec.ism.ui.labels.update.custom.dashboard', default: 'Update custom dashboard')}</a>
                            <g:render template="/_common/modals/deleteCustomDashboard"/>
                        </g:if>
                    </g:if>
                </p>
                <g:if test="${exceedsTimeframeBoundary}">
                    <g:if test="${selectedInterval.intervalInMinutes == 60}">
                        <p class="text-error"><g:message
                                code="de.iteratec.isocsi.csi.timeframe.boundary.hourly.exceeded"
                                default="Gew&auml;hlter Zeitraum zu gro&szlig;"/></p>
                    </g:if>
                    <g:elseif test="${selectedInterval.intervalInMinutes == 60 * 24}">
                        <p class="text-error"><g:message code="de.iteratec.isocsi.csi.timeframe.boundary.daily.exceeded"
                                                         default="Gew&auml;hlter Zeitraum zu gro&szlig;"/></p>
                    </g:elseif>
                    <g:elseif test="${selectedInterval.intervalInMinutes == 60 * 24 * 7}">
                        <p class="text-error"><g:message
                                code="de.iteratec.isocsi.csi.timeframe.boundary.weekly.exceeded"
                                default="Gew&auml;hlter Zeitraum zu gro&szlig;"/></p>
                    </g:elseif>
                    <g:else>
                        <p class="text-error"><g:message code="de.iteratec.isocsi.csi.timeframe.boundary.exceeded"
                                                         default="Gew&auml;hlter Zeitraum zu gro&szlig;"/></p>
                    </g:else>
                </g:if>
        </form>
    </div>
</div>

<g:if test="${wptCustomerSatisfactionValues}">
    <hr>

    <div class="row">
        <div class="col-md-1">
            <div id="chart-table-toggle" class="btn-group" data-toggle="buttons-radio" id="job-filter-toggle">
                <button type="button" class="btn btn-small active" id="chart-toggle"><g:message
                        code="de.iteratec.isocsi.csi.button.graphView" default="Kurvendarstellung"/></button>
                <button type="button" class="btn btn-small" id="table-toggle"><g:message
                        code="de.iteratec.isocsi..csi.button.tableView" default="Tabellendarstellung"/></button>
            </div>
        </div>
    </div>

    <br>

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
        <div id="chartbox">
            <div class="col-md-12 well">
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
                          ]"/>
            </div>
        </div>
        <%-- table --%>
        <div id="csi-table" class="col-md-12" style="display: none;">
            <g:if test="${flash.tableDataError}">
                <div class="alert alert-error">
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
            <div class="row">
                <div class="col-md-12" id="noDataForCurrentSelectionWarning">
                    <strong><g:message
                            code="de.iteratec.isocsi.CsiDashboardController.no.data.on.current.selection"/></strong>
                </div>
            </div>
        </g:if>
    </g:if>
</g:else>

<g:render template="/_common/modals/createUserspecifiedDashboard" model="[item: item]"/>

<content tag="include.bottom">
    <asset:javascript src="csidashboard/csiDashboard.js"/>
    <asset:javascript src="iteratecChartRickshaw.js"/>
    <asset:script type="text/javascript">

        var pagesToEvents = [];
        <g:each var="page" in="${pages}">
        <g:if test="${eventsOfPages[page.id] != null}">
        pagesToEvents[${page.id}] = [<g:each var="event" in="${eventsOfPages[page.id]}">${event}, </g:each>];
        </g:if>
        </g:each>

        var browserToLocation = [];
        <g:each var="browser" in="${browsers}">
        <g:if test="${locationsOfBrowsers[browser.id] != null}">
        browserToLocation[${browser.id}] = [<g:each var="location"
                                                            in="${locationsOfBrowsers[browser.id]}">${location}, </g:each>];
        </g:if>
        </g:each>

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
            $("#dia-width").val(chartWidth);
            $("#dia-height").val(chartHeight);
            $("#dia-y-axis-max").val(loadTimeMaximum);
            $("#dia-y-axis-min").val(loadTimeMinimum);
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

            initSelectMeasuringsControls(pagesToEvents, browserToLocation, allMeasuredEventElements, allBrowsers, allLocations);

            doOnDomReady(
                    'dd.mm.yyyy',
                    ${weekStart},
                    '${g.message(code: 'web.gui.jquery.chosen.multiselect.noresultstext', 'default': 'Keine Eintr&auml;ge gefunden f&uuml;r ')}'
            )

            <g:if test="${dashboardName}">
                updateDateTimePicker(${from.getTime()}, ${to.getTime()}, '${fromHour}', '${toHour}', ${selectedTimeFrameInterval});
            </g:if>

            if (navigator.userAgent.indexOf('MSIE') !== -1 || navigator.appVersion.indexOf('Trident/') > 0 || navigator.appVersion.indexOf('Edge/') > 0) {
                $("#dia-save-chart-as-png").removeClass("btn-primary");
                $("#dia-save-chart-as-png").addClass("btn-primary.disabled");
                $("#dia-save-chart-as-png").attr("disabled", "disabled");
                $("#dia-save-chart-as-png").attr("title", "<g:message
            code="de.iteratec.ism.ui.button.save.disabled.tooltip"/>");
            }
            setAdjustments();

        });

    </asset:script>
</content>

</body>

</html>
