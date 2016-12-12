//= require iteratecChartRickshaw.js
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
function doOnDomReady(noResultsTextForChosenSelects) {

    $("input[name='aggrGroupAndInterval']").change(setFilterElementsVisibility);

    $("input[name='aggrGroupAndInterval']:checked").each(setFilterElementsVisibility);

    $("#override-long-processing-time").click(function () {
        $("#overwriteWarningAboutLongProcessingTime").prop('checked', true);
        $("#chart-submit").submit();
    });


    // Toggle Buttons
    $("#chart-toggle").click(function () {
        $("#csi-table").hide();
        $("#chartbox").fadeIn();
        $(this).addClass("active").siblings().removeClass("active");
    });
    $("#table-toggle").click(function () {
        $("#chartbox").hide();
        $("#csi-table").fadeIn();
        $(this).addClass("active").siblings().removeClass("active")
    });

    if ($("#chartbox").length > 0) {
        createGraph();
    }

}
/**
 * show or hide filter-elements, depending on aggregator type
 */
var setFilterElementsVisibility = function () {

    var selectedAggrGroupAndInterval = $(this).val();
    if (selectedAggrGroupAndInterval == 'measured_event') {
        $("#filter-complete-tabbable").fadeIn();
        $("#filter-browser-and-location").fadeIn();
		$("#filter-connectivityprofile").fadeIn();
        $("#filter-navtab-browser-and-location").fadeIn();
		$("#filter-navtab-connectivityprofile").fadeIn();
        $("#filter-measured-event").fadeIn();
    }
    else {
        $("#filter-browser-and-location").fadeOut();
		$("#filter-connectivityprofile").fadeOut();
        $("#filter-navtab-browser-and-location").fadeOut();
		$("#filter-navtab-connectivityprofile").fadeOut();
        $("#filter-measured-event").fadeOut();
        $('#filter-navtab-page a').click();
    }

    if (selectedAggrGroupAndInterval == 'weekly_page' || selectedAggrGroupAndInterval == 'daily_page') {
        $("#filter-complete-tabbable").fadeIn();
    }

    if (selectedAggrGroupAndInterval == 'weekly_shop' || selectedAggrGroupAndInterval == 'daily_shop'){
        $("#filter-complete-tabbable").fadeOut();
    }

    if (selectedAggrGroupAndInterval == 'daily_system' || selectedAggrGroupAndInterval == 'weekly_system') {
        $("#filter-navtab-jobGroup").hide();
        $("#filter-complete-tabbable").hide();
        $("#filter-navtab-csiSystem").show();
    } else {
        $("#filter-navtab-jobGroup").show();
        $("#filter-navtab-csiSystem").hide();
    }
};
