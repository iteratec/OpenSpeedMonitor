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
package de.iteratec.osm.d3Data

import org.springframework.beans.factory.annotation.Autowired

class D3HtmlCreator {
    def i18nService

    /**
     * Creates divs for de.iteratec.osm.d3data.MultiLineChart
     * @param chartIdentifer a unique name for chart on the page
     * @param modal if modal == true the created chart div is smaller
     */
    def generateMultiLineChartHtml = {chartIdentifier, modal ->
        StringWriter writer = new StringWriter()

        if(modal){
            return writer << """<div class="col-md-4" id="${chartIdentifier}">
                                    <div id="tooltip" class="hidden">
                                        <p><strong id="heading"></strong></p>
                                        <p><span id="info"></span></p>
                                    </div>
                                </div>"""
        }
        else {
            return writer << """<div class="col-md-8" id="${chartIdentifier}">
                                    <div id="tooltip" class="hidden">
                                        <p><strong id="heading"></strong></p>
                                        <p><span id="info"></span></p>
                                    </div>
                                </div>"""
        }
    }

    /**
     * Creates container for de.iteratec.osm.d3data.BarChartData
     */
    def generateBarChartHtml = {chartIdentifier ->
        StringWriter writer = new StringWriter()
            return writer << """<div class="row">
                                    <div class="col-md-8" id="barChartSpan">
                                        <svg class="chart" id="${chartIdentifier}"></svg>
                                    </div>
                                </div>"""
    }

    /**
     * Creates container for de.iteratex.osm.d3data.MatrixViewData
     */
    def generateMatrixViewHtml = {chartIdentifier ->
        StringWriter writer = new StringWriter()
        return writer << """<div class="row">
                                <div class="col-md-8" id="${chartIdentifier}"></div>
                                    <div id="tooltipMatrixView" class="hidden">
                                    <p><strong id="columnName"></strong></p>
                                    <p><strong id="rowName"></strong></p>
                                    <p><span id="matrixWeight"></span></p>
                                    </div>
                            </div>
                        """
    }


    /**
     * Creates container for de.iteratec.osm.d3data.TreemapData
     */
    def generateTreemapHtml = {chartIdentifier ->
        StringWriter writer = new StringWriter()
        return writer << """<div class="row">
                                <div class="col-md-8" id="treemapSpan">
                                    <div class="treemap" id= ${chartIdentifier}></div>
                                    <div id="tooltip" class="hidden">
                                        <p><strong id="heading"></strong></p>
                                        <p><span id="info"></span></p>
                                    </div>
                                </div>
                                <div class="col-md-3" id="zeroWeightSpan">
                                </div>
                            </div>"""
    }

    /**
     * Creates container for de.iteratec.osm.d3data.ScheduleChartData
     */
    def generateScheduleChartHtml = {
        chartIdentifier, unitOne, unitMultiple, on, off, hideIntersectionLabel, showIntersectionLabel, showLabel ->


        StringWriter writer = new StringWriter()
        return writer << """<div class="row">
                                <div class="col-md-12" id="${"ScheduleChart" + chartIdentifier}">
                                    <div id="tooltip" class="hidden">
                                        <p><strong id="heading"></strong></p>
                                        <p><span id="info"></span></p>
                                    </div>
                                </div>
                                <div class="col-md-12 text-center" id="${"duration-to-show" + chartIdentifier}">
                                    <br>
                                    ${showLabel}
                                    <div class="btn-group">
                                        <button class="btn btn-info btn-sm" value="1">1</button>
                                        <button class="btn btn-info btn-sm" value="2">2</button>
                                        <button class="btn btn-info btn-sm" value="4">4</button>
                                        <button class="btn btn-info btn-sm" value="6">6</button>
                                        <button class="btn btn-info btn-sm" value="12">12</button>
                                        <button class="btn btn-info btn-sm" value="24">24</button>
                                    </div>
                                    ${unitMultiple}
                                </div>
                                <div class="col-md-12 text-center" id="${"show-overused-queues" + chartIdentifier}">
                                    <div class="btn-group">
                                        <button class="btn btn-info btn-sm" value="on">${showIntersectionLabel}</button>
                                        <button class="btn btn-info btn-sm" value="off">${hideIntersectionLabel}</button>
                                    </div>
                                </div>
                            </div>"""
    }

}
