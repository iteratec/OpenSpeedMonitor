"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

/**
 * Requires OpenSpeedMonitor.urls.resultSelection.getJobGroups to be defined
 */
OpenSpeedMonitor.resultSelection = (function(){
    var selectIntervalTimeframeCard = $("#select-interval-timeframe-card");
    var selectJobGroupCard = $("#select-jobgroup-card");
    var selectPageLocationConnectivityCard = $('#select-page-location-connectivity');
    var pageTabElement = $('#page-tab');
    var browserTabElement = $('#browser-tab');
    var connectivityTabElement = $('#connectivity-tab');
    var resultSelectionUrls = (OpenSpeedMonitor.urls || {}).resultSelection;
    var currentQueryArgs = {};
    var updatesEnabled = true;
    var ajaxRequests = {};
    var spinnerJobGroup = new OpenSpeedMonitor.Spinner(selectJobGroupCard, "small");
    var spinnerPageLocationConnectivity = new OpenSpeedMonitor.Spinner(selectPageLocationConnectivityCard, "small");
    var initiators = ["jobGroups", "pages", "browsers", "connectivity"];

    if (!initiators.every(function(i) {return resultSelectionUrls[i] !== undefined})) {
        throw "No OpenSpeedMonitor.urls.resultSelection needs to be an object with URLs for all controller actions";
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
            updateCards("timeFrame");
        });
        selectJobGroupCard.on("jobGroupSelectionChanged", function (ev, jobGroupSelection) {
            currentQueryArgs.jobGroupIds = jobGroupSelection.hasAllSelected ? null : jobGroupSelection.ids;
            updateCards("jobGroups");
        });
        selectPageLocationConnectivityCard.on("pageSelectionChanged", function (ev, pageSelection) {
            currentQueryArgs.pageIds = pageSelection.hasAllSelected ? null : pageSelection.ids;
            updateCards("pages");
        });
        selectPageLocationConnectivityCard.on("measuredEventSelectionChanged", function (ev, measuredEventSelection) {
            currentQueryArgs.measuredEventIds =  measuredEventSelection.hasAllSelected ? null : measuredEventSelection.ids;
            updateCards("pages");
        });
        selectPageLocationConnectivityCard.on("browserSelectionChanged", function (ev, browserSelection) {
            currentQueryArgs.browserIds = browserSelection.hasAllSelected ? null : browserSelection.ids;
            updateCards("browsers");
        });
        selectPageLocationConnectivityCard.on("locationSelectionChanged", function (ev, locationSelection) {
            currentQueryArgs.locationIds =  locationSelection.hasAllSelected ? null : locationSelection.ids;
            updateCards("browsers");
        });
        selectPageLocationConnectivityCard.on("connectivitySelectionChanged", function (ev, connectivitySelection) {
            currentQueryArgs.connectivityIds = connectivitySelection.hasAllSelected ? null : connectivitySelection.ids;
            currentQueryArgs.nativeConnectivity = connectivitySelection.hasAllSelected ? null : connectivitySelection.native;
            currentQueryArgs.customConnectivities = connectivitySelection.hasAllSelected ? null : connectivitySelection.customNames;
            updateCards("connectivity");
        });
    };

    var setQueryArgsFromTimeFrame = function(timeFrame) {
        currentQueryArgs.from = timeFrame[0].toISOString();
        currentQueryArgs.to = timeFrame[1].toISOString();
    };

    var updateCards = function (initiator) {
        if (!updatesEnabled) {
            return;
        }
        if (OpenSpeedMonitor.selectJobGroupCard && initiator != "jobGroups") {
            spinnerJobGroup.start();
            updateCard(resultSelectionUrls["jobGroups"], OpenSpeedMonitor.selectJobGroupCard.updateJobGroups, spinnerJobGroup);
        }
        if (initiator != "pages" && initiator != "browsers" && initiator != "connectivity") {
            spinnerPageLocationConnectivity.start();
        }
        var spinner = null;
        if (OpenSpeedMonitor.selectPageLocationConnectivityCard && initiator != "pages") {
            spinner = pageTabElement.hasClass("active") ? spinnerPageLocationConnectivity : null;
            updateCard(resultSelectionUrls["pages"], OpenSpeedMonitor.selectPageLocationConnectivityCard.updateMeasuredEvents, spinner);
        }
        if (OpenSpeedMonitor.selectPageLocationConnectivityCard && initiator != "browsers") {
            spinner = browserTabElement.hasClass("active") ? spinnerPageLocationConnectivity : null;
            updateCard(resultSelectionUrls["browsers"], OpenSpeedMonitor.selectPageLocationConnectivityCard.updateLocations, spinner);
        }
        if (OpenSpeedMonitor.selectPageLocationConnectivityCard && initiator != "connectivity") {
            spinner = connectivityTabElement.hasClass("active") ? spinnerPageLocationConnectivity : null;
            updateCard(resultSelectionUrls["connectivity"], OpenSpeedMonitor.selectPageLocationConnectivityCard.updateConnectivityProfiles, spinner);
        }
    };

    var enableUpdates = function (enable) {
        var oldValue = updatesEnabled;
        updatesEnabled = enable;
        return oldValue;
    };

    var updateCard = function(url, handler, spinner) {
        if (ajaxRequests[url]) {
            ajaxRequests[url].abort();
        }
        ajaxRequests[url] = $.ajax({
            url: url,
            type: 'GET',
            data: currentQueryArgs,
            dataType: "json",
            success: function (data) {
                var updateWasEnabled = enableUpdates(false);
                handler(data);
                enableUpdates(updateWasEnabled);
                if (spinner) {
                    spinner.stop();
                }
            },
            error: function (e, statusText) {
                if (statusText != "abort") {
                    if (spinner) {
                        spinner.stop();
                    }
                    // TODO(sburnicki): Show a proper error in the UI
                    throw e;
                }
            },
            traditional: true // grails compatible parameter array encoding
        });
    };

    init();
    return {
    };
})();