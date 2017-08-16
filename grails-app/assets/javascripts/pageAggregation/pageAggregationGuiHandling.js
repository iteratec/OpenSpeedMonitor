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

    var init = function() {
        drawGraphButton.click(loadData);
        $(window).on('pageAggregationStateLoaded', function() {
            loadData();
        });
        $(window).on('resize', function() {
            renderChart({autoWidth: true});
        });
        $("input[name='stackBars']").on("change", function() {
            renderChart({stackBars: getStackBars()});
        });
        $(".chart-filter").click(onFilterClick);
    };

    var getSelectedFilter = function () {
        return $(".chart-filter.selected").data("filter");
    };

    var getStackBars = function () {
        return $("#inFrontButton input").prop("checked");
    };

    var onFilterClick = function(event) {
        event.preventDefault();
        $(".chart-filter").toggleClass('selected', false);
        $(this).toggleClass('selected', true);
        renderChart({selectedFilter: $(this).data("filter")});
    };

    var updateFilters = function (filterRules) {
        filterRules = filterRules || [];
        var $filterDropdownGroup = $("#filter-dropdown-group");
        var $customerJourneyHeader = $filterDropdownGroup.find("#customer-journey-header");
        var selectedFilter = getSelectedFilter();
        $filterDropdownGroup.find('.filterRule').remove();
        $filterDropdownGroup.toggleClass("hidden", false);

        Object.keys(filterRules).forEach(function(filterRuleKey) {
            var listItem = $("<li class='filterRule'><a href='#' class='chart-filter'><i class='fa fa-check' aria-hidden='true'></i>" + filterRuleKey + "</a></li>");
            var link = $("a", listItem);
            link.data('filter', filterRuleKey);
            link.click(onFilterClick);
            listItem.insertAfter($customerJourneyHeader);
        });

        var selectedFilterElement = $(".chart-filter").filter(function() { return $(this).data("filter") === selectedFilter; });
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
        data.selectedFilter = updateFilters(data.filterRules);
        data.stackBars = updateStackBars(data);

        renderChart(data);
        $('html, body').animate({scrollTop:0},'500');
        OpenSpeedMonitor.ChartModules.UrlHandling.ChartSwitch.updateUrls(true);
        $("#dia-save-chart-as-png").removeClass("disabled");
    };

    var renderChart = function (data) {
        if (data) {
            pageAggregationChart.setData(data);
            $(window).trigger("pageAggregationStateChanged");
        }
        pageAggregationChart.render();
    };

    var loadData = function() {
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
        $.ajax({
            type: 'POST',
            data: queryData,
            url: OpenSpeedMonitor.urls.pageAggregationGetData,
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
