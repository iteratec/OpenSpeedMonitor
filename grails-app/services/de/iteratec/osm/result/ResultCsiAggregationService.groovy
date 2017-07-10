/*
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
*
* Licensed under the Apache License, Version 2.0 (the "License")
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

package de.iteratec.osm.result

import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.PerformanceLoggingService

/**
 * Calculates {@link de.iteratec.osm.report.chart.CsiAggregation}s for EventResults.
 *
 * @author rhe
 */
class ResultCsiAggregationService {

    /** injected by grails */
    CsiAggregationUtilService csiAggregationUtilService
    BrowserService browserService
    EventResultDaoService eventResultDaoService
    PerformanceLoggingService performanceLoggingService
    I18nService i18nService


    static Map<MeasurandGroup, List<Measurand>> getAggregatorMapForOptGroupSelect() {
        Map<MeasurandGroup, List<Measurand>> result = [:]
        MeasurandGroup.values().each { result.put(it, [])}
        Measurand.values().each {result.get(it.getMeasurandGroup()).add(it)}

        return Collections.unmodifiableMap(result)
    }
}
