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
    var warningNoData = $('#warning-no-data');
    var showButtons = $('#show-button-group input, #show-button-group button');
    var warningLongProcessing = $('#warning-long-processing');
    var warningNoJobGroupSelected = $('#warning-no-job-group');
    var warningNoPageSelected = $('#warning-no-page');
    var resultSelectionUrls = (OpenSpeedMonitor.urls || {}).resultSelection;
    var currentQueryArgs = {
        from: null,
        to: null,
        jobGroupIds: null,
        pageIds: null,
        measuredEventIds: null,
        browserIds: null,
        locationIds: null,
        connectivityIds: null,
        nativeConnectivity: null,
        customConnectivities: null
    };
    var lastUpdateJSON = JSON.stringify(currentQueryArgs);
    var updatesEnabled = true;
    var ajaxRequests = {};
    var spinnerJobGroup = new OpenSpeedMonitor.Spinner(selectJobGroupCard, "small");
    var spinnerPageLocationConnectivity = new OpenSpeedMonitor.Spinner(selectPageLocationConnectivityCard, "small");
    var initiators = ["jobGroups", "pages", "browsers", "connectivity", "resultCount"];
    var hasJobGroupSelection = !!$("#folderSelectHtmlId").val();
    var hasPageSelection = !!$("#pageSelectHtmlId").val();
    var lastResultCount = 1;

    if (!initiators.every(function(i) {return resultSelectionUrls[i] !== undefined})) {
        throw "No OpenSpeedMonitor.urls.resultSelection needs to be an object with URLs for all controller actions";
    }

    var init = function() {
        enableUpdates(false);
        registerEvents();

        // if the cards are already initialized, we directly update job groups and jobs
        if (OpenSpeedMonitor.selectIntervalTimeframeCard) {
            setQueryArgsFromTimeFrame(null, OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame());
        }
        if (OpenSpeedMonitor.selectJobGroupCard) {
            setQueryArgsFromJobGroupSelection(null, OpenSpeedMonitor.selectJobGroupCard.getJobGroupSelection());
        }
        if (OpenSpeedMonitor.selectPageLocationConnectivityCard) {
            setQueryArgsFromPageSelection(null, OpenSpeedMonitor.selectPageLocationConnectivityCard.getPageSelection());
            setQueryArgsFromMeasuredEventSelection(null, OpenSpeedMonitor.selectPageLocationConnectivityCard.getMeasuredEventSelection());
            setQueryArgsFromBrowserSelection(null, OpenSpeedMonitor.selectPageLocationConnectivityCard.getBrowserSelection());
            setQueryArgsFromLocationSelection(null, OpenSpeedMonitor.selectPageLocationConnectivityCard.getLocationSelection());
            setQueryArgsFromConnectivitySelection(null, OpenSpeedMonitor.selectPageLocationConnectivityCard.getConnectivitySelection());
        }
        enableUpdates(true);
        updateCards();
    };

    var registerEvents = function() {
        selectIntervalTimeframeCard.on("timeFrameChanged", setQueryArgsFromTimeFrame);
        selectJobGroupCard.on("jobGroupSelectionChanged", setQueryArgsFromJobGroupSelection);
        selectPageLocationConnectivityCard.on("pageSelectionChanged", setQueryArgsFromPageSelection);
        selectPageLocationConnectivityCard.on("measuredEventSelectionChanged", setQueryArgsFromMeasuredEventSelection);
        selectPageLocationConnectivityCard.on("browserSelectionChanged", setQueryArgsFromBrowserSelection);
        selectPageLocationConnectivityCard.on("locationSelectionChanged", setQueryArgsFromLocationSelection);
        selectPageLocationConnectivityCard.on("connectivitySelectionChanged", setQueryArgsFromConnectivitySelection);
    };

    var setQueryArgsFromTimeFrame = function (event, timeFrameSelection) {
        currentQueryArgs.from = timeFrameSelection[0].toISOString();
        currentQueryArgs.to = timeFrameSelection[1].toISOString();
        updateCards("timeFrame");
    };

    var setQueryArgsFromJobGroupSelection = function (event, jobGroupSelection) {
        hasJobGroupSelection = !!(jobGroupSelection && jobGroupSelection.ids && jobGroupSelection.ids.length > 0);
        currentQueryArgs.jobGroupIds = jobGroupSelection.hasAllSelected ? null : jobGroupSelection.ids;
        updateCards("jobGroups");
    };

    var setQueryArgsFromPageSelection = function (event, pageSelection) {
        hasPageSelection = !!(pageSelection && pageSelection.ids && pageSelection.ids.length > 0);
        currentQueryArgs.pageIds = pageSelection.hasAllSelected ? null : pageSelection.ids;
        updateCards("pages");
    };

    var setQueryArgsFromBrowserSelection = function (event, browserSelection) {
        currentQueryArgs.browserIds = browserSelection.hasAllSelected ? null : browserSelection.ids;
        updateCards("browsers");
    };

    var setQueryArgsFromLocationSelection = function (ev, locationSelection) {
        currentQueryArgs.locationIds =  locationSelection.hasAllSelected ? null : locationSelection.ids;
        updateCards("browsers");
    };

    var setQueryArgsFromConnectivitySelection = function (event, connectivitySelection) {
        currentQueryArgs.connectivityIds = connectivitySelection.hasAllSelected ? null : connectivitySelection.ids;
        currentQueryArgs.nativeConnectivity = connectivitySelection.hasAllSelected ? null : connectivitySelection.native;
        currentQueryArgs.customConnectivities = connectivitySelection.hasAllSelected ? null : connectivitySelection.customNames;
        updateCards("connectivity");
    };

    var setQueryArgsFromMeasuredEventSelection = function (event, measuredEventSelection) {
        currentQueryArgs.measuredEventIds =  measuredEventSelection.hasAllSelected ? null : measuredEventSelection.ids;
        updateCards("pages");
    };

    var updateCards = function (initiator) {
        validateForm();
        var currentUpdateJSON = JSON.stringify(currentQueryArgs);
        if (!updatesEnabled || !currentQueryArgs.from || !currentQueryArgs.to || lastUpdateJSON == currentUpdateJSON) {
            return;
        }
        lastUpdateJSON = currentUpdateJSON;
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
        updateCard(resultSelectionUrls["resultCount"], updateResultCount, spinner);
    };

    var validateForm = function () {
        warningNoPageSelected.toggle(!hasPageSelection && lastResultCount != 0);
        warningNoJobGroupSelected.toggle(!hasJobGroupSelection && lastResultCount != 0);
        showButtons.prop("disabled", lastResultCount == 0 || !hasJobGroupSelection || !hasPageSelection);
    };

    var updateResultCount = function (resultCount) {
        lastResultCount = resultCount;
        warningLongProcessing.toggle(resultCount < 0 && hasPageSelection && hasJobGroupSelection);
        warningNoData.toggle(resultCount == 0);
        validateForm();
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

    var enableUpdates = function (enable) {
        var oldValue = updatesEnabled;
        updatesEnabled = enable;
        return oldValue;
    };

    init();
    return {
    };
})();