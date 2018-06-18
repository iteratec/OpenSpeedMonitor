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
        $(window).on('resize', function () {
            renderChart({}, false);
        });
        $(window).on('historyStateLoaded', function () {
            loadData(false);
        });

        drawGraphButton.click(function () {
            loadData(true);
        });
        window.addEventListener("pageComparisonValidation", function (event) {
            setShowButtonState(event.detail.isValid)
        });
        setShowButtonState(false);
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

    var renderChart = function (data, isStateChange) {
        if (data) {
            pageComparisonChart.setData(data);
            if (isStateChange) {
                $(window).trigger("historyStateChanged");
            }
        }
        pageComparisonChart.render();
    };

    var handleNewData = function (data, isStateChange) {
        $("#chart-card").removeClass("hidden");
        renderChart(data, isStateChange)
    };

    var loadData = function (isStateChange) {
        var selectedTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();
        spinner.start();
        $.ajax({
            type: 'POST',
            data: {
                from: selectedTimeFrame[0].toISOString(),
                to: selectedTimeFrame[1].toISOString(),
                measurand: JSON.stringify(OpenSpeedMonitor.BarchartMeasurings.getValues()),
                selectedPageComparisons: JSON.stringify(window.pageComparisonComponent.getComparisons())
            },
            url: OpenSpeedMonitor.urls.pageComparisonGetData,
            dataType: "json",
            success: function (data) {
                spinner.stop();
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