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

String.prototype.unescapeHtml = function () {
    var temp = document.createElement("div");
    temp.innerHTML = this;
    var result = temp.childNodes[0].nodeValue;
    temp.removeChild(temp.firstChild);
    return result;
}

function parseDate(date) {
  return  Date.UTC(date.substr(0, 4), date.substr(5, 2) - 1, date.substr(8, 2), date.substr(11, 2), date.substr(14, 2), date.substr(17, 2));
}

function createSplineChart(div, title, yType, data, maxValue, lineType, measurementUnit, exportUrl) {
	if (lineType == "point") {
		plotOptions = {};
	} else {
		plotOptions = {
            spline: {
                lineWidth: 2,
                states: {
                    hover: {
                        lineWidth: 3
                    }
                },
                marker: {
                    enabled: false,
                    states: {
                        hover: {
                            enabled: true,
                            symbol: 'circle',
                            radius: 5,
                            lineWidth: 1
                        }
                    }
                }
            }
    	}
	}
        	
	var chart = new Highcharts.Chart({
            chart: {
                renderTo: div,
                zoomType: 'xy',
                type: 'spline'
            },
            title: {
                text: title
            },
            xAxis: {
                type: 'datetime',
                min: xFrom,
                max: xUntil
            },
            yAxis: {
                title: {
                    text: yType
                },
                min: 0,
                max: maxValue
            },
            tooltip: {
                crosshairs: [true, true],
         		formatter: function() {
                    var testAgentPart = this.point.testAgent ? '<br/>Test Agent: ' + this.point.testAgent : '';
                    return '<b>'+ this.series.name +'</b><br/>'+
				        Highcharts.dateFormat('%e. %b %H:%M %Z', this.x) +':- '+ this.y +' ' + measurementUnit + testAgentPart; }

            },
            plotOptions: plotOptions,
            exporting: {
            	scale: 1,
            	sourceWidth: 900,
            	sourceHeight: 500,
            	url: exportUrl
            },
            
            series: data
    });
}

function createLineChart(
		div, 
		title, 
		yType, 
		data, 
		yAxisMax, 
		lineType, 
		measurementUnit, 
		xAxisMin, xAxisMax, 
		markerEnabled, 
		dataLabelsActivated, 
		yAxisScalable) {
	
	createLineChart(
			div, 
			title, 
			yType, 
			data, 
			yAxisMax, 
			lineType, 
			measurementUnit, 
			xAxisMin, xAxisMax, 
			markerEnabled, 
			dataLabelsActivated, 
			yAxisScalable,
			0,
			2,
			false);
}
function createLineChart(
		div, 
		title, 
		yType, 
		data, 
		yAxisMax, 
		lineType, 
		measurementUnit, 
		xAxisMin, xAxisMax, 
		markerEnabled, 
		dataLabelsActivated, 
		yAxisScalable,
		yAxisMin,
		lineWidthGlobal,
		optimizeForExport) {
	createLineChart(
			div, 
			title, 
			yType, 
			data, 
			yAxisMax, 
			lineType, 
			measurementUnit, 
			xAxisMin, xAxisMax, 
			markerEnabled, 
			dataLabelsActivated, 
			yAxisScalable,
			yAxisMin,
			lineWidthGlobal,
			optimizeForExport,
			0,
			'http://export.highcharts.com')
}

