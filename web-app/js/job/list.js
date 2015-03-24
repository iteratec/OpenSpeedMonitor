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

JobStatusUpdater = function() {
	var getJobsUrl = '';
	var getLastRunUrl = '';
	var cancelJobUrl = '';
	
	var finishedJobs = [];
	var repeatFnTimer = null;
	
	function cancelJobRun(jobId, testId) {
		jQuery.ajax({
			type : 'POST',
			url : JobStatusUpdater.cancelJobUrl,
			data: { jobId: jobId, testId: testId },
			success: function() {
				$(this).remove();
			}
		});
	}
	
	function updateDateLastRun(jobId) {
		jQuery.ajax({
			type : 'POST',
			url : JobStatusUpdater.getLastRunUrl,
			data: { jobId: jobId },
			success: function(timeagoHtml) {
				var parentTableRow = $('[name="selected.' + jobId + '"]').parent().parent();
				var lastRunTableCell = $($('abbr.timeago', parentTableRow)[0]).parent();
				lastRunTableCell.html(timeagoHtml);
				$('abbr.timeago', lastRunTableCell).timeago();
			}
		});
	}
	
	// from http://stackoverflow.com/a/19519701
	var vis = (function(){
	    var stateKey, eventKey, keys = {
	        hidden: "visibilitychange",
	        webkitHidden: "webkitvisibilitychange",
	        mozHidden: "mozvisibilitychange",
	        msHidden: "msvisibilitychange"
	    };
	    for (stateKey in keys) {
	        if (stateKey in document) {
	            eventKey = keys[stateKey];
	            break;
	        }
	    }
	    return function(c) {
	        if (c) document.addEventListener(eventKey, c);
	        return !document[stateKey];
	    }
	})();
	
	function repeatFn(firstRun) {
		jQuery.ajax({
			type : 'POST',
			url : JobStatusUpdater.getJobsUrl,
			success : function(result, textStatus) {
				$('#serverdown').hide();
				$('[id^="runningstatus-"]').html('');
				$.each(result, function (jobId, data) {
					for (var i = 0; i < data.length; i++) {
						var cssClass = data[i].terminated ? 'done' : 'running';
						var cancelLink = data[i].cancelLinkHtml ? data[i].cancelLinkHtml : '';
						var status = data[i].status < 400 ? '<a href="' + data[i].testUrl + '">' + data[i].message + '</a>' : data[i].message;
						// detect whether job just terminated
						if (data[i].terminated && JobStatusUpdater.finishedJobs.indexOf(data[i].testId) === -1) {
							JobStatusUpdater.finishedJobs.push(data[i].testId);
							if (!firstRun) {
								updateDateLastRun(jobId);
							}
						}							
						
						$('#runningstatus-' + jobId).append('<span class="status ' + cssClass + '">' + status + cancelLink + '</span>');
					}
				});
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				$('[id^="runningstatus-"]').html('');
				$('#serverdown').show();
			}
		});
	}
	
	function initLoop(getJobsUrl, cancelJobUrl, getLastRunUrl, repeatAfterMs) {
		this.getJobsUrl = getJobsUrl;
		this.getLastRunUrl = getLastRunUrl;
		this.cancelJobUrl = cancelJobUrl;
		this.finishedJobs = [];
		repeatFn(true);
		this.repeatFnTimer = setInterval('JobStatusUpdater.repeatFn(false)', repeatAfterMs);
		
		// Poll server only when current page is visible, else pause:
		vis(function(){
			  if (vis() && !JobStatusUpdater.repeatFnTimer) {
				  JobStatusUpdater.repeatFnTimer = window.setInterval('JobStatusUpdater.repeatFn(false)', repeatAfterMs);
			  } else if (!vis() && JobStatusUpdater.repeatFnTimer) {
				  window.clearInterval(JobStatusUpdater.repeatFnTimer);
				  JobStatusUpdater.repeatFnTimer = null;
			  }
		});
	}
	
	return {
		initLoop: initLoop,
		cancelJobRun: cancelJobRun,
		repeatFn: repeatFn
	}
}();

