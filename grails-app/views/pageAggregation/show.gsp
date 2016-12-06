<%@ page defaultCodec="none" %></page>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isocsi.pageAggregation" default="Page Aggregation"/></title>
    <asset:stylesheet src="/pageAggregation/show.css"/>

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
            <div class="action-row">
                <div class="col-md-12">

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
                                <a href="#downloadAsPngModal" id="dia-save-chart-as-png"
                                   class="btn btn-primary btn-sm disabled"
                                   data-toggle="modal" role="button"
                                   style="vertical-align: top;" onclick="setDefaultValues('svg-container')">
                                    <g:message code="de.iteratec.ism.ui.button.save.name"/>
                                </a>
                            </li>
                        </ul>
                    </div>

                </div>
            </div>

            <div class="row card-well">
                <div class="col-md-4">
                    <g:render template="/_resultSelection/selectIntervalTimeframeCard"
                              model="${['selectedTimeFrameInterval': selectedTimeFrameInterval, 'from': from,
                                        'fromHour'                 : fromHour, 'to': to, 'toHour': toHour, 'showIncludeInterval': false,
                                        'includeInterval'          : includeInterval]}"/>

                    <g:render template="/_resultSelection/selectBarchartMeasurings" model="[
                            aggrGroupValuesUnCached: aggrGroupValuesUnCached
                    ]"/>
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
                    <div class="card" id="select-page-location">
                        <legend>
                            <g:message code="de.iteratec.osm.result.page.label" default="Page"/>
                        </legend>
                        <g:render template="/_resultSelection/selectPageContent" model="[
                                'pages'                : pages,
                                'selectedPages'        : selectedPages,
                                'showMeasuredEventForm': false
                        ]"/>
                    </div>
                </div>
            </div>

        </form>
    </div>
</div>

<g:render template="/_common/modals/downloadAsPngDialog" model="['chartContainerID': 'svg-container']"/>

<content tag="include.bottom">
    <asset:javascript src="csidashboard/csiDashboard.js"/>
    <asset:javascript src="pngDownloader.js"/>
    <asset:javascript src="/pageAggregation/pageAggregation.js"/>
    <asset:script type="text/javascript">
        OpenSpeedMonitor.ChartModules.UrlHandling.PageAggregation().init();
                $(document).ready(function () {
                    doOnDomReady(
                            '${dateFormat}',
        ${weekStart},
                    '${g.message(code: 'web.gui.jquery.chosen.multiselect.noresultstext', 'default': 'Keine Eintr&auml;ge gefunden f&uuml;r ')}'
            );

        });



        function drawGraph() {
            var selectedTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();
            var selectedSeries = OpenSpeedMonitor.BarchartMeasurings.getValues();
            var spinner = OpenSpeedMonitor.Spinner("#chart-container");
            OpenSpeedMonitor.ChartModules.PageAggregationBarChart = OpenSpeedMonitor.ChartModules.PageAggregationBarChart || OpenSpeedMonitor.ChartModules.PageAggregation("svg-container");
            spinner.start();
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
                    })),
                    selectedSeries: JSON.stringify(selectedSeries)
                },
                url: "${createLink(controller: 'pageAggregation', action: 'getBarchartData')}",
                dataType: "json",
                success: function (data) {
                    spinner.stop();
                    if (!$("#error-div").hasClass("hidden"))
                        $("#error-div").addClass("hidden");

                    if (!$.isEmptyObject(data)) {
                        if (!$("#no-data-div").hasClass("hidden"))
                            $("#no-data-div").addClass("hidden");
                        OpenSpeedMonitor.ChartModules.PageAggregationBarChart.drawChart(data);
                        $("#dia-save-chart-as-png").removeClass("disabled");
                    } else {
                        $("#no-data-div").removeClass("hidden")
                    }
                },
                error: function (e) {
                    spinner.stop();
                    $("#error-div").removeClass("hidden");
                    $("#error-message").html(e.responseText);
                }
            });
        }
    </asset:script>
</content>

</body>
</html>
