<%@ page defaultCodec="none" %></page>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isocsi.pageComparision.title" default="Page Comparison"/></title>
    <asset:stylesheet src="/csiBenchmark/show.less"/>

</head>

<body>
<h1>
    <a href="#" class="btn hidden" id="timeSeriesWithDataLink"><i class="fa fa-line-chart"></i></a>
    <a href="#" class="btn hidden" id="pageAggregationWithDataLink"><i class="fa fa-bar-chart"></i></a>
    <a href="#" class="btn hidden" id="distributionWithDataLink"><i class="fa fa-area-chart"></i></a>
    <g:message code="de.iteratec.isocsi.pageComparision.title" default="Page Comparison"/>
    <g:if test="${grailsApplication.config.getProperty('grails.de.iteratec.osm.detailAnalysis.enablePersistenceOfDetailAnalysisData')?.equals("true")}">
        <a href="#" class="btn hidden" id="detailAnalysisWithDataLink"><i class="fa fa-pie-chart"></i></a>
    </g:if>
    <a href="#" class="btn hidden" id="resultListWithDataLink"><i class="fa fa-th-list"></i></a>
</h1>

<p>
    <g:message code="de.iteratec.isocsi.pageComparison.description"
               default="The displayed chart shows the differnces between two pages. Usually used to compare pages as 'entry page' and as 'follow page'."/>
</p>

<div class="card hidden" id="chart-card">
    <div id="error-div" class="hidden">
        <div class="alert alert-danger">
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
                        <button type="button" onClick="drawGraph()" id="graphButtonHtmlId"
                                class="btn btn-primary show-button">
                            ${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default': 'Show')}</button>
                    </div>
                    <g:render template="/_resultSelection/hiddenWarnings"/>
                </div>
            </div>

            <div class="row card-well">
                <div class="col-md-6">
                    <g:render template="pagesComparisonSelectionCard"
                              model="${['pages'    : pages,
                                        'jobGroups': jobGroups]}"/>
                </div>

                <div class="col-md-4">
                    <g:render template="/_resultSelection/selectBarchartMeasurings" model="[
                            aggrGroupValuesUnCached          : aggrGroupValuesUnCached,
                            multipleMeasurands               : false,
                            multipleSeries                   : false,
                            'selectedAggrGroupValuesUnCached': selectedAggrGroupValuesUnCached
                    ]"/>
                </div>
            </div>

            <div class="row card-well">
                <div class="col-md-4">
                    <g:render template="/_resultSelection/selectIntervalTimeframeCard"
                              model="${['selectedTimeFrameInterval': selectedTimeFrameInterval, 'from': from,
                                        'fromHour'                 : fromHour, 'to': to, 'toHour': toHour, 'showIncludeInterval': false,
                                        'includeInterval'          : includeInterval,
                                        hideHourSelection          : true]}"/>
                </div>

            </div>
        </form>
    </div>
</div>
<g:render template="/_common/modals/downloadAsPngDialog" model="['chartContainerID': 'svg-container']"/>

<content tag="include.bottom">
    <asset:javascript src="pngDownloader.js"/>
    <asset:javascript src="chartSwitch"/>
    <asset:javascript src="/pageComparison/pageComparison.js"/>
    <asset:script type="text/javascript">
        var pageComparisonSelectionCardLoaded = false;
        var timeframeCardLoaded = false;
        var barchartMeasuringsLoaded = false;

        var allLoaded = function () {
            if(pageComparisonSelectionCardLoaded && timeframeCardLoaded && barchartMeasuringsLoaded) {
                OpenSpeedMonitor.ChartModules.UrlHandling.PageComparison().init();
                OpenSpeedMonitor.ChartModules.UrlHandling.ChartSwitch.updateUrls(true);
            }
        };

         $(window).on("pageComparisonSelectionCardLoaded", function () {
             pageComparisonSelectionCardLoaded = true;
             allLoaded();
        });
         $(window).on("selectIntervalTimeframeCardLoaded", function () {
             timeframeCardLoaded = true;
             allLoaded();
        });
         $(window).on("barchartMeasuringsLoaded", function () {
             barchartMeasuringsLoaded = true;
             allLoaded();
        });


        // declare the spinner outside of the drawGraph function to prevent creation of multiple spinnerContainer
        var spinner = OpenSpeedMonitor.Spinner("#chart-container");

        function drawGraph() {
            var selectedTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();

            OpenSpeedMonitor.ChartModules.PageComparisonBarChart = OpenSpeedMonitor.ChartModules.PageComparisonBarChart ||  OpenSpeedMonitor.ChartModules.PageComparisonChart("svg-container");

            spinner.start();
            $.ajax({
                type: 'POST',
                data: {
                    from: selectedTimeFrame[0].toISOString(),
                    to: selectedTimeFrame[1].toISOString(),
                    measurand: JSON.stringify(OpenSpeedMonitor.BarchartMeasurings.getValues()),
                    selectedPageComparisons: JSON.stringify(OpenSpeedMonitor.PageComparisonSelection.getValues())
                },
                url: "${createLink(controller: 'pageComparison', action: 'getBarchartData')}",
                dataType: "json",
                success: function (data) {
                    spinner.stop();
                    if (!$("#error-div").hasClass("hidden"))
                        $("#error-div").addClass("hidden");

                    if (!$.isEmptyObject(data)) {
                        $('#warning-no-data').hide();
                        OpenSpeedMonitor.ChartModules.PageComparisonBarChart.drawChart(data);
                        OpenSpeedMonitor.ChartModules.UrlHandling.ChartSwitch.updateUrls(true);
                        $("#dia-save-chart-as-png").removeClass("disabled");
                    } else {
                        $('#warning-no-data').show();
                    }
                },
                error: function (e) {
                    spinner.stop();
                    $("#error-div").removeClass("hidden");
                    $("#chart-card").removeClass("hidden");
                    $("#error-message").html(e.responseText);
                    }
                });
            }

    </asset:script>
</content>

</body>
</html>
