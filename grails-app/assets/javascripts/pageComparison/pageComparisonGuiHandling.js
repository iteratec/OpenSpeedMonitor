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
    var percentileLoaded = false;
    var inputPercentileSlider = $("input[id='percentageSlider']");
    var inputPercentileField = $("input[id='percentageField']");
    var percentile = "50";
    var lastPercentile = percentile;
    var noDataAvailable = false;
    var currentQuery = null;

    var init = function () {
        $(window).on('resize', function () {
            $("#chart-card").removeClass("hidden");
            if(pageComparisonChart.isDataAvailable()) {
                renderChart({}, false);
            }
        });
        $(window).on('historyStateLoaded', function () {
            $("#chart-card").removeClass("hidden");
            updatePercentile(inputPercentileField.val());
            togglePercentileOptionsVisibility();
            loadData(false);
        });
        $("input[name='aggregationValue']").on("change", function () {
            spinner.start();
            togglePercentileOptionsVisibility();
            renderChart({aggregationValue: getAggregationValue()}, true, true);
        });
        drawGraphButton.on('click', function () {
            loadData(true);
        });
        window.addEventListener("pageComparisonValidation", function (event) {
            setShowButtonState(event.detail.isValid)
        });
        inputPercentileSlider.on("input", function () {
            updatePercentile(inputPercentileSlider.val(), false);
        });
        inputPercentileSlider.on("change", function () {
            updatePercentile(inputPercentileSlider.val(), true);
        });
        inputPercentileField.on("change", function () {
            updatePercentile(inputPercentileField.val(), true);
        });
        togglePercentileOptionsVisibility();
        setShowButtonState(false);
    };

    var togglePercentileOptionsVisibility = function () {
        if(getAggregationValue() === "avg") {
            inputPercentileField.attr("disabled", "disabled");
            inputPercentileSlider.attr("disabled", "disabled");
        } else {
            inputPercentileField.removeAttr("disabled");
            inputPercentileSlider.removeAttr("disabled");
        }
    };

    var updatePercentile = function (perc, loadContent) {
        percentile = perc ? perc : 0;
        if(!percentile.match("\\d{1,3}")) {
            percentile = 0;
        }
        percentile = Math.min(100, Math.max(0, percentile));
        inputPercentileSlider.val(percentile);
        inputPercentileField.val(percentile);
        if(loadContent) {
            loadPercentile();
        }
    };

    var setShowButtonState = function (enable) {
        var button = document.getElementById("graphButtonHtmlId");
        if (enable) {
            enableButton(button)
        } else {
            disableButton(button)
        }
    };

    var disableButton = function (button) {
        button.setAttribute("disabled", "disabled");
        document.getElementById('warning-no-page').style.display = 'block'
    }

    var enableButton = function (button) {
        button.removeAttribute("disabled");
        document.getElementById('warning-no-page').style.display = 'none'
    }

    var renderChart = function (data, isStateChange, isAggregationValueChange) {
        if (avgLoaded && percentileLoaded) {
            spinner.stop()
        }

        if (data && !noDataAvailable) {
            pageComparisonChart.setData(data);
            if (isStateChange) {
                $(window).trigger("historyStateChanged");
            }
        }
        if (!data.series) pageComparisonChart.render(isAggregationValueChange);
        if (data.series && percentileLoaded && avgLoaded) {
            pageComparisonChart.render(isAggregationValueChange);
        }
    };

    var getAggregationValue = function () {
        return $('input[name=aggregationValue]:checked').val() === "avg" ? "avg" : lastPercentile;
    };

    var handleNewData = function (data, isStateChange, isAggregationValueChange) {
        $("#chart-card").removeClass("hidden");

        if ($.isEmptyObject(data)) {
            $('#warning-no-data').show();
            spinner.stop();
            noDataAvailable = true;
            return;
        }
        noDataAvailable = false;

        data.aggregationValue = getAggregationValue();
        renderChart(data, isStateChange, isAggregationValueChange);
    };

    var loadData = function (isStateChange) {
        pageComparisonChart.resetData();
        avgLoaded = false;
        percentileLoaded = false;

        var selectedTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();
        var queryData = {
            from: selectedTimeFrame[0].toISOString(),
            to: selectedTimeFrame[1].toISOString(),
            measurand: JSON.stringify(OpenSpeedMonitor.BarchartMeasurings.getValues()),
            selectedPageComparisons: JSON.stringify(window.pageComparisonComponent.getComparisons())
        };

        spinner.start();
        currentQuery = queryData;
        getDataForAggregationValue(percentile, queryData, isStateChange);
        getDataForAggregationValue("avg", queryData, isStateChange);
    };

    var loadPercentile = function () {
        if(currentQuery) {
            spinner.start();
            getDataForAggregationValue(percentile, currentQuery, true);
        }
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
                    handleNewData(data, isStateChange, false);
                }
                else if(!percentileLoaded){
                    percentileLoaded = true;
                    lastPercentile = aggregationValue;
                    handleNewData(data, isStateChange, false);
                }
                else {
                    lastPercentile = aggregationValue;
                    handleNewData(data, true, true);
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