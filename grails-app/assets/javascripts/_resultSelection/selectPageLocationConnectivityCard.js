"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectPageLocationConnectivityCard = (function() {
    var cardElement = $('#select-page-location-connectivity');
    var noResultsText = "No results"; // TODO(sburnicki): Use i18n string

    var pagesSelectElement = $("#pageSelectHtmlId");
    var measuredEventsSelectElement = $("#selectedMeasuredEventsHtmlId");
    var measuredEventsAllCheckboxElement = $("#selectedAllMeasuredEvents");

    var browsersAllCheckboxElement = $("#selectedAllBrowsers");
    var browsersSelectElement = $("#selectedBrowsersHtmlId");
    var locationsSelectElement = $("#selectedLocationsHtmlId");
    var locationsAllCheckboxElement = $("#selectedAllLocations");

    var init = function() {
        initPageMeasuredEventsControls();
        initBrowserAndLocationControls();
        initConnectivityControls();
        fixChosen();
        registerEvents();
    };

    var initPageMeasuredEventsControls = function () {
        if (!measuredEventsAllCheckboxElement.length || !measuredEventsSelectElement.length) {
            return;
        }
        var allMeasuredEvents = collectAllValues(measuredEventsSelectElement);
        var pagesToEventsMap = measuredEventsSelectElement.data('pagesToEvents') || {};
        initSelectAndCheckBoxFunction(measuredEventsAllCheckboxElement, measuredEventsSelectElement);
        var updateMeasuredEvents = function() {
            updateSelectFields(pagesSelectElement, measuredEventsSelectElement, pagesToEventsMap, allMeasuredEvents);
        };
        pagesSelectElement.change(updateMeasuredEvents);
        updateMeasuredEvents();
        measuredEventsSelectElement.chosen({ search_contains: true, width: "100%", no_results_text: noResultsText });
    };

    var initBrowserAndLocationControls = function () {
        initSelectAndCheckBoxFunction(browsersAllCheckboxElement, browsersSelectElement);
        if (!locationsAllCheckboxElement.length || !locationsSelectElement.length) {
            return;
        }
        var allLocations = collectAllValues(locationsSelectElement);
        var browserToLocationMap = locationsSelectElement.data('browserToLocation') || {};
        initSelectAndCheckBoxFunction(locationsAllCheckboxElement, locationsSelectElement);
        var updateLocations = function () {
            updateSelectFields(browsersSelectElement, locationsSelectElement, browserToLocationMap, allLocations);
        };
        browsersSelectElement.change(updateLocations);
        browsersAllCheckboxElement.change(updateLocations);
        updateLocations();
        locationsSelectElement.chosen({ search_contains: true, width: "100%", no_results_text: noResultsText });
    };

    var initConnectivityControls = function () {
        initSelectAndCheckBoxFunction("#selectedAllConnectivityProfiles", "#selectedConnectivityProfilesHtmlId");
        initTextFieldAndCheckBoxFunction("#includeCustomConnectivity", "#customConnectivityName");
    };

    var registerEvents = function() {

    };

    var collectAllValues = function(selector) {
        var allValues = {};
        $(selector).find('option').each(function(i, element) {
            allValues[element.value] = $(element).text();
        });
        return allValues;
    };

    var sortAlpha = function (a,b) {
        return a.innerHTML.toLowerCase() > b.innerHTML.toLowerCase() ? 1 : -1;
    };

    var initSelectAndCheckBoxFunction = function(checkBox, selectBox) {
        checkBox = $(checkBox);
        selectBox = $(selectBox);
        checkBox.on('change', function(event) {
            if(event.currentTarget.checked == true) {
                selectBox.css({opacity: 0.5});
                selectBox.find("option").prop('selected', false);
                selectBox.trigger("chosen:updated");
            } else {
                selectBox.css({opacity: 1});
            }
        });

        selectBox.on('change', function() {
            var hasSelection = selectBox.find('option:selected').length;
            checkBox.prop('checked', !hasSelection);
            selectBox.css({opacity: hasSelection ? 1.0 : 0.5});
            selectBox.trigger("chosen:updated");
        });
        if(checkBox.is(':checked')){
            selectBox.css({opacity: 0.5});
        }
    };

    var initTextFieldAndCheckBoxFunction = function(checkBox, textField) {
        $(checkBox).on('change', function(event) {
            $(textField).prop('disabled', !event.currentTarget.checked);
        });
    };

    /*
     * Updates a relation between two select boxes:
     *
     * for example there is a page -> to browser relation. If you select a page you'll only see the browsers related to the page.
     *
     * @param: fromSelector the css selector for relation select, select for page in the example
     * @param: toSelector  the css selector for target select, select for browser in the example
     * @param: fromTo array that contains the relations, example: key: page -> value: browser
     * @param: allElements all elements that can be contained in the target select box.
     */
    var updateSelectFields = function(fromSelector, toSelector, fromTo, allElements) {
        var selectedFrom = $(fromSelector).val();
        var selectedTo = $(toSelector).val();
        var key;
        $(toSelector).empty();

        if(selectedFrom != null) {
            for(key in fromTo ) {
                if (!fromTo.hasOwnProperty(key) || !fromTo[key] || $.inArray(key, selectedFrom) < 0) {
                    continue;
                }
                for( var i=0; i<fromTo[key].length; i++ ) {
                    if (allElements[fromTo[key][i]] != "") {
                        $(toSelector).append('<option value="' + fromTo[key][i] + '">' + allElements[fromTo[key][i]] + '</option>');
                    }
                }
            }
        } else {
            for(key in allElements) {
                $(toSelector).append('<option value=\"' + key + '\">'+ allElements[key] + '</option>');
            }
        }

        for(key in selectedTo) {
            $(toSelector).find('option[value="'+selectedTo[key]+'"]').prop('selected', true);
        }

        $(toSelector).find('option').sort(sortAlpha).appendTo(toSelector);
        $(toSelector).trigger("chosen:updated");
        $(toSelector).scrollTop();
    };

    init();
    return {

    };
})();



