<%@ page defaultCodec="none" %></page>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title>
        <g:message code="de.iteratec.osm.distributionChart" default="Distribution Chart"/>
    </title>
    %{--JOHANNES2DO: enable chart switch functionality--}%
    %{--<asset:javascript src="chartSwitch"/>--}%
    <asset:stylesheet src="d3Charts/distributionChart"/>
</head>

<body>
<h1><g:message code="de.iteratec.osm.distributionChart" default="Distribution Chart"/></h1>

<p>
    <g:message code="de.iteratec.osm.distributionChart.description.short"
               default="The webpagetest raw data of the respective interval is the basis for the displayed distribution."/>
</p>

<div class="card hidden" id="chart-card">
    <div id="error-div" class="hidden">
        <div class="alert alert-danger">
            <div id="error-message"></div>
        </div>
    </div>
    <g:render template="distributionChart"/>
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
                              model="${['selectedTimeFrameInterval': selectedTimeFrameInterval,
                                        'from'                     : from,
                                        'fromHour'                 : fromHour,
                                        'to'                       : to,
                                        'toHour'                   : toHour,
                                        'showIncludeInterval'      : false,
                                        'includeInterval'          : includeInterval]}"/>

                    <g:render template="/_resultSelection/selectDistributionChartMeasurings" model="[
                            measurandsUncached: measurandsUncached
                    ]"/>
                </div>

                <div class="col-md-3">
                    <div id="filter-navtab-jobGroup">
                        <g:render template="/_resultSelection/selectJobGroupCard"
                                  model="['folders'             : folders,
                                          'selectedFolder'      : selectedFolder,
                                          'tagToJobGroupNameMap': tagToJobGroupNameMap]"/>
                    </div>
                </div>
                %{--the rest----------------------------------------------------------------------------------------------}%
                <div id="filter-complete-tabbable" class="col-md-5">
                    <g:render template="/_resultSelection/selectPageLocationConnectivityCard"
                              model="['showOnlyPage'         : true,
                                      'hideMeasuredEventForm': true,
                                      'pages'                : pages,
                                      'selectedPages'        : selectedPages]"/>
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
    <asset:javascript src="/distributionChart/distributionChart.js"/>
    <asset:script type="text/javascript">
        $(window).load(function() {
            OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/resultSelection.js" />')
            OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/distributionChart/distributionChart.js" />', true, 'distributionChart')
        });

        var spinner = OpenSpeedMonitor.Spinner("#chart-container");

        function drawGraph() {
            var selectedTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();

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
                    selectedMeasurand: JSON.stringify($.map($("#measurandSelection option:selected"), function (e) {
                        return $(e).val()
                    }))
                },
                url: "${createLink(controller: 'distributionChart', action: 'getDistributionChartData')}",
                dataType: "json",
                success: function (data) {
                    spinner.stop();
                    if (!$("#error-div").hasClass("hidden"))
                        $("#error-div").addClass("hidden");

                    if (!$.isEmptyObject(data)) {
                        $('#warning-no-data').hide();
                        OpenSpeedMonitor.ChartModules.distributionChart.drawChart(data);
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
