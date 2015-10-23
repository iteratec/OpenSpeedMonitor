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

class D3HtmlCreator {
    /**
     * Creates divs for de.iteratec.osm.d3data.MultiLineChart
     * @param chartIdentifer a unique name for chart on the page
     * @param modal if modal == true the created chart div is smaller
     */
    def generateMultiLineChartHtml = {chartIdentifier, modal ->
        def writer = new StringWriter()

        if(modal){
            return writer << """<div class="span4" id="${chartIdentifier}">
                                    <div id="tooltip" class="hidden">
                                        <p><strong id="heading"></strong></p>
                                        <p><span id="info"></span></p>
                                    </div>
                                </div>"""
        }
        else {
            return writer << """<div class="span8" id="${chartIdentifier}">
                                    <div id="tooltip" class="hidden">
                                        <p><strong id="heading"></strong></p>
                                        <p><span id="info"></span></p>
                                    </div>
                                </div><br />"""
        }
    }
}
