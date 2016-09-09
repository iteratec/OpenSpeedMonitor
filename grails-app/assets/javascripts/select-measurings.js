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

/*
 * Contains functionality used in template /eventResultDashboard/_selectMeasurings.gsp
*/
var initSelectMeasuringsControls = function(
    pagesToEvents,
    browserToLocation,
    allMeasuredEventElements,
    allBrowsers,
    allLocations
){
	if ($("#selectedAllMeasuredEvents").length > 0 && $("#selectedMeasuredEventsHtmlId").length > 0 ){
		collectAllValues(allMeasuredEventElements, "#selectedMeasuredEventsHtmlId");
		initSelectAndCheckBoxFunction("#selectedAllMeasuredEvents", "#selectedMeasuredEventsHtmlId");

		$('#pageSelectHtmlId').change(function() { updateSelectFields("#pageSelectHtmlId", "#selectedMeasuredEventsHtmlId",pagesToEvents, allMeasuredEventElements); });
		updateSelectFields("#pageSelectHtmlId", "#selectedMeasuredEventsHtmlId",pagesToEvents, allMeasuredEventElements);
	}
	if ($("#selectedAllBrowsers").length > 0 && $("#selectedBrowsersHtmlId").length > 0 ){
		collectAllValues(allBrowsers, "#selectedBrowsersHtmlId");
		initSelectAndCheckBoxFunction("#selectedAllBrowsers", "#selectedBrowsersHtmlId");
	}
	if ($("#selectedAllLocations").length > 0 && $("#selectedLocationsHtmlId").length > 0 ){
		collectAllValues(allLocations, "#selectedLocationsHtmlId");

		initSelectAndCheckBoxFunction("#selectedAllLocations", "#selectedLocationsHtmlId");
		$('#selectedBrowsersHtmlId').change(function() { updateSelectFields("#selectedBrowsersHtmlId", "#selectedLocationsHtmlId", browserToLocation, allLocations); });
		updateSelectFields("#selectedBrowsersHtmlId", "#selectedLocationsHtmlId", browserToLocation, allLocations);
	}
    if ($("#selectedAllConnectivityProfiles").length > 0 && $("#selectedConnectivityProfilesHtmlId").length > 0 ){
        initSelectAndCheckBoxFunction("#selectedAllConnectivityProfiles", "#selectedConnectivityProfilesHtmlId");
    }
	if ($("#includeCustomConnectivity").length > 0 && $("#customConnectivityName").length > 0 ){
		initTextFieldAndCheckBoxFunction("#includeCustomConnectivity", "#customConnectivityName");
	}
	$("#advanced-filter-row").fadeOut();
	$('#simple-job-filter').button('toggle');

    fixChosen();
};

function sortAlpha(a,b){  
	return a.innerHTML.toLowerCase() > b.innerHTML.toLowerCase() ? 1 : -1;
};  

var initSelectAndCheckBoxFunction = function(checkBox, selectBox) {
		$(checkBox).on('change', function(event) {
			if(event.currentTarget.checked == true) {
				$(selectBox).css({opacity: 0.5});
				$(selectBox + " option").prop('selected', false);
				$(selectBox).trigger("chosen:updated");
			} else {
				$(selectBox).css({opacity: 1});
			}
		});

		$(selectBox).on('change', function(event) {
			if ($(selectBox).find('option:selected').length > 0) {
				$(checkBox).prop('checked', false);
				$(selectBox).css({opacity: 1});
			} else {
				$(checkBox).prop('checked', true);
				$(selectBox).css({opacity: 0.5});
			}
			$(selectBox).trigger("chosen:updated");
		});
	$(selectBox).css({opacity: 0.5});
};

var initTextFieldAndCheckBoxFunction = function(checkBox, textField) {
	$(checkBox).on('change', function(event) {
		if(event.currentTarget.checked == true) {
			$(textField).prop('disabled', false);
		} else {
			$(textField).prop('disabled', true);
		}
	});
};

/*
 * array of all selectable measured events
 */
var allMeasuredEventElements =  [];

/*
 * array of all selectable browsers
 */
var allBrowsers =  [];

/*
 * set of all selectable locations
 */
var allLocations =  [];

/*
 * Collects All values of an selectBox
 */
var collectAllValues = function(allElementVar, selector) {
	$(selector + ' option').each(function(i, element) {
		allElementVar[element.value]=$(element).text();
	});
}

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
	
	$(toSelector).empty();

	if(selectedFrom != null) {
		for( var k=0; k<fromTo.length; k++ ) {
            
		    if($.inArray(k+"", selectedFrom)!=-1 && fromTo[k]!=null) {
                for( var i=0; i<fromTo[k].length; i++ ) {
		    		if(allElements[fromTo[k][i]]!="") {
						$(toSelector).append('<option value="'+ fromTo[k][i] +'">'+ allElements[fromTo[k][i]] + '</option>');
					} 
			    
		    	}
		    }
		}
	} else {
	 	for(var key in allElements) {
	 		$(toSelector).append('<option value=\"' + key + '\">'+ allElements[key] + '</option>');
	 	}
	 }
	
	for(var key in selectedTo) {
		$(toSelector + '  option[value="'+selectedTo[key]+'"]').prop('selected', true);
	}
	
	$(toSelector + ' option').sort(sortAlpha).appendTo(toSelector);
	$(toSelector).trigger("chosen:updated");
	$(toSelector).scrollTop();
};

var initChosenSelects = function(noResultsTextForChosenSelects){
	$('#selectedMeasuredEventsHtmlId').chosen({ search_contains: true, width: "100%", no_results_text: noResultsTextForChosenSelects });
	$('#selectedLocationsHtmlId').chosen({ search_contains: true, width: "100%", no_results_text: noResultsTextForChosenSelects });
}

$('#simple-job-filter').click(function(){
	$("#advanced-filter-row").fadeOut();
});

$('#advanced-job-filter').click(function(){ 
	$("#advanced-filter-row").fadeIn();
});