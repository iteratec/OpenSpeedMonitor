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
function doOnDomReady(dateFormat, weekStart, noResultsTextForChosenSelects){
    initDatepicker(dateFormat, weekStart, 24*3);
    initTimepicker(false);

    var preSelection = $('#timeframeSelect').val()>0;
    disOrEnableFieldsetsOfManualDateTimeSelection(preSelection);

    initIntervalSelect();
    initChosenSelects(noResultsTextForChosenSelects);

    updateSelectionConstraints();
    addInfoHandlers();

    if($("#chartbox").length > 0){
        createGraph();
    }
}

function addInfoHandlers() {
    $('#selectedBrowsersHtmlId').on('change', updateSelectionConstraintBrowser);
    $('#selectedLocationsHtmlId').on('chosen:updated', updateSelectionConstraintBrowser);
    $('#selectedAllBrowsers').on('change', updateSelectionConstraintBrowser);

    $('#selectedConnectivityProfilesHtmlId').on('chosen:updated', updateSelectionConstraintConnectivity);
    $('#selectedAllConnectivityProfiles').on('change', updateSelectionConstraintConnectivity);
    $('#customConnectivityName').on('input', updateSelectionConstraintConnectivity);
    $('#includeNativeConnectivity').on('change', updateSelectionConstraintConnectivity);
    $('#includeCustomConnectivity').on('change', updateSelectionConstraintConnectivity);

    $('#selectAggregatorUncachedHtmlId').on('change', updateSelectionConstraintFirstView);
    $('#selectAggregatorCachedHtmlId').on('change', updateSelectionConstraintRepeatView);

    $('input.trim-selection').on('change', updateSelectionConstraintTrim);
}

/**
 * Updates information shown on the right of the headers of collapsed parts of the gui.
 */
function updateSelectionConstraints() {
    updateSelectionConstraintBrowser();
    updateSelectionConstraintConnectivity();
    updateSelectionConstraintFirstView();
    updateSelectionConstraintRepeatView();
    updateSelectionConstraintTrim();
}

function showSummaryRow(row, text) {
    row = $(row);
    row.find('td').html(text);
    row.show();
}

function hideSummaryRow(row) {
    $(row).hide();
}

function updateSelectionConstraintBrowser() {
    var selectedBrowsers = $('#selectedAllBrowsers').is(':checked') ?
        'all' : getTextList('#selectedBrowsersHtmlId option:selected', 50);
    var selectedLocations = $('#selectedAllLocations').is(':checked') ?
        'all' : getTextList('#selectedLocationsHtmlId option:selected', 50);
    if (selectedBrowsers != 'all' || selectedLocations != 'all') {
        showSummaryRow('#selectionConstraintBrowser', selectedBrowsers + " | " + selectedLocations);
    } else {
        hideSummaryRow('#selectionConstraintBrowser')
    }
}

function updateSelectionConstraintConnectivity() {
    var selectedConnectivities = $('#selectedAllConnectivityProfiles').is(':checked')?
        'all predefined' : getTextList('#selectedConnectivityProfilesHtmlId option:selected', 50);
    if(document.getElementById('includeNativeConnectivity').checked){
        selectedConnectivities += ", NATIVE"
    }
    if(document.getElementById('includeCustomConnectivity').checked){
        selectedConnectivities += ", CUSTOM";
        var customConnNameRegex = document.getElementById('customConnectivityName').value;
        if(customConnNameRegex){
            selectedConnectivities += ": '" + customConnNameRegex + "'"
        }
    }
    if (selectedConnectivities != 'all predefined') {
        showSummaryRow('#selectionConstraintConnectivity', selectedConnectivities);
    } else {
        hideSummaryRow('#selectionConstraintConnectivity');
    }
}

function updateSelectionConstraintFirstView() {
    var selectedFirstView = getTextList('#selectAggregatorUncachedHtmlId option:selected', 100);
    if (selectedFirstView != 'doc complete time') {
        showSummaryRow('#selectionConstraintFirstView', selectedFirstView ? selectedFirstView : '\u2205');
    } else {
        hideSummaryRow('#selectionConstraintFirstView');
    }
}

function updateSelectionConstraintRepeatView() {
    var selectedRepeatView = getTextList('#selectAggregatorCachedHtmlId option:selected', 100);
    if (selectedRepeatView) {
        showSummaryRow('#selectionConstraintRepeatView', selectedRepeatView ? selectedRepeatView : '\u2205');
    } else {
        hideSummaryRow('#selectionConstraintRepeatView');
    }
}

function updateSelectionConstraintTrim() {
    var trims = [
        getTrimSelection("#appendedInputBelowLoadTimes", "#appendedInputAboveLoadTimes", "ms"),
        getTrimSelection("#appendedInputBelowRequestCounts", "#appendedInputAboveRequestCounts", "c"),
        getTrimSelection("#appendedInputBelowRequestSizes", "#appendedInputAboveRequestSizes", "kb")
    ].filter(String);
    if (trims.length) {
        showSummaryRow("#selectionConstraintTrim", trims.join(" | "));
    } else {
        hideSummaryRow("#selectionConstraintTrim");
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

function getTextList(selectedOptions, max){
	var groupsTextList = [];
	$(selectedOptions).each(function(){
		groupsTextList.push($(this).text());
	});	
	var text = groupsTextList.join(", ");
    return text.length > max ? (text.substring(0, max-3) + '...') : text;
}

function initIntervalSelect() {
	//initialization///////////////////////
	var lastIntervalSelection = OpenSpeedMonitor.clientSideStorageUtils().getFromLocalStorage('de.iteratec.osm.result.dashboard.intervalselection');
	var intervalSelectionFromQueryParam = $.getUrlVar('selectedInterval');
	if(intervalSelectionFromQueryParam != null) $('#selectedIntervalHtmlId').val(intervalSelectionFromQueryParam);
	else if(lastIntervalSelection != null) $('#selectedIntervalHtmlId').val(lastIntervalSelection);
	//register events///////////////////////
	$('#selectedIntervalHtmlId').on("change", function(event){
		OpenSpeedMonitor.clientSideStorageUtils().setToLocalStorage('de.iteratec.osm.result.dashboard.intervalselection', $('#selectedIntervalHtmlId').val());
	});
}
