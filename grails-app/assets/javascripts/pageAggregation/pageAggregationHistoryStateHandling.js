//= require /urlHandling/urlHelper.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.UrlHandling = OpenSpeedMonitor.ChartModules.UrlHandling || {};

OpenSpeedMonitor.ChartModules.UrlHandling.PageAggregation = (function () {
    var restoredState = "";

    var initWaitForPostload = function () {
        var dependencies = ["pageAggregation", "selectIntervalTimeframeCard"];
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
        state["selectedPages"] = $("#pageSelectHtmlId").val();
        state['selectedAggrGroupValuesUnCached'] = [];
        state["selectedFilter"] = $(".chart-filter.selected").data("filter");
        var measurandSelects = $(".measurand-select");
        // leave out last select as it's the "hidden clone"
        for (var i = 0; i < measurandSelects.length - 1; i++) {
            var val = $(measurandSelects[i]).val();
            if (val) {
                state['selectedAggrGroupValuesUnCached'].push(val);
            }
        }
        var comparativeTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getComparativeTimeFrame();
        if (comparativeTimeFrame) {
            state["comparativeFrom"] = comparativeTimeFrame[0].toISOString();
            state["comparativeTo"] = comparativeTimeFrame[1].toISOString();
        }
        var inFrontButton = $("#inFrontButton");
        if (inFrontButton.length) {
            state["stackBars"] = inFrontButton.hasClass("active") ? "1" : "0";
        }

        var encodedState = urlEncodeState(state);
        if (encodedState !== restoredState) {
            restoredState = encodedState;
            window.history.pushState(state, "", "show?" + encodedState);
        }
    };

    var loadState = function (state) {
        if (!state) {
            return;
        }
        var encodedState = urlEncodeState(state);
        if (encodedState === restoredState) {
            return;
        }
        setTimeFrame(state);
        setJobGroups(state);
        setPages(state);
        setMeasurands(state);
        setSelectedFilter(state);
        setStackBars(state);
        restoredState = encodedState;
        if(state.selectedFolder && state.selectedPages){
            $(window).trigger("historyStateLoaded");
        }
    };

    var setTimeFrame = function (state) {
        var timeFrame = (state["from"] && state["to"]) ? [new Date(state["from"]), new Date(state["to"])] : null;
        OpenSpeedMonitor.selectIntervalTimeframeCard.setTimeFrame(timeFrame, state["selectedTimeFrameInterval"]);
        if (state["comparativeFrom"] && state["comparativeTo"]) {
            var comparativeTimeFrame = [new Date(state["comparativeFrom"]), new Date(state["comparativeTo"])];
            OpenSpeedMonitor.selectIntervalTimeframeCard.setComparativeTimeFrame(comparativeTimeFrame);
        }
    };

    var setJobGroups = function (params) {
        if (params['selectedFolder']) {
            setMultiSelect("folderSelectHtmlId", params['selectedFolder']);
        }
    };
    var setPages = function (params) {
        if (params['selectedPages']) {
            setMultiSelect("pageSelectHtmlId", params['selectedPages']);
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
        for (var i = 0; i < measurands.length -1; i++) {
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
        $(".chart-filter").each(function() {
            var $this = $(this);
            $this.toggleClass("selected", $this.data("filter") === state["selectedFilter"]);
        });
    };

    var setStackBars = function (state) {
        if (state["stackBars"] === undefined) {
            return;
        }
        var isStacked = state["stackBars"] === "1";
        $("#inFrontButton input").prop("checked", isStacked);
        $("#inFrontButton").toggleClass("active", isStacked);
        $("#besideButton input").prop("checked", !isStacked);
        $("#besideButton").toggleClass("active", !isStacked);
    };

    var setMultiSelect = function (id, values) {
        $("#" + id).val(values);
        $("#" + id).trigger("change")
    };

    initWaitForPostload();
    return {
    };
})();
