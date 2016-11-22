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
    var getPagesUrl = ((OpenSpeedMonitor.urls || {}).resultSelection || {}).getPages;
    var currentQueryArgs = {};

    if (!getJobGroupsUrl) {
        console.log("No OpenSpeedMonitor.urls.resultSelection.getJobGroups needs to be defined");
        return;
    }
    if (!getPagesUrl) {
        console.log("No OpenSpeedMonitor.urls.resultSelection.getPages needs to be defined");
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
            currentQueryArgs.jobGroups = values;
            updateCards();
        })
    };

    var setQueryArgsFromTimeFrame = function(timeFrame) {
        currentQueryArgs.from = timeFrame[0].toISOString();
        currentQueryArgs.to = timeFrame[1].toISOString();
    };

    var updateCards = function () {
        updateJobGroups();
        updatePages();
    };

    var updateJobGroups = function() {
        $.ajax({
            url: getJobGroupsUrl,
            type: 'GET',
            data: currentQueryArgs,
            dataType: "json",
            success: function (jobGroups) {
                if (selectJobGroupCard) {
                    OpenSpeedMonitor.selectJobGroupCard.updateJobGroups(jobGroups);
                }
            },
            error: function (e) {
                // TODO(sburnicki): Show a proper error in the UI
                throw e;
            }
        });
    };

    var updatePages = function() {
        $.ajax({
            url: getPagesUrl,
            type: 'GET',
            data: currentQueryArgs,
            dataType: "json",
            success: function (pages) {
                if (selectPageLocationConnectivityCard) {
                    OpenSpeedMonitor.selectPageLocationConnectivityCard.updatePages(pages);
                }
            },
            error: function (e) {
                // TODO(sburnicki): Show a proper error in the UI
                throw e;
            }
        });
    };

    init();
    return {
    };
})();