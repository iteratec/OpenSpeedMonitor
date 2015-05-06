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
 * Initializes bootstrap-datepickers and registers events for changes on datpicker-inputs.
 */
var initDatepicker = function(dateformatToSet, weekstartToSet, timespanFromNowInHours) {

	//read previous selections from local storage///////////////////////////////////////////////////////////////////

	var lastTimeframeSelection = getFromLocalStorage('de.iteratec.osm.result.dashboard.timeframeselection')
	var lastFromSelection = getFromLocalStorage('de.iteratec.osm.result.dashboard.from');
	var lastToSelection = getFromLocalStorage('de.iteratec.osm.result.dashboard.to');

	//initialization///////////////////////////////////////////////////////////////////
	
	var now = new Date();
	var dateInPastToSetAsFrom = new Date(now.getTime()-(1000*60*60*timespanFromNowInHours));

	$("#fromDatepicker").datepicker({
		format: dateformatToSet,
		weekStart: weekstartToSet,
		endDate: '+1'
	});
	if (!$("#from").val()) {
		var toSet = getDateAs_ddMMyyyy(dateInPastToSetAsFrom);
		$("#fromDatepicker").val(toSet);
		$("#fromDatepicker").datepicker("update");
		$("#from").val(toSet)
	}
	$("#toDatepicker").datepicker({
		format: dateformatToSet,
		weekStart: weekstartToSet,
		endDate: '+1d'
	});
	if (!$("#to").val()) {
		var toSet = getDateAs_ddMMyyyy(now);
		$("#toDatepicker").val(toSet);
		$("#toDatepicker").datepicker("update");
		$("#to").val(toSet);
	}

	//set previous selections ///////////////////////////////////////////////////////////////////

	var fromDateFromQueryParams = $.getUrlVar('from');
	var toDateFromQueryParams = $.getUrlVar('to');
	var selectedTimeFrameIntervalFromQueryParams = $.getUrlVar('selectedTimeFrameInterval');

	if(fromDateFromQueryParams != null) setFrom(fromDateFromQueryParams);
	else if(lastFromSelection != null) setFrom(lastFromSelection);

	if(selectedTimeFrameIntervalFromQueryParams != null) $('#timeframeSelect').val(selectedTimeFrameIntervalFromQueryParams);
	else if(lastTimeframeSelection != null) $('#timeframeSelect').val(lastTimeframeSelection);

	if(toDateFromQueryParams != null) setTo(toDateFromQueryParams);
	else if(lastToSelection != null) setTo(lastToSelection);

	//register events///////////////////////////////////////////////////////////////////
	
	$('#fromDatepicker').datepicker().on('changeDate', function(ev){
		var dateAsDdMMyyyy = getDateAs_ddMMyyyy(ev.date);
        $('#from').val(dateAsDdMMyyyy);
		setToLocalStorage('de.iteratec.osm.result.dashboard.from', dateAsDdMMyyyy);
	    if(!$('#setFromHour').is(':checked')){setFromHour("00:00");}
	    $("#toDatepicker").datepicker('setStartDate', ev.date);
	    $("#fromDatepicker").datepicker("hide");
	});
	$('#toDatepicker').datepicker().on('changeDate', function(ev){
		var dateAsDdMMyyyy = getDateAs_ddMMyyyy(ev.date);
        $('#to').val(dateAsDdMMyyyy);
		setToLocalStorage('de.iteratec.osm.result.dashboard.to', dateAsDdMMyyyy);
		if(!$('#setToHour').is(':checked')){setToHour("23:59");}
	    $("#toDatepicker").datepicker("hide");
	});
	
	$('#timeframeSelect').on('change', function(ev){
		setToLocalStorage('de.iteratec.osm.result.dashboard.timeframeselection', this.value);
		var preSelection = this.value>0;
		
		if(preSelection) setDateAndTimeRespectivePreselection();
		disOrEnableFieldsetsOfManualDateTimeSelection(preSelection);
		
	})
	
};
/**
 * Initializes bootstrap-timepickers and registers events for changes on timepicker-inputs.
 */
