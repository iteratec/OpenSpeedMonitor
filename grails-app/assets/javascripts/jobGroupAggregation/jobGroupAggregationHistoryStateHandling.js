//= require /urlHandling/urlHelper.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.UrlHandling = OpenSpeedMonitor.ChartModules.UrlHandling || {};

OpenSpeedMonitor.ChartModules.UrlHandling.JobGroupAggregation = (function () {
    var loadedState = "";

    var initWaitForPostload = function () {
        var dependencies = ["jobGroupAggregation", "selectIntervalTimeframeCard"];
        OpenSpeedMonitor.postLoader.onLoaded(dependencies, function () {
            loadState(OpenSpeedMonitor.ChartModules.UrlHandling.UrlHelper.getUrlParameter());
            addEventHandlers();
        });
    };

    var addEventHandlers = function () {
        $(window).on("historyStateChanged", saveState);
        window.onpopstate = function (event) {
            var state = event.state || OpenSpeedMonitor.ChartModules.UrlHandling.UrlHelper.getUrlParameter();
            loadState(state);
        };
    };

    var urlEncodeState = function (state) {
        return $.param(state, true);
    };

    var saveState = function () {
        var state = {};
        state["from"] = $("#fromDatepicker").val();
        state["to"] = $("#toDatepicker").val();
        state["selectedTimeFrameInterval"] = $("#timeframeSelect").val();
        state["selectedFolder"] = $("#folderSelectHtmlId").val();
        state["selectedAggrGroupValuesUnCached"] = [];
        state["selectedFilter"] = $(".chart-filter.selected").data("filter");
        state["selectedAggregationValue"] = $('input[name=aggregationValue]:checked').val();
        var measurandSelects = $(".measurand-select");
        // leave out last select as it's the "hidden clone"
        for (var i = 0; i < measurandSelects.length - 1; i++) {
            var val = $(measurandSelects[i]).val();
            if (val) {
                state['selectedAggrGroupValuesUnCached'].push(val);
            }
        }

        var inFrontButton = $("#inFrontButton");
        if (inFrontButton.length) {
            state["stackBars"] = inFrontButton.hasClass("active") ? "1" : "0";
        }

        var encodedState = urlEncodeState(state);
        if (encodedState !== loadedState) {
            loadedState = encodedState;
            window.history.pushState(state, "", "show?" + encodedState);
        }
    };

    var loadState = function (state) {
        if (!state) {
            return;
        }
        var encodedState = urlEncodeState(state);
        if (encodedState === loadedState) {
            return;
        }
        setTimeFrame(state);
        setJobGroups(state);
        setMeasurands(state);
        setSelectedFilter(state);
        setAggregationValue(state);
        loadedState = encodedState;
        if (state.selectedFolder) {
            $(window).trigger("historyStateLoaded");
        }
    };

    var setTimeFrame = function (state) {
        var timeFrame = (state["from"] && state["to"]) ? [new Date(state["from"]), new Date(state["to"])] : null;
        OpenSpeedMonitor.selectIntervalTimeframeCard.setTimeFrame(timeFrame, state["selectedTimeFrameInterval"]);
    };

    var setJobGroups = function (params) {
        if (params['selectedFolder']) {
            setMultiSelect("folderSelectHtmlId", params['selectedFolder']);
        }
    };

    var setMeasurands = function (params) {
        var measurands = params['selectedAggrGroupValuesUnCached'];
        if (!measurands) {
            return;
        }
        if (measurands.constructor !== Array) {
            measurands = [measurands];
        }
        $(".measurandSeries-clone").remove();
        var currentAddButton = $("#addMeasurandButton");
        for (var i = 0; i < measurands.length - 1; i++) {
            currentAddButton.click();
        }
        var selects = $(".measurand-select");
        measurands.forEach(function (measurand, i) {
            $(selects[i]).val(measurand);
        });
    };

    var setSelectedFilter = function (state) {
        if (!state["selectedFilter"]) {
            return;
        }
        $(".chart-filter").each(function () {
            var $this = $(this);
            $this.toggleClass("selected", $this.data("filter") === state["selectedFilter"]);
        });
    };

    var setAggregationValue = function (state) {
        if (!state["selectedAggregationValue"]) {
            return
        }
        var isAvg = state["selectedAggregationValue"] === "avg";
        $("#averageButton input").prop("checked", isAvg);
        $("#averageButton").toggleClass("active", isAvg);
        $("#medianButton input").prop("checked", !isAvg);
        $("#medianButton").toggleClass("active", !isAvg);
    };

    var setMultiSelect = function (id, values) {
        $("#" + id).val(values);
        $("#" + id).trigger("change")
    };

    initWaitForPostload();
    return {};
})();