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
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.p13n.CookieBasedSettingsService
import grails.test.mixin.*

import org.junit.Before
import org.junit.Test

/**
 * Test-suite for {@link OsmChartTagLib}.
 */
@TestFor(OsmChartTagLib)
@Mock([OsmConfiguration])
class OsmChartTagLibTests {

	@Before
	void setUp(){
		//test data common for all tests
		TestDataUtil.createOsmConfig()
	}

	@Test
	void testSingleYAxisChartTagWithHighcharts() {
		def osmChartTagLib = mockTagLib(OsmChartTagLib)
		osmChartTagLib.configService = new ConfigService()
		osmChartTagLib.cookieBasedSettingsService = [getChartingLibraryToUse: {-> return ChartingLibrary.HIGHCHARTS}] as CookieBasedSettingsService
		// create test-specific data

		Date now = new Date(1373631796000L);
		Date oneHourAfterNow = new Date(1373635396000L);
		Date twoHoursAfterNow = new Date(1373638996000L);

		OsmChartPoint nowPoint = new OsmChartPoint(time: now.getTime(), measuredValue: 1.5d, countOfAggregatedResults: 1, sourceURL: new URL(
				"https://www.example.com/now"), testingAgent: null);
		OsmChartPoint oneHourAfterNowPoint_withoutURL = new OsmChartPoint(time: oneHourAfterNow.getTime(), measuredValue: 3d, countOfAggregatedResults: 1, sourceURL: null, testingAgent: null);
		OsmChartPoint twoHoursAfterNowPoint = new OsmChartPoint(time: twoHoursAfterNow.getTime(), measuredValue: 2.3d, countOfAggregatedResults: 1, sourceURL: new URL(
				"https://www.example.com/twoHoursAfterNow"), testingAgent: null);

		OsmChartGraph graph=new OsmChartGraph();
		graph.setLabel("job1");
		graph.setPoints([
			nowPoint,
			oneHourAfterNowPoint_withoutURL,
			twoHoursAfterNowPoint
		]);

		List<OsmChartGraph> data = [graph]

		String chartTitle = 'Antwortzeit WPT-Monitore'
		String targetDivId = 'myDivId'
		String targetYType = 'Antwortzeit [ms]'
		String targetWidth = '100%'
		String expectedData = '{name: "job1",data:['+
				'{x:1373631796000,y:1.5,events:{click:function(e){window.open(\'https://www.example.com/now\');}}},'+
				'{x:1373635396000,y:3.0},'+
				'{x:1373638996000,y:2.3,events:{click:function(e){window.open(\'https://www.example.com/twoHoursAfterNow\');}}},'+
				']},'

		grailsApplication.config.grails.de.iteratec.osm.report.chart.chartTagLib = ChartingLibrary.HIGHCHARTS
		grailsApplication.config.grails.de.iteratec.osm.report.chart.highchartsExportServerUrl = 'http://export.highcharts.com'
		String expectedHtml = getExpectedHtmlForSingleYAxisChartWithHighcharts(targetDivId, targetWidth, chartTitle, targetYType, 'null', 'null', expectedData).stripIndent()

		Map<String, Object> model = [
			targetDivId: targetDivId,
			data: data,
			chartTitle: chartTitle,
			targetYType: targetYType,
			targetWidth: targetWidth,
			xAxisMin: 100,
			xAxisMax: 1000,
			yAxisMin: 10,
			yAxisMax: 100,
			measurementUnit: 'ms',
			markerEnabled: 'true',
			dataLabelsActivated: 'false',
			yAxisScalable: 'true'

		]

		// execute test

		String actualHtml = applyTemplate(
				'<iteratec:singleYAxisChart divId=\"${targetDivId}\" data=\"${data}\" heightOfChart=\"800px\" '+
				'title=\"${chartTitle}\" yType=\"${targetYType}\" width=\"${targetWidth}\" xAxisMin=\"${xAxisMin}\" xAxisMax=\"${xAxisMax}\" yAxisMin=\"${yAxisMin}\" yAxisMax=\"${yAxisMax}\"' +
				' measurementUnit=\"${measurementUnit}\" markerEnabled=\"${markerEnabled}\" dataLabelsActivated=\"${dataLabelsActivated}\" yAxisScalable=\"${yAxisScalable}\" />',
				model).stripIndent()

		// assertions
        assertEquals(expectedHtml, actualHtml)

	}