function filterJobList() {
    var filterText = $.trim($('#filterByLabel').val());
    setToLocalStorage('de.iteratec.osm.job.list.filters.jobname',filterText);
    var filterJobGroup = $.trim($('#filterByJobGroup').val());
    setToLocalStorage('de.iteratec.osm.job.list.filters.jobgroup',filterJobGroup);
    var filterLocation = $.trim($('#filterByLocation').val());
    setToLocalStorage('de.iteratec.osm.job.list.filters.location',filterLocation);    
    var filterSkript = $.trim($('#filterBySkript').val());
    setToLocalStorage('de.iteratec.osm.job.list.filters.skript',filterSkript);    
    var filterBrowser = $.trim($('#filterByBrowser').val());
    setToLocalStorage('de.iteratec.osm.job.list.filters.browser',filterBrowser);    
    var filterCheckedJobs = $('#filterCheckedJobs').prop('checked');
    setToLocalStorage('de.iteratec.osm.job.list.filters.checkedjobs',filterCheckedJobs);
    var filterInactiveJobs = $('#filterInactiveJobs').prop('checked');
    setToLocalStorage('de.iteratec.osm.job.list.filters.inactivejobs',filterInactiveJobs);
    var filterHighlightedJobs = $('#filterHighlightedJobs').prop('checked');
    setToLocalStorage('de.iteratec.osm.job.list.filters.highlightedjobs',filterHighlightedJobs);
    var filterRunningJobs = $('#filterRunningJobs').prop('checked');
    setToLocalStorage('de.iteratec.osm.job.list.filters.runningjobs',filterRunningJobs);
    var checkedTags = $('#filterTags').val();
    $('#filterTags_chosen > ul > li.search-choice').size()
    
    var reText = new RegExp(filterText, 'i');
    var reJobGroup = new RegExp(filterJobGroup, 'i');
    var reLocation = new RegExp(filterLocation, 'i');
    var reBrowser = new RegExp(filterBrowser, 'i');
    var reSkript = new RegExp(filterSkript, 'i');
    
	$('table tbody tr').each(function() {
		var tr = $(this);
		var jobName = $('.jobName', tr).text();
		var jobGroup = $('.jobgroup', tr).text();
    var location = $('.location', tr).text();
    var browser = $('.browser', tr).text();
    var skript = $('.skript', tr).text();
    	var showRow = true;
    	if (showRow && filterHighlightedJobs) { showRow = tr.hasClass('highlight'); }
    	if (showRow && filterCheckedJobs) { showRow = $('.jobCheckbox', tr).prop('checked'); }
    	if (showRow && !filterInactiveJobs) { showRow = $('.job_active', tr).val() == 'true'; }
    	if (showRow && filterRunningJobs) { showRow = $('.running', tr).length > 0; }
    	
    	if (showRow && filterJobGroup !== '') { showRow = jobGroup.search(reJobGroup) > -1; }
      if (showRow && filterLocation !== '') { showRow = location.search(reLocation) > -1; }
      if (showRow && filterBrowser !== '') { showRow = browser.search(reBrowser) > -1; }
      if (showRow && filterSkript !== '') { showRow = skript.search(reSkript) > -1; }
    	if (showRow && filterText !== '') { showRow = jobName.search(reText) > -1; }
    	
    	if (showRow && checkedTags) {
    		showRow = false;
    		checkedTags.map(function(currentTag) {
    			var usedTags = tr.attr('data-tags');
    			if (usedTags && usedTags.split(',').indexOf(currentTag) > -1)
    				showRow = true;
    		}); 
    	}
    	tr.toggle(showRow);
    });
}

function initTable(nextExecutionLink) {
	FutureOnlyTimeago.init($('abbr.timeago'), nextExecutionLink);
	updatePrettyCrons();
	filterJobList();
}

InactiveJobLoader = function(listLink, nextExecutionLink) {
	
	var listJobsLink = listLink;
	var nextJobExecutionLink = nextExecutionLink;
	
	this.loadJobs = function() {
		var opts = {
				lines: 15, // The number of lines to draw
				length: 20, // The length of each line
				width: 10, // The line thickness
				radius: 30, // The radius of the inner circle
				corners: 1, // Corner roundness (0..1)
				rotate: 0, // The rotation offset
				direction: 1, // 1: clockwise, -1: counterclockwise
				color: '#000', // #rgb or #rrggbb or array of colors
				speed: 1, // Rounds per second
				trail: 60, // Afterglow percentage
				shadow: true, // Whether to render a shadow
				hwaccel: false, // Whether to use hardware acceleration
				className: 'spinner', // The CSS class to assign to the spinner
				zIndex: 2e9, // The z-index (defaults to 2000000000)
				top: 'auto', // Top position relative to parent in px
				left: '50%' // Left position relative to parent in px
		};
		var spinner = new Spinner(opts).spin(document.getElementById('jobtable'));
		jQuery.ajax({
			type : 'POST',
			url : listJobsLink,
			success: function(result) {
				$('#jobtable tbody').empty();
				$('#jobtable tbody').replaceWith(result);
				initTable(nextJobExecutionLink);
				// to prevent flickering:
				JobStatusUpdater.repeatFn(false);
				spinner.stop();
				
				var o = $('#jobtable');
				var $win = $(window)
		    , $head = $('thead.header', o)
		    , isFixed = 0;
			  o.find('thead.header-copy').width($head.width());
			  o.find('thead.header > tr > th').each(function (i, h) {
			    var w = $(h).width();
			    o.find('thead.header-copy> tr > th:eq('+i+')').width(w)
			  });
			  
			},
			error: function(result) {
				console.log(result);
				spinner.stop();
			}
		});
	}
}

