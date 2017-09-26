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
        drawGraphButton.click(function() {
            loadData(true);
        });
        $(window).on('historyStateLoaded', function() {
            loadData(false);
        });
        $(window).on('resize', function() {
            jobGroupAggregationChart.setData({autoWidth: true});
            jobGroupAggregationChart.render();
        });
        $("input[name='aggregationValue']").on("change", function() {
            renderChart({aggregationValue: $('input[name=aggregationValue]:checked').val()}, true);
        });
        $(".chart-filter").click(onFilterClick);
    };

    var getSelectedFilter = function () {
        return $(".chart-filter.selected").data("filter");
    };

    var onFilterClick = function() {
        event.preventDefault();
        $(".chart-filter").toggleClass('selected', false);
        $(this).toggleClass('selected', true);
        renderChart({activeFilter: $(this).data("filter")}, true);
    };

    var handleNewData = function (data, isStateChange) {
        spinner.stop();
        $("#chart-card").removeClass("hidden");
        $("#error-div").toggleClass("hidden", true);

        if ($.isEmptyObject(data)) {
            $('#warning-no-data').show();
            return;
        }

        $('#warning-no-data').hide();
        data.width = -1;
        data.activeFilter = getSelectedFilter();

        renderChart(data, isStateChange);
        $("#dia-save-chart-as-png").removeClass("disabled");
    };

    var renderChart = function (data, isStateChange) {
        if (data) {
            jobGroupAggregationChart.setData(data);
            if (isStateChange) {
                $(window).trigger("historyStateChanged");
            }
        }
        jobGroupAggregationChart.render();
    };

    var loadData = function(isStateChanged) {
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
            success: function (data) {
                handleNewData(data, isStateChanged);
            },
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
