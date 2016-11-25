"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.resultSelection = (function(){
    var selectIntervalTimeframeCard = $("#select-interval-timeframe-card");

    var init = function() {
        registerEvents();

        // if the time frame selection is already initialized, we directly update job groups and jobs
        if (OpenSpeedMonitor.selectIntervalTimeframeCard) {
            updateJobGroupsAndJobs(OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame());
        }
    };

    var registerEvents = function() {
        selectIntervalTimeframeCard.on("timeFrameChanged", function (ev, timeFrame) {
           updateJobGroupsAndJobs(timeFrame)
        });
    };

    var updateJobGroupsAndJobs = function(timeFrame) {

    };

    init();
    return {
        updateJobGroupsAndJobs: updateJobGroupsAndJobs
    };
})();