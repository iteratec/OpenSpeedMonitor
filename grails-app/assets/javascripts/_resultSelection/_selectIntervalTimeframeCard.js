"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectIntervalTimeframeCard = (function(){
    var startDateTimePickerElement = $("#startDateTimePicker");
    var startDateTimePicker = OpenSpeedMonitor.DateTimePicker(startDateTimePickerElement, "00:00");
    var endDateTimePickerElement = $("#endDateTimePicker");
    var endDateTimePicker = OpenSpeedMonitor.DateTimePicker(endDateTimePickerElement, "23:59");

    var clientStorage = OpenSpeedMonitor.clientSideStorageUtils();
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
        startDateTimePicker.values(clientStorage.getObjectFromLocalStorage(clientStorageStartObjectKeys));
        endDateTimePicker.values(clientStorage.getObjectFromLocalStorage(clientStorageEndObjectKeys));

        registerEvents();
    };

    var registerEvents = function () {
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

    var timeFrameChanged = function() {

    };

    init();
})();
