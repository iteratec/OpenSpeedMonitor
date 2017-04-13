<%@ page defaultCodec="none" %></page>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isocsi.pageAggregation" default="Page Aggregation"/></title>
</head>

<body>
<h1>Pages: Entry and Follow</h1>

<p>
    <g:message code="de.iteratec.isocsi.pageAggregation.description.short"
               default="The webpagetest raw data of the respective interval is the basis for the displayed mean values."/>
</p>
<div class="card hidden" id="chart-card">
    <div id="error-div" class="hidden">
        <div class="alert alert-danger">
            <div id="error-message"></div>
        </div>
    </div>
    <g:render template="barChart"/>
</div>

<content tag="include.bottom">
    <asset:javascript src="/pageAggregation/pageAggregation.js"/>
    <asset:script type="text/javascript">

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
                    $("#chart-card").removeClass("hidden");
                    $("#error-message").html(e.responseText);
                }
            });
        }

    </asset:script>
</content>

</body>
</html>
