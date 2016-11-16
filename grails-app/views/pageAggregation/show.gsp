<%@ page defaultCodec="none" %></page>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isocsi.pageAggregation" default="Page Aggregation"/></title>
</head>

<body>

<%-- main menu --%>
<g:render template="/layouts/mainMenu"/>

<div class="row">
    <div class="col-md-12">
        <%-- heading --%>
        <h3><g:message code="de.iteratec.isocsi.pageAggregation" default="Page Aggregation"/></h3>

        <p>
            <g:message code="de.iteratec.isocsi.pageAggregation.description.short"
                       default="The webpagetest raw data of the respective interval is the basis for the displayed mean values."/>
        </p>

        %{-- no data message --}%
        <div id="no-data-div" class="col-md-12 hidden">
            <div class="alert alert-danger">
                <strong><g:message code="de.iteratec.ism.no.data.on.current.selection.heading"/></strong>
                <g:message code="de.iteratec.ism.no.data.on.current.selection"/>
            </div>
        </div>
        %{-- error messages --}%
        <div id="error-div" class="col-md-12 hidden">
            <div class="alert alert-danger">
                <strong><g:message code="de.iteratec.isocsi.CsiDashboardController.selectionErrors.title"/></strong>

                <div id="error-message"></div>
            </div>
        </div>
    </div>
</div>


<g:render template="barChart"/>

<div class="row">
    <div class="col-md-12">
        <form id="dashBoardParamsForm">
            <!-- Split button to show/download -->
            <div class="btn-group pull-right">
                <button type="button" onClick="drawGraph()" id="graphButtonHtmlId" class="btn btn-primary">
                    ${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default': 'Show')}</button>
                <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown"
                        aria-haspopup="true" aria-expanded="false">
                    <span class="caret"></span>
                    <span class="sr-only">Toggle Dropdown</span>
                </button>
                <ul class="dropdown-menu">
                    <li>
                        <a href="#" id="dia-save-chart-as-png" class="btn btn-primary btn-sm disabled" role="button"
                           style="vertical-align: top;" onclick="downloadBarchart()">
                            <g:message code="de.iteratec.ism.ui.button.save.name"/>
                        </a>
                    </li>
                </ul>
            </div>
            <!-- Actual tabs -->
            <ul class="nav nav-tabs card-well-tabs">
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
                    <div class="row">
                        <div class="col-md-4">
                            <g:render template="/_resultSelection/selectIntervalTimeframeCard"
                                      model="${['selectedTimeFrameInterval': selectedTimeFrameInterval, 'from': from,
                                                'fromHour'                 : fromHour, 'to': to, 'toHour': toHour, 'showIncludeInterval': false,
                                                'includeInterval'          : includeInterval]}"/>
                        </div>

                        <div class="col-md-3">

                            <div id="filter-navtab-jobGroup">
                                <g:render template="/_resultSelection/selectJobGroupCard"
                                          model="['folders'             : folders, 'selectedFolder': selectedFolder,
                                                  'tagToJobGroupNameMap': tagToJobGroupNameMap]"/>
                            </div>
                        </div>
                        %{--the rest----------------------------------------------------------------------------------------------}%
                        <div id="filter-complete-tabbable" class="col-md-5">
                            <g:render template="/_resultSelection/selectPageLocationConnectivityCard"
                                      model="['locationsOfBrowsers'             : locationsOfBrowsers,
                                              'eventsOfPages'                   : eventsOfPages,
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
                        </div>
                    </div>
                </div>

                <div class="tab-pane" id="tabVariableSelection">
                    <div class="row">
                        <div class="col-md-4">
                            <g:render template="/_resultSelection/firstViewCard" model="[
                                    selectedAggrGroupValuesUnCached: selectedAggrGroupValuesUnCached,
                                    aggrGroupValuesUnCached        : aggrGroupValuesUnCached,
                                    selectedAggrGroupValuesUnCached: selectedAggrGroupValuesUnCached
                            ]"/>
                        </div>

                        <div class="col-md-3">
                            <g:render template="/_resultSelection/trimValuesCard" model="[
                                    trimBelowLoadTimes    : trimBelowLoadTimes,
                                    trimAboveLoadTimes    : trimAboveLoadTimes,
                                    trimBelowRequestCounts: trimBelowRequestCounts,
                                    trimAboveRequestCounts: trimAboveRequestCounts,
                                    trimBelowRequestSizes : trimBelowRequestSizes,
                                    trimAboveRequestSizes : trimAboveRequestSizes
                            ]"/>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>

<content tag="include.bottom">
    <asset:javascript src="csidashboard/csiDashboard.js"/>
    <asset:javascript src="pngDownloader.js"/>
    <asset:script type="text/javascript">

        $(document).ready(function () {
            doOnDomReady(
                    '${dateFormat}',
        ${weekStart},
                    '${g.message(code: 'web.gui.jquery.chosen.multiselect.noresultstext', 'default': 'Keine Eintr&auml;ge gefunden f&uuml;r ')}'
            );
        });

        function drawGraph() {
            var selectedTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();
            $.ajax({
                type: 'POST',
                data: {
                    from: selectedTimeFrame[0].toISOString(),
                    to: selectedTimeFrame[1].toISOString(),
                    selectedJobGroups: JSON.stringify($.map($("#folderSelectHtmlId option:selected"), function (e) {
                        return $(e).text()
                    })),
                    selectedPages: JSON.stringify($.map($("#pageSelectHtmlId option:selected"), function (e) {
                        return $(e).text()
                    }))
                },
                url: "${createLink(controller: 'pageAggregation', action: 'getBarchartData')}",
                dataType: "json",
                complete: function() {
                },
                success: function (data) {
                    if (!$.isEmptyObject(data)) {
                        OpenSpeedMonitor.ChartModules.PageAggregation("svg-container").drawChart(data);
                        $("#dia-save-chart-as-png").removeClass("disabled");
                        if (!$("#no-data-div").hasClass("hidden"))
                            $("#no-data-div").addClass("hidden");
                        if (!$("#error-div").hasClass("hidden"))
                            $("#error-div").addClass("hidden")
                    } else {
                        $("#no-data-div").removeClass("hidden")
                    }
                },
                error: function (e) {
                    $("#error-div").removeClass("hidden");
                    $("#error-message").html(e.responseText);
                }
            });
        }

        function downloadBarchart() {
            var svgNode = d3.select("svg").node();
            downloadAsPNG(svgNode, "barchart.png", parseInt(svgNode.getAttribute("width"), 10), parseInt(svgNode.getAttribute("height"), 10));
        }

    </asset:script>
</content>

</body>
</html>
