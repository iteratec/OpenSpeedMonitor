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
        $('#graphButtonHtmlId').on('click', saveState);
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

        var encodedState = urlEncodeState(state);
        if (encodedState !== restoredState) {
            restoredState = encodedState;
            window.history.pushState(state, "", "show?" + encodedState);
        }
    };

    var loadState = function (state) {
        if (!state ) {
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
        restoredState = encodedState;
        if(state.selectedFolder && state.selectedPages){
            $("#graphButtonHtmlId").click();
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

    var setMultiSelect = function (id, values) {
        $("#" + id).val(values);
        $("#" + id).trigger("change")
    };

    initWaitForPostload();
    return {
    };
})();