function createLineChart(
		div, 
		title, 
		yType, 
		data, 
		yAxisMax, 
		lineType, 
		measurementUnit, 
		xAxisMin, xAxisMax, 
		markerEnabled, 
		dataLabelsActivated, 
		yAxisScalable,
		yAxisMin,
		lineWidthGlobal,
		optimizeForExport,
		heightOfChart,
		exportUrl) {
	
	Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });
	Highcharts.dateFormats = {
	        W: function (timestamp) {
	            var date = new Date(timestamp),
	                day = date.getUTCDay() == 0 ? 7 : date.getUTCDay(),
	                dayNumber;
	            date.setDate(date.getUTCDate() + 4 - day);
	            dayNumber = Math.floor((date.getTime() - new Date(date.getUTCFullYear(), 0, 1, -6)) / 86400000);
	            var ret = 1 + Math.floor(dayNumber / 7); 
	            /*alert('date='+date+' | day='+day+' | dayNumber='+dayNumber+'\nret='+ret)*/
	            return ret;
	            
	        }
	    }
	if (lineType == "point") {
		plotOptions = {};
	} else {
		plotOptions = {
			line: {
				lineWidth: lineWidthGlobal,
				marker: {
					enabled: markerEnabled
				},
				dataLabels: {
                    enabled: dataLabelsActivated,
                    y: -35,
                    x: 6,
                    rotation: 270,
                    style: {
                        fontSize: '13pt',
                        color: '#b3b3b3',
                        fontWeight:'bold'
                    }
                }
			},
            spline: {
                lineWidth: 2,
                states: {
                    hover: {
                        lineWidth: 3
                    }
                },
                marker: {
                    enabled: false,
                    states: {
                        hover: {
                            enabled: true,
                            symbol: 'circle',
                            radius: 5,
                            lineWidth: 1
                        }
                    }
                }
            },
			series: {
				turboThreshold: 100000
			}
    	}
	}
	if(optimizeForExport){
		createChartForExport(
				div, 
    			title, 
    			yType, 
    			data, 
    			measurementUnit, 
    			xAxisMin, xAxisMax, 
    			yAxisMin, yAxisMax,
    			exportUrl,
				heightOfChart);
	}else{
		if(!yAxisScalable){
			createChartWithFixYAxis(
					div, 
	    			title, 
	    			yType, 
	    			data, 
	    			measurementUnit, 
	    			xAxisMin, xAxisMax, 
	    			yAxisMin, yAxisMax,
	    			heightOfChart, exportUrl);
	    }else{
	    	createChartWithScalableYAxis(
	    			div, 
	    			title, 
	    			yType, 
	    			data, 
	    			measurementUnit, 
	    			xAxisMin, xAxisMax, 
	    			yAxisMin, yAxisMax,
	    			heightOfChart, exportUrl);
	    }		
	}
}

function clearUnusedAxes(axes, data) {
	var cleanedAxes=new Array();
	
	for ( var i = 0; i < data.length; i++) {
		
		if($.inArray(axes[data[i]["yAxis"]], cleanedAxes)==-1) {
			cleanedAxes.push(axes[data[i]["yAxis"]]);
		}
		data[i]["yAxis"]=$.inArray(axes[data[i]["yAxis"]], cleanedAxes);
	}
	
	return cleanedAxes;
	
}

function createLineChartWithMultipleYAxis(
		div, 
		title, 
		yTypes, 
		data, 
		yAxisMaxs, 
		lineType, 
		measurementUnits, 
		xAxisMin, xAxisMax, 
		markerEnabled, 
		dataLabelsActivated, 
		yAxisScalable,
		yAxisMin,
		lineWidthGlobal,
		optimizeForExport,
		highChartsTurboThreshold,
		exportUrl,
		heightOfChart) {
		
	
	yTypes=clearUnusedAxes(yTypes, data);
	
	Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });
	Highcharts.dateFormats = {
	        W: function (timestamp) {
	            var date = new Date(timestamp),
	                day = date.getUTCDay() == 0 ? 7 : date.getUTCDay(),
	                dayNumber;
	            date.setDate(date.getUTCDate() + 4 - day);
	            dayNumber = Math.floor((date.getTime() - new Date(date.getUTCFullYear(), 0, 1, -6)) / 86400000);
	            var ret = 1 + Math.floor(dayNumber / 7); 
	            /*alert('date='+date+' | day='+day+' | dayNumber='+dayNumber+'\nret='+ret)*/
	            return ret;
	            
	        }
	    }
	if (lineType == "point") {
		plotOptions = {};
	} else {
		plotOptions = {
			line: {
				lineWidth: lineWidthGlobal,
				marker: {
					enabled: markerEnabled
				},
				dataLabels: {
                    enabled: dataLabelsActivated,
                    y: -50,
                    x: 6,
                    rotation: 270,
                    style: {
                        fontSize: '14pt',
                        color: '#b3b3b3',
                        fontWeight:'bold'
                    }
                }
			},
            spline: {
                lineWidth: 2,
                states: {
                    hover: {
                        lineWidth: 3
                    }
                },
                marker: {
                    enabled: false,
                    states: {
                        hover: {
                            enabled: true,
                            symbol: 'circle',
                            radius: 5,
                            lineWidth: 1
                        }
                    }
                }
            },
			series: {
				turboThreshold: highChartsTurboThreshold
			}
    	}
	}
	
		if(!yAxisScalable){
			createChartWithFixYAxisForMultipleYAxis(
					div, 
	    			title, 
	    			yTypes, 
	    			data, 
	    			measurementUnits, 
	    			xAxisMin, xAxisMax, 
	    			yAxisMin, yAxisMaxs,
	    			exportUrl,
					heightOfChart);
	    }else{
	    	createChartWithMultipleScalableYAxis(
	    			div, 
	    			title, 
	    			yTypes, 
	    			data, 
	    			measurementUnits, 
	    			xAxisMin, xAxisMax, 
	    			yAxisMin, yAxisMaxs,
	    			exportUrl,
					heightOfChart);
	    }		
	
}