function stringToBoolean(string) {
	if(!string) return false;
	switch(string.toLowerCase()){
		case "true": case "yes": case "1": return true;
		case "false": case "no": case "0": case null: return false;
		default: return false;
	}
}

/**
 * Called on jquerys DOM-ready.
 * Initializes DOM-nodes and registers events. 
 */
function doOnDomReady(
	getRunningAndRecentlyFinishedJobsLink,
	cancelJobRunLink,
	getLastRunLink,
	showOnlyActiveJobs,
	nextExecutionLink,
	listLink){
	
	$('#updateHints').popover();
	$('#checkAll').on('click', function() {
		$('.jobCheckbox').filter(function (index, elem) { return $(elem).parent().parent().is(':visible') }).prop('checked', this.checked);
	});

	// pass along filter settings when sorting columns:
	$('thead a').click(function(e) {
		$('form').attr('action', $(this).attr('href')).submit();
		return false;
	});

	if ($('#filterTags').size() == 0) {
		$('#filterTags').hide();
	} else {
		$('#filterTags').chosen({ no_results_text: '' }).change(filterJobList);
	}
	
	var filterValueJobname = getFromLocalStorage('de.iteratec.osm.job.list.filters.jobname');
	var filterValueJobgroup = getFromLocalStorage('de.iteratec.osm.job.list.filters.jobgroup');
  var filterValueLocation = getFromLocalStorage('de.iteratec.osm.job.list.filters.location');
  var filterValueSkript = getFromLocalStorage('de.iteratec.osm.job.list.filters.skript');
  var filterValueBrowser = getFromLocalStorage('de.iteratec.osm.job.list.filters.browser');
	
	var filterValueCheckedJobs = stringToBoolean(getFromLocalStorage('de.iteratec.osm.job.list.filters.checkedjobs'));
	var filterValueInactiveJobs = stringToBoolean(getFromLocalStorage('de.iteratec.osm.job.list.filters.inactivejobs'));
	var filterValueHighlightedJobs = stringToBoolean(getFromLocalStorage('de.iteratec.osm.job.list.filters.highlightedjobs'));
	var filterValueRunningJobs = stringToBoolean(getFromLocalStorage('de.iteratec.osm.job.list.filters.runningjobs'));
	
	if(filterValueJobname != null) $('#filterByLabel').val(filterValueJobname);
	if(filterValueJobgroup != null) $('#filterByJobGroup').val(filterValueJobgroup);
  if(filterValueLocation != null) $('#filterByLocation').val(filterValueLocation);
  if(filterValueSkript != null) $('#filterBySkript').val(filterValueSkript);
  if(filterValueBrowser != null) $('#filterByBrowser').val(filterValueBrowser);
	
	if(filterValueCheckedJobs != null) $('#filterCheckedJobs').prop("checked",filterValueCheckedJobs);
	if(filterValueInactiveJobs != null) $('#filterInactiveJobs').prop("checked",filterValueInactiveJobs);
	if(filterValueHighlightedJobs != null) $('#filterHighlightedJobs').prop("checked",filterValueHighlightedJobs);
	if(filterValueRunningJobs != null) $('#filterRunningJobs').prop("checked",filterValueRunningJobs);
	
	initTable(nextExecutionLink);
	
	JobStatusUpdater.initLoop(
			getRunningAndRecentlyFinishedJobsLink,
			cancelJobRunLink,
			getLastRunLink,
			5000);
	 
	$('.table-fixed-header').fixedHeader();
	
	
	// when only active jobs were loaded, either issue an AJAX request to load all jobs
	// immediately (if "Show inactive jobs" checkbox is checked) or set an event handler
	// to load all jobs asynchronously when checking the checkbox for the first time.
	var inactiveJobLoader = new InactiveJobLoader(listLink, nextExecutionLink);
	if(showOnlyActiveJobs){
		if ($('#filterInactiveJobs').prop('checked')) {
			inactiveJobLoader.loadJobs();
		} else {
			$('#filterInactiveJobs').one('click', inactiveJobLoader.loadJobs);
		}
	}
	
}

$(window).load(function() {
	if ($("tr.highlight").length > 0) {
		$('html, body').animate({
			scrollTop : $("tr.highlight").offset().top - 100
		}, 1000);
	}
});
