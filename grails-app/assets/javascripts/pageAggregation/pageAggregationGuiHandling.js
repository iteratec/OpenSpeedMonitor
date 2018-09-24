//= require pageAggregationChart.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.GuiHandling = OpenSpeedMonitor.ChartModules.GuiHandling || {};

OpenSpeedMonitor.ChartModules.GuiHandling.handleComparativeTimeframeConstraints = (function () {
    var activateComparativeTimeframeButton = document.querySelector("#addComparativeTimeFrame");
    var disableComparativeTimeframeButton = document.querySelector("#removeComparativeTimeFrame");
    var addMeasurandButton = document.querySelector("#addMeasurandButton");
    var removeMeasurandButtons = document.querySelectorAll(".removeMeasurandButton");

    var init = function () {
        createEventListeners();
    };

    var createEventListeners = function () {
        activateComparativeTimeframeButton.addEventListener("click", disableAddMeasurandButton);
        disableComparativeTimeframeButton.addEventListener("click", enableAddMeasurandButton);

        addMeasurandButton.addEventListener("click", disableComparativeTimeframe);
        addMeasurandButton.addEventListener("click", function () {

            removeMeasurandButtons = document.querySelectorAll(".removeMeasurandButton");
            removeMeasurandButtons.forEach(function (button) {
                button.addEventListener("click", enableComparativeTimeframeIfAllowed);
            });

        });
    };

    var disableAddMeasurandButton = function () {
        addMeasurandButton.setAttribute("disabled", "");
    };

    var enableAddMeasurandButton = function () {
        addMeasurandButton.removeAttribute("disabled");
    };

    var disableComparativeTimeframe = function () {
        activateComparativeTimeframeButton.setAttribute("disabled", "");
    };

    var enableComparativeTimeframeIfAllowed = function () {
        var numberOfSelectedMeasurands = document.querySelector("#measurands").childElementCount;
        if (numberOfSelectedMeasurands == 1) {
            activateComparativeTimeframeButton.removeAttribute("disabled");
        }
    };

    init();

    return {};

})();

