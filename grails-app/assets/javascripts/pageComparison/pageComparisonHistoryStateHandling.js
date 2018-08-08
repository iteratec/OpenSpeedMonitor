//= require /urlHandling/urlHelper.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.UrlHandling = OpenSpeedMonitor.ChartModules.UrlHandling || {};

OpenSpeedMonitor.ChartModules.UrlHandling.PageComparison = (function () {
    var loadedState = "";

    var initWaitForPostload = function () {
        var dependencies = ["pageComparison", "resultSelection", "selectIntervalTimeframeCard"];
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
        //TODO adapt to angular component
        var state = {};
        state["from"] = $("#fromDatepicker").val();
        state["to"] = $("#toDatepicker").val();
        state["selectedTimeFrameInterval"] = $("#timeframeSelect").val();
        state['measurand'] = $("#selectedAggrGroupValuesUnCached").val();
        state['firstJobGroups'] = [];
        state['firstPages'] = [];
        state['secondJobGroups'] = [];
        state['secondPages'] = [];
        window.pageComparisonComponent.getComparisons().forEach(function (comparison) {
            state['firstJobGroups'].push(comparison['firstJobGroupId']);
            state['firstPages'].push(comparison['firstPageId']);
            state['secondJobGroups'].push(comparison['secondJobGroupId']);
            state['secondPages'].push(comparison['secondPageId']);
        });
        state["selectedAggregationValue"] = $('input[name=aggregationValue]:checked').val();
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
        setComparisons(state);
        setMeasurand(state);
        setAggregationValue(state);
        loadedState = encodedState;
        if (state['firstJobGroups'] && state['firstPages'] && state['secondJobGroups'] && state['secondPages']) {
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

    var setComparisons = function (params) {
        var pageId1s = params['firstPages'];
        var pageId2s = params['secondPages'];
        var jobGroupId1 = params['firstJobGroups'];
        var jobGroupId2 = params['secondJobGroups'];
        var rows = jobGroupId1.length;
        var isArray = jobGroupId1.constructor == Array;
        var comparisons = [];
        if (isArray) {
            for (var i = 0; i < rows; i++) {
                var comparison = {};
                comparison['firstJobGroupId'] = parseInt(jobGroupId1[i]);
                comparison['secondJobGroupId'] = parseInt(jobGroupId2[i]);
                comparison['firstPageId'] = parseInt(pageId1s[i]);
                comparison['secondPageId'] = parseInt(pageId2s[i]);
                comparisons.push(comparison);
            }
        } else {
            var comparison = {};
            comparison['firstJobGroupId'] = parseInt(jobGroupId1);
            comparison['secondJobGroupId'] = parseInt(jobGroupId2);
            comparison['firstPageId'] = parseInt(pageId1s);
            comparison['secondPageId'] = parseInt(pageId2s);
            comparisons.push(comparison);
        }
        window.pageComparisonComponent.setComparisons(comparisons);
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

    var setMeasurand = function (params) {
        var measurand = params['measurand'];
        if (!measurand) {
            return;
        }

        var selects = $(".measurand-select");
        if (measurand.startsWith("_UTM")) {
            var optGroupUserTimings = $(selects[0]).find('.measurand-opt-group-USER_TIMINGS');
            var alreadyThere = optGroupUserTimings.length > 1;
            if (!alreadyThere) {
                OpenSpeedMonitor.domUtils.updateSelectOptions(optGroupUserTimings, [{
                    id: measurand,
                    name: measurand
                }])
            }
        }
        $("#selectedAggrGroupValuesUnCached").val(measurand);
    };

    initWaitForPostload();
    return {};
})();
