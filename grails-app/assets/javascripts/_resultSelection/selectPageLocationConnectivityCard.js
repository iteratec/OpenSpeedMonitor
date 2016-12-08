//= require _selectWithSelectAllCheckBox.js
//= require _connectedSelects.js
//= require_self

"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectPageLocationConnectivityCard = (function() {
    var cardElement = $('#select-page-location-connectivity');
    var resetButtonElement = cardElement.find(".reset-selection");
    var pageSelectElement = $("#pageSelectHtmlId");
    var measuredEventsSelectElement = $("#selectedMeasuredEventsHtmlId");
    var browserSelectElement = $("#selectedBrowsersHtmlId");
    var locationsSelectElement =  $("#selectedLocationsHtmlId");
    var connectivitySelectElement = $("#selectedConnectivityProfilesHtmlId");
    var noResultsText = "No results. Please select a different time frame."; // TODO(sburnicki): use 18n
    var pageEventsConnectedSelects;
    var browserLocationConnectedSelects;

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
                ids: (connectivitySelectElement.val() || []).filter(OpenSpeedMonitor.stringUtils.isNumeric),
                customNames: $.map(connectivitySelectElement.find('option[value="custom"]:selected'), function (option) {
                    return option.text;
                }),
                native: connectivitySelectElement.find('option[value="native"]:selected').length > 0,
                hasAllSelected: OpenSpeedMonitor.domUtils.hasAllOptionsSelected(connectivitySelectElement)
            });
        });
        resetButtonElement.on("click", function() {
            OpenSpeedMonitor.domUtils.deselectAllOptions(pageSelectElement, true);
            OpenSpeedMonitor.domUtils.deselectAllOptions(measuredEventsSelectElement);
            OpenSpeedMonitor.domUtils.deselectAllOptions(browserSelectElement, true);
            OpenSpeedMonitor.domUtils.deselectAllOptions(locationsSelectElement);
            OpenSpeedMonitor.domUtils.deselectAllOptions(connectivitySelectElement);
        });
    };

    var triggerChangeEvent = function (eventType, values) {
        cardElement.trigger(eventType, values);
    };

    var updateMeasuredEvents = function (measuredEventsWithPages) {
        pageEventsConnectedSelects.updateOptions(measuredEventsWithPages);
    };

    var updateLocations = function (locationsWithBrowsers) {
        browserLocationConnectedSelects.updateOptions(locationsWithBrowsers);
    };

    var updateConnectivityProfiles = function (connectivityProfiles) {
        OpenSpeedMonitor.domUtils.updateSelectOptions(connectivitySelectElement, connectivityProfiles, noResultsText);
    };

    init();
    return {
        updateMeasuredEvents: updateMeasuredEvents,
        updateLocations: updateLocations,
        updateConnectivityProfiles: updateConnectivityProfiles
    };
})();



