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
	  //if ($('select#connectivityProfile').size() > 0) {
		//  $('select#connectivityProfile').chosen(chosenOptions);
	  //}
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

function doOnDomReady(nextExecutionLink, customConnNameForNative, connectivityProfileId, noTrafficShapingAtAll) {
    initializeSelects();
    $("[rel=tooltip]").tooltip({ html: true });
    $("[rel=popover]").popover();
  	
	$('#active').change(function () {
		$('[name="execution-schedule-shown"]').keyup();
	});

    prepareConnectivityProfileControls(customConnNameForNative, connectivityProfileId, noTrafficShapingAtAll)
	$('#connectivityProfile').change();

	$('#maxDownloadTimeInMinutes a').click(function() {
		$('#maxDownloadTimeInMinutes input')
			.removeClass('non-editable')
			.removeAttr('readonly');
		$('#maxDownloadTimeInMinutes a').css('visibility', 'hidden');
	});

    var cronExpression = $('#execution-schedule').val();
    jQuery.ajax({
        type: 'POST',
        data: 'value=' + cronExpression,
        url: nextExecutionLink,
        success: function (data, textStatus) {

            //$('#execution-schedule').val(execScheduleWithSeconds);
            //alert(data);
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


function prepareConnectivityProfileControls(customConnNameForNative, connectivityProfileId, noTrafficShapingAtAll){

    addNullProfileOptions(customConnNameForNative)
    selectOptionIfOneOfNullOptionsIsSetForJob(connectivityProfileId, noTrafficShapingAtAll, customConnNameForNative)
    registerCustomConnEventHandlers(customConnNameForNative)

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

    document.getElementById('custom-connectivity-name').value = getCustomConnNameFrom();
}
//set native
function selectOptionIfOneOfNullOptionsIsSetForJob(connectivityProfileId, noTrafficShapingAtAll, customConnNameForNative){

    var profilesSelect = document.getElementById('connectivityProfile');

    if(connectivityProfileId == null){
        if(noTrafficShapingAtAll){

            for (var i = 0; i < profilesSelect.options.length; i++) {
                if (profilesSelect.options[i].text === customConnNameForNative) {
                    profilesSelect.selectedIndex = i;
                    break;
                }
            }

            profilesSelect.options[profilesSelect.options.selectedIndex].selected = true;
        }else {

            for (var i = 0; i < profilesSelect.options.length; i++) {
                if (profilesSelect.options[i].text != customConnNameForNative && profilesSelect.options[i].value == "null") {
                    profilesSelect.selectedIndex = i;
                    break;
                }
            }

        }
    }
}
function registerCustomConnEventHandlers(customConnNameForNative){

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

    var customConnNameInput = document.getElementById('custom-connectivity-name');

    document.getElementById('custom-bandwidthDown').oninput = function () {
        customConnNameInput.value = getCustomConnNameFrom();
    };
    document.getElementById('custom-bandwidthUp').oninput = function () {
        customConnNameInput.value = getCustomConnNameFrom();
    };
    document.getElementById('custom-latency').oninput = function () {
        customConnNameInput.value = getCustomConnNameFrom();
    };
    document.getElementById('custom-packetLoss').oninput = function () {
        customConnNameInput.value = getCustomConnNameFrom();
    };

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
function getCustomConnNameFrom(){

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

// from http://stackoverflow.com/a/21375637
// necessary to prevent Chosen from being cut off in tabs
$(function () {
	   fixChoosen();
});
function fixChoosen() {
   var els = jQuery(".chosen");
   els.on("chosen:showing_dropdown", function () {
      $(this).parents("div").css("overflow", "visible");
   });
   els.on("chosen:hiding_dropdown", function () {
      var $parent = $(this).parents("div");

      // See if we need to reset the overflow or not.
      var noOtherExpanded = $('.chosen-with-drop', $parent).length == 0;
      if (noOtherExpanded)
         $parent.css("overflow", "");
   });
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