	private String getExpectedHtmlForSingleYAxisChartWithHighcharts(String divId, String width, String title, String yType, String maxValue, String lineType, String data) {
		def sw = new StringWriter()

		sw  = """<div id="${divId}" style="width: ${width};"></div>
			<script type="text/javascript">
				\$(document).ready(function() {
					window.CHARTLIB = "HIGHCHARTS";
					var data = [${data}];
		createLineChart("myDivId", "Antwortzeit WPT-Monitore", "Antwortzeit [ms]", data, 100, "null",
			"ms", 100, 1000, true, false, true, 10, 2, false, 800px, "http://export.highcharts.com");
			});
		</script>"""

		sw.toString()
	}

	@Test
	void testSingleYAxisChartTagWithRickshaw() {
		def osmChartTagLib = mockTagLib(OsmChartTagLib)
		osmChartTagLib.configService = new ConfigService()
		osmChartTagLib.cookieBasedSettingsService = [getChartingLibraryToUse: {-> return ChartingLibrary.RICKSHAW}] as CookieBasedSettingsService
		// create test-specific data

		Date now = new Date(1373631796000L);
		Date oneHourAfterNow = new Date(1373635396000L);
		Date twoHoursAfterNow = new Date(1373638996000L);

		OsmChartPoint nowPoint = new OsmChartPoint(time: now.getTime(), measuredValue: 1.5d, countOfAggregatedResults: 1, sourceURL: new URL(
				"https://www.example.com/now"), testingAgent: null);
		OsmChartPoint oneHourAfterNowPoint_withoutURL = new OsmChartPoint(time: oneHourAfterNow.getTime(), measuredValue: 3d, countOfAggregatedResults: 1, sourceURL: null, testingAgent: null);
		OsmChartPoint twoHoursAfterNowPoint = new OsmChartPoint(time: twoHoursAfterNow.getTime(), measuredValue: 2.3d, countOfAggregatedResults: 1, sourceURL: new URL(
				"https://www.example.com/twoHoursAfterNow"), testingAgent: null);

		OsmChartGraph graph=new OsmChartGraph();
		graph.setLabel("job1");
		graph.setPoints([
			nowPoint,
			oneHourAfterNowPoint_withoutURL,
			twoHoursAfterNowPoint
		]);

		List<OsmChartGraph> data = [graph]

		String chartTitle = 'Antwortzeit WPT-Monitore'
		String targetDivId = 'myDivId'
		String targetYType = 'Antwortzeit [ms]'
		String targetWidth = '100%'

		grailsApplication.config.grails.de.iteratec.osm.report.chart.chartTagLib = ChartingLibrary.RICKSHAW
		grailsApplication.config.grails.de.iteratec.osm.report.chart.highchartsExportServerUrl = 'http://export.highcharts.com'

		String expectedHtml = getExpectedHtmlForSingleYAxisChartWithRickshaw(targetDivId).stripIndent()

		Map<String, Object> model = [
			targetDivId: targetDivId,
			data: data,
			chartTitle: chartTitle,
			targetYType: targetYType,
			targetWidth: targetWidth,
			xAxisMin: 100,
			xAxisMax: 1000,
			yAxisMin: 10,
			yAxisMax: 100,
			measurementUnit: 'ms',
			markerEnabled: 'true',
			dataLabelsActivated: 'false',
			yAxisScalable: 'true'

		]

		// execute test

		String actualHtml = applyTemplate(
				'<iteratec:singleYAxisChart divId=\"${targetDivId}\" data=\"${data}\" heightOfChart=\"800px\" '+
				'title=\"${chartTitle}\" yType=\"${targetYType}\" width=\"${targetWidth}\" xAxisMin=\"${xAxisMin}\" xAxisMax=\"${xAxisMax}\" yAxisMin=\"${yAxisMin}\" yAxisMax=\"${yAxisMax}\"' +
				' measurementUnit=\"${measurementUnit}\" markerEnabled=\"${markerEnabled}\" dataLabelsActivated=\"${dataLabelsActivated}\" yAxisScalable=\"${yAxisScalable}\" />',
				model).stripIndent()

		// assertions

		assertEquals(expectedHtml, actualHtml)

	}

