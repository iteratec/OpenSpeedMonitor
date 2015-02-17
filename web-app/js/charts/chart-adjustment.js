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
    var html = d3.select("svg")
          .node().parentNode.innerHTML;
    if(!!navigator.userAgent.match(/Trident/)){
      html = html.replace(/ xmlns=\"http:\/\/www.w3.org\/2000\/svg\"/, '');
    }
    var imgsrc = 'data:image/svg+xml;base64,'+ btoa(html);
    
    var canvas = document.querySelector("canvas"),
      context = canvas.getContext("2d");
    
    var imageRenderDeferrer = $.Deferred();
    var image=document.createElement("img");
    image.setAttribute('id', 'newI');
    document.body.appendChild(image);
    image.onload = function() {console.log("rka");
      canvas.width = image.width;
      canvas.height = image.height;
      imageRenderDeferrer.resolve();
    };
//    $('#newI').load(function(){ imageRenderDeferrer.resolve(); }).attr('src', imgsrc);

//    image.onerror=function(msg, url, linenumber){console.log('Error message: '+msg+'\nURL: '+url+'\nLine Number: '+linenumber);}
//    image.onerror=function(evt){console.log('rk1');console.log(evt);}
    image.onerror = function (errorMsg, url, lineNumber, column, errorObj) {
      console.log(errorMsg);
      console.log('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
      + ' Column: ' + column + ' StackTrace: ' +  errorObj);
  }
//    $('#newI').on('load', function() {
//      // do stuff on success
//      console.log("rk1");
//    })
//    .on('error', function() {
//      // do stuff on smth wrong (error 404, etc.)
//      console.log("rk2");
//    })
//    .each(function() {
//        if(this.complete) {
//          $(this).load();
//        } else if(this.error) {
//          $(this).error();
//        }
//    });
    
    image.src = imgsrc;console.log("rkx");
    $.when(imageRenderDeferrer).then(function(){console.log("rkb");
      context.drawImage(image, 0, 0);
      try {
        var canvasdata = canvas.toDataURL("image/png");

        var a = document.getElementById("converteddataurl");
//        var a = document.createElement("a");
//        a.download = "sample.png";
        
        var currentdate = new Date(); 
        var datetime = "" + currentdate.getFullYear() + "-"  
                        + (currentdate.getMonth()+1)  + "-" 
                        + currentdate.getDate() + "_"
                        + currentdate.getHours() + "-"  
                        + currentdate.getMinutes() + "-" 
                        + currentdate.getSeconds() + "";
        
        newA = document.createElement('a');
        newA.setAttribute('href', '');
        newA.setAttribute('id', 'converteddataurl');
        newA.setAttribute('style', "display:none");
        document.body.appendChild(newA);
        
        var a = document.getElementById("converteddataurl");
        
//        var newFileName = self.constructFileName(fileType);
//        a.download = newFileName;
        a.download = "osm_csi_" + datetime + ".png";
        a.href = canvasdata;
        a.click();
        removeObjectFromDom("#converteddataurl");
      } 
      catch(err) {
        // handle IE -> allow CORS
      }
    });
  });
  
  //  convert and download highcharts
//  d3.select("#dia-save-chart-as-png").on("click", function(){
//    var html = d3.select("svg")
//          .node().parentNode.innerHTML;
//    if(!!navigator.userAgent.match(/Trident/)){
//      html = html.replace(/ xmlns=\"http:\/\/www.w3.org\/2000\/svg\"/, '');
//    }
//    var imgsrc = 'data:image/svg+xml;base64,'+ btoa(html);
//    
//    var retVal = self.prepareNewBlankCanvas();
//    var canvas = retVal.canvas;
//    var context = retVal.ctx;
//
//    var image = new Image;
//    image.src = imgsrc;
//    image.onload = function() {
//      context.drawImage(image, 0, 0);
//      try {
//        var canvasdata = canvas.toDataURL("image/png");
//
//        var pngimg = '<img src="'+canvasdata+'">'; 
//          d3.select("#pngdataurl").html(pngimg);
//
//        var a = document.getElementById("converteddataurl");
////        var a = document.createElement("a");
////        a.download = "sample.png";
//        
//        var currentdate = new Date(); 
//        var datetime = "" + currentdate.getFullYear() + "-"  
//                        + (currentdate.getMonth()+1)  + "-" 
//                        + currentdate.getDate() + "_"
//                        + currentdate.getHours() + "-"  
//                        + currentdate.getMinutes() + "-" 
//                        + currentdate.getSeconds() + "";
//        a.download = "osm_csi_" + datetime + ".png";
//        a.href = canvasdata;
//        a.click();
//      } 
//      catch(err) {
//        // handle IE -> allow CORS
//      }
//    };
//  });
    
