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

package de.iteratec.osm.report.chart

import de.iteratec.osm.ConfigService
import de.iteratec.osm.p13n.CookieBasedSettingsService
import de.iteratec.osm.util.Constants
import de.iteratec.osm.util.OsmCookieService;

class OsmChartTagLib {

	ConfigService configService
	CookieBasedSettingsService cookieBasedSettingsService
	static namespace = "iteratec"

	def singleYAxisChart = { attrs, body ->

		List<OsmChartGraph> data = attrs["data"];
		String title = attrs["title"]
		String lineType = attrs["lineType"]
		String yType = attrs["yType"]
		String width= attrs["width"]
		String yAxisMax = attrs["yAxisMax"] ?: "null"
		String divId = attrs["divId"] ?: UUID.randomUUID().toString();
		String measurementUnit = attrs['measurementUnit'] ?: 'unkown'
		Long xAxisMin = attrs['xAxisMin'] ? Long.valueOf(attrs['xAxisMin']) : null
		Long xAxisMax = attrs['xAxisMax'] ? Long.valueOf(attrs['xAxisMax']) : null
		Boolean markerEnabled = attrs['markerEnabled']!=null ? Boolean.valueOf(attrs['markerEnabled']) : false
		Boolean dataLabelsActivated = attrs['dataLabelsActivated']!=null ? Boolean.valueOf(attrs['dataLabelsActivated']) : false
		Boolean yAxisScalable = attrs['yAxisScalable']!=null ? Boolean.valueOf(attrs['yAxisScalable']) : false
		String yAxisMin = attrs["yAxisMin"] = attrs['yAxisMin'] ?: '0'
		String lineWidthGlobal = attrs["lineWidthGlobal"] = attrs['lineWidthGlobal'] ?: '2'
		Boolean optimizeForExport = attrs['optimizeForExport']!=null ? Boolean.valueOf(attrs['optimizeForExport']) : false
		Boolean openDatapointLinksInNewWindow = attrs['openDataPointLinksInNewWindow']!=null ? Boolean.valueOf(attrs['openDataPointLinksInNewWindow'])  : true
		String exportUrl = attrs['exportUrl'] ?: grailsApplication.config.grails.de.iteratec.osm.report.chart.highchartsExportServerUrl
		
		ChartingLibrary chartLibToUse = cookieBasedSettingsService.getChartingLibraryToUse()
		log.debug("chartLibToUse while processing osm chart tgalib=${chartLibToUse}")

		if (chartLibToUse == ChartingLibrary.HIGHCHARTS) {
			String heightOfChart = attrs["heightOfChart"] ?: "${configService.getInitialChartHeightInPixels()}"
			def htmlCreater = new HighchartHtmlCreater()
			out << htmlCreater.createChartHtml(data, title, lineType, yType, width, yAxisMin, yAxisMax, divId, false, measurementUnit,
					xAxisMin, xAxisMax, markerEnabled, dataLabelsActivated, yAxisScalable, lineWidthGlobal, optimizeForExport, heightOfChart, openDatapointLinksInNewWindow, exportUrl)
		}else if (chartLibToUse == ChartingLibrary.RICKSHAW)
		{
			String heightOfChart = "${configService.getInitialChartHeightInPixels()}px"
			data.each {
				it.measurandGroup = MeasurandGroup.NO_MEASURAND
			}
			List<OsmChartAxis> highChartLabels = new LinkedList<OsmChartAxis>();
			highChartLabels.add(new OsmChartAxis(yType, MeasurandGroup.NO_MEASURAND, "",1, OsmChartAxis.LEFT_CHART_SIDE))
			
			def htmlCreater = new RickshawHtmlCreater()
			out << htmlCreater.generateHtmlForMultipleYAxisGraph(divId, data, heightOfChart, highChartLabels, title, markerEnabled)
		}else {
			throw new IllegalArgumentException("Illegal charting library: ${chartLibToUse} not contained in available charting libraries: " +
					"${grailsApplication.config.grails.de.iteratec.osm.report.chart.availableChartTagLibs}")
		}

		return out.toString()
	}

