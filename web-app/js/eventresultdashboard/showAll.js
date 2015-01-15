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
 * Updates information shown on the right of the headers of collapsed parts of the gui.
 */
var updateCollapseInfos = function() {
	if($('#collapseOne').height() == 0){
		setCollapseDateInfos(true);
	}else{
		setCollapseDateInfos(false);
	}
	if($('#collapseTwo').height() == 0){
		setCollapseJobInfos(true);
	}else{
		setCollapseJobInfos(false);
	}
	if($('#collapseThree').height() == 0){
		setCollapseMeasurementInfos(true);
	}else{
		setCollapseMeasurementInfos(false);
	}
};
var setCollapseDateInfos = function(toSet){
	if(toSet){
		var aggregation = $('#selectedIntervalHtmlId option:selected').text();
		aggregation = aggregation?aggregation:'\u2205';
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
		$('#accordion-info-date').text(aggregation+' | '+dateToSet);
	}else{
		$('#accordion-info-date').text('');
	}
};
	var setCollapseJobInfos = function(toSet){
	if(toSet){
		var delimitterIfMultipleOptions = ', ';
		var selectedGroups = getMaxCharacters(getTextList($('#folderSelectHtmlId option:selected')).join(delimitterIfMultipleOptions), 50);
		var selectedPages = getMaxCharacters(getTextList($('#pageSelectHtmlId option:selected')).join(delimitterIfMultipleOptions), 50);
		var selectedEvents = $('#selectedAllMeasuredEvents').is(':checked')?
				'ALL':getMaxCharacters(getTextList($('#selectedMeasuredEventsHtmlId option:selected')).join(delimitterIfMultipleOptions), 50);
		var selectedBrowsers = $('#selectedAllBrowsers').is(':checked')?
				'ALL':getMaxCharacters(getTextList($('#selectedBrowsersHtmlId option:selected')).join(delimitterIfMultipleOptions), 50);
		var selectedLocations = $('#selectedAllLocations').is(':checked')?
				'ALL':getMaxCharacters(getTextList($('#selectedLocationsHtmlId option:selected')).join(delimitterIfMultipleOptions), 50);
		$('#accordion-info-jobs').text(selectedGroups + ' | ' + selectedPages + ' | ' + selectedEvents + ' | ' + selectedBrowsers + ' | ' + selectedLocations);
	}else{
		$('#accordion-info-jobs').text('');
	}
};
var setCollapseMeasurementInfos = function(toSet){
	if(toSet){
		var delimitterIfMultipleOptions = ', ';
		var selectedUncached = getMaxCharacters(getTextList($('#selectAggregatorUncachedHtmlId option:selected')).join(delimitterIfMultipleOptions), 100);
		var selectedCached = getMaxCharacters(getTextList($('#selectAggregatorCachedHtmlId option:selected')).join(delimitterIfMultipleOptions), 100);
		$('#accordion-info-measurements').text(selectedUncached + ' | ' + selectedCached);
	}else{
		$('#accordion-info-measurements').text('');
	}
};
var getTextList = function(selectedOptions){
	var groupsTextList = [];
	selectedOptions.each(function(i){
		groupsTextList.push($(this).text());
	});	
	return groupsTextList;
};
var getMaxCharacters = function(toGetMaxFrom, max){
	var maxChar = toGetMaxFrom.length > max?toGetMaxFrom.substring(0,max-3)+'...':toGetMaxFrom;
	maxChar = maxChar==''?'\u2205':maxChar;
	return maxChar;
};
var setChevron = function(accordionElement) {
	if(!accordionElement.hasClass("collapse")) {
		$(accordionElement).parent().find("a.accordion-toggle").removeClass("icon-chevron-down").addClass("icon-chevron-up")
	} else {
		$(accordionElement).parent().find("a.accordion-toggle").removeClass("icon-chevron-up").addClass("icon-chevron-down")
	}
}
function initIntervalSelect() {
	//initialization///////////////////////
	var lastIntervalSelection = getFromLocalStorage('de.iteratec.osm.result.dashboard.intervalselection');
	if(lastIntervalSelection != null) $('#selectedIntervalHtmlId').val(lastIntervalSelection);
	//register events///////////////////////
	$('#selectedIntervalHtmlId').on("change", function(event){
		setToLocalStorage('de.iteratec.osm.result.dashboard.intervalselection', $('#selectedIntervalHtmlId').val());
	});
}
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
	
	setChevron($('#collapseOne'));
	
	$('#collapseOne').on('shown', function (e) {
        $(this).parent().find("a.accordion-toggle").removeClass("icon-chevron-down").addClass("icon-chevron-up");
		e.preventDefault();
		setCollapseDateInfos(false);
	});
	$('#collapseOne').on('hidden', function (e) {
        $(this).parent().find("a.accordion-toggle").removeClass("icon-chevron-up").addClass("icon-chevron-down");
		setCollapseDateInfos(true);
	});
	
	setChevron($('#collapseTwo'));
	$('#collapseTwo').on('shown', function (e) {
        $(this).parent().find("a.accordion-toggle").removeClass("icon-chevron-down").addClass("icon-chevron-up");
		e.preventDefault();
		setCollapseJobInfos(false);
	});
	$('#collapseTwo').on('hidden', function (e) {
        $(this).parent().find("a.accordion-toggle").removeClass("icon-chevron-up").addClass("icon-chevron-down");
		setCollapseJobInfos(true);
	});
	
	setChevron($('#collapseThree'));
	
	$('#collapseThree').on('shown', function (e) {
        $(this).parent().find("a.accordion-toggle").removeClass("icon-chevron-down").addClass("icon-chevron-up");
		e.preventDefault();
		setCollapseMeasurementInfos(false);
	});
	$('#collapseThree').on('hidden', function (e) {
        $(this).parent().find("a.accordion-toggle").removeClass("icon-chevron-up").addClass("icon-chevron-down");
		setCollapseMeasurementInfos(true);
	});
	
	// Scroll to Chartbox
	if($("#chartbox").length > 0){
        $('html,body').animate({scrollTop: ($("#chartbox").offset().top+180)},{duration: 'fast'});
	}else{
		$('html,body').animate({scrollTop: 0},{duration: 'fast'});
	}

}
