//= require /urlHandling/urlHelper.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.UrlHandling = OpenSpeedMonitor.ChartModules.UrlHandling || {};

OpenSpeedMonitor.ChartModules.UrlHandling.PageAggregation = (function () {
    var loadedState = "";

    var initWaitForPostload = function () {
        var dependencies = ["pageComparison", "resultSelection"];
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
        state['measurand'] = $("#selectedAggrGroupValuesUnCached").val();
        state['firstJobGroup'] = [];
        state['firstPage'] = [];
        state['secondJobGroup'] = [];
        state['secondPage'] = [];

        var comparisonRows = $(".addPageComparisonRow");
        // leave out last select as it's the "hidden clone"
        for (var i = 0; i < comparisonRows.length ; i++) {
            var firstJobGroup = $(comparisonRows[i]).find("select[name=firstJobGroupSelect]").val();
            var firstPage = $(comparisonRows[i]).find("select[name=firstPageSelect]").val();
            var secondJobGroup = $(comparisonRows[i]).find("select[name=secondJobGroupSelect]").val();
            var secondPage = $(comparisonRows[i]).find("select[name=secondPageSelect]").val();
            state['firstJobGroup'].push(firstJobGroup);
            state['firstPage'].push(firstPage);
            state['secondJobGroup'].push(secondJobGroup);
            state['secondPage'].push(secondPage);
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
        setPages(state);
        setMeasurand(state);
        loadedState = encodedState;
        if(state['firstJobGroup'] && state['firstPage'] && state['secondJobGroup'] && state['secondPage']){
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

    var setPages = function (params) {
        var firstPages = params['firstPage'];
        var secondPages = params['secondPage'];
        var firstJobGroup = params['firstJobGroup'];
        var secondJobGroup = params['secondJobGroup'];
        var rows = firstJobGroup.length;
        var isArray = firstJobGroup.constructor == Array;
        var firstJobGroupSelects = $("select[name=firstJobGroupSelect]");
        var firstPageSelects = $("select[name=firstPageSelect]");
        var secondJobGroupSelects = $("select[name=secondJobGroupSelect]");
        var secondPageSelects = $("select[name=secondPageSelect]");

        if(isArray){
            ensureComparisonRowAmount(rows);
            for (var i = 0; i<rows; i++) {
                $(firstJobGroupSelects[i]).val(firstJobGroup[i]);
                $(secondJobGroupSelects[i]).val(secondJobGroup[i]);
                $(firstPageSelects[i]).val(firstPages[i]);
                $(secondPageSelects[i]).val(secondPages[i]);
            }
        } else{
                $(firstJobGroupSelects[0]).val(firstJobGroup);
                $(secondJobGroupSelects[0]).val(secondJobGroup);
                $(firstPageSelects[0]).val(firstPages);
                $(secondPageSelects[0]).val(secondPages);
        }
    };

    var ensureComparisonRowAmount = function(amount){
      var addButton = $("#addPageComparison");
      for(var i= 0; i<amount-1; ++i){
            addButton.click();
      }
    };

    var setMeasurand = function (params) {
        var measurand = params['measurand'];
        if (!measurand) {
            return;
        }
        $("#selectedAggrGroupValuesUnCached").val(measurand);
    };

    initWaitForPostload();
    return {
    };
})();
