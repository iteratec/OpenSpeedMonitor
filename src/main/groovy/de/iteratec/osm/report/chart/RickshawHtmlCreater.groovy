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

import de.iteratec.osm.csi.DefaultTimeToCsMapping
import de.iteratec.osm.csi.RickshawTransformableCsMapping
import de.iteratec.osm.result.CachedView
import groovy.json.StringEscapeUtils


class RickshawHtmlCreater {

    def generateCsiMappingsChartHtml = { chartIdentifier, bottomOffsetXAxis, yAxisRightOffset,
                                         chartBottomOffset, yAxisTopOffset, bottomOffsetLegend ->

        def sw = new StringWriter()

        //edit/show: 130
        //modal: 220

        sw << """
        <div id="chart_container_${chartIdentifier} style="position: relative;margin-left: 25px;">
            <div id="y_axis_${chartIdentifier}" style="position:relative;right:${yAxisRightOffset}px;top:${
            yAxisTopOffset
        }px"></div>
            <div id="chart_${chartIdentifier}" style="position:relative;bottom:${chartBottomOffset}px;"></div>
            <div id="legend_container_${chartIdentifier}">
                <div id="smoother_${chartIdentifier}" title="Smoothing"></div>
                <div id="legend_${chartIdentifier}" style="position:relative;bottom:${bottomOffsetLegend}px;"></div>
            </div>
            <div id="x_axis_${chartIdentifier}" style="position:relative;bottom: ${bottomOffsetXAxis}px;"></div>
        </div>
        """

    }

    /**
     * Generates Html code to define containers used by rickshaw
     * to place its components. Additional a javascript function
     * will be called, which is responsible to draw the rickshaw graph.
     */
    def generateHtmlForMultipleYAxisGraph = { String divId, List<OsmChartGraph> graphs, boolean dataLabelsActivated, String heightOfChart, String width, List<OsmChartAxis> yAxesLabels, String title, String labelSummary, boolean markerEnabled, List annotations, String yAxisMin, String yAxisMax, String downloadPngLabel, String downloadCsvSubmitButton ->
        def sw = new StringWriter()
        def data = transformData(graphs, yAxesLabels)
        def height = heightOfChart

        if (divId == null || divId == "") {
            divId = "rickshaw_all"
        }

        if (title == null) {
            title = "";
        }

        sw << """
		<div id="${divId}" class="graph">
			<div id="rickshaw_chart_title" class="rickshaw_chart_title"></div>
			<div id="rickshaw_label_summary_box">
                <div id="rickshaw_chart_label_summary">${labelSummary}</div>
            </div>
			<div id="rickshaw_main" style="height: ${height}">
                <a href="#adjustChartModal" id="rickshaw_adjust_chart_link" data-toggle="modal" data-target="#adjustChartModal">
                    <i class="fa fa-sliders"></i>
                </a>
                <div class="btn-group hidden" id="download-dropdown">
                <div  type="button" id="download-dropdown-button"
                   class="dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    <i class="fa fa-download"></i>
                </div>
                <ul class="dropdown-menu dropdown-menu-right">
                        ${downloadPngLabel ? '<li><a href="#" id="dia-save-chart-as-png">'+downloadPngLabel+'</a></li>' : ''}
                        <li>
                            ${downloadCsvSubmitButton}
                        </li>
                </ul>
                </div>
                
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
            var createGraph = function(){
                var args = {
                    divId: "${divId}",
                    title: "${title}",
                    data : ${data},
                    height: ${height},
                    width: ${width},
                    top: "${yAxisMax}",
                    bottom: "${yAxisMin}",
                    dataLabelsActivated : ${dataLabelsActivated},
                    NUMBER_OF_YAXIS_TICKS : 5,
                    drawPointMarkers: ${markerEnabled},
                    annotations : ${annotations}
                };
                rickshawGraphBuilder = new RickshawGraphBuilder(args);
            }
        </script>"""
    }

//    /**
//     * Removes "px" from height of chart. So the height can be
//     * treated like an integer in javascript.
//     */
//    def transformHeightOfChart = { String heightOfChart ->
//        def height = heightOfChart.split("px")[0];
//        return height;
//    }

    /**
     * Transforms the data stored in a List of HighchartGraphs
     * into a datastructure which can be used in javascript.
     */
    def transformData = { List<OsmChartGraph> graphs, List<OsmChartAxis> yAxesLabels ->
        def StringEscapeUtils escapeUtils = new StringEscapeUtils()
        def sw = new StringWriter()
        def prefix1 = ""

        sw << "["

        graphs.each { graph ->
            def label = ""
            yAxesLabels.each { eachLabel ->
                if (eachLabel.group == graph.measurandGroup) {
                    label = eachLabel.labelI18NIdentifier
                }
            }

            sw << prefix1 + """ { measurandGroup: "${escapeUtils.escapeJava(graph.measurandGroup.toString())}", """
            sw << """yAxisLabel: "${escapeUtils.escapeJava(label)}", """
            sw << """name: "${escapeUtils.escapeJava(graph.label)}", """
            sw << """data: """ + generateDataPoints(graph) + """ }"""
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
        graph.getPoints().each { eachPoint ->

            def url = "undefined"
            def csiAggregation = eachPoint.csiAggregation

            if (measurandGroup == "REQUEST_SIZES") {
                csiAggregation = csiAggregation / 1000;
            }

            if (eachPoint.sourceURL != null) {
                url = eachPoint.sourceURL.toString();
            }

            def wptResultInfo = null
            if (eachPoint.chartPointWptInfo) {
                // the enum is declared in the wrong order
                def cached = eachPoint.chartPointWptInfo.cachedView.isCached()

                // json object containing all relevant infos to create a url to jump to wpt
                def pointInfo = eachPoint.chartPointWptInfo
                wptResultInfo = """{ wptServerBaseurl: "${pointInfo.serverBaseUrl.toString()}", testId: "${
                    pointInfo.testId.toString()
                }", numberOfWptRun: "${pointInfo.numberOfWptRun.toString()}", oneBaseStepIndexInJourney: "${
                    pointInfo.oneBaseStepIndexInJourney.toString()
                }", cachedView: ${cached} }""";
            }


            testingAgent = eachPoint.testingAgent != null ? ',testAgent:\'' + eachPoint.testingAgent + '\'' : ''
            sw << prefix + """{ x: ${eachPoint.time / 1000}, y: ${csiAggregation}, url: "${url}" ${
                testingAgent
            }, wptResultInfo: ${wptResultInfo} }"""
            prefix = ", "
        }
        sw << """ ]"""
    }

    /**
     * Transforms the data stored in a List of {@link DefaultTimeToCsMapping}s
     * into a datastructure which can be used in javascript.
     */
    def transformCSIMappingData = { List<RickshawTransformableCsMapping> transformableMappings ->

        def sw = new StringWriter()
        sw << "["

        Map seriesData = [:].withDefault { key -> new HashMap<Integer, Double>() }
        transformableMappings.each { mapping ->
            seriesData[mapping.retrieveGroupingCriteria()][mapping.retrieveLoadTimeInMilliSecs()] = mapping.retrieveCustomerSatisfactionInPercent()
        }
        seriesData.each { String name, Map loadTimeToCsMap ->
            sw << " { "
            sw << " name: '${name}', "
            sw << " color: palette.color(), "
            sw << " data: [ "
            loadTimeToCsMap.keySet().sort().each { loadTime ->
                sw << " {x: ${loadTime}, y: ${loadTimeToCsMap[loadTime]}}, "
            }
            sw << " ]}, "
        }

        sw << " ]"
    }
}
