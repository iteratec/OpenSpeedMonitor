//= require _selectWithSelectAllCheckBox.js
//= require _connectedSelects.js
//= require_self

"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectPageLocationConnectivityCard = (function() {
    var cardElement = $('#select-page-location-connectivity');
    var pageSelectElement = $("#pageSelectHtmlId");
    var measuredEventsSelectElement = $("#selectedMeasuredEventsHtmlId");
    var noResultsText = "No results. Please select a different time frame."; // TODO(sburnicki): use 18n
    var pageEventsConnectedSelects;
    var triggerEventsEnabled = true;


    var init = function() {
        pageEventsConnectedSelects = OpenSpeedMonitor.ConnectedSelects(pageSelectElement, $(),
            measuredEventsSelectElement, $("#selectedAllMeasuredEvents"));
        OpenSpeedMonitor.ConnectedSelects($("#selectedBrowsersHtmlId"), $("#selectedAllBrowsers"), $("#selectedLocationsHtmlId"), $("#selectedAllLocations"));
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

    var updatePages = function(pages) {
        var wasTriggerEnabled = enableTriggerEvents(false);
        var selection = pageSelectElement.val();
        pageSelectElement.empty();
        pageSelectElement.append(OpenSpeedMonitor.domUtils.createOptionsByIdAndName(pages));
        if (!pageSelectElement.children().length) {
            pageSelectElement.append($("<option/>", { disabled: "disabled", text: noResultsText }));
        }
        pageSelectElement.val(selection);
        pageSelectElement.trigger("change");
        enableTriggerEvents(wasTriggerEnabled);
    };

    var updateMeasuredEvents = function (measuredEvents) {
        var uniquePages = [];
        var pageToEvents = {};
        var wasTriggerEnabled = enableTriggerEvents(false);
        measuredEvents.forEach(function (measuredEvent) {
            var page = measuredEvent.testedPage;
            if (!pageToEvents[page.id]) {
                uniquePages.push(page);
                pageToEvents[page.id] = [];
            }
            pageToEvents[page.id].push({id: measuredEvent.id, name: measuredEvent.name});
        });
        updatePages(uniquePages);
        pageEventsConnectedSelects.updateMapping(pageToEvents);
        enableTriggerEvents(wasTriggerEnabled);
    };

    init();
    return {
        updatePages: updatePages,
        updateMeasuredEvents: updateMeasuredEvents
    };
})();



