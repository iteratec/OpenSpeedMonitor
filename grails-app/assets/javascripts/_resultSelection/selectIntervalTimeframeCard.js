//= require _timeRangePicker.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectIntervalTimeframeCard = (function(){
    var cardElement = $("#select-interval-timeframe-card");
    var timeFrameSelectElement = $("#timeframeSelect");
    var intervalSelectElement = $("#selectedIntervalHtmlId");
    var timeFramePickerElement = $("#timeframe-picker");
    var timeFramePicker = null;

    var clientStorage = OpenSpeedMonitor.clientSideStorageUtils();
    var clientStorageTimeFramePresetKey = "de.iteratec.osm.result.dashboard.timeframeselection";
    var clientStorageIntervalKey = "de.iteratec.osm.result.dashboard.intervalselection";
    var clientStorageTimeFrameFromKey = 'de.iteratec.osm.result.dashboard.from';
    var clientStorageTimeFrameToKey = 'de.iteratec.osm.result.dashboard.to';
    var manualTimeFrameSelection = "0"; // manual selection
    var defaultIntervalSelection = "-1"; // Raw data
    var defaultTimeFramePreselect = (3 * 24 * 60 * 60).toString(); // 3 days
    var isSavedDashboard = OpenSpeedMonitor.urlUtils.getVar("dashboardID") !== undefined;

    var init = function() {
        // initialize controls with values. Either from presets, from URL, from local storage or defaults
        intervalSelectElement.val(defaultValueForInterval());
        var timeFramePreselection = defaultValueForTimeFramePreselection();
        timeFrameSelectElement.val(timeFramePreselection);
        timeFramePicker = OpenSpeedMonitor.timeRangePicker(timeFramePickerElement);

        setTimeFramePreselection(timeFramePreselection, true);
        if (timeFramePreselection === manualTimeFrameSelection) {
            timeFramePicker.setRange(defaultValueForStart(), defaultValueForEnd());
        }

        registerEvents();
        triggerTimeFrameChanged(); // initial event
    };

    var defaultValueForInterval = function() {
        if (isSavedDashboard) {
            return intervalSelectElement.val();
        }
        return OpenSpeedMonitor.urlUtils.getVar("selectedInterval") ||
               clientStorage.getFromLocalStorage(clientStorageIntervalKey) ||
               defaultIntervalSelection;
    };

    var defaultValueForStart = function() {
        if (isSavedDashboard) {
            return timeFramePicker.getRange()[0].toISOString();
        }
        return OpenSpeedMonitor.urlUtils.getVar("from") ||
               clientStorage.getFromLocalStorage(clientStorageTimeFrameFromKey);
    };

    var defaultValueForEnd = function() {
        if (isSavedDashboard) {
            return timeFramePicker.getRange()[1].toISOString();
        }
        return OpenSpeedMonitor.urlUtils.getVar("to") ||
            clientStorage.getFromLocalStorage(clientStorageTimeFrameToKey);
    };

    var defaultValueForTimeFramePreselection = function() {
        if (isSavedDashboard) {
            return timeFrameSelectElement.val();
        }
        return OpenSpeedMonitor.urlUtils.getVar("selectedTimeFrameInterval") ||
            clientStorage.getFromLocalStorage(clientStorageTimeFramePresetKey) ||
            defaultTimeFramePreselect;
    };

    var registerEvents = function() {
        intervalSelectElement.on("change", function () {
            clientStorage.setToLocalStorage(clientStorageIntervalKey, this.value);
        });

        timeFrameSelectElement.on("change", function() {
            clientStorage.setToLocalStorage(clientStorageTimeFramePresetKey, this.value);
            setTimeFramePreselection(this.value);
        });

        timeFramePickerElement.on("rangeChanged", function (ev, from, to) {
            clientStorage.setToLocalStorage(clientStorageTimeFrameFromKey, from.toISOString());
            clientStorage.setToLocalStorage(clientStorageTimeFrameToKey, to.toISOString());
            clientStorage.setToLocalStorage(clientStorageTimeFramePresetKey, manualTimeFrameSelection);
            timeFrameSelectElement.val(manualTimeFrameSelection);
            triggerTimeFrameChanged();
        })
    };

    var setTimeFramePreselection = function(value, suppressEvent) {
        value = parseInt(value);
        if (isNaN(value) || value <= 0) { // manual time selection
            return;
        }
        var to = new Date();
        var from = new Date(to.getTime() - (value * 1000));
        if (value > 24 * 60 * 60) {
            to.setHours(23, 59, 59, 999);
            from.setHours(0, 0, 0, 0);
        }
        timeFramePicker.setRange(from, to);
        if (!suppressEvent) {
            cardElement.trigger("timeFrameChanged", [getTimeFrame()]);
        }
    };

    var getTimeFrame = function () {
      return timeFramePicker.getRange();
    };

    var triggerTimeFrameChanged = function() {
        cardElement.trigger("timeFrameChanged", [getTimeFrame()]);
    };

    init();
    return {
        getTimeFrame : getTimeFrame
    }

})();
