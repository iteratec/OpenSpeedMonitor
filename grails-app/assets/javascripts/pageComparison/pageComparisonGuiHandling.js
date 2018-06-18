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
    var avgLoaded = false;
    var medianLoaded = false;

    var init = function () {
        $(window).on('resize', function () {
            $("#chart-card").removeClass("hidden");
            renderChart({}, false);
        });
        $(window).on('historyStateLoaded', function () {
            loadData(false);
        });
        $("input[name='aggregationValue']").on("change", function () {
            spinner.start();
            renderChart({aggregationValue: getAggregationValue()}, true, true);
        });
        drawGraphButton.click(function () {
            loadData(true);
        });

        OpenSpeedMonitor.ChartModules.GuiHandling.PageComparison.Comparisons.setShowButtonDisabledCallback(setShowButtonDisabled);
        setShowButtonDisabled(true);
    };

    var renderChart = function (data, isStateChange, isAggregationValueChange) {
        if (avgLoaded && getAggregationValue() === "avg") {
            spinner.stop()
        }
        if (medianLoaded && getAggregationValue() === "median") {
            spinner.stop()
        }
        if (data) {
            pageComparisonChart.setData(data);
            if (isStateChange) {
                $(window).trigger("historyStateChanged");
            }
        }
        if (!data.series) pageComparisonChart.render(isAggregationValueChange);
        if (data.series && getAggregationValue() === data.series[0].data[0].aggregationValue) {
            pageComparisonChart.render(isAggregationValueChange);
        }
    };

    var getAggregationValue = function () {
        return $('input[name=aggregationValue]:checked').val()
    };

    var handleNewData = function (data, isStateChange) {
        $("#chart-card").removeClass("hidden");
        data.aggregationValue = getAggregationValue();
        renderChart(data, isStateChange)
    };

    var setShowButtonDisabled = function (state) {
        if(!state) {
            $('#graphButtonHtmlId').removeAttr('disabled', 'disabled');
            $('#warning-no-page').hide();
        } else {
            $('#graphButtonHtmlId').attr('disabled', 'disabled');
            $('#warning-no-page').show();
        }
    };

    var loadData = function (isStateChange) {
        pageComparisonChart.resetData();
        avgLoaded = false;
        medianLoaded = false;

        var selectedTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();
        var queryData = {
            from: selectedTimeFrame[0].toISOString(),
            to: selectedTimeFrame[1].toISOString(),
            measurand: JSON.stringify(OpenSpeedMonitor.BarchartMeasurings.getValues()),
            selectedPageComparisons: JSON.stringify(OpenSpeedMonitor.ChartModules.GuiHandling.PageComparison.Comparisons.getComparisons())
        };

        spinner.start();
        getDataForAggregationValue("median", queryData, isStateChange);
        getDataForAggregationValue("avg", queryData, isStateChange);
    };

    function getDataForAggregationValue(aggregationValue, queryData, isStateChange) {
        queryData.selectedAggregationValue = aggregationValue;
        $.ajax({
            type: 'POST',
            data: queryData,
            url: OpenSpeedMonitor.urls.pageComparisonGetData,
            dataType: "json",
            success: function (data) {
                if (aggregationValue === "avg") {
                    avgLoaded = true;
                } else {
                    medianLoaded = true;
                }
                if (!$("#error-div").hasClass("hidden"))
                    $("#error-div").addClass("hidden");

                if (!$.isEmptyObject(data)) {
                    $('#warning-no-data').hide();
                    handleNewData(data, isStateChange);
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
    return {}
})();