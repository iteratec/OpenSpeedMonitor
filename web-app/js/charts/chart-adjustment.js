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
 * Initialization and event-handlers for chart-adjustment-accordion.
 */
$(document).ready(function() {
	var chartAdjustmentIsOnPage = ($('#collapseAdjustment').length > 0); 
	if(chartAdjustmentIsOnPage){
		var adjuster
		if (CHARTLIB.toUpperCase() == "HIGHCHARTS"){
			adjuster = new HighchartAdjuster();
		} else if (CHARTLIB.toUpperCase() == "RICKSHAW"){
			// done in rickshawChartCreation.js
		} else {
			$('#collapseAdjustment').parent().parent().parent().remove();
		}
	}
});

function HighchartAdjuster() {
	this.initialize = function() {
		var multipleYAxis = false;
		if($('#dia-y-axis-min').length > 0 && $('#dia-y-axis-max').length > 0){
			multipleYAxis = true;
		}
		
		//initialize
		$('#dia-width').val(chart.chartWidth);
		$('#dia-height').val(chart.chartHeight);
		if (multipleYAxis) {
			if($('#dia-y-axis-min').val().length == 0) $('#dia-y-axis-min').val(chart.yAxis[0].dataMin);
			if($('#dia-y-axis-max').val().length == 0) $('#dia-y-axis-max').val(chart.yAxis[0].dataMax);
		}
		$(".collapse").collapse('hide');
		//register events
		$('#to-enable-marker').bind('change', function(){
			var toEnableMarkers = $(this).is(':checked');
			var countAllDatapoints = 0;
			jQuery.each(
					chart.series, 
					function (i, series) { 
						countAllDatapoints+=series.data.length;
					});
			if (toEnableMarkers && countAllDatapoints > 10000) {
				window.alert("Too many datapoints to show and label them!");
				$(this).prop('checked', false);
			} else {
				jQuery.each(
						chart.series, 
						function (i, series) { 
							series.update(
									{
										marker:{enabled: toEnableMarkers},
										dataLabels: {enabled: toEnableMarkers}
									}
							)
						}
				);
			}
		});
		$('#dia-change-chartsize').bind('click', function(){
			var diaWidth = $('#dia-width').val()
			var diaHeight = $('#dia-height').val()
			var maxWidth = 5000
			var maxHeight = 3000
			if ($.isNumeric(diaWidth) && $.isNumeric(diaHeight)
					&& parseInt(diaWidth)>0 && parseInt(diaWidth)<=maxWidth
					&& parseInt(diaHeight)>0 && parseInt(diaHeight)<=maxHeight) {
				chart.setSize($('#dia-width').val(), $('#dia-height').val());
			}else{
				window.alert("Width and height of diagram has to be numeric values and maximum is 5.000 x 3.000 pixel!");
			}
		});
		if (multipleYAxis){
			$('#dia-change-yaxis').bind('click', function(){
				var diaYAxisMin = $('#dia-y-axis-min').val()
				var diaYAxisMax = $('#dia-y-axis-max').val()
				if ($.isNumeric(diaYAxisMin) && $.isNumeric(diaYAxisMax)
						&& parseInt(diaYAxisMax) > parseInt(diaYAxisMin)) {
					chart.yAxis[0].setExtremes(diaYAxisMin, diaYAxisMax);
				}else{
					window.alert("Minimum and maximum of Y-Axis has to be numeric values and maximum must be greater than minimum!");
				}
			});
		}
		$('#dia-title').bind('input', function(){
			chart.setTitle({text: $(this).val()});
		});
	}
	this.initialize();
}