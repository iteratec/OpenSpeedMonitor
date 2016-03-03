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

package de.iteratec.osm.result

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.CsiValue
import de.iteratec.osm.report.chart.CsiAggregation


class CsiValueService {

    OsmConfigCacheService osmConfigCacheService
    static transactional = true

    /**
     * Whether or not this value should be factored in csi-calculations.
     * <ul>
     * <li>{@link EventResult}s are relevant if they have set a loadTimeInMillisecs and customerSatisfactionInPercent and customerSatisfactionInPercent
     * is within valid range.</li>
     * <li>{@link MesauredValue}s are relevant if they have a state of {@link Calculated#Yes}</li>
     * </ul>
     * @return
     * @see CsiConfigCacheService
     */
    public boolean isCsiRelevant(CsiValue csiValue){
        switch (csiValue){
            case {it instanceof EventResult}: return isCsiRelevant(csiValue as EventResult)
            case {it instanceof CsiAggregation}: return isCsiRelevant(csiValue as CsiAggregation)
            default:return false
        }
    }

    public boolean isCsiRelevant(EventResult eventResult) {
        return eventResult.csByWptDocCompleteInPercent && eventResult.docCompleteTimeInMillisecs &&
                (eventResult.docCompleteTimeInMillisecs > osmConfigCacheService.getCachedMinDocCompleteTimeInMillisecs(24) &&
                        eventResult.docCompleteTimeInMillisecs < osmConfigCacheService.getCachedMaxDocCompleteTimeInMillisecs(24)) &&
                eventResult.jobResult.job.jobGroup.csiConfiguration != null
    }

    public boolean isCsiRelevant(CsiAggregation csiAggregation) {
        return csiAggregation.isCalculated() && csiAggregation.csByWptDocCompleteInPercent != null
    }
}
