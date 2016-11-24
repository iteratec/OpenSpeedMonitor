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
    var noResultsText = "No results. Please select a different time frame."; // TODO(sburnicki): use 18n
    var pageEventsConnectedSelects;
    var browserLocationConnectedSelects;
    var triggerEventsEnabled = true;


    var init = function() {
        pageEventsConnectedSelects = OpenSpeedMonitor.ConnectedSelects(pageSelectElement, $(),
            measuredEventsSelectElement, $("#selectedAllMeasuredEvents"));
        browserLocationConnectedSelects = OpenSpeedMonitor.ConnectedSelects(browserSelectElement,
            $("#selectedAllBrowsers"), locationsSelectElement, $("#selectedAllLocations"));
        initConnectivityControls();
        fixChosen();
        registerEvents();
    };

    var initConnectivityControls = function () {
        OpenSpeedMonitor.SelectWithSelectAllCheckBox("#selectedConnectivityProfilesHtmlId", "#selectedAllConnectivityProfiles");
        initTextFieldAndCheckBoxFunction("#includeCustomConnectivity", "#customConnectivityName");
    };

    var registerEvents = function() {
        pageSelectElement.on("change", function () {
            var hasAllSelected = OpenSpeedMonitor.domUtils.hasAllOptionsSelected(pageSelectElement);
            triggerChangeEvent("pageSelectionChanged", [pageSelectElement.val(), hasAllSelected]);
        });
        measuredEventsSelectElement.on("change", function () {
            var hasAllSelected = OpenSpeedMonitor.domUtils.hasAllOptionsSelected(measuredEventsSelectElement);
            triggerChangeEvent("measuredEventSelectionChanged", [measuredEventsSelectElement.val(), hasAllSelected]);
        });
        browserSelectElement.on("change", function () {
            var hasAllSelected = OpenSpeedMonitor.domUtils.hasAllOptionsSelected(browserSelectElement);
            triggerChangeEvent("browserSelectionChanged", [browserSelectElement.val(), hasAllSelected]);
        });
        locationsSelectElement.on("change", function () {
            var hasAllSelected = OpenSpeedMonitor.domUtils.hasAllOptionsSelected(locationsSelectElement);
            triggerChangeEvent("locationSelectionChanged", [locationsSelectElement.val(), hasAllSelected]);
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

    var initTextFieldAndCheckBoxFunction = function(checkBox, textField) {
        $(checkBox).on('change', function(event) {
            $(textField).prop('disabled', !event.currentTarget.checked);
        });
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

    init();
    return {
        updateMeasuredEvents: updateMeasuredEvents,
        updateLocations: updateLocations
    };
})();