	private String getExpectedHtmlForSingleYAxisChartWithRickshaw(String divId) {
		def sw = new StringWriter()

		sw  = """
		<div id="${divId}" class="graph">
			<div id="rickshaw_chart_title" class="rickshaw_chart_title">Title</div>
			<div id="rickshaw_main">
				<div id="rickshaw_yAxis_0" class="rickshaw_y-axis_left"></div>
				<div id="rickshaw_y-axes_right"></div>
				<div id="rickshaw_chart"></div>
				<div id="rickshaw_x-axis"></div>
			</div>

            <div id="rickshaw_timeline"></div>
			<div id="rickshaw_addons">
				<div id="rickshaw_slider"></div>
				<div id="rickshaw_legend"></div>
			</div>
		</div>

		<script type="text/javascript">
			var CHARTLIB="RICKSHAW";
			var rickshawGraphBuilder;
			\$(document).ready(function(){
				var args = {
					divId: "${divId}",
					title: "Antwortzeit WPT-Monitore",
					data : [ { measurandGroup: "PERCENTAGES",   yAxisLabel: "Antwortzeit [ms]", name: "job1",  data: [  { x: 1373631796, y: 1.5, url: "https://www.example.com/now" }, { x: 1373635396, y: 3.0, url: "undefined" }, { x: 1373638996, y: 2.3, url: "https://www.example.com/twoHoursAfterNow" } ] } ],
					heightOfChart :  400,
                    dataLabelsActivated : false,
					NUMBER_OF_YAXIS_TICKS : 5,
					drawPointMarkers: true,
                    annotations : null
				};
				rickshawGraphBuilder = new RickshawGraphBuilder(args);
			});
		</script> """

		sw.toString()
	}

	@Test
	void testMultipleYAxisChartTagWithRickshaw() {

		def osmChartTagLib = mockTagLib(OsmChartTagLib)
		osmChartTagLib.configService = new ConfigService()
		osmChartTagLib.cookieBasedSettingsService = [getChartingLibraryToUse: {-> return ChartingLibrary.RICKSHAW}] as CookieBasedSettingsService
		// create test-specific data

		Date now = new Date(1373631796000L);
		Date oneHourAfterNow = new Date(1373635396000L);
		Date twoHoursAfterNow = new Date(1373638996000L);

		OsmChartPoint nowPoint = new OsmChartPoint(time: now.getTime(), measuredValue: 1.5d, countOfAggregatedResults: 1, sourceURL: new URL(
				"https://www.example.com/now"), testingAgent: null);
		OsmChartPoint oneHourAfterNowPoint_withoutURL = new OsmChartPoint(time: oneHourAfterNow.getTime(), measuredValue: 3d, countOfAggregatedResults: 1, sourceURL: null, testingAgent: null);
		OsmChartPoint twoHoursAfterNowPoint = new OsmChartPoint(time: twoHoursAfterNow.getTime(), measuredValue: 2.3d, countOfAggregatedResults: 1, sourceURL: new URL(
				"https://www.example.com/twoHoursAfterNow"), testingAgent: null);

		OsmChartGraph graph1=new OsmChartGraph();
		graph1.setMeasurandGroup(MeasurandGroup.LOAD_TIMES)
		graph1.setLabel("job1");
		graph1.setPoints([
			nowPoint,
			oneHourAfterNowPoint_withoutURL,
			twoHoursAfterNowPoint
		]);
		OsmChartGraph graph2=new OsmChartGraph();
		graph2.setMeasurandGroup(MeasurandGroup.PERCENTAGES)
		graph2.setLabel("job2");
		graph2.setPoints([
			nowPoint,
			oneHourAfterNowPoint_withoutURL,
			twoHoursAfterNowPoint
		]);
		List<OsmChartGraph> data = [graph1, graph2]

		OsmChartAxis axis1 = new OsmChartAxis("Percentages", MeasurandGroup.PERCENTAGES, "",1, OsmChartAxis.LEFT_CHART_SIDE);
		OsmChartAxis axis2 = new OsmChartAxis("Load Times", MeasurandGroup.LOAD_TIMES, "",1, OsmChartAxis.RIGHT_CHART_SIDE);
		List<OsmChartAxis> highChartLabels = [axis1, axis2];

		String chartTitle = 'Antwortzeit WPT-Monitore'
		String targetDivId = 'myDivId'
		String targetYType = 'Antwortzeit [ms]'
		String targetWidth = '100%'

		grailsApplication.config.grails.de.iteratec.osm.report.chart.chartTagLib = ChartingLibrary.RICKSHAW
		grailsApplication.config.grails.de.iteratec.osm.report.chart.highchartsExportServerUrl = 'http://export.highcharts.com'

		String expectedHtml = getExpectedHtmlForMultipleYAxisChartWithRickshaw(targetDivId).stripIndent()

		Map<String, Object> model = [
			data: data,
			title: chartTitle,
			width: targetWidth,
			divId: targetDivId,
			xAxisMin: 100,
			xAxisMax: 1000,
			yAxisMin: 10,
			yAxisMax: 100,
			measurementUnit: 'ms',
			markerEnabled: 'true',
			dataLabelsActivated: 'false',
			yAxisScalable: 'true',
			heightOfChart: '600px',
			highChartLabels: highChartLabels
		]


		// execute test
		String actualHtml = applyTemplate(
				'<iteratec:multipleAxisChart divId=\"${divId}\" data=\"${data}\" heightOfChart=\"600px\" '+
				'title=\"${title}\" highChartLabels=\"${highChartLabels}\" />',
				model).stripIndent()

		// assertions
		assertEquals(expectedHtml, actualHtml)
	}

