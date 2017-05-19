<%@ page defaultCodec="none" %></page>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isocsi.csiBenchmark.title" default="Csi Benchmark"/></title>
    <asset:stylesheet src="/csiBenchmark/show.less"/>

</head>

<body>
<h1><g:message code="de.iteratec.isocsi.csiBenchmark.title" default="Csi Benchmark"/></h1>

<p>
    <g:message code="de.iteratec.isocsi.csiBenchmark.description"
               default="This chart shows a csi benchmark of jobGroups averaged over the selected interval. The current date will not be taken into account."/>
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
                <div class="col-md-4">
                    <g:render template="/_resultSelection/selectIntervalTimeframeCard"
                              model="${['selectedTimeFrameInterval': selectedTimeFrameInterval, 'from': from,
                                        'fromHour'                 : fromHour, 'to': to, 'toHour': toHour, 'showIncludeInterval': false,
                                        'includeInterval'          : includeInterval,
                                        hideHourSelection          : true]}"/>
                </div>

                <div class="col-md-3">

                    <div id="filter-navtab-jobGroup">
                        <g:render template="/_resultSelection/selectJobGroupCard"
                                  model="['folders'             : folders, 'selectedFolder': selectedFolder,
                                          'tagToJobGroupNameMap': tagToJobGroupNameMap]"/>
                    </div>
                </div>

                <div class="col-md-3">
                    <div class="card">
                        <h2>
                            <g:message code="de.iteratec.osm.csi.type.heading" default="CSI Type"/>
                        </h2>

                        <div class="radio">
                            <label>
                                <input type="radio" name="csiTypeRadios" id="optionsRadios1" value="docComplete"
                                       checked>
                                &nbsp;${message(code: "de.iteratec.osm.csi.type.byDocComplete.label", default: "Doc Complete")}
                            </label>
                        </div>

                        <div class="radio">
                            <label>
                                <input type="radio" name="csiTypeRadios" id="optionsRadios2" value="visuallyComplete">
                                &nbsp;${message(code: "de.iteratec.osm.csi.type.byVisuallyComplete.label", default: "Visually Complete")}
                            </label>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>

<g:render template="/_common/modals/downloadAsPngDialog" model="['chartContainerID': 'svg-container']"/>

<content tag="include.bottom">
    <asset:javascript src="/csiBenchmark/csiBenchmark.js"/>
    <asset:javascript src="chartSwitch"/>
    <asset:script type="text/javascript">
        OpenSpeedMonitor.ChartModules.UrlHandling.CsiBenchmark().init();

        // declare the spinner outside of the drawGraph function to prevent creation of multiple spinnerContainer
        var spinner = OpenSpeedMonitor.Spinner("#chart-container");

        function drawGraph() {
            var selectedTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();
            OpenSpeedMonitor.ChartModules.CsiBenchmarkBarChart = OpenSpeedMonitor.ChartModules.CsiBenchmarkBarChart || OpenSpeedMonitor.ChartModules.CsiBenchmarkChart("svg-container");
            spinner.start();
            $.ajax({
                type: 'POST',
                data: {
                    from: selectedTimeFrame[0].toISOString(),
                    to: selectedTimeFrame[1].toISOString(),
                    selectedJobGroups: JSON.stringify($.map($("#folderSelectHtmlId option:selected"), function (e) {
                        return $(e).text()
                    })),
                    csiType: $('input[name=csiTypeRadios]:checked').val()
                },
                url: "${createLink(controller: 'csiBenchmark', action: 'getBarChartData')}",
                dataType: "json",
                success: function (data) {
                    spinner.stop();
                    if (!$("#error-div").hasClass("hidden"))
                        $("#error-div").addClass("hidden");

                    if (!$.isEmptyObject(data)) {
                        $('#warning-no-data').hide();
                        $('#chart-card').show();
                        OpenSpeedMonitor.ChartModules.CsiBenchmarkBarChart.drawChart(data);
                        OpenSpeedMonitor.ChartModules.UrlHandling.ChartSwitch.updateUrls(true);
                        $("#dia-save-chart-as-png").removeClass("disabled");
                    } else {
                        $('#warning-no-data').show();
                        $('#chart-card').hide()
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
