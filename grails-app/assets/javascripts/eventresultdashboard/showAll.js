/* 
 * OpenSpeedMonitor (OSM)
 * Copyright 2014 iteratec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Called on jquerys DOM-ready.
 * Initializes DOM-nodes and registers events.
 */
function doOnDomReady(noResultsTextForChosenSelects) {

    $('[data-toggle="popover"]').popover();

    updateSelectionConstraints();
    showOrHideWarningRibbon();
    addInfoHandlers();

    if ($("#chartbox").length > 0) {
        createGraph();
    }
}

function addInfoHandlers() {
    $('#selectedBrowsersHtmlId').on('change', showOrHideWarningRibbon);
    $('#selectedLocationsHtmlId').on('chosen:updated', showOrHideWarningRibbon);
    $('#selectedAllBrowsers').on('change', showOrHideWarningRibbon);

    $('#selectedConnectivityProfilesHtmlId').on('chosen:updated', showOrHideWarningRibbon);
    $('#selectedAllConnectivityProfiles').on('change', showOrHideWarningRibbon);

    $('#selectAggregatorUncachedHtmlId').on('change', showOrHideWarningRibbon);
    $('#selectAggregatorCachedHtmlId').on('change', showOrHideWarningRibbon);

    $('input.trim-selection').on('change', showOrHideWarningRibbon);

    $('#dataTableId').on('inserted.bs.popover', function () {
        updateSelectionConstraints();
    });
}

/**
 * Updates information shown on the right of the headers of collapsed parts of the gui.
 */
function updateSelectionConstraints() {
    updateSelectionConstraintBrowser();
    updateSelectionConstraintConnectivity();
    updateSelectionConstraintTrim();
}

function showOrHideWarningRibbon() {
    if (browserOrLocationSelectionIsCustom() || connectivitySelectionIsCustom() || trimValuesAreDefined()) {
        $("#dataTableId").attr("hidden", false)
    } else {
        $("#dataTableId").attr("hidden", true)
    }
}

function browserOrLocationSelectionIsCustom() {
    return !$('#selectedAllBrowsers').is(':checked') || !$('#selectedAllLocations').is(':checked')
}

function connectivitySelectionIsCustom() {
    return !$('#selectedAllConnectivityProfiles').is(':checked')
}

function trimValuesAreDefined() {
    var trims = [
        getTrimSelection("#appendedInputBelowLoadTimes", "#appendedInputAboveLoadTimes", "ms"),
        getTrimSelection("#appendedInputBelowRequestCounts", "#appendedInputAboveRequestCounts", "c"),
        getTrimSelection("#appendedInputBelowRequestSizes", "#appendedInputAboveRequestSizes", "kb")
    ].filter(String);
    return trims.length ? true : false
}

function showSummaryRow(row, text) {
    var $row = $(row);
    $row.find('td').html(text);
    $row.attr("hidden", false);
}

function hideSummaryRow(row) {
    $(row).attr("hidden", true);
}

function updateSelectionConstraintBrowser() {
    if (!browserOrLocationSelectionIsCustom()) {
        hideSummaryRow('#selectionConstraintBrowser')
    } else {
        var selectedBrowsers = $('#selectedAllBrowsers').is(':checked') ?
            'all' : getTextList('#selectedBrowsersHtmlId option:selected', 50);
        var selectedLocations = $('#selectedAllLocations').is(':checked') ?
            'all' : getTextList('#selectedLocationsHtmlId option:selected', 50);
        showSummaryRow('#selectionConstraintBrowser', selectedBrowsers + " | " + selectedLocations);
    }
}

function updateSelectionConstraintConnectivity() {
    if (!connectivitySelectionIsCustom()) {
        hideSummaryRow('#selectionConstraintConnectivity');
    } else {
        var selectedConnectivities = getTextList('#selectedConnectivityProfilesHtmlId option:selected', 50);
        showSummaryRow('#selectionConstraintConnectivity', selectedConnectivities);
    }
}

function updateSelectionConstraintTrim() {
    if (!trimValuesAreDefined()) {
        hideSummaryRow("#selectionConstraintTrim");
    } else {
        var trims = [
            getTrimSelection("#appendedInputBelowLoadTimes", "#appendedInputAboveLoadTimes", "ms"),
            getTrimSelection("#appendedInputBelowRequestCounts", "#appendedInputAboveRequestCounts", "c"),
            getTrimSelection("#appendedInputBelowRequestSizes", "#appendedInputAboveRequestSizes", "kb")
        ].filter(String);
        showSummaryRow("#selectionConstraintTrim", trims.join(" | "));
    }
}

function getTrimSelection(trimBelowInput, trimAboveInput, trimUnit) {
    var trimBelow = $(trimBelowInput).val();
    var trimAbove = $(trimAboveInput).val();
    var text = trimBelow ? (trimBelow + " < ") : "";
    text += (trimBelow || trimAbove) ? ("<span class='unit'>" + trimUnit + "</span>") : "";
    text += trimAbove ? (" < " + trimAbove) : "";
    return text
}

function getTextList(selectedOptions, max) {
    var groupsTextList = [];
    $(selectedOptions).each(function () {
        groupsTextList.push($(this).text());
    });
    var text = groupsTextList.join(", ");
    return text.length > max ? (text.substring(0, max - 3) + '...') : text;
}

