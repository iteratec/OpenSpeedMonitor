//= require jobGroupAggregationChart.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.GuiHandling = OpenSpeedMonitor.ChartModules.GuiHandling || {};

OpenSpeedMonitor.ChartModules.GuiHandling.jobGroupAggregation = (function () {
    var jobGroupAggregationChart = OpenSpeedMonitor.ChartModules.JobGroupAggregationHorizontal("#job-group-aggregation-svg");
    var spinner = OpenSpeedMonitor.Spinner("#chart-container");
    var drawGraphButton = $("#graphButtonHtmlId");

    var init = function() {
        drawGraphButton.click(loadData);
        $(window).on('resize', function() {
            jobGroupAggregationChart.setData({autoWidth: true});
            jobGroupAggregationChart.render();
        });
        $(".chart-filter").click(onFilterClick);
    };

    var onFilterClick = function() {
        jobGroupAggregationChart.setData({activeFilter: $(this).data("filter")});
        jobGroupAggregationChart.render();
        $(".chart-filter i").toggleClass('filterInactive', true);
        $("i", $(this)).toggleClass('filterInactive', false);
    };

    var handleNewData = function (data) {
        spinner.stop();
        $("#chart-card").removeClass("hidden");
        $("#error-div").toggleClass("hidden", true);

        if ($.isEmptyObject(data)) {
            $('#warning-no-data').show();
            return;
        }

        $('#warning-no-data').hide();
        data.width = -1;

        jobGroupAggregationChart.setData(data);
        jobGroupAggregationChart.render();
        OpenSpeedMonitor.ChartModules.UrlHandling.ChartSwitch.updateUrls(true);
        $("#dia-save-chart-as-png").removeClass("disabled");
    };

    var loadData = function() {
        var selectedTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();
        var selectedSeries = OpenSpeedMonitor.BarchartMeasurings.getValues();

        var queryData = {
            from: selectedTimeFrame[0].toISOString(),
            to: selectedTimeFrame[1].toISOString(),
            selectedJobGroups: JSON.stringify($.map($("#folderSelectHtmlId option:selected"), function (e) {
                return $(e).text()
            })),
            selectedSeries: JSON.stringify(selectedSeries)

        };

        spinner.start();
        $.ajax({
            type: 'POST',
            data: queryData,
            url: OpenSpeedMonitor.urls.jobGroupAggregationGetData,
            dataType: "json",
            success: handleNewData,
            error: function (e) {
                spinner.stop();
                $("#chart-card").removeClass("hidden")
                if (e.responseText === "no data") {
                    $("#error-div").addClass("hidden");
                    $('#warning-no-data').show();
                }
                else {
                    $("#error-div").removeClass("hidden");
                    $("#error-message").html(e.responseText);
                }
            }
        });
    };

    init();
    return {
    };
})();
