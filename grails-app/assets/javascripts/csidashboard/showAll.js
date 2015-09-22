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
	
	initChosenSelects(noResultsTextForChosenSelects);
	
	$("input[name='aggrGroup']").change(setFilterElementsVisibility); 
	
	$("input[name='aggrGroup']:checked").each(setFilterElementsVisibility); 
	
	$("#override-long-processing-time").click(function() {
		$("#overwriteWarningAboutLongProcessingTime").prop('checked', true);
		$("#chart-submit").submit();
	});
	
	
	// Toggle Buttons
	$("#chart-toggle").click(function() {
		$("#csi-table").hide();
		$("#chartbox").fadeIn();
	});
	$("#table-toggle").click(function() {
		$("#chartbox").hide();
		$("#csi-table").fadeIn();
	});
	
	// Scroll to Chartbox
	if($("#chartbox").length > 0){
		$('html,body').animate({scrollTop: ($("#chartbox").offset().top-390)},'slow');
	}	
}
/**
 * show or hide filter-elements, depending on aggregator type
 */
var setFilterElementsVisibility = function() {
	if ($(this).val() == 'measuredEvent') {
		$("#browser-filter").fadeIn();
			$("#advanced-filter-buttons").fadeIn();
			
		if($("#advanced-job-filter").hasClass('active')) {
			$("#advanced-filter-row").fadeIn();
		}
	}
	else {
		$("#browser-filter").fadeOut();
		$("#advanced-filter-row").fadeOut();
		$("#advanced-filter-buttons").fadeOut();
	}
	if (($(this).val() != 'shop') && ($(this).val() != 'daily_shop')) {
		$("#page-filter").fadeIn();
	} else {
		$("#page-filter").fadeOut();
		$("#advanced-filter-row").fadeOut();
	}
}