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
        intervalSelectElement.val(clientStorage.getFromLocalStorage(clientStorageIntervalKey));

        startDateTimePicker.setValues(clientStorage.getObjectFromLocalStorage(clientStorageStartObjectKeys));
        endDateTimePicker.setValues(clientStorage.getObjectFromLocalStorage(clientStorageEndObjectKeys));

        var timeFramePreselection = clientStorage.getFromLocalStorage(clientStorageTimeFramePresetKey);
        timeFrameSelectElement.val(timeFramePreselection);
        setTimeFramePreselection(timeFramePreselection);

        registerEvents();
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
