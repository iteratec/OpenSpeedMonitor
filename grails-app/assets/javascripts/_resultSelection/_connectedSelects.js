//= require _selectWithSelectAllCheckBox.js
//= require_self

"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.ConnectedSelects = function(parentSelect, parentSelectAllCheckbox, childSelect, childSelectAllCheckbox) {
    parentSelect = $(parentSelect);
    parentSelectAllCheckbox = $(parentSelectAllCheckbox);
    childSelect = $(childSelect);
    childSelectAllCheckbox = $(childSelectAllCheckbox);
    var parentChildMapping = {};
    var noResultsText = "No results"; // TODO(sburnicki): use i18n string


    var init = function () {
        OpenSpeedMonitor.SelectWithSelectAllCheckBox(parentSelect, parentSelectAllCheckbox);
        OpenSpeedMonitor.SelectWithSelectAllCheckBox(childSelect, childSelectAllCheckbox);
        initParentChildMapping();
        parentSelect.change(updateChildValues);
        parentSelectAllCheckbox.change(updateChildValues);
        updateChildValues();
        childSelect.chosen({ search_contains: true, width: "100%", no_results_text: noResultsText });
    };

    var initParentChildMapping = function() {
        var allChildValues = collectAllValues(childSelect);
        var rawParentChildMapping = childSelect.data('parent-child-mapping') || {};
        parentChildMapping = {};
        for (var parentId in rawParentChildMapping) {
            if (rawParentChildMapping.hasOwnProperty(parentId) && rawParentChildMapping[parentId]) {
                parentChildMapping[parentId] = $.map(rawParentChildMapping[parentId], function (childId) {
                    return {id: childId, name: allChildValues[childId]};
                })
            }
        }
    };


    var collectAllValues = function(selector) {
        var allValues = {};
        $(selector).find('option').each(function(i, element) {
            allValues[element.value] = $(element).text();
        });
        return allValues;
    };

    var sortAlpha = function (a,b) {
        return $(a).text().toLowerCase().localeCompare($(b).text().toLowerCase());
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
    var updateChildValues = function() {
        var parentSelection = parentSelect.val() || OpenSpeedMonitor.domUtils.getAllOptionValues(parentSelect);
        var childSelection = childSelect.val();
        var parentId;
        childSelect.empty();

        var newOptions = [];
        for (parentId in parentChildMapping) {
            if (!parentChildMapping.hasOwnProperty(parentId) || !parentChildMapping[parentId] || $.inArray(parentId, parentSelection) < 0) {
                continue;
            }
            newOptions = newOptions.concat(OpenSpeedMonitor.domUtils.createOptionsByIdAndName(parentChildMapping[parentId]));
        }

        childSelect.append(newOptions.sort(sortAlpha));
        childSelect.val(childSelection);
        childSelect.trigger("chosen:updated");
        childSelectAllCheckbox.prop('checked', !childSelect.val());
        childSelect.trigger("change");
    };

    var updateOptions = function (childWithParentList) {
        var parentList = [];
        parentChildMapping = {};
        childWithParentList.forEach(function (child) {
            var parent = child.parent;
            if (!parentChildMapping[parent.id]) {
                parentList.push(parent);
                parentChildMapping[parent.id] = [];
            }
            parentChildMapping[parent.id].push(child);
        });
        OpenSpeedMonitor.domUtils.updateSelectOptions(parentSelect, parentList, noResultsText);
        updateChildValues();
    };

    init();
    return {
        updateOptions: updateOptions
    };
};