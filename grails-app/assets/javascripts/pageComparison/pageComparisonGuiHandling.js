//= require pageComparison.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.GuiHandling = OpenSpeedMonitor.ChartModules.GuiHandling || {};



OpenSpeedMonitor.ChartModules.GuiHandling.pageComparison = (function () {
    var pageComparisonChart = OpenSpeedMonitor.ChartModules.PageComparisonChart("#page-comparison-svg");
    var spinner = OpenSpeedMonitor.Spinner("#chart-container");
    var drawGraphButton = $("#graphButtonHtmlId");

    var init = function () {
        $(window).on('resize', function() {
            renderChart({autoWidth: true}, false);
        });



        var pageComparisonSelectionCardLoaded = false;
        var timeframeCardLoaded = false;
        var barchartMeasuringsLoaded = false;

        var allLoaded = function () {
            if(pageComparisonSelectionCardLoaded && timeframeCardLoaded && barchartMeasuringsLoaded) {
                // pageComparisonChart.init();
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

    var renderChart = function (data, isStateChange) {
       if (data) {
            pageComparisonChart.setData(data);
            if (isStateChange) {
                $(window).trigger("historyStateChanged");
            }
        }
        pageComparisonChart.render();

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
                    pageComparisonChart.setData(data);
                    pageComparisonChart.render();
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