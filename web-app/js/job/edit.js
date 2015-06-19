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

function doOnDomReady(newJob, nextExecutionLink, customConnNameForNative, connectivityProfileId, noTrafficShapingAtAll, tagsLink) {

    $("ul[name='tags']").tagit({select:true, tagSource: tagsLink});

    $("[rel=tooltip]").tooltip({ html: true });
    $("[rel=popover]").popover();

    $('#active').change(function () {
        $('[name="execution-schedule-shown"]').keyup();
    });

    prepareConnectivityProfileControls(newJob, customConnNameForNative, connectivityProfileId, noTrafficShapingAtAll)
    $('#connectivityProfile').change();

    initializeSelects();

    $('#maxDownloadTimeInMinutes a').click(function() {
        $('#maxDownloadTimeInMinutes input')
            .removeClass('non-editable')
            .removeAttr('readonly');
        $('#maxDownloadTimeInMinutes a').css('visibility', 'hidden');
    });

    fixChosen();

    var cronExpression = $('#execution-schedule').val();
    jQuery.ajax({
        type: 'POST',
        data: 'value=' + cronExpression,
        url: nextExecutionLink,
        success: function (data, textStatus) {
            $('#cronhelp-next-execution').html(
                data + ' ' + warnInactive(data, getExecutionScheduleSetButInactiveLabel()) + ' '
            );
            FutureOnlyTimeago.init($('abbr.timeago'), nextExecutionLink);
            $('#cronhelp-readable-expression').html(
                data ? getPrettyCron(cronExpression.substr(cronExpression.indexOf(' ') + 1)) : ''
            );

        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {}
    });

};

function initializeSelects() {
	  var chosenOptions = {
			  disable_search_threshold: 10,
			  no_results_text: '',
			  width: "35em",
			  search_contains: true
	  };
	  if ($('select#jobgroup').size() > 0) {
		  $('select#jobgroup').chosen(chosenOptions);
	  }
	  if ($('select#location').size() > 0) {
		  $('select#location').chosen(chosenOptions);
	  }		  
	  if ($('select#connectivityProfile').size() > 0) {
		  $('select#connectivityProfile').chosen(chosenOptions);
	  }
	  if ($('select#script').size() > 0) {
		  chosenOptions.allow_single_deselect = true;
		  $('select#script').chosen(chosenOptions);
	  }
}

function warnInactive(data, notActiveMessage) {
	if (!$('input[name="active"]').prop('checked') && !/^\s*$/.test(data)) {
		return notActiveMessage;
	} else {
		return '';
	}
}

function updateScriptEditHref(baseUrl, scriptId) {
	$('a#editScriptLink').attr('href', baseUrl + '/' + scriptId);
}

jQuery.fn.visibilityToggle = function() {
    return this.css('visibility', function(i, visibility) {
        return (visibility == 'visible') ? 'hidden' : 'visible';
    });
};

/**
 * <ul>
 *  <li>Adding options to connectivity profiles select which do not represent conn profile domain objects ('Native' and 'Custom').</li>
 *  <li>Register all necessary event handlers for connectivity concerned controls.</li>
 *  <li>If Job to show has 'native' or 'custom' connectivity. So no conn profile domain object is associated to it,
 *  the connectivity option will be selected manually in select.</li>
 * </ul>
 * @param newJob Boolean which describes whether or not this page is for creation of a new job.
 * @param customConnNameForNative The name for option 'Native'. It's defined as a constant in a backend service.
 * @param connectivityProfileId Id of job associated connectivity profile. May be null if job has 'Native' or 'Custom' connectivity.
 * @param noTrafficShapingAtAll True, if job has 'Native' connectivity. Otherwise false.
 */
function prepareConnectivityProfileControls(newJob, customConnNameForNative, connectivityProfileId, noTrafficShapingAtAll){

    addNullProfileOptions(customConnNameForNative)
    registerConnectivityProfilesEventHandlers(customConnNameForNative)
    if(connectivityProfileId == null){
        selectConnectivityManually(newJob, noTrafficShapingAtAll, customConnNameForNative)
    }

}
/**
 * Adding the following two options to connectivity profiles:
 * <ul>
 *     <li>Custom: custom connectivity where attributes like bandwidth and latency can be set manually</li>
 *     <li>Native: No traffic shaping at all</li>
 * </ul>
 * @param customConnNameForNative Custom connectivity name for native connectivity.
 */
function addNullProfileOptions(customConnNameForNative){
    var optNative = document.createElement('option');
    optNative.value = null;
    optNative.text = customConnNameForNative;
    var optCustom = document.createElement('option');
    optCustom.value = null;
    optCustom.text = 'Custom';
    var connProfileSelect = document.getElementById('connectivityProfile');
    connProfileSelect.appendChild(optNative);
    connProfileSelect.appendChild(optCustom);
    connProfileSelect.dispatchEvent(new Event("chosen:updated"));
}

function selectConnectivityManually(newJob, noTrafficShapingAtAll, customConnNameForNative){

    var profilesSelect = document.getElementById('connectivityProfile');

    if(newJob){
        profilesSelect.selectedIndex = 0;
    } else if(noTrafficShapingAtAll){

        for (var i = 0; i < profilesSelect.options.length; i++) {
            if (profilesSelect.options[i].text === customConnNameForNative) {
                profilesSelect.selectedIndex = i;
                break;
            }
        }

    } else {

        for (var i = 0; i < profilesSelect.options.length; i++) {
            if (profilesSelect.options[i].text != customConnNameForNative && profilesSelect.options[i].value == "null") {

                var customConnNameInput = document.getElementById('custom-connectivity-name');
                if(getCustomConnNameFromDom() != customConnNameInput.value){
                    customConnNameInput.readOnly = false;
                    document.getElementById('setCustomConnNameManually').checked = true;
                    removeUpdateCustomNameEventListeners();
                }
                profilesSelect.selectedIndex = i;

                break;

            }
        }

    }
    profilesSelect.options[profilesSelect.options.selectedIndex].selected = true;
}
function registerConnectivityProfilesEventHandlers(customConnNameForNative){
    document.getElementById('connectivityProfile').onchange = function(){
        var selectedOption = this.options[this.selectedIndex];
        if(selectedOption.text == "Custom"){

            toggleCustomConnDetails(true)
            document.getElementById('noTrafficShapingAtAll').value = false;
            document.getElementById('customConnectivityProfile').value = true;

        }else {

            toggleCustomConnDetails(false)
            document.getElementById('customConnectivityProfile').value = false;
            if(selectedOption.text == customConnNameForNative){
                document.getElementById('noTrafficShapingAtAll').value = true;
            }else {
                document.getElementById('noTrafficShapingAtAll').value = false;
            }

        }
    };

    addUpdateCustomNameEventListeners();

    document.getElementById('setCustomConnNameManually').onchange = function(){
        var manualCustomConnName = this.checked;
        if(manualCustomConnName){
            document.getElementById('custom-connectivity-name').readOnly = false;
            removeUpdateCustomNameEventListeners();
        }else{
            document.getElementById('custom-connectivity-name').readOnly = true;
            updateCustomConnName();
            addUpdateCustomNameEventListeners();
        }
    }

}
function addUpdateCustomNameEventListeners(){
    if (document.addEventListener) {
        addUpdateCustomNameEventListenersForAllMajorBrowsers();
    } else if (document.attachEvent) {
        addUpdateCustomNameEventListenersForIE8AndEarlier();
    }

}
function addUpdateCustomNameEventListenersForAllMajorBrowsers(){
    document.getElementById('custom-bandwidthDown').addEventListener("input", updateCustomConnName);
    document.getElementById('custom-bandwidthUp').addEventListener("input", updateCustomConnName);
    document.getElementById('custom-latency').addEventListener("input", updateCustomConnName);
    document.getElementById('custom-packetLoss').addEventListener("input", updateCustomConnName);
}
function addUpdateCustomNameEventListenersForIE8AndEarlier(){
    document.getElementById('custom-bandwidthDown').attachEvent("onchange", updateCustomConnName);
    document.getElementById('custom-bandwidthUp').attachEvent("onchange", updateCustomConnName);
    document.getElementById('custom-latency').attachEvent("onchange", updateCustomConnName);
    document.getElementById('custom-packetLoss').attachEvent("onchange", updateCustomConnName);
}
function removeUpdateCustomNameEventListeners(){
    if (document.removeEventListener) {
        removeUpdateCustomNameEventListenersForAllMajorBrowsers();
    } else if (document.detachEvent) {
        removeUpdateCustomNameEventListenersForIE8AndEarlier();
    }

}
function removeUpdateCustomNameEventListenersForAllMajorBrowsers(){
    document.getElementById('custom-bandwidthDown').removeEventListener("input", updateCustomConnName);
    document.getElementById('custom-bandwidthUp').removeEventListener("input", updateCustomConnName);
    document.getElementById('custom-latency').removeEventListener("input", updateCustomConnName);
    document.getElementById('custom-packetLoss').removeEventListener("input", updateCustomConnName);
}
function removeUpdateCustomNameEventListenersForIE8AndEarlier(){
    document.getElementById('custom-bandwidthDown').detachEvent("onchange", updateCustomConnName);
    document.getElementById('custom-bandwidthUp').detachEvent("onchange", updateCustomConnName);
    document.getElementById('custom-latency').detachEvent("onchange", updateCustomConnName);
    document.getElementById('custom-packetLoss').detachEvent("onchange", updateCustomConnName);
}
function updateCustomConnName() {
    document.getElementById('custom-connectivity-name').value = getCustomConnNameFromDom();
}
function toggleCustomConnDetails(visibilityToSet){
    $('#connectivityProfileDetails').toggle(visibilityToSet);
    if(!visibilityToSet){
        document.getElementById('custom-connectivity-name').value = "";
        document.getElementById('custom-bandwidthDown').value = "";
        document.getElementById('custom-bandwidthUp').value = "";
        document.getElementById('custom-latency').value = "";
        document.getElementById('custom-packetLoss').value = "";
    }
}
function getCustomConnNameFromDom(){

    var bandwidthDownValue = document.getElementById('custom-bandwidthDown').value;
    var bandwidthUpValue = document.getElementById('custom-bandwidthUp').value;
    var latencyValue = document.getElementById('custom-latency').value;
    var packetLossValue = document.getElementById('custom-packetLoss').value;

    var bwDownOrEmpty = bandwidthDownValue?bandwidthDownValue:"[bwDown]";
    var bwUpOrEmpty = bandwidthUpValue?bandwidthUpValue:"[bwUp]";
    var latencyOrEmpty = latencyValue?latencyValue:"[latency]";
    var plrOrEmpty = packetLossValue?packetLossValue:"[plr]";

    return "Custom (" + bwDownOrEmpty + "/" + bwUpOrEmpty + " Kbps, " + latencyOrEmpty + "ms, " + plrOrEmpty + "% PLR)";
}

function domainDeleteConfirmation(message,id,link){
    var confirmMessage = "";
    if(link != null || link == ""){
        jQuery.ajax({
            type : 'GET',
            url : link,
            data: {id:id},
            async:   false,
            success: function(result) {
                confirmMessage = message + "<br>" + result;
            },
            error: function(result) {
                confirmMessage = message;
            }});
        return confirmMessage;
    } else{
        return message;
    }

}
function toggleCronInstructions(){
    var cronInstructions = document.querySelector('#cron-instructions');
    cronInstructions.style.display=="none" ?
        cronInstructions.style.display="inline" : cronInstructions.style.display="none";
}
function updateExecScheduleInformations(execScheduleWithSeconds, nextExecutionLink) {
    jQuery.ajax({
        type: 'POST',
        data: 'value=' + execScheduleWithSeconds,
        url: nextExecutionLink,
        success: function (data, textStatus) {

            $('#execution-schedule').val(execScheduleWithSeconds);
            $('#cronhelp-next-execution').html(
                data + ' ' + warnInactive(data, getExecutionScheduleSetButInactiveLabel()) + ' '
            );
            FutureOnlyTimeago.init($('abbr.timeago'), nextExecutionLink);
            $('#cronhelp-readable-expression').html(
                data ? getPrettyCron(execScheduleWithSeconds.substr(execScheduleWithSeconds.indexOf(' ') + 1)) : ''
            );

        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
        }
    });
}
/**
 * Necessary to prevent Chosen dropdown from being cut off in tabs
 * @see http://stackoverflow.com/a/21375637
 * TODO: after first selection dropdown of chosen select disappears after click. So one have to hold the mouse button while selection :(
 */
function fixChosen() {
    var els = jQuery(".chosen");
    els.on("chosen:showing_dropdown", function (event, chosen) {
        $(this).parents("div").css("overflow", "visible");
    });
    els.on("chosen:hiding_dropdown", function () {
        var $parent = $(this).parents("div");
        // See if we need to reset the overflow or not.
        var noOtherExpanded = $('.chosen-with-drop', $parent).length == 0;
        if (noOtherExpanded)
            $parent.css("overflow", "");
        $('.tab-content').scrollTop(($('.tab-content').height()*2));
    });
}