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
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.util.slurpersupport.NodeChild
import spock.lang.Specification
import static org.junit.Assert.assertTrue
/**
 * Test-suite for {@link OsmChartTagLib}.
 */
@TestFor(OsmChartTagLib)
@Mock([OsmConfiguration])
class OsmChartTagLibSpec extends Specification {

    static def HTML_FRAGMENT_PARSER

	def doWithSpring = {
		configService(ConfigService)
	}

	void setup(){
		//test data common for all tests
		TestDataUtil.createOsmConfig()
        HTML_FRAGMENT_PARSER = new org.cyberneko.html.parsers.SAXParser()
        HTML_FRAGMENT_PARSER.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment",true)
	}

	void "HTML provided by taglib iteratec:singleYAxisChart should represent given input"() {

        setup:

        //test specific mocks

		def osmChartTagLib = mockTagLib(OsmChartTagLib)
		osmChartTagLib.configService = grailsApplication.mainContext.getBean('configService')

		// create test-specific data

		Date now = new Date(1373631796000L);
		Date oneHourAfterNow = new Date(1373635396000L);
		Date twoHoursAfterNow = new Date(1373638996000L);

		OsmChartPoint nowPoint = new OsmChartPoint(time: now.getTime(), csiAggregation: 1.5d, countOfAggregatedResults: 1, sourceURL: new URL(
				"https://www.example.com/now"), testingAgent: null);
		OsmChartPoint oneHourAfterNowPoint_withoutURL = new OsmChartPoint(time: oneHourAfterNow.getTime(), csiAggregation: 3d, countOfAggregatedResults: 1, sourceURL: null, testingAgent: null);
		OsmChartPoint twoHoursAfterNowPoint = new OsmChartPoint(time: twoHoursAfterNow.getTime(), csiAggregation: 2.3d, countOfAggregatedResults: 1, sourceURL: new URL(
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
			yAxisScalable: 'true',
			initialChartHeight: "400"
		]

		when:

		String actualHtml = applyTemplate(
				'<iteratec:singleYAxisChart divId=\"${targetDivId}\" data=\"${data}\" heightOfChart=\"400\" '+
				'title=\"${chartTitle}\" yType=\"${targetYType}\" width=\"${targetWidth}\" xAxisMin=\"${xAxisMin}\" xAxisMax=\"${xAxisMax}\" yAxisMin=\"${yAxisMin}\" yAxisMax=\"${yAxisMax}\"' +
				' measurementUnit=\"${measurementUnit}\" markerEnabled=\"${markerEnabled}\" dataLabelsActivated=\"${dataLabelsActivated}\" yAxisScalable=\"${yAxisScalable}\" labelSummary=""/>',
				model).stripIndent()

		then:

        NodeChild actualHtmlAsNode = new XmlSlurper(HTML_FRAGMENT_PARSER).parseText(actualHtml)

        actualHtmlAsNode.childNodes().size() == 6

        def chartTitleNode = actualHtmlAsNode.childNodes().getAt(0)
        chartTitleNode.attributes['id'] == 'rickshaw_chart_title'
        chartTitleNode.attributes['class'] == 'rickshaw_chart_title'
        chartTitleNode.text() == 'Title'

        def rickshawLableSummaryBoxNode = actualHtmlAsNode.childNodes().getAt(1)
        rickshawLableSummaryBoxNode.attributes['id'] == 'rickshaw_label_summary_box'

        def rickshawMainNode = actualHtmlAsNode.childNodes().getAt(2)
        rickshawMainNode.attributes['id'] == 'rickshaw_main'

        def rickshawTimelineNode = actualHtmlAsNode.childNodes().getAt(3)
        rickshawTimelineNode.attributes['id'] == 'rickshaw_timeline'

        def rickshawAddonsNode = actualHtmlAsNode.childNodes().getAt(4)
        rickshawAddonsNode.attributes['id'] == 'rickshaw_addons'

        def javascriptRickshawNode = actualHtmlAsNode.childNodes().getAt(5)
        javascriptRickshawNode.attributes['type'] == 'text/javascript'

        String javascript = javascriptRickshawNode.text();
        List<String> javascriptLines = javascript.tokenize("\n")*.trim()

        javascriptLines[0] == 'var CHARTLIB="RICKSHAW";'
        javascriptLines[1] == 'var rickshawGraphBuilder;'
        javascriptLines[4] == "divId: \"myDivId\","
        javascriptLines[6] == "data : [ { measurandGroup: \"PERCENTAGES\",   yAxisLabel: \"Antwortzeit [ms]\", name: \"job1\",  data: [  { x: 1373631796, y: 1.5, url: \"https://www.example.com/now\" }, { x: 1373635396, y: 3.0, url: \"undefined\" }, { x: 1373638996, y: 2.3, url: \"https://www.example.com/twoHoursAfterNow\" } ] } ],"
        javascriptLines[7] == "height: 400,"

	}

	void "HTML provided by taglib iteratec:multipleAxisChart should represent given input"() {

        setup:

        //test specific mocks

        def osmChartTagLib = mockTagLib(OsmChartTagLib)
		osmChartTagLib.configService = grailsApplication.mainContext.getBean('configService')

        // create test-specific data

		Date now = new Date(1373631796000L);
		Date oneHourAfterNow = new Date(1373635396000L);
		Date twoHoursAfterNow = new Date(1373638996000L);

		OsmChartPoint nowPoint = new OsmChartPoint(time: now.getTime(), csiAggregation: 1.5d, countOfAggregatedResults: 1, sourceURL: new URL(
				"https://www.example.com/now"), testingAgent: null);
		OsmChartPoint oneHourAfterNowPoint_withoutURL = new OsmChartPoint(time: oneHourAfterNow.getTime(), csiAggregation: 3d, countOfAggregatedResults: 1, sourceURL: null, testingAgent: null);
		OsmChartPoint twoHoursAfterNowPoint = new OsmChartPoint(time: twoHoursAfterNow.getTime(), csiAggregation: 2.3d, countOfAggregatedResults: 1, sourceURL: new URL(
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
			heightOfChart: '200',
			highChartLabels: highChartLabels
		]


		when:

		String actualHtml = applyTemplate(
				'<iteratec:multipleAxisChart divId=\"${divId}\" data=\"${data}\" heightOfChart=\"600\" '+
				'title=\"${title}\" highChartLabels=\"${highChartLabels}\" labelSummary="" />',
				model).stripIndent()

        then:

        NodeChild actualHtmlAsNode = new XmlSlurper(HTML_FRAGMENT_PARSER).parseText(actualHtml)

        actualHtmlAsNode.childNodes().size() == 6

        def chartTitleNode = actualHtmlAsNode.childNodes().getAt(0)
        chartTitleNode.attributes['id'] == 'rickshaw_chart_title'
        chartTitleNode.attributes['class'] == 'rickshaw_chart_title'
        chartTitleNode.text() == 'Title'

        def rickshawLableSummaryBoxNode = actualHtmlAsNode.childNodes().getAt(1)
        rickshawLableSummaryBoxNode.attributes['id'] == 'rickshaw_label_summary_box'

        def rickshawMainNode = actualHtmlAsNode.childNodes().getAt(2)
        rickshawMainNode.attributes['id'] == 'rickshaw_main'

        def rickshawTimelineNode = actualHtmlAsNode.childNodes().getAt(3)
        rickshawTimelineNode.attributes['id'] == 'rickshaw_timeline'

        def rickshawAddonsNode = actualHtmlAsNode.childNodes().getAt(4)
        rickshawAddonsNode.attributes['id'] == 'rickshaw_addons'

        def javascriptRickshawNode = actualHtmlAsNode.childNodes().getAt(5)
        javascriptRickshawNode.attributes['type'] == 'text/javascript'

        String javascript = javascriptRickshawNode.text();
        List<String> javascriptLines = javascript.tokenize("\n")*.trim()

        assertTrue(javascript.contains("title: \"Antwortzeit WPT-Monitore\""))

        javascriptLines[0] == 'var CHARTLIB="RICKSHAW";'
        javascriptLines[1] == 'var rickshawGraphBuilder;'
        javascriptLines[4] == "divId: \"myDivId\","
        javascriptLines[6] == "data : [ { measurandGroup: \"LOAD_TIMES\",   yAxisLabel: \"Load Times\", name: \"job1\",  data: [  { x: 1373631796, y: 1.5, url: \"https://www.example.com/now\" }, { x: 1373635396, y: 3.0, url: \"undefined\" }, { x: 1373638996, y: 2.3, url: \"https://www.example.com/twoHoursAfterNow\" } ] },  { measurandGroup: \"PERCENTAGES\",   yAxisLabel: \"Percentages\", name: \"job2\",  data: [  { x: 1373631796, y: 1.5, url: \"https://www.example.com/now\" }, { x: 1373635396, y: 3.0, url: \"undefined\" }, { x: 1373638996, y: 2.3, url: \"https://www.example.com/twoHoursAfterNow\" } ] } ],"
        javascriptLines[7] == "height: 600,"

	}

}
