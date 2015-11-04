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

    updateCollapseInfos();

    addAccordionHandlers();

    setChevron($('#collapseOne'));

    if($("#chartbox").length > 0){
        createGraph();
    }

    scrollToChartbox(200);

}

function addAccordionHandlers() {
    $('#collapseOne').on('shown', function (e) {
        $(this).parent().find("a.accordion-toggle").removeClass("fa fa-chevron-down").addClass("fa fa-chevron-up");
        e.preventDefault();
    });
    $('#collapseOne').on('hidden', function (e) {
        $(this).parent().find("a.accordion-toggle").removeClass("fa fa-chevron-up").addClass("fa fa-chevron-down");
    });
    setChevron($('#collapseTwo'));
    $('#collapseTwo').on('shown', function (e) {
        $(this).parent().find("a.accordion-toggle").removeClass("fa fa-chevron-down").addClass("fa fa-chevron-up");
        e.preventDefault();
    });
    $('#collapseTwo').on('hidden', function (e) {
        $(this).parent().find("a.accordion-toggle").removeClass("fa fa-chevron-up").addClass("fa fa-chevron-down");
    });
    setChevron($('#collapseThree'));
    $('#collapseThree').on('shown', function (e) {
        $(this).parent().find("a.accordion-toggle").removeClass("fa fa-chevron-down").addClass("fa fa-chevron-up");
        e.preventDefault();
    });
    $('#collapseThree').on('hidden', function (e) {
        $(this).parent().find("a.accordion-toggle").removeClass("fa fa-chevron-up").addClass("fa fa-chevron-down");
    });

    addAccordionInfoHandlers()
}

function addAccordionInfoHandlers(){
    addTimeframeInfoHandlers();
    addJobfilterInfoHandlers();
    addMeasurandsInfoHandlers();
}

function addJobfilterInfoHandlers() {
    $('#folderSelectHtmlId').on('change', function (e) {
        setCollapseJobInfos();
    });
    $('#pageSelectHtmlId').on('change', function (e) {
        //setCollapseJobInfos();
    });

    $('#selectedMeasuredEventsHtmlId').on('chosen:updated', function (evt, params) {
        setCollapseJobInfos();
    });
    $('#selectedBrowsersHtmlId').on('change', function (e) {
        setCollapseJobInfos();
    });
    $('#selectedLocationsHtmlId').on('chosen:updated', function (e) {
        setCollapseJobInfos();
    });
    $('#selectedConnectivityProfilesHtmlId').on('chosen:updated', function (e) {
        setCollapseJobInfos();
    });
    $('#selectedAllConnectivityProfiles').on('change', function (e) {
        setCollapseJobInfos();
    });
    $('#customConnectivityName').on('input', function (e) {
        setCollapseJobInfos();
    });
    $('#selectedAllMeasuredEvents').on('change', function (e) {
        setCollapseJobInfos();
    });
    $('#selectedAllBrowsers').on('change', function (e) {
        setCollapseJobInfos();
    });
    $('#includeNativeConnectivity').on('change', function (e) {
        setCollapseJobInfos();
    });
}
function addTimeframeInfoHandlers() {
    $('#selectedIntervalHtmlId').on('change', function (e) {
        setCollapseDateInfos();
    });
    $('#timeframeSelect').on('change', function (e) {
        setCollapseDateInfos();
    });
    $('#fromDatepicker').on('change', function (e) {
        setCollapseDateInfos();
    });
    $('#toDatepicker').on('change', function (e) {
        setCollapseDateInfos();
    });
    $('#fromHourTimepicker').on('change', function (e) {
        setCollapseDateInfos();
    });
    $('#toHourTimepicker').on('change', function (e) {
        setCollapseDateInfos();
    });
}
function addMeasurandsInfoHandlers() {
    $('#selectAggregatorUncachedHtmlId').on('change', function (e) {
        setCollapseMeasurementInfos()
    });
    $('#selectAggregatorCachedHtmlId').on('change', function (e) {
        setCollapseMeasurementInfos()
    });
}

/**
 * Updates information shown on the right of the headers of collapsed parts of the gui.
 */
