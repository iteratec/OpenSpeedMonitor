/*
 * OpenSpeedMonitor (OSM)
 * Copyright 2014 iteratec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.jobListFilter = (function(){
    var storageUtils = OpenSpeedMonitor.clientSideStorageUtils();
    var filterTextLocalStorage = 'de.iteratec.osm.job.list.filter';
    var filterTextInput = $("#filterInput");
    var jobTableRows = $("#jobtable tbody tr");
    var clearFilterButton = $("#clearFilter");
    var allFilterCheckboxes = {
        byName: {
            element: $("#filterByName"),
            localStorage: "de.iteratec.osm.job.list.filter.by.name",
            valueClassName: '.jobName',
            isChecked: null
        },
        byJobGroup: {
            element: $("#filterByJobGroup"),
            localStorage: "de.iteratec.osm.job.list.filter.by.jobgroup",
            valueClassName: '.jobgroup',
            isChecked: null
        },
        byScript: {
            element: $("#filterByScript"),
            localStorage: "de.iteratec.osm.job.list.filter.by.script",
            valueClassName: '.script',
            isChecked: null
        },
        byLocation: {
            element: $("#filterByLocation"),
            localStorage: "de.iteratec.osm.job.list.filter.by.location",
            valueClassName: '.location',
            isChecked: null
        },
        byBrowser: {
            element: $("#filterByBrowser"),
            localStorage: "de.iteratec.osm.job.list.filter.by.browser",
            valueClassName: '.browser',
            isChecked: null
        },
        byTags: {
            element: $("#filterByTags"),
            localStorage: "de.iteratec.osm.job.list.filter.by.tags",
            valueClassName: null,
            isChecked: null
        }
    };
    var showOnlyCheckboxes = {
        showOnlyChecked: {
            element: $("#filterCheckedJobs"),
            localStorage: "de.iteratec.osm.job.list.filter.showOnly.checked",
            isChecked: null
        },
        showOnlyHighlighted: {
            element: $("#filterHighlightedJobs"),
            localStorage: "de.iteratec.osm.job.list.filter.showOnly.highlighted",
            isChecked: null
        },
        showOnlyRunning: {
            element: $("#filterRunningJobs"),
            localStorage: "de.iteratec.osm.job.list.filter.showOnly.running",
            isChecked: null
        },
        showOnlyActive: {
            element: $("#filterActiveJobs"),
            localStorage: "de.iteratec.osm.job.list.filter.showOnly.active",
            isChecked: null
        }
    };
    $.extend(allFilterCheckboxes, showOnlyCheckboxes);
    var filterByCheckboxes = [ allFilterCheckboxes.byName, allFilterCheckboxes.byJobGroup, allFilterCheckboxes.byTags,
        allFilterCheckboxes.byScript, allFilterCheckboxes.byLocation, allFilterCheckboxes.byBrowser];

    var init = function () {
        initFilterInput();
        initFilterCheckboxes();
        initClearFilterButton();
        filter();
    };

    var initFilterInput = function () {
        var filterText = storageUtils.getFromLocalStorage(filterTextLocalStorage);
        filterTextInput.val(filterText);
        filterTextInput.change(filter);
        filterTextInput.keyup(filter);
    };

    var initFilterCheckboxes = function () {
        $.each(allFilterCheckboxes, function(name, checkbox) {
            var localStorageValue = storageUtils.getFromLocalStorage(checkbox.localStorage);
            if (localStorageValue !== null) {
                checkbox.isChecked = OpenSpeedMonitor.stringUtils.stringToBoolean(localStorageValue);
                checkbox.element.prop("checked", checkbox.isChecked);
                checkbox.element.parent().toggleClass("active", checkbox.isChecked); // bootstrap button-style
            }
            checkbox.element.change(filter);
        });
    };

    var initClearFilterButton = function () {
        clearFilterButton.click(clearFilter);
    };

    var clearFilter = function () {
        filterTextInput.val("");
        filter();
    };

    var filter = function () {
        saveState();
        var filterTerms = getFilterTerms();
        clearFilterButton.toggle(filterTerms.length > 0);
        jobTableRows.each(function(idx, row) {
            row = $(row);
            var showRow = filterMatchesRow(filterTerms, row);
            row.toggleClass("hidden", !showRow);
            if(!showRow) {
                row.find(".jobCheckbox").attr("checked", false)
            }
        });
        applyStriping();
        saveState();
    };

    var getFilterTerms = function () {
        return filterTextInput.val().toLowerCase().split(/\s+/).filter(function (val) { return val; });
    };

    var filterMatchesRow = function (filterTerms, row) {
        return showOnlyFiltersMatchRow(row) &&
               filterTerms.every(function(term) { return filterTermMatchesRow(term, row); });
    };

    var showOnlyFiltersMatchRow = function (row) {
        var isMatch = !showOnlyCheckboxes.showOnlyActive.isChecked || row.find(".job_active").val() == "true";
        isMatch = isMatch && (!showOnlyCheckboxes.showOnlyHighlighted.isChecked || row.hasClass("highlight"));
        isMatch = isMatch && (!showOnlyCheckboxes.showOnlyChecked.isChecked || row.find(".jobCheckbox").prop("checked"));
        isMatch = isMatch && (!showOnlyCheckboxes.showOnlyRunning.isChecked || row.find(".running").length > 0);
        return isMatch;
    };
    
    var filterTermMatchesRow = function (filterTerm, row) {
        var checkAll = filterByCheckboxes.every(function(checkbox) { return !checkbox.isChecked; });
        var isMatch = filterByCheckboxes.some(function (checkbox) {
            return (checkAll || checkbox.isChecked) &&
                   row.find(checkbox.valueClassName).text().toLowerCase().indexOf(filterTerm) >= 0;
        });
        return isMatch || filterTermMatchesTags(filterTerm, row, checkAll);
    };

    var filterTermMatchesTags = function (filterTerm, row, checkAll) {
        if (!checkAll && !allFilterCheckboxes.byTags.isChecked) {
            return false;
        }
        var usedTags = row.attr('data-tags');
        return (usedTags && usedTags.toLowerCase().indexOf(filterTerm) >= 0);
    };

    var applyStriping = function () {
        $("#jobtable tr:not(.hidden)").each(function (index) {
            $(this).toggleClass("stripe", !!(index & 1));
        });
    };

    var saveState = function () {
        storageUtils.setToLocalStorage(filterTextLocalStorage, filterTextInput.val());
        $.each(allFilterCheckboxes, function(name, checkbox) {
            checkbox.isChecked = checkbox.element.prop('checked');
            storageUtils.setToLocalStorage(checkbox.localStorage, checkbox.isChecked);
        });
    };

    init();
    return {
        clearFilter: clearFilter,
        filter: filter
    }
})();