var initTimepicker = function(whetherToShowMeridian) {

	//read previous selections from local storage///////////////////////////////////////////////////////////////////

	var lastFromHourSelection = getFromLocalStorage('de.iteratec.osm.result.dashboard.fromHour');
	var lastToHourSelection = getFromLocalStorage('de.iteratec.osm.result.dashboard.toHour');
	var lastManualFromHourSelection = getFromLocalStorage('de.iteratec.osm.result.dashboard.manualFromHour');
	var lastManualToHourSelection = getFromLocalStorage('de.iteratec.osm.result.dashboard.manualToHour');

	//initialization///////////////////////////////////////////////////////////////////
	
	$('#fromHourTimepicker').timepicker({
    showMeridian: whetherToShowMeridian
	});
	$('#toHourTimepicker').timepicker({
		showMeridian: whetherToShowMeridian
	});
	setFromHour($('#fromHourTimepicker').val());
	setToHour($('#toHourTimepicker').val());

	//set previous selections ///////////////////////////////////////////////////////////////////

	var fromHourFromQueryParams = $.getUrlVar('fromHour');
	var toHourFromQueryParams = $.getUrlVar('toHour');
	var setFromHourFromQueryParam = $.getUrlVar('setFromHour');
	var setToHourFromQueryParam = $.getUrlVar('setToHour');

	if(window.location.href.indexOf("event/edit") > -1) {
	  fromHourFromQueryParams = $('#eventTime').val();
	  setFromHourFromQueryParam = $('#eventTime').val();
	}
	
	if(fromHourFromQueryParams != null) setFromHour(fromHourFromQueryParams);
	else if(lastFromHourSelection != null) setFromHour(lastFromHourSelection);

	if(toHourFromQueryParams != null) setToHour(toHourFromQueryParams);
	else if(lastToHourSelection != null) setToHour(lastToHourSelection);

	if(setFromHourFromQueryParam !=null) setManualFromHourCheckbox(setFromHourFromQueryParam);
	else if(lastManualFromHourSelection !=null) setManualFromHourCheckbox(lastManualFromHourSelection);

	if(setToHourFromQueryParam != null) setManualToHourCheckbox(setToHourFromQueryParam)
	else if(lastManualToHourSelection !=null) setManualToHourCheckbox(lastManualToHourSelection);

	//register events///////////////////////////////////////////////////////////////////

	$('#setFromHour').on('change', function(ev){
		var manualFromHourSelection = $('#setFromHour').is(':checked');
		$('#fldset-startdate-hour :input').attr("disabled", !manualFromHourSelection);
		setToLocalStorage('de.iteratec.osm.result.dashboard.manualFromHour', manualFromHourSelection);
	});
	$('#setToHour').on('change', function(ev){
		var manualToHourSelection = $('#setToHour').is(':checked');
		$('#fldset-enddate-hour :input').attr("disabled", !manualToHourSelection);
		setToLocalStorage('de.iteratec.osm.result.dashboard.manualToHour', manualToHourSelection);
	});
	$('#fromHourTimepicker').on('changeTime.timepicker', function(ev){
		setToLocalStorage('de.iteratec.osm.result.dashboard.fromHour', ev.time.value);
		$("#fromHour").val(ev.time.value);
	});
	$('#toHourTimepicker').on('changeTime.timepicker', function(ev){
		setToLocalStorage('de.iteratec.osm.result.dashboard.toHour', ev.time.value);
		$("#toHour").val(ev.time.value);
	});
};
var getDateAs_ddMMyyyy = function(date){
	return addLeadingZeroIfNecessary(addLeadingZeroIfNecessary(date.getDate())+"."+addLeadingZeroIfNecessary(date.getMonth()+1)+"."+date.getFullYear())
};
var addLeadingZeroIfNecessary = function(date) {
	if(date<10) {
		date="0"+date;
	}
	return date;
};
var disOrEnableFieldsetsOfManualDateTimeSelection = function(preSelection){
	var manualSelection = !preSelection
	//dis-/enable hole fieldset of manual date/time selection 
	$('#fldset-startdate :input').attr("disabled", preSelection);
	$('#fldset-enddate :input').attr("disabled", preSelection);
	//disable fieldsets for manual time selection if parent fieldset of date/time selection
	//is enabled and checkbox for manual time selection isn't checked (default)
	if(manualSelection && !$('#setFromHour').is(':checked')){
		$('#fldset-startdate-hour :input').attr("disabled", true);
	}
	if(manualSelection && !$('#setToHour').is(':checked')){
		$('#fldset-enddate-hour :input').attr("disabled", true);
	}
};

var setDateAndTimeRespectivePreselection = function() {
	var now=new Date();
	var start=new Date(now.getTime() - ( $('#timeframeSelect').val() * 1000));
	var day, month;

	setFrom(getDateAs_ddMMyyyy(start));
	setFromHour(start.getHours().toString() + ":" + start.getMinutes().toString())

  setTo(getDateAs_ddMMyyyy(now));
	setToHour(now.getHours().toString() + ":" + now.getMinutes().toString())

};
var setTo = function(toToSet){
	setToLocalStorage('de.iteratec.osm.result.dashboard.to', toToSet);
	$("#to").val(toToSet);
	$("#toDatepicker").val(toToSet);
	$("#toDatepicker").datepicker("update");
};
var setFrom = function(fromToSet){
	setToLocalStorage('de.iteratec.osm.result.dashboard.from', fromToSet);
	$("#from").val(fromToSet);
	$("#fromDatepicker").val(fromToSet);
	$("#fromDatepicker").datepicker("update");
};
var setToHour = function(toHourToSet){
	setToLocalStorage('de.iteratec.osm.result.dashboard.toHour', toHourToSet);
	$("#toHour").val(toHourToSet);
	toHourToSet = toHourToSet=='00:00'?'00:001':toHourToSet;	//hack to fix a bug in bootstrap timepicker
	$("#toHourTimepicker").timepicker('setTime', toHourToSet);
};
var setFromHour = function(fromHourToSet){
	setToLocalStorage('de.iteratec.osm.result.dashboard.fromHour', fromHourToSet);
	$("#fromHour").val(fromHourToSet);
	fromHourToSet = (fromHourToSet=='00:00'||fromHourToSet=='0:00')?'00:001':fromHourToSet;	//hack to fix a bug in bootstrap timepicker
	$("#fromHourTimepicker").timepicker('setTime', fromHourToSet);
};
var setManualFromHourCheckbox = function(lastManualFromHourSelection){
	$('#setFromHour').attr('checked', stringToBoolean(lastManualFromHourSelection));
	$('#fldset-startdate-hour :input').attr("disabled", !stringToBoolean(lastManualFromHourSelection));
};
var setManualToHourCheckbox = function(lastManualToHourSelection){
	$('#setToHour').attr('checked', stringToBoolean(lastManualToHourSelection));
	$('#fldset-enddate-hour :input').attr("disabled", !stringToBoolean(lastManualToHourSelection));
};