function createChartForExport(
		div, 
		title, 
		yType, 
		data, 
		measurementUnit, 
		xAxisMin, xAxisMax, 
		yAxisMin, yAxisMax,
		exportUrl,
		heightOfChart){
	window.chart = new Highcharts.Chart({
		chart: {
			renderTo: div,
			zoomType: 'xy',
			type: 'line',
			height: heightOfChart
		},
		title: {
			text: title,
			style: {
                fontSize: '20pt',
                fontWeight:'bold'
            }
		},
		legend: {
			enabled: true,
			margin: 25
		},
		xAxis: {
			type: 'datetime',
			min: xAxisMin,
			max: xAxisMax,
			labels: {
				rotation: 90,
				y: 30,
// format: '{value: %d.%m.%Y %H:%M:%S KW %W %Y}',
				format: '{value: %d.%m}',
				style: {
                    fontSize: '12pt',
                    color: '#273b4f'
                }
			}
		},
		yAxis: {
			title: {
				text: null,
				style: {
                    fontSize: '14pt',
                    fontWeight:'bold'
                }
			},
			min: yAxisMin,
			max: yAxisMax,
			labels: {
				format: '{value} %',
				style: {
                    fontSize: '12pt',
                    color: '#273b4f'
                }
			}
		},
		tooltip: {
            crosshairs: [true, true],
			formatter: function() {
                var testAgentPart = this.point.testAgent ? '<br/>Test Agent: ' + this.point.testAgent : '';
				return '<b>'+ this.series.name +'</b><br/>'+
				    Highcharts.dateFormat('%e. %b %H:%M', this.x) +' | '+ this.y +' ' + measurementUnit + testAgentPart; }
		
		},
		plotOptions: plotOptions,
		exporting: {
			scale: 1,
			sourceWidth: 1418,
			sourceHeight: 557,
			url: exportUrl
		},
		
		series: data
	});
}

function createChartWithFixYAxis(
		div, 
		title, 
		yType, 
		data, 
		measurementUnit, 
		xAxisMin, xAxisMax, 
		yAxisMin, yAxisMax,
		heightOfChart, exportUrl){
	window.chart = new Highcharts.Chart({
		chart: {
			renderTo: div,
			zoomType: 'xy',
			type: 'line',
			height: heightOfChart
		},
		title: {
			text: title
		},
		xAxis: {
			type: 'datetime',
			min: xAxisMin,
			max: xAxisMax
		},
		yAxis: {
			title: {
				text: yType
			},
			min: yAxisMin,
			max: yAxisMax
		},
		tooltip: {
            crosshairs: [true, true],
			formatter: function() {
                var testAgentPart = this.point.testAgent ? '<br/>Test Agent: ' + this.point.testAgent : '';
				return '<b>'+ this.series.name +'</b><br />'+
				    Highcharts.dateFormat('%e.%m.%Y %H:%M', this.x) +' | '+ this.y +' ' + measurementUnit + testAgentPart; }
		},
		plotOptions: plotOptions,
		exporting: {
			scale: 1,
			sourceWidth: 900,
			sourceHeight: 500,
			url: exportUrl
		},
		series: data
	});	
	
}

function createChartWithFixYAxisForMultipleYAxis(
		div, 
		title, 
		yTypes, 
		data, 
		measurementUnit, 
		xAxisMin, xAxisMax, 
		yAxisMin, yAxisMaxs,
		exportUrl,
		heightOfChart){
	window.chart = new Highcharts.Chart({
		chart: {
			renderTo: div,
			zoomType: 'xy',
			type: 'line',
			height: heightOfChart
		},
		title: {
			text: title
		},
		xAxis: {
			type: 'datetime',
			min: xAxisMin,
			max: xAxisMax
		},
		yAxis: getYAxisArrayFix(yTypes, yAxisMaxs, yAxisMin),
		tooltip: {
            crosshairs: [true, true],
			formatter: function() {
                var testAgentPart = this.point.testAgent ? '<br/>Test Agent: ' + this.point.testAgent : '';
				return '<b>'+ this.series.name +'</b><br />'+
				    Highcharts.dateFormat('%e.%m.%Y %H:%M', this.x) +' | ' + this.y + testAgentPart; }
		},
		plotOptions: plotOptions,
		exporting: {
			scale: 1,
			sourceWidth: 900,
			sourceHeight: 500,
			url: exportUrl
		},
		series:  data
	});	
	
}



