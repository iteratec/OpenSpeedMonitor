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

class RickshawHtmlCreater {

    /**
     * Generates Html code to define containers used by rickshaw
     * to place its components. Additional a javascript function
     * will be called, which is responsible to draw the rickshaw graph.
     */
    def generateHtmlForMultipleYAxisGraph = { String divId, List<OsmChartGraph> graphs, boolean dataLabelsActivated, String heightOfChart, List<OsmChartAxis> yAxesLabels, String title, boolean markerEnabled, List annotations ->

        def sw = new StringWriter()
        def data = transformData(graphs, yAxesLabels)
        def height = transformHeightOfChart(heightOfChart)

        if (divId == null || divId == "") {
            divId = "rickshaw_all"
        }

        if (title == null) {
            title = "";
        }

        sw <<"""
		<div id="${divId}" class="graph">
			<div id="rickshaw_chart_title" class="rickshaw_chart_title">Title</div>
			<div id="rickshaw_main">
				<div id="rickshaw_yAxis_0" class="rickshaw_y-axis_left"></div>
				<div id="rickshaw_y-axes_right"></div>
				<div id="rickshaw_chart"></div>
				<div id="rickshaw_x-axis"></div>
			</div>

			<div id="rickshaw_addons">
                <div id="rickshaw_timeline"></div>
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
					title: "${title}",
					data : ${data},
					heightOfChart :  ${height},
                    dataLabelsActivated : ${dataLabelsActivated},
					NUMBER_OF_YAXIS_TICKS : 5,
					drawPointMarkers: ${markerEnabled},
                    annotations : ${annotations}
				};
				rickshawGraphBuilder = new RickshawGraphBuilder(args);
			});
		</script> """
    }

    /**
     * Removes "px" from height of chart. So the height can be
     * treated like an integer in javascript.
     */
    def transformHeightOfChart = { String heightOfChart ->
        def height = heightOfChart.split("px")[0];
        return height;
    }

    /**
     * Transforms the data stored in a List of HighchartGraphs
     * into a datastructure which can be used in javascript.
     */
    def transformData = { List<OsmChartGraph> graphs, List<OsmChartAxis> yAxesLabels ->

        def sw = new StringWriter()
        def prefix1 = ""

        sw << "["

        graphs.each { graph ->
            def label = ""
            yAxesLabels.each { eachLabel ->
                if( eachLabel.group == graph.measurandGroup) {
                    label = eachLabel.labelI18NIdentifier
                }
            }

            sw << prefix1 + """ { measurandGroup: "${graph.measurandGroup}",  """
            sw << """ yAxisLabel: "${label}","""
            sw << """ name: "${graph.label}", """
            sw << """ data: """ + generateDataPoints(graph) + """ }"""
            prefix1 = ", "
        }

        sw << " ]"
    }

    /**
     * Generates a javascript list which contains data of one specific OsmChartGraph.
     */
    def generateDataPoints = { OsmChartGraph graph ->

        def sw = new StringWriter()
        def measurandGroup = graph.measurandGroup.toString();
        def prefix = ""
        String testingAgent

        sw << """[ """
		graph.getPoints().each {eachPoint ->

            def url = "undefined"
			def measuredValue = eachPoint.measuredValue

            if(measurandGroup == "LOAD_TIMES" || measurandGroup == "REQUEST_SIZES" ) {
                measuredValue = measuredValue / 1000;
            }

			if (eachPoint.sourceURL != null) {
				url = eachPoint.sourceURL.toString();
            }

            testingAgent = eachPoint.testingAgent !=null ? ',testAgent:\'' + eachPoint.testingAgent + '\'' : ''
			sw << prefix +""" { x: ${eachPoint.time / 1000}, y: ${measuredValue}, url: "${url}" ${testingAgent}}"""
            prefix = ","
        }
        sw << """ ]"""
    }
}