OpenSpeedMonitor.ChartModules.GuiHandling.pageAggregation = (function () {
    var pageAggregationChart = OpenSpeedMonitor.ChartModules.PageAggregation("#page-aggregation-svg");
    var spinner = OpenSpeedMonitor.Spinner("#chart-container");
    var drawGraphButton = $("#graphButtonHtmlId");
    var stackBarSwitch = $("#stackBarSwitch");
    var inFrontButton = $("#inFrontButton");
    var besideButton = $("#besideButton");
    var avgLoaded = false;
    var medianLoaded = false;

    var init = function () {
        drawGraphButton.on('click', function () {
            $("#chart-card").removeClass("hidden");
            loadData(true);
        });
        $(window).on('historyStateLoaded', function () {
            loadData(false);
        });
        $(window).on('resize', function () {
            renderChart({}, false);
        });
        $("input[name='stackBars']").on("change", function () {
            renderChart({stackBars: getStackBars()}, true);
        });
        $("input[name='aggregationValue']").on("change", function () {
            spinner.start();
            renderChart({aggregationValue: getAggregationValue()}, true, true);
        });
        $(".chart-filter").on('click', onFilterClick);
    };

    var getSelectedFilter = function () {
        return $(".chart-filter.selected").data("filter");
    };

    var getStackBars = function () {
        return $("#inFrontButton input").prop("checked");
    };

    var getAggregationValue = function () {
        return $('input[name=aggregationValue]:checked').val()
    };

    var onFilterClick = function (event) {
        event.preventDefault();
        $(".chart-filter").toggleClass('selected', false);
        $(this).toggleClass('selected', true);
        renderChart({selectedFilter: $(this).data("filter")}, true);
    };

    var updateFilters = function (filterRules) {
        filterRules = filterRules || [];
        var $filterDropdownGroup = $("#filter-dropdown-group");
        var $customerJourneyHeader = $filterDropdownGroup.find("#customer-journey-header");
        var selectedFilter = getSelectedFilter();
        $filterDropdownGroup.find('.filterRule').remove();
        $filterDropdownGroup.toggleClass("hidden", false);

        Object.keys(filterRules).forEach(function (filterRuleKey) {
            var listItem = $("<li class='filterRule'><a href='#' class='chart-filter'><i class='fas fa-check' aria-hidden='true'></i>" + filterRuleKey + "</a></li>");
            var link = $("a", listItem);
            link.data('filter', filterRuleKey);
            link.on('click', onFilterClick);
            listItem.insertAfter($customerJourneyHeader);
        });

        var selectedFilterElement = $(".chart-filter").filter(function () {
            return $(this).data("filter") === selectedFilter;
        });
        if (selectedFilterElement.length) {
            selectedFilterElement.toggleClass("selected", true);
        } else {
            selectedFilter = "desc";
            $("#all-bars-desc").toggleClass("selected", true);
        }
        return selectedFilter;
    };

    var countMeasurandGroups = function (data) {
        var measurandGroups = {};
        data.series.forEach(function (value) {
            measurandGroups[value.measurandGroup] = true;
        });
        return Object.keys(measurandGroups).length;
    };

    var countMeasurands = function (data) {
        var measurands = {};
        data.series.forEach(function (value) {
            measurands[value.measurand] = true;
        });
        return Object.keys(measurands).length;
    };

    var updateStackBars = function (data) {
        var numMeasurandGroups = countMeasurandGroups(data);
        var numMeasurands = countMeasurands(data);
        var stackBars = false;
        stackBarSwitch.toggle(numMeasurandGroups < 2 && numMeasurands > 1);
        if (data.hasComparativeData || numMeasurands === 1) {
            stackBars = true;
        } else if (numMeasurandGroups > 1) {
            stackBars = false;
        } else {
            stackBars = getStackBars();
        }
        inFrontButton.toggleClass("active", stackBars);
        besideButton.toggleClass("active", !stackBars);
        return stackBars;
    };

    var handleNewData = function (data, isStateChange) {
        $("#chart-card").removeClass("hidden");
        $("#error-div").toggleClass("hidden", true);

        if ($.isEmptyObject(data)) {
            $('#warning-no-data').show();
            spinner.stop();
            return;
        }

        $('#warning-no-data').hide();
        data.width = -1;
        data.selectedFilter = updateFilters(data.filterRules);
        data.stackBars = updateStackBars(data);
        data.aggregationValue = getAggregationValue();

        renderChart(data, isStateChange);
        $('html, body').animate({scrollTop: 0}, '500');
        $("#dia-save-chart-as-png").removeClass("disabled");
    };

    var renderChart = function (data, isStateChange, isAggregationValueChange) {
        if (avgLoaded && getAggregationValue() === "avg") {
            spinner.stop()
        }
        if (medianLoaded && getAggregationValue() === "median") {
            spinner.stop()
        }
        if (data) {
            pageAggregationChart.setData(data);
            if (isStateChange) {
                $(window).trigger("historyStateChanged");
            }
        }
        if (!data.series) pageAggregationChart.render(isAggregationValueChange);
        if (data.series && getAggregationValue() === data.series[0].aggregationValue) {
            pageAggregationChart.render(isAggregationValueChange);
        }
    };

    var loadData = function (isStateChange) {
        pageAggregationChart.resetData();
        avgLoaded = false;
        medianLoaded = false;

        var selectedTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();
        var comparativeTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getComparativeTimeFrame();
        var selectedSeries = OpenSpeedMonitor.BarchartMeasurings.getValues();

        var queryData = {
            from: selectedTimeFrame[0].toISOString(),
            to: selectedTimeFrame[1].toISOString(),
            selectedJobGroups: JSON.stringify($.map($("#folderSelectHtmlId option:selected"), function (e) {
                return $(e).text()
            })),
            selectedPages: JSON.stringify($.map($("#pageSelectHtmlId option:selected"), function (e) {
                return $(e).text()
            })),
            selectedSeries: JSON.stringify(selectedSeries)
        };

        if (comparativeTimeFrame) {
            queryData.fromComparative = comparativeTimeFrame[0].toISOString();
            queryData.toComparative = comparativeTimeFrame[1].toISOString();
        }
        spinner.start();
        getDataForAggregationValue("median", queryData, isStateChange);
        getDataForAggregationValue("avg", queryData, isStateChange);
    };

    function getDataForAggregationValue(aggregationValue, queryData, isStateChange) {
        queryData.selectedAggregationValue = aggregationValue;
        $.ajax({
            type: 'POST',
            data: queryData,
            url: OpenSpeedMonitor.urls.pageAggregationGetData,
            dataType: "json",
            success: function (data) {
                if (aggregationValue === "avg") {
                    avgLoaded = true;
                } else {
                    medianLoaded = true;
                }
                handleNewData(data, isStateChange);
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
