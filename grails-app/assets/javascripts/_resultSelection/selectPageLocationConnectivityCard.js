//= require _selectWithSelectAllCheckBox.js
//= require _connectedSelects.js
//= require_self

"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectPageLocationConnectivityCard = (function() {
    var cardElement = $('#select-page-location-connectivity');
    var pageSelectElement = $("#pageSelectHtmlId");
    var measuredEventsSelectElement = $("#selectedMeasuredEventsHtmlId");
    var browserSelectElement = $("#selectedBrowsersHtmlId");
    var locationsSelectElement =  $("#selectedLocationsHtmlId");
    var connectivitySelectElement = $("#selectedConnectivityProfilesHtmlId");
    var noResultsText = "No results. Please select a different time frame."; // TODO(sburnicki): use 18n
    var pageEventsConnectedSelects;
    var browserLocationConnectedSelects;
    var triggerEventsEnabled = true;


    var init = function() {
        pageEventsConnectedSelects = OpenSpeedMonitor.ConnectedSelects(pageSelectElement, $(),
            measuredEventsSelectElement, $("#selectedAllMeasuredEvents"));
        browserLocationConnectedSelects = OpenSpeedMonitor.ConnectedSelects(browserSelectElement,
            $("#selectedAllBrowsers"), locationsSelectElement, $("#selectedAllLocations"));
        OpenSpeedMonitor.SelectWithSelectAllCheckBox(connectivitySelectElement, "#selectedAllConnectivityProfiles");
        fixChosen();
        registerEvents();
    };

    var registerEvents = function() {
        pageSelectElement.on("change", function () {
            triggerChangeEvent("pageSelectionChanged", {
                ids: pageSelectElement.val(),
                hasAllSelected: OpenSpeedMonitor.domUtils.hasAllOptionsSelected(pageSelectElement)
            });
        });
        measuredEventsSelectElement.on("change", function () {
            triggerChangeEvent("measuredEventSelectionChanged", {
                ids: measuredEventsSelectElement.val(),
                hasAllSelected: OpenSpeedMonitor.domUtils.hasAllOptionsSelected(measuredEventsSelectElement)
            });
        });
        browserSelectElement.on("change", function () {
            triggerChangeEvent("browserSelectionChanged", {
                ids: browserSelectElement.val(),
                hasAllSelected: OpenSpeedMonitor.domUtils.hasAllOptionsSelected(browserSelectElement)
            });
        });
        locationsSelectElement.on("change", function () {
            triggerChangeEvent("locationSelectionChanged", {
                ids: locationsSelectElement.val(),
                hasAllSelected: OpenSpeedMonitor.domUtils.hasAllOptionsSelected(browserSelectElement)
            });
        });
        connectivitySelectElement.on("change", function () {
            triggerChangeEvent("connectivitySelectionChanged", {
                ids: connectivitySelectElement.val().filter(OpenSpeedMonitor.stringUtils.isNumeric),
                customNames: $.map(connectivitySelectElement.find('option[value="custom"]:selected'), function (option) {
                    return option.text;
                }),
                native: connectivitySelectElement.find('option[value="native"]:selected').length > 0,
                hasAllSelected: OpenSpeedMonitor.domUtils.hasAllOptionsSelected(connectivitySelectElement)
            });
        });
    };

    var triggerChangeEvent = function (eventType, values) {
        if (triggerEventsEnabled) {
            cardElement.trigger(eventType, values);
        }
    };

    var enableTriggerEvents = function(enable) {
        var oldValue = triggerEventsEnabled;
        triggerEventsEnabled = enable;
        return oldValue;
    };

    var updateMeasuredEvents = function (measuredEventsWithPages) {
        var wasTriggerEnabled = enableTriggerEvents(false);
        pageEventsConnectedSelects.updateOptions(measuredEventsWithPages);
        enableTriggerEvents(wasTriggerEnabled);
    };

    var updateLocations = function (locationsWithBrowsers) {
        var wasTriggerEnabled = enableTriggerEvents(false);
        browserLocationConnectedSelects.updateOptions(locationsWithBrowsers);
        enableTriggerEvents(wasTriggerEnabled);
    };

    var updateConnectivityProfiles = function (connectivityProfiles) {
        var wasTriggerEnabled = enableTriggerEvents(false);
        OpenSpeedMonitor.domUtils.updateSelectOptions(connectivitySelectElement, connectivityProfiles, noResultsText);
        enableTriggerEvents(wasTriggerEnabled);
    };

    init();
    return {
        updateMeasuredEvents: updateMeasuredEvents,
        updateLocations: updateLocations,
        updateConnectivityProfiles: updateConnectivityProfiles
    };
})();



