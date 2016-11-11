//= require /bower_components/bootstrap-timepicker/js/bootstrap-timepicker.js
//= require _dateTimePicker.js
//= require_self

"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectIntervalTimeframeCard = (function(){
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

    var init = function() {
        // initialize controls with values. Either from URL or from local storage
        var interval = OpenSpeedMonitor.urlUtils.getVar("selectedInterval") ||
                       clientStorage.getFromLocalStorage(clientStorageIntervalKey);
        intervalSelectElement.val(interval);

        var startDateTime = dateTimeValuesFromUrl("from", "setFromHour", "fromHour") ||
                            clientStorage.getObjectFromLocalStorage(clientStorageStartObjectKeys);
        startDateTimePicker.setValues(startDateTime);

        var endDateTime = dateTimeValuesFromUrl("to", "setToHour", "toHour") ||
                          clientStorage.getObjectFromLocalStorage(clientStorageEndObjectKeys);
        endDateTimePicker.setValues(endDateTime);
        endDateTimePicker.setStartDate(startDateTime.date);

        var timeFramePreselection = OpenSpeedMonitor.urlUtils.getVar("selectedTimeFrameInterval") ||
                                    clientStorage.getFromLocalStorage(clientStorageTimeFramePresetKey);
        timeFrameSelectElement.val(timeFramePreselection);
        setTimeFramePreselection(timeFramePreselection);

        registerEvents();
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
            timeFrameChanged();
        });

        endDateTimePickerElement.on("changeDateTime", function(ev, dateTimeValues) {
            clientStorage.setObjectToLocalStorage(clientStorageEndObjectKeys, dateTimeValues);
            timeFrameChanged();
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

    var timeFrameChanged = function() {

    };

    init();
})();
