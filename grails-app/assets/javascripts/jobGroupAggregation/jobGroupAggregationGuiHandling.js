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
    var avgLoaded = false;
    var medianLoaded = false;
    var inputPercentileSlider = $("input[id='percentageSlider']");
    var inputPercentileField = $("input[id='percentageField']");
    var percentile = "50";
    var lastPercentile = "median";

    var init = function () {
        drawGraphButton.on('click', function () {
            $("#chart-card").removeClass("hidden");
            loadData(true);
        });
        $(window).on('historyStateLoaded', function () {
            loadData(false);
        });
        $(window).on('resize', function () {
            jobGroupAggregationChart.setData({});
            jobGroupAggregationChart.render();
        });
        $("input[name='aggregationValue']").on("change", function () {
            spinner.start();
            togglePercentileOptionsVisibility();
            renderChart({aggregationValue: getAggregationValue()}, true, true);
        });
        $(".chart-filter").on('click', onFilterClick);

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

    var getSelectedFilter = function () {
        return $(".chart-filter.selected").data("filter");
    };

    var getAggregationValue = function () {
        return $('input[name=aggregationValue]:checked').val() === "avg" ? "avg" : lastPercentile;
    };

    var onFilterClick = function (event) {
        event.preventDefault();
        $(".chart-filter").toggleClass('selected', false);
        $(this).toggleClass('selected', true);
        renderChart({activeFilter: $(this).data("filter")}, true);
    };

    var handleNewData = function (data, isStateChange, isAggregationValueChange) {
        $("#chart-card").removeClass("hidden");
        $("#error-div").toggleClass("hidden", true);

        if ($.isEmptyObject(data)) {
            $('#warning-no-data').show();
            return;
        }

        $('#warning-no-data').hide();
        data.width = -1;
        data.activeFilter = getSelectedFilter();

        renderChart(data, isStateChange, isAggregationValueChange);
        $("#dia-save-chart-as-png").removeClass("disabled");
    };

    var renderChart = function (data, isStateChange, isAggregationValueChange) {
        if(medianLoaded && avgLoaded) {
            spinner.stop();
        }
        if (data) {
            jobGroupAggregationChart.setData(data);
            if (isStateChange) {
                $(window).trigger("historyStateChanged");
            }
        }
        if (!data.groupData) jobGroupAggregationChart.render(isAggregationValueChange);
        else if (data.groupData && medianLoaded && avgLoaded) {
            jobGroupAggregationChart.render(isAggregationValueChange);
        }
    };

    var loadData = function (isStateChange) {
        jobGroupAggregationChart.resetData();
        avgLoaded = false;
        medianLoaded = false;

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
        getDataForAggregationValue("median", queryData, isStateChange);
        getDataForAggregationValue("avg", queryData, isStateChange);
    };

    var loadPercentile = function () {
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
        getDataForAggregationValue(percentile, queryData, true);
    };

    function getDataForAggregationValue(aggregationValue, queryData, isStateChanged) {
        queryData.selectedAggregationValue = aggregationValue;
        $.ajax({
            type: 'POST',
            data: queryData,
            url: OpenSpeedMonitor.urls.jobGroupAggregationGetData,
            dataType: "json",
            success: function (data) {
                if (aggregationValue === "avg") {
                    avgLoaded = true;
                    data.aggregationValue = "avg";
                    handleNewData(data, isStateChanged, false);
                }
                else if(aggregationValue === "median"){
                    medianLoaded = true;
                    data.aggregationValue = "median";
                    handleNewData(data, isStateChanged, false);
                }
                else {
                    lastPercentile = aggregationValue;
                    data.aggregationValue = aggregationValue;
                    handleNewData(data, true, true);
                }
            },
            error: function (e) {
                spinner.stop();
                $("#chart-card").removeClass("hidden");
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
    }

    init();
    return {};
})();
