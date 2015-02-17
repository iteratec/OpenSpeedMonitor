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
    var exporter
		if (CHARTLIB.toUpperCase() == "HIGHCHARTS"){
			adjuster = new HighchartAdjuster();
			exporter = new  HighchartExporter();
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
		$("#collapseAdjustment").collapse('hide');
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
      var minWidth = 540
      var maxHeight = 3000
      if ($.isNumeric(diaWidth) && $.isNumeric(diaHeight)
          && parseInt(diaWidth)>0 && parseInt(diaWidth)<=maxWidth && parseInt(diaWidth) >= minWidth
          && parseInt(diaHeight)>0 && parseInt(diaHeight)<=maxHeight) {
        chart.setSize($('#dia-width').val(), $('#dia-height').val());
      }else{
        window.alert("Width and height of diagram has to be numeric values. Maximum is 5.000 x 3.000 pixels, minimum width is 540 pixels.");
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

function HighchartExporter(args) {
  var self = this;
  
  this.initialize = function(args) {
    
    //convert and download highcharts
    d3.select("#dia-save-chart-as-png").on("click", function(){
      var retVal = prepareNewBlankCanvas("svg");
      var canvas = retVal.canvas;
      
      var html = d3.select("svg").node().parentNode.innerHTML;
      html = html.replace(/<tspan x=\".*?\">/gi, '');
      html = html.replace(/<\/tspan>/gi, '');
      canvg(canvas, html);
  
      //convert to image
      try {
        downloadCanvas(canvas, "png");
        removeObjectFromDom("#canvas_everything_merged");
      } 
      catch(err) {} // handle IE  
    });
  
  }
  
  this.initialize(args);
}