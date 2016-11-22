"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectPageLocationConnectivityCard = (function() {
    var cardElement = $('#select-page-location-connectivity');
    var noResultsText = "No results"; // TODO(sburnicki): Use i18n string
    var pageSelectElement = $("#pageSelectHtmlId");

    var init = function() {
        initParentChildSelectionControls(pageSelectElement, $(), $("#selectedMeasuredEventsHtmlId"), $("#selectedAllMeasuredEvents"));
        initParentChildSelectionControls($("#selectedBrowsersHtmlId"), $("#selectedAllBrowsers"), $("#selectedLocationsHtmlId"), $("#selectedAllLocations"));
        initConnectivityControls();
        fixChosen();
        registerEvents();
    };

    var initParentChildSelectionControls = function (parentSelect, parentSelectAllCheckbox, childSelect, childSelectAllCheckbox) {
        initSelectAndCheckBoxFunction(parentSelectAllCheckbox, parentSelect);
        initSelectAndCheckBoxFunction(childSelectAllCheckbox, childSelect);

        var allChildValues = collectAllValues(childSelect);
        var parentChildMapping = childSelect.data('parent-child-mapping') || {};
        var updateChildValues = function () {
            updateSelectFields(parentSelect, childSelect, parentChildMapping, allChildValues);
        };
        parentSelect.change(updateChildValues);
        parentSelectAllCheckbox.change(updateChildValues);
        updateChildValues();
        childSelect.chosen({ search_contains: true, width: "100%", no_results_text: noResultsText });
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

        if (!checkBox.length || !selectBox.length) {
            return;
        }

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

    var updatePages = function(pages) {
        pageSelectElement.empty();
        pageSelectElement.append(OpenSpeedMonitor.domUtils.createOptionsByIdAndName(pages));
        pageSelectElement.trigger("change");
    };

    init();
    return {
        updatePages: updatePages
    };
})();



