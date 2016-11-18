"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

/**
 * Requires OpenSpeedMonitor.urls.resultSelection.getJobGroups to be defined
 */
OpenSpeedMonitor.resultSelection = (function(){
    var selectIntervalTimeframeCard = $("#select-interval-timeframe-card");
    var selectJobGroupCard = $("select-jobgroup-card")
    var getJobGroupsUrl = ((OpenSpeedMonitor.urls || {}).resultSelection || {}).getJobGroups;

    if (!getJobGroupsUrl) {
        console.log("No OpenSpeedMonitor.urls.resultSelection.getJobGroups needs to be defined");
        return;
    }

    var init = function() {
        registerEvents();

        // if the time frame selection is already initialized, we directly update job groups and jobs
        if (OpenSpeedMonitor.selectIntervalTimeframeCard) {
            updateJobGroupsAndJobs(OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame());
        }
    };

    var registerEvents = function() {
        selectIntervalTimeframeCard.on("timeFrameChanged", function (ev, start, end) {
           updateJobGroupsAndJobs(start, end)
        });
    };

    var updateJobGroupsAndJobs = function(start, end) {
        $.ajax({
            url: getJobGroupsUrl,
            type: 'GET',
            data: {
                from: start.toISOString(),
                to: end.toISOString()
            },
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

    init();
    return {
        updateJobGroupsAndJobs: updateJobGroupsAndJobs
    };
})();