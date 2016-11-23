"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

/**
 * Requires OpenSpeedMonitor.urls.resultSelection.getJobGroups to be defined
 */
OpenSpeedMonitor.resultSelection = (function(){
    var selectIntervalTimeframeCard = $("#select-interval-timeframe-card");
    var selectJobGroupCard = $("#select-jobgroup-card");
    var selectPageLocationConnectivityCard = $('#select-page-location-connectivity');
    var getJobGroupsUrl = ((OpenSpeedMonitor.urls || {}).resultSelection || {}).getJobGroups;
    var getMeasuredEventsUrl = ((OpenSpeedMonitor.urls || {}).resultSelection || {}).getMeasuredEvents;
    var currentQueryArgs = {};
    var updatesEnabled = true;

    if (!getJobGroupsUrl) {
        console.log("No OpenSpeedMonitor.urls.resultSelection.getJobGroups needs to be defined");
        return;
    }
    if (!getMeasuredEventsUrl) {
        console.log("No OpenSpeedMonitor.urls.resultSelection.getMeasuredEvents needs to be defined");
        return;
    }

    var init = function() {
        registerEvents();

        // if the time frame selection is already initialized, we directly update job groups and jobs
        if (OpenSpeedMonitor.selectIntervalTimeframeCard) {
            setQueryArgsFromTimeFrame(OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame());
            updateCards();
        }
    };

    var registerEvents = function() {
        selectIntervalTimeframeCard.on("timeFrameChanged", function (ev, start, end) {
            setQueryArgsFromTimeFrame([start, end]);
            updateCards();
        });
        selectJobGroupCard.on("jobGroupSelectionChanged", function (ev, values) {
            currentQueryArgs.jobGroupIds = values;
            updateCards();
        })
    };

    var setQueryArgsFromTimeFrame = function(timeFrame) {
        currentQueryArgs.from = timeFrame[0].toISOString();
        currentQueryArgs.to = timeFrame[1].toISOString();
    };

    var updateCards = function () {
        if (!updatesEnabled) {
            return;
        }
        if (OpenSpeedMonitor.selectJobGroupCard) {
            updateCard(getJobGroupsUrl, OpenSpeedMonitor.selectJobGroupCard.updateJobGroups);
        }
        if (OpenSpeedMonitor.selectPageLocationConnectivityCard) {
            updateCard(getMeasuredEventsUrl, OpenSpeedMonitor.selectPageLocationConnectivityCard.updateMeasuredEvents);
        }
    };

    var enableUpdates = function (enable) {
        var oldValue = updatesEnabled;
        updatesEnabled = enable;
        return oldValue;
    };

    var updateCard = function(url, handler) {
        $.ajax({
            url: url,
            type: 'GET',
            data: currentQueryArgs,
            dataType: "json",
            success: function (data) {
                var updateWasEnabled = enableUpdates(false);
                handler(data);
                enableUpdates(updateWasEnabled);
            },
            error: function (e) {
                // TODO(sburnicki): Show a proper error in the UI
                throw e;
            },
            traditional: true // grails compatible parameter array encoding
        });
    };

    init();
    return {
    };
})();