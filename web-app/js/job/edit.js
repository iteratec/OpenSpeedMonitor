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

$(document).ready(function() {	
	$('#cronStringInstructions').popover();
	$('#connectivityProfile').append($('<option></option>').val('null').text('Custom'));
	if ($('#customConnectivityProfile').val() == 'true') {
		$('#connectivityProfile option:last-child').attr('selected', 'selected');
	}
    initializeSelects();
    $("[rel=tooltip]").tooltip({ html: true });
    $("[rel=popover]").popover();
  	
	$('#active').change(function () {
		$('[name="executionSchedule"]').keyup();
	});
	$('[name="executionSchedule"]').keyup()
	
	$('#connectivityProfile').change(function() {
		var custom = $('#connectivityProfile :selected').text() == 'Custom';
		$('#connectivityProfileDetails').toggle(custom);
		$('#customConnectivityProfile').val(custom);
	});
	$('#connectivityProfile').change();
	
	$('#maxDownloadTimeInMinutes a').click(function() {
		$('#maxDownloadTimeInMinutes input')
			.removeClass('non-editable')
			.removeAttr('readonly');
		$('#maxDownloadTimeInMinutes a').css('visibility', 'hidden');
	});

});

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