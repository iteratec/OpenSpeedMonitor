//= require _selectWithSelectAllCheckBox.js
//= require _connectedSelects.js
//= require_self

"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectPageLocationConnectivityCard = (function() {
    var cardElement = $('#select-page-location-connectivity');
    var pageSelectElement = $("#pageSelectHtmlId");
    var pageEventsConnectedSelects;


    var init = function() {
        pageEventsConnectedSelects = OpenSpeedMonitor.ConnectedSelects(pageSelectElement, $(),
            $("#selectedMeasuredEventsHtmlId"), $("#selectedAllMeasuredEvents"));
        OpenSpeedMonitor.ConnectedSelects($("#selectedBrowsersHtmlId"), $("#selectedAllBrowsers"), $("#selectedLocationsHtmlId"), $("#selectedAllLocations"));
        initConnectivityControls();
        fixChosen();
        registerEvents();
    };



    var initConnectivityControls = function () {
        OpenSpeedMonitor.SelectWithSelectAllCheckBox("#selectedAllConnectivityProfiles", "#selectedConnectivityProfilesHtmlId");
        initTextFieldAndCheckBoxFunction("#includeCustomConnectivity", "#customConnectivityName");
    };

    var registerEvents = function() {

    };


    var initTextFieldAndCheckBoxFunction = function(checkBox, textField) {
        $(checkBox).on('change', function(event) {
            $(textField).prop('disabled', !event.currentTarget.checked);
        });
    };

    var updatePages = function(pages) {
        var selection = pageSelectElement.val();
        pageSelectElement.empty();
        pageSelectElement.append(OpenSpeedMonitor.domUtils.createOptionsByIdAndName(pages));
        pageSelectElement.val(selection);
        pageSelectElement.trigger("change");
    };

    var updateMeasuredEvents = function (measuredEvents) {
        var uniquePages = [];
        var pageToEvents = {};
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
    };

    init();
    return {
        updatePages: updatePages,
        updateMeasuredEvents: updateMeasuredEvents
    };
})();