function updateCollapseInfos() {
    setCollapseDateInfos();
    setCollapseJobInfos();
    setCollapseMeasurementInfos();
}
function setCollapseDateInfos(){

    var aggregation = $('#selectedIntervalHtmlId option:selected').text();
    var unicodeEmptySet = '\u2205';
    aggregation = aggregation?aggregation:unicodeEmptySet;
    var dateToSet;
    if($('#timeframeSelect').val()==0){
        var from = $('#from').val();
        from = from?from:'\u2205';
        var to = $('#to').val();
        to = to?to:'\u2205';
        var toHour = $('#toHour').val();
        toHour = toHour?toHour:'\u2205';
        var fromHour = $('#fromHour').val();
        fromHour = fromHour?fromHour:'\u2205';
        dateToSet = from+' '+fromHour+'\xA0\xBB\xA0'+to+' '+toHour;
    }else{
        dateToSet = $('#timeframeSelect :selected').text();
    }

    $('#accordion-info-date').text('');
    document.getElementById('accordion-info-date').appendChild(document.createTextNode(aggregation));
    document.getElementById('accordion-info-date').appendChild(document.createElement('br'));
    document.getElementById('accordion-info-date').appendChild(document.createTextNode(dateToSet));

}
function setCollapseJobInfos(){

        var delimitterIfMultipleOptions = ', ';
        var delimitterInfoTypes = ' | ';
        var maxNumberOfLetters = 50;

		var selectedGroups = getMaxCharacters(getTextList($('#folderSelectHtmlId option:selected')).join(delimitterIfMultipleOptions), 85);
        var selectedPages = getMaxCharacters(getTextList($('#pageSelectHtmlId option:selected')).join(delimitterIfMultipleOptions), maxNumberOfLetters);
        var selectedEvents = $('#selectedAllMeasuredEvents').is(':checked')?
				'ALL':getMaxCharacters(getTextList($('#selectedMeasuredEventsHtmlId option:selected')).join(delimitterIfMultipleOptions), maxNumberOfLetters);
        var selectedBrowsers = $('#selectedAllBrowsers').is(':checked')?
				'ALL':getMaxCharacters(getTextList($('#selectedBrowsersHtmlId option:selected')).join(delimitterIfMultipleOptions), maxNumberOfLetters);
        var selectedLocations = $('#selectedAllLocations').is(':checked')?
				'ALL':getMaxCharacters(getTextList($('#selectedLocationsHtmlId option:selected')).join(delimitterIfMultipleOptions), maxNumberOfLetters);
        var selectedConnectivities = $('#selectedAllConnectivityProfiles').is(':checked')?
            'ALL PREDEFINED':getMaxCharacters(getTextList($('#selectedConnectivityProfilesHtmlId option:selected')).join(delimitterIfMultipleOptions), maxNumberOfLetters);
        if(document.getElementById('includeNativeConnectivity').checked){
            selectedConnectivities += ", NATIVE"
        }
        var customConnNameRegex = document.getElementById('customConnectivityName').value;
        if(customConnNameRegex){
            selectedConnectivities += ", '" + customConnNameRegex + "'"
        }

        $('#accordion-info-jobs').text('');
        document.getElementById('accordion-info-jobs').appendChild(document.createTextNode(selectedGroups));
        document.getElementById('accordion-info-jobs').appendChild(document.createElement('br'));
        document.getElementById('accordion-info-jobs').appendChild(document.createTextNode(
                [selectedPages, selectedEvents].join(delimitterInfoTypes))
        );
        document.getElementById('accordion-info-jobs').appendChild(document.createElement('br'));
        document.getElementById('accordion-info-jobs').appendChild(document.createTextNode(
                [selectedBrowsers, selectedLocations].join(delimitterInfoTypes))
        );
        document.getElementById('accordion-info-jobs').appendChild(document.createElement('br'));
        document.getElementById('accordion-info-jobs').appendChild(document.createTextNode(selectedConnectivities));

}
function setCollapseMeasurementInfos(){
    $('#accordion-info-measurements').text('');
    var delimitterIfMultipleOptions = ', ';
    var selectedUncached = getMaxCharacters(getTextList($('#selectAggregatorUncachedHtmlId option:selected')).join(delimitterIfMultipleOptions), 100);
    var selectedCached = getMaxCharacters(getTextList($('#selectAggregatorCachedHtmlId option:selected')).join(delimitterIfMultipleOptions), 100);
    document.getElementById('accordion-info-measurements').appendChild(document.createTextNode(selectedUncached));
    document.getElementById('accordion-info-measurements').appendChild(document.createElement('br'));
    document.getElementById('accordion-info-measurements').appendChild(document.createTextNode(selectedCached));
}
function getTextList(selectedOptions){
	var groupsTextList = [];
	selectedOptions.each(function(i){
		groupsTextList.push($(this).text());
	});	
	return groupsTextList;
}
function getMaxCharacters(toGetMaxFrom, max){
	var maxChar = toGetMaxFrom.length > max?toGetMaxFrom.substring(0,max-3)+'...':toGetMaxFrom;
	maxChar = maxChar==''?'\u2205':maxChar;
	return maxChar;
}
function ensureMinLength(targetString, minLength){
    while(targetString.length < minLength){
        targetString += ' '
    }
}
function setChevron(accordionElement) {
	if(!accordionElement.hasClass("collapse")) {
		$(accordionElement).parent().find("a.accordion-toggle").removeClass("fa fa-chevron-up").addClass("fa fa-chevron-dow")
	} else {
		$(accordionElement).parent().find("a.accordion-toggle").removeClass("fa fa-chevron-down").addClass("fa fa-chevron-up")
	}
}
function initIntervalSelect() {
	//initialization///////////////////////
	var lastIntervalSelection = getFromLocalStorage('de.iteratec.osm.result.dashboard.intervalselection');
	var intervalSelectionFromQueryParam = $.getUrlVar('selectedInterval');
	if(intervalSelectionFromQueryParam != null) $('#selectedIntervalHtmlId').val(intervalSelectionFromQueryParam);
	else if(lastIntervalSelection != null) $('#selectedIntervalHtmlId').val(lastIntervalSelection);
	//register events///////////////////////
	$('#selectedIntervalHtmlId').on("change", function(event){
		setToLocalStorage('de.iteratec.osm.result.dashboard.intervalselection', $('#selectedIntervalHtmlId').val());
	});
}