	private String getExpectedHtmlForMultipleYAxisChartWithRickshaw(String divId) {
		def sw = new StringWriter()

		sw <<"""
		<div id="${divId}" class="graph">
			<div id="rickshaw_chart_title" class="rickshaw_chart_title">Title</div>
			<div id="rickshaw_main">
				<div id="rickshaw_yAxis_0" class="rickshaw_y-axis_left"></div>
				<div id="rickshaw_y-axes_right"></div>
				<div id="rickshaw_chart"></div>
				<div id="rickshaw_x-axis"></div>
			</div>

            <div id="rickshaw_timeline"></div>
			<div id="rickshaw_addons">
				<div id="rickshaw_slider"></div>
				<div id="rickshaw_legend"></div>
			</div>
		</div>

		<script type="text/javascript">
			var CHARTLIB="RICKSHAW";
			var rickshawGraphBuilder;
			\$(document).ready(function(){
				var args = {
					divId: "${divId}",
					title: "Antwortzeit WPT-Monitore",
					data : [ { measurandGroup: "LOAD_TIMES",   yAxisLabel: "Load Times", name: "job1",  data: [  { x: 1373631796, y: 0.0015, url: "https://www.example.com/now" }, { x: 1373635396, y: 0.003, url: "undefined" }, { x: 1373638996, y: 0.0023, url: "https://www.example.com/twoHoursAfterNow" } ] },  { measurandGroup: "PERCENTAGES",   yAxisLabel: "Percentages", name: "job2",  data: [  { x: 1373631796, y: 1.5, url: "https://www.example.com/now" }, { x: 1373635396, y: 3.0, url: "undefined" }, { x: 1373638996, y: 2.3, url: "https://www.example.com/twoHoursAfterNow" } ] } ],
					heightOfChart :  600,
                    dataLabelsActivated : false,
					NUMBER_OF_YAXIS_TICKS : 5,
					drawPointMarkers: false,
                    annotations : null
				};
				rickshawGraphBuilder = new RickshawGraphBuilder(args);
			});
		</script> """

		sw.toString()
	}

