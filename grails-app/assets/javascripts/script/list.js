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

//= require node_modules/StickyTableHeaders/js/jquery.stickytableheaders.js
//= require_self

function filterScriptList() {
    var filterText = $.trim($('#filterByLabel').val());
    var reText = new RegExp(filterText, 'i');
	$('table tbody tr').each(function() {
		var tr = $(this);
		var scriptLabel = $('.scriptLabel', tr).text();
    	var showRow = true;
    	if (showRow && filterText !== '') { showRow = scriptLabel.search(reText) > -1; }
    	tr.toggle(showRow);
    });
	// reapply striping
    $("#script-table tr:not(.hidden)").each(function (index) {
        $(this).toggleClass("stripe", !!(index & 1));
    });
}

$(function () {
  $("#script-table").stickyTableHeaders({
    cacheHeaderHeight: true
  });
});
