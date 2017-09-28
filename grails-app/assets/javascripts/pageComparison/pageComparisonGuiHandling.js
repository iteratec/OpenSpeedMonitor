//= require pageComparison.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.GuiHandling = OpenSpeedMonitor.ChartModules.GuiHandling || {};



OpenSpeedMonitor.ChartModules.GuiHandling.pageComparison = (function () {
    OpenSpeedMonitor.ChartModules.PageComparisonBarChart = OpenSpeedMonitor.ChartModules.PageComparisonBarChart || OpenSpeedMonitor.ChartModules.PageComparisonChart("svg-container");
    // var pageAggregationChart = OpenSpeedMonitor.ChartModules.PageAggregation("#page-aggregation-svg");
    var spinner = OpenSpeedMonitor.Spinner("#chart-container");
    var drawGraphButton = $("#graphButtonHtmlId");

    var init = function () {
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
        drawGraphButton.click(function () {
            loadData();
        });
    };

    var loadData = function () {
        var selectedTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();
        spinner.start();
        $.ajax({
            type: 'POST',
            data: {
                from: selectedTimeFrame[0].toISOString(),
                to: selectedTimeFrame[1].toISOString(),
                measurand: JSON.stringify(OpenSpeedMonitor.BarchartMeasurings.getValues()),
                selectedPageComparisons: JSON.stringify(OpenSpeedMonitor.PageComparisonSelection.getValues())
            },
            url: OpenSpeedMonitor.urls.pageComparisonGetData,
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
    };
    init();
    return {
    }
})();