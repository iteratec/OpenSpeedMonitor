<%@ page defaultCodec="none" %></page>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isocsi.pageAggregation" default="Page Aggregation"/></title>
    <asset:javascript src="chartSwitch"/>
    <asset:stylesheet src="/pageAggregation/show.css"/>

</head>

<body>
<h1><g:message code="de.iteratec.isocsi.pageAggregation" default="Page Aggregation"/></h1>

<p>
    <g:message code="de.iteratec.isocsi.pageAggregation.description.short"
               default="The webpagetest raw data of the respective interval is the basis for the displayed mean values."/>
</p>
<div class="card hidden" id="chart-card">
    <div id="error-div" class="hidden">
        <div class="alert alert-danger">
            <strong><g:message code="de.iteratec.isocsi.CsiDashboardController.selectionErrors.title"/></strong>

            <div id="error-message"></div>
        </div>
    </div>
    <g:render template="barChart"/>
</div>

<div class="row">
    <div class="col-md-12">
        <form id="dashBoardParamsForm">
            <!-- show button -->
            <div class="action-row">
                <div class="col-md-12">

                    <div class="btn-group pull-right" id="show-button-group">
                        <a href="#" type="button" onClick="drawGraph()" id="graphButtonHtmlId" class="btn btn-primary show-button">
                            ${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default': 'Show')}</a>
                    </div>
                    <g:render template="/_resultSelection/hiddenWarnings" />
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
                    <g:render template="/_resultSelection/selectPageLocationConnectivityCard" model="[
                            'showOnlyPage'         : true,
                            'hideMeasuredEventForm': true,
                            'pages'                : pages,
                            'selectedPages'        : selectedPages
                    ]"/>
                </div>
                <button class="reset-result-selection btn btn-default btn-sm" type="button" title="Reset">
                    <i class="fa fa-undo"></i> Reset
                </button>
            </div>
        </form>
    </div>
</div>

<g:render template="/_common/modals/downloadAsPngDialog" model="['chartContainerID': 'svg-container']"/>

<content tag="include.bottom">
    <asset:javascript src="pngDownloader.js"/>
    <asset:javascript src="/pageAggregation/pageAggregation.js"/>
    <asset:script type="text/javascript">
        OpenSpeedMonitor.ChartModules.UrlHandling.PageAggregation().init();
        $(window).load(function() {
            OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/resultSelection.js" absolute="true"/>')
        });

        // declare the spinner outside of the drawGraph function to prevent creation of multiple spinnerContainer
        var spinner = OpenSpeedMonitor.Spinner("#chart-container");

        function drawGraph() {
            var selectedTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();
            var selectedSeries = OpenSpeedMonitor.BarchartMeasurings.getValues();
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
                        $('#warning-no-data').hide();
                        OpenSpeedMonitor.ChartModules.PageAggregationBarChart.drawChart(data);
                        $("#dia-save-chart-as-png").removeClass("disabled");
                    } else {
                        $('#warning-no-data').show();
                    }
                },
                error: function (e) {
                    spinner.stop();
                    $("#error-div").removeClass("hidden");
                    $("#error-message").html(e.responseText);
                }
            });
        }
        OpenSpeedMonitor.ChartModules.UrlHandling.ChartSwitch("${createLink(action: 'showAll', controller: 'eventResultDashboard', absolute: true)}",
            "${createLink(action: 'show', controller: 'pageAggregation', absolute: true)}",
            "${createLink(action: 'listResults', controller: 'tabularResultPresentation', absolute: true)}",
            "${createLink(action: 'getPagesForMeasuredEvents', controller: 'page', absolute: true)}",
            "${createLink(action: 'show', controller: 'detailAnalysis', absolute: true)}").init();

    </asset:script>
</content>

</body>
</html>
