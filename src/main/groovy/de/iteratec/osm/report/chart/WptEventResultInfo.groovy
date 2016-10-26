/*
* OpenSpeedMonitor (OSM)
* Copyright 2016 iteratec GmbH
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

package de.iteratec.osm.report.chart.dao

import de.iteratec.osm.result.CachedView
import groovy.transform.EqualsAndHashCode


/*
* A data structure which is used to store the relevant infos to build
* the url linking to the WebPageTest details of a datapoint in the chart.
* */
@EqualsAndHashCode
class WptEventResultInfo {

    String serverBaseUrl
    String testId
    Integer numberOfWptRun
    CachedView cachedView
    Integer oneBaseStepIndexInJourney
}
