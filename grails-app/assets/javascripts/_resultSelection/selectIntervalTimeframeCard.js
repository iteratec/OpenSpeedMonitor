//= require /bower_components/bootstrap-timepicker/js/bootstrap-timepicker.js
//= require _dateTimePicker.js
//= require_self

"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectIntervalTimeframeCard = (function(){
    var cardElement = $("#select-interval-timeframe-card");
    var startDateTimePickerElement = $("#startDateTimePicker");
    var startDateTimePicker = OpenSpeedMonitor.DateTimePicker(startDateTimePickerElement, "00:00");
    var endDateTimePickerElement = $("#endDateTimePicker");
    var endDateTimePicker = OpenSpeedMonitor.DateTimePicker(endDateTimePickerElement, "23:59");
    var timeFrameSelectElement = $("#timeframeSelect");
    var manualTimeFrameFieldSet = $("#manual-timeframe-selection");
    var intervalSelectElement = $("#selectedIntervalHtmlId");

    var clientStorage = OpenSpeedMonitor.clientSideStorageUtils();
    var clientStorageTimeFramePresetKey = "de.iteratec.osm.result.dashboard.timeframeselection";
    var clientStorageIntervalKey = "de.iteratec.osm.result.dashboard.intervalselection";
    var clientStorageStartObjectKeys = {
        date: 'de.iteratec.osm.result.dashboard.from',
        manualTime: 'de.iteratec.osm.result.dashboard.manualFromHour',
        time: 'de.iteratec.osm.result.dashboard.fromHour'
    };
    var clientStorageEndObjectKeys = {
        date: 'de.iteratec.osm.result.dashboard.to',
        manualTime: 'de.iteratec.osm.result.dashboard.manualToHour',
        time: 'de.iteratec.osm.result.dashboard.toHour'
    };
    var defaultIntervalSelection = "-1"; // Raw data
    var defaultTimeFramePreselect = (3 * 24 * 60 * 60).toString(); // 3 days
    var isSavedDashboard = OpenSpeedMonitor.urlUtils.getVar("dashboardID") !== undefined;

    var init = function() {
        // initialize controls with values. Either from presets, from URL, from local storage or defaults
        intervalSelectElement.val(defaultValueForInterval());

        var startValues = defaultValueForStart();
        startDateTimePicker.setValues(startValues);
        endDateTimePicker.setValues(defaultValueForEnd());
        endDateTimePicker.setStartDate(startValues.date);

        var timeFramePreselection = defaultValueForTimeFramePreselection();
        timeFrameSelectElement.val(timeFramePreselection);
        setTimeFramePreselection(timeFramePreselection);

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
            return startDateTimePicker.getValues();
        }
        return dateTimeValuesFromUrl("from", "setFromHour", "fromHour") ||
               clientStorage.getObjectFromLocalStorage(clientStorageStartObjectKeys);
    };

    var defaultValueForEnd = function() {
        if (isSavedDashboard) {
            return endDateTimePicker.getValues();
        }
        return dateTimeValuesFromUrl("to", "setToHour", "toHour") ||
            clientStorage.getObjectFromLocalStorage(clientStorageEndObjectKeys);
    };

    var defaultValueForTimeFramePreselection = function() {
        if (isSavedDashboard) {
            return timeFrameSelectElement.val();
        }
        return OpenSpeedMonitor.urlUtils.getVar("selectedTimeFrameInterval") ||
            clientStorage.getFromLocalStorage(clientStorageTimeFramePresetKey) ||
            defaultTimeFramePreselect;
    };

    var dateTimeValuesFromUrl = function(dateKey, manualTimeKey, timeKey) {
        var date = OpenSpeedMonitor.urlUtils.getVar(dateKey);
        var manualTime = OpenSpeedMonitor.urlUtils.getVar(manualTimeKey) == "on";
        var time = OpenSpeedMonitor.urlUtils.getVar(timeKey);
        if (!date || !time) {
            return null;
        }
        return {
            date: date,
            manualTime: manualTime,
            time: time
        };
    };

    var registerEvents = function() {
        intervalSelectElement.on("change", function () {
            clientStorage.setToLocalStorage(clientStorageIntervalKey, this.value);
        });

        timeFrameSelectElement.on("change", function() {
            clientStorage.setToLocalStorage(clientStorageTimeFramePresetKey, this.value);
            setTimeFramePreselection(this.value);
        });

        startDateTimePickerElement.on("changeDateTime", function(ev, dateTimeValues) {
            clientStorage.setObjectToLocalStorage(clientStorageStartObjectKeys, dateTimeValues);
            endDateTimePicker.setStartDate(dateTimeValues.date);
            triggerTimeFrameChanged();
        });

        endDateTimePickerElement.on("changeDateTime", function(ev, dateTimeValues) {
            clientStorage.setObjectToLocalStorage(clientStorageEndObjectKeys, dateTimeValues);
            triggerTimeFrameChanged();
        });
    };

    var setTimeFramePreselection = function(value) {
        value = parseInt(value);
        if (isNaN(value) || value <= 0) { // manual time selection
            manualTimeFrameFieldSet.prop("disabled", false);
            return;
        }
        // timeframe preselection based on current date
        manualTimeFrameFieldSet.prop("disabled", true);
        var now = new Date();
        var startDate = new Date(now.getTime() - (value * 1000));
        startDateTimePicker.setValuesByDate(startDate);
        endDateTimePicker.setValuesByDate(now);
        endDateTimePicker.setStartDate(startDate);
    };

    var getTimeFrame = function () {
      return [startDateTimePicker.getValuesAsDate(), endDateTimePicker.getValuesAsDate()];
    };

    var triggerTimeFrameChanged = function() {
        cardElement.trigger("timeFrameChanged", getTimeFrame());
    };

    init();
    return {
        getTimeFrame : getTimeFrame
    }
})();