function createChartWithScalableYAxis(
		div, 
		title, 
		yType, 
		data, 
		measurementUnit, 
		xAxisMin, xAxisMax, 
		yAxisMin, yAxisMax,
		heightOfChart, exportUrl){
	window.chart = new Highcharts.Chart({
		chart: {
			renderTo: div,
			zoomType: 'xy',
			type: 'line',
			height: heightOfChart
		},
		title: {
			text: title
		},
		xAxis: {
			type: 'datetime',
			min: xAxisMin,
			max: xAxisMax
		},
		yAxis: {
			title: {
				text: yType
			}
		},
		tooltip: {
            crosshairs: [true, true],
			formatter: function() {
                var testAgentPart = this.point.testAgent ? '<br/>Test Agent: ' + this.point.testAgent : '';
				return '<b>'+ this.series.name +'</b><br />'+
				    Highcharts.dateFormat('%e. %b %H:%M', this.x) +' | '+ this.y +' ' + measurementUnit + testAgentPart; }
		
		},
		plotOptions: plotOptions,
		exporting: {
			scale: 1,
			sourceWidth: 900,
			sourceHeight: 500,
			url: exportUrl

		},
		
		series: data
	});
}

function createChartWithMultipleScalableYAxis(
		div, 
		title, 
		yTypes, 
		data, 
		measurementUnit, 
		xAxisMin, xAxisMax, 
		yAxisMin, yAxisMax,
		exportUrl,
		heightOfChart){
	
	
	window.chart = new Highcharts.Chart({
		chart: {
			renderTo: div,
			zoomType: 'xy',
			type: 'line',
			height: heightOfChart
		},
		title: {
			text: title
		},
		xAxis: {
			type: 'datetime',
			min: xAxisMin,
			max: xAxisMax
		},
		yAxis: getYAxisArrayScalable(yTypes),
		tooltip: {
            crosshairs: [true, true],
			formatter: function() {
                var testAgentPart = this.point.testAgent ? '<br/>Test Agent: ' + this.point.testAgent : '';
                return '<b>'+ this.series.name +'</b><br />'+
				    Highcharts.dateFormat('%e. %b %H:%M', this.x) +' | '+ this.y +' ' + measurementUnit + testAgentPart; }
		
		},
		plotOptions: plotOptions,
		exporting: {
			scale: 1,
			sourceWidth: 900,
			sourceHeight: 500,
			url: exportUrl
		},
		
		series: data
	});
	
	
}

function getYAxisArrayFix(yTypes, yAxisMaxs, yAxisMin) {

	var arr = new Array();
	var yAxisMaxArr = yAxisMaxs.split(';');
	/*
	 * alert("highchart-taglib.js : getYAxisArrayFix():"+"yTypeArray: " +
	 * yTypesArr.length);
	 */

	for ( var i = 0; i < yTypes.length; i++) {
		
		if (yTypes.length != yAxisMaxArr.length || yAxisMaxArr[i]=="null") {
			yAxisMaxArr[i] = null;
		}
		
		var yType=yTypes[i];
		
		var formatterFunc=function (label) {
			return function() {
					var returner;

					if (label["divisor"]!=null && label["divisor"]!=0) {
						returner = (parseFloat(this.value) / parseFloat(label["divisor"]));
					} else {
						returner = this.value;
					}

					return returner;
				
			}
		}

		arr[i] = {
			labels : {
				formatter : formatterFunc(yType),
				style : {
					color :yType["color"]
				}
			},
			title : {
				text : yType["label"].unescapeHtml()+" "+" [" + yType["unit"] + "]",
				style : {
					color : yType["color"]
				}
			},
			min : yAxisMin,
			max : yAxisMaxArr[i],
			opposite : getOppositeStatus(yType)
		};
	}
	return arr;

}

function getOppositeStatus(yType) {
	var opposite=false;
	
	if(yType["labelPosition"]==1) {
		opposite=true;
	}
	
	return opposite;
}

function getYAxisArrayScalable(yTypes) {
	
	var arr = new Array();
	
		for (var i = 0; i < yTypes.length; i++) {
			arr[i] = {
					labels: {
				         formatter: function() {
				             return this.value +'°C';
				         },
				         style: {
				             color: '#89A54E'
				         }
				     },
					title: {
						text: yTypes[i],
				         style: {
				        	 color: '#89A54E'
				         }
					}
					};
		}
	

	return arr;

}

function dataToMultipleSeries(data) {
	var arr = new Array();
	var i = 0;
	for (var key in data) {
		arr[i] = {
				name: key,
				yAxis: i,
				data: data[key]}
		i++;
	}
	
	return arr;
}