	@Test
	void testMultipleYAxisChartTagWithHighchart() {
		def osmChartTagLib = mockTagLib(OsmChartTagLib)
		osmChartTagLib.configService = new ConfigService()
		osmChartTagLib.cookieBasedSettingsService = [getChartingLibraryToUse: {-> return ChartingLibrary.HIGHCHARTS}] as CookieBasedSettingsService
		// create test-specific data

		Date now = new Date(1373631796000L);
		Date oneHourAfterNow = new Date(1373635396000L);
		Date twoHoursAfterNow = new Date(1373638996000L);

		OsmChartPoint nowPoint = new OsmChartPoint(time: now.getTime(), measuredValue: 1.5d, countOfAggregatedResults: 1, sourceURL: new URL(
				"https://www.example.com/now"), testingAgent: null);
		OsmChartPoint oneHourAfterNowPoint_withoutURL = new OsmChartPoint(time: oneHourAfterNow.getTime(), measuredValue: 3d, countOfAggregatedResults: 1, sourceURL: null, testingAgent: null);
		OsmChartPoint twoHoursAfterNowPoint = new OsmChartPoint(time: twoHoursAfterNow.getTime(), measuredValue: 2.3d, countOfAggregatedResults: 1, sourceURL: new URL(
				"https://www.example.com/twoHoursAfterNow"), testingAgent: null);

		OsmChartGraph graph1=new OsmChartGraph();
		graph1.setMeasurandGroup(MeasurandGroup.LOAD_TIMES)
		graph1.setLabel("job1");
		graph1.setPoints([
			nowPoint,
			oneHourAfterNowPoint_withoutURL,
			twoHoursAfterNowPoint
		]);
		OsmChartGraph graph2=new OsmChartGraph();
		graph2.setMeasurandGroup(MeasurandGroup.PERCENTAGES)
		graph2.setLabel("job2");
		graph2.setPoints([
			nowPoint,
			oneHourAfterNowPoint_withoutURL,
			twoHoursAfterNowPoint
		]);
		List<OsmChartGraph> data = [graph1, graph2]

		OsmChartAxis axis1 = new OsmChartAxis("Percentages", MeasurandGroup.PERCENTAGES, "",1, OsmChartAxis.LEFT_CHART_SIDE);
		OsmChartAxis axis2 = new OsmChartAxis("Load Times", MeasurandGroup.LOAD_TIMES, "",1, OsmChartAxis.RIGHT_CHART_SIDE);
		OsmChartAxis axis3 = new OsmChartAxis("Load Times", MeasurandGroup.REQUEST_COUNTS, "",1, OsmChartAxis.RIGHT_CHART_SIDE);
		OsmChartAxis axis4 = new OsmChartAxis("Load Times", MeasurandGroup.REQUEST_SIZES, "",1, OsmChartAxis.RIGHT_CHART_SIDE);
		OsmChartAxis axis5 = new OsmChartAxis("Load Times", MeasurandGroup.NO_MEASURAND, "",1, OsmChartAxis.RIGHT_CHART_SIDE);
		List<OsmChartAxis> highChartLabels = [axis1, axis2, axis3, axis4, axis5];

		String divId = 'myDivId'

		grailsApplication.config.grails.de.iteratec.osm.report.chart.chartTagLib = ChartingLibrary.HIGHCHARTS
		grailsApplication.config.grails.de.iteratec.osm.report.chart.highchartsExportServerUrl = 'http://export.highcharts.com'

		String expectedHtml = getExpectedHtmlForMultipleYAxisChartWithHighchart(divId).stripIndent()

		Map<String, Object> model = [
			data: data,
			divId: divId,
			xAxisMin: 100,
			xAxisMax: 1000,
			yAxisMin: 10,
			yAxisMax: 100,
			measurementUnit: 'ms',
			markerEnabled: 'true',
			dataLabelsActivated: 'false',
			yAxisScalable: 'true',
			heightOfChart: '600px',
			highChartLabels: highChartLabels
		]


		// execute test
		String actualHtml = applyTemplate(
				'<iteratec:multipleAxisChart divId=\"${divId}\" data=\"${data}\" heightOfChart=\"600px\" '+
				'xAxisMin=\"${xAxisMin}\" xAxisMax=\"${xAxisMax}\" yAxisMin=\"${yAxisMin}\" ' +
				'yAxisMax=\"${yAxisMax}\" measurementUnit=\"${measurementUnit}\" markerEnabled=\"${markerEnabled}\" ' +
				'dataLabelsActivated=\"${dataLabelsActivated}\" yAxisScalable=\"${yAxisScalable}\" ' +
				'highChartLabels=\"${highChartLabels}\" />',
				model).stripIndent()

		// assertions
		assertEquals(expectedHtml, actualHtml)
	}

	private String getExpectedHtmlForMultipleYAxisChartWithHighchart(String divId) {
		def sw = new StringWriter()

		sw << """<div id="${divId}" style="width: null;"></div>
		<script type="text/javascript">
			\$(document).ready(function() {
				window.CHARTLIB = "HIGHCHARTS";
				var data = [{name: "job1",yAxis: 1, data:[{x:1373631796000,y:1.5,events:{click:function(e){window.open('https://www.example.com/now');}}},{x:1373635396000,y:3.0},{x:1373638996000,y:2.3,events:{click:function(e){window.open('https://www.example.com/twoHoursAfterNow');}}},]},{name: "job2",yAxis: 0, data:[{x:1373631796000,y:1.5,events:{click:function(e){window.open('https://www.example.com/now');}}},{x:1373635396000,y:3.0},{x:1373638996000,y:2.3,events:{click:function(e){window.open('https://www.example.com/twoHoursAfterNow');}}},]},];
				createLineChartWithMultipleYAxis("${divId}", "", [{ "label": "Percentages", "labelPosition": 0, "unit": "", "divisor": 1.0, "color": "#000000" },{ "label": "Load Times", "labelPosition": 1, "unit": "", "divisor": 1.0, "color": "#000000" },{ "label": "Load Times", "labelPosition": 1, "unit": "", "divisor": 1.0, "color": "#000000" },{ "label": "Load Times", "labelPosition": 1, "unit": "", "divisor": 1.0, "color": "#000000" },{ "label": "Load Times", "labelPosition": 1, "unit": "", "divisor": 1.0, "color": "#000000" },], data, "100", "null",
					"ms", 100, 1000, true, false, true,
					10, 2, false, 100000, "http://export.highcharts.com", "600px");
					});
				</script>"""

		sw.toString()
	}

}