	def multipleAxisChart =
	{ attrs, body ->

		List<OsmChartGraph> data = attrs["data"];
		String title = attrs["title"]
		String lineType = attrs["lineType"]
		String width= attrs["width"]
		String yAxisMaxs = attrs["yAxisMax"] ?: "null;null;null"
		String divId = attrs["divId"] ?: UUID.randomUUID().toString();
		String measurementUnits = attrs['measurementUnit'] ?: 'unkown'
		Long xAxisMin = attrs['xAxisMin'] ? Long.valueOf(attrs['xAxisMin']) : null
		Long xAxisMax = attrs['xAxisMax'] ? Long.valueOf(attrs['xAxisMax']) : null
		Boolean markerEnabled = attrs['markerEnabled']!=null ? Boolean.valueOf(attrs['markerEnabled']) : false
		Boolean dataLabelsActivated = attrs['dataLabelsActivated']!=null ? Boolean.valueOf(attrs['dataLabelsActivated']) : false
		Boolean yAxisScalable = attrs['yAxisScalable']!=null ? Boolean.valueOf(attrs['yAxisScalable']) : false
		String yAxisMin = attrs["yAxisMin"] = attrs['yAxisMin'] ?: '0'
		String lineWidthGlobal = attrs["lineWidthGlobal"] = attrs['lineWidthGlobal'] ?: '2'
		Boolean optimizeForExport = attrs['optimizeForExport']!=null ? Boolean.valueOf(attrs['optimizeForExport']) : false
		Long defaultThreshold = 100000
		Long highChartsTurboThreshold = attrs['highChartsTurboThreshold'] ? Long.valueOf(attrs['highChartsTurboThreshold']) : defaultThreshold
		Boolean openDatapointLinksInNewWindow = attrs['openDatapointLinksInNewWindow']!=null ? Boolean.valueOf(attrs['openDatapointLinksInNewWindow'])  : true
		String exportUrl = attrs['exportUrl'] ?: grailsApplication.config.grails.de.iteratec.osm.report.chart.highchartsExportServerUrl
		List<OsmChartAxis> yAxesLabels = attrs['highChartLabels']
		
		if (title == null) {
			title = "";
		}

		ChartingLibrary chartLibToUse = cookieBasedSettingsService.getChartingLibraryToUse()
		log.debug("chartLibToUse while processing osm chart taglib=${chartLibToUse}")

		if (chartLibToUse == ChartingLibrary.HIGHCHARTS){
			String heightOfChart = attrs["heightOfChart"] ?: "${configService.getInitialChartHeightInPixels()}"
			def htmlCreater = new HighchartHtmlCreater()
			out << htmlCreater.createChartHtmlMultipleYAxis(data, title, lineType, width, yAxisMin, yAxisMaxs, divId, false, measurementUnits,
					xAxisMin, xAxisMax, markerEnabled, dataLabelsActivated, yAxisScalable, lineWidthGlobal, optimizeForExport, yAxesLabels,
					highChartsTurboThreshold, openDatapointLinksInNewWindow, exportUrl, heightOfChart)
		}else if (chartLibToUse == ChartingLibrary.RICKSHAW)
		{
			String heightOfChart = attrs["heightOfChart"] ?: "${configService.getInitialChartHeightInPixels()}px"
			def htmlCreater = new RickshawHtmlCreater()
			out << htmlCreater.generateHtmlForMultipleYAxisGraph(divId, data, heightOfChart, yAxesLabels, title, markerEnabled)
		} else {
			throw new IllegalArgumentException("Illegal charting library: ${chartLibToUse} not contained in available charting libraries: " +
					"${grailsApplication.config.grails.de.iteratec.osm.report.chart.availableChartTagLibs}")
		}

		return out.toString()
	}

}