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

package de.iteratec.osm.d3

import de.iteratec.osm.d3Data.D3HtmlCreator

/**
 * TagLib for D3 charts
 */
class D3ChartTagLib {
    static namespace = "iteratec"

    /**
     * attrs['chartIdentifer'] has to be unique for the page, otherwise d3 overrides other charts
     * If attrs['modal'] == true the created container is smaller
     */
    def multiLineChart = { attrs, body ->
        def htmlCreator = new D3HtmlCreator()
        out << htmlCreator.generateMultiLineChartHtml(attrs['chartIdentifier'], attrs['modal'])

        return out.toString()
    }
}