//    d3.select("#dia-save-chart-as-png").on("click", function(){
//      console.log("rkrkrk2");
//      //merge all canvases into one
//      
//      var retVal = self.prepareNewBlankCanvas();
//      var canvas = retVal.canvas;
//      var context = retVal.ctx;
//      
//      var html = d3.select("svg").node().parentNode.innerHTML;console.log(html);
//      if(!!navigator.userAgent.match(/Trident/)){
//        html = html.replace(/ xmlns=\"http:\/\/www.w3.org\/2000\/svg\"/, '');
//      }console.log(html);
//      var imgsrc = 'data:image/svg+xml;base64,'+ btoa(html);
////      
////      var canvas = document.querySelector("canvas"),
////        context = canvas.getContext("2d");
//      console.log("rkrkrk3");
//      var image = new Image;
//      image.src = imgsrc;
//      image.onload = function() {console.log("rkrkrk4");
//        context.drawImage(image, 0, 0);
//        try {console.log("rkrkrk5");
//          self.downloadCanvas(canvas, "png");
//          self.removeObjectFromDom("#canvas_everything_merged");
//        } 
//        catch(err) {} // handle IE 
//      };
//    });

//    d3.select("#dia-save-chart-as-png").on("click", function(){
//      deferrerCollection = new Array();    
//      deferrerCollection.push($.Deferred());
//      self.renderSvgElementOnNewCanvasWithDelay($('svg').first(), 'canvas_everything_merged', deferrerCollection[deferrerCollection.length - 1]);
//    
//      $.when.apply($, deferrerCollection).then(function(){
//  
//        var canvas = document.querySelector("#canvas_everything_merged");
//          
//        //convert to image
//        try {
//          self.downloadCanvas(canvas, "png");
//          self.removeObjectFromDom("#canvas_everything_merged");
//        }
//        catch(err) {} // handle IE        
//      });
//    });
  }

  this.renderSvgElementOnNewCanvasWithDelay = function(svgElement, newCanvasId, deferrer) {
    var html2 = svgElement.clone().wrapAll("<div/>").parent().html();
    html2 = html2.replace(/<svg (.*?)>/, '<svg xmlns="http://www.w3.org/2000/svg" $1>');
    html2 = html2.replace(/top: -20px/, 'top: 0');
    
    if(!!navigator.userAgent.match(/Trident/)){
      html2 = html2.replace(/ xmlns=\"http:\/\/www.w3.org\/2000\/svg\"/, '');
    }console.log(html2);
    var imgsrc = 'data:image/svg+xml;base64,'+ btoa(html2);
    var img = '<img src="'+imgsrc+'">';
    
    var input = document.createElement("textarea");
    input.innerHTML = imgsrc;
    document.body.appendChild(input);
    
    var canvas = document.createElement('canvas');
    canvas.setAttribute('id', newCanvasId);
    canvas.setAttribute('style', "display:none");
    canvas.width = 3000;
    canvas.height = 5000;
    document.body.appendChild(canvas);
    
    var ctx2 = canvas.getContext("2d");
    
    var image2 = new Image;
    image2.src = imgsrc;console.log("rk1");console.log(imgsrc);
    image2.onload = function() {console.log("rk2");
      ctx2.drawImage(image2, 0, 0);
      deferrer.resolve();
    }
  }
  
  this.constructFileName = function(fileType) {
    var currentdate = new Date(); 
    var datetime = "" + currentdate.getFullYear() + "-"  
                    + (currentdate.getMonth()+1)  + "-" 
                    + currentdate.getDate() + "_"
                    + currentdate.getHours() + "-"  
                    + currentdate.getMinutes() + "-" 
                    + currentdate.getSeconds() + "";
    
    var curAreaName = (window.location.pathname.indexOf("eventResultDashboard") > -1) ? "event" : "csi";
    return ("osm_" + curAreaName + "_" + datetime + "." + fileType + "");
  }

  this.prepareNewBlankCanvas = function() {
    var canvas = document.createElement('canvas');
    canvas.setAttribute('id', 'canvas_everything_merged');
    canvas.setAttribute('style', "display:none");
    canvas.width = 3000;
    canvas.height = 5000;
    document.body.appendChild(canvas);
    var ctx = canvas.getContext("2d");
    ctx.clearRect(0, 0, canvas.width, canvas.height);
  
    ctx.canvas.width  = $( "svg" ).first().width();
    ctx.canvas.height = $( "svg" ).first().height();
    ctx.globalCompositeOperation = "destination-under";
    ctx.fillStyle = '#fff';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    return {
      canvas: canvas,
      ctx: ctx
    };
  }

  this.removeObjectFromDom = function(objectId) {
    useMe = document.querySelector(objectId);
    useMe.parentNode.removeChild(useMe);
  }

  this.downloadCanvas = function(canvas, fileType) { // currently, most browsers only support toDataURL with mimeTypes 'jpeg' and 'png'
    var canvasdata = canvas.toDataURL("image/" + fileType + "");      
    var pngimg = '<img src="'+canvasdata+'">'; 
    
    newA = document.createElement('a');
    newA.setAttribute('href', '');
    newA.setAttribute('id', 'converteddataurl');
    newA.setAttribute('style', "display:none");
    document.body.appendChild(newA);
    
    var a = document.getElementById("converteddataurl");
    
    var newFileName = self.constructFileName(fileType);
    a.download = newFileName;
    a.href = canvasdata;
    a.click();
    removeObjectFromDom("#converteddataurl");
  }
  
  this.initialize(args);
}