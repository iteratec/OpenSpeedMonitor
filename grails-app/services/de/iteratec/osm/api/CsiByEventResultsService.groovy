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

package de.iteratec.osm.api

import de.iteratec.osm.api.dto.CsiByEventResultsDto
import de.iteratec.osm.csi.CsTargetGraph
import de.iteratec.osm.csi.CsTargetGraphDaoService
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.MeanCalcService
import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.transaction.Transactional
import org.joda.time.DateTime

@Transactional
class CsiByEventResultsService {
	
	EventResultDaoService eventResultDaoService
	WeightingService weightingService
	MeanCalcService meanCalcService
	CsTargetGraphDaoService csTargetGraphDaoService
    PerformanceLoggingService performanceLoggingService

	/**
	 * <p>
	 * Calculates customer satisfaction index (CSI) for a hole system. Underlying data are the {@link EventResult}s which are queried by given queryParams.
	 * The system to calculate csi for should be set in queryParams.jobGroupIds.
	 * </p>
	 * @param start Start-timestamp for calculation:
	 * @param end End-timestamp for calculation:
	 * @param queryParams Params to query {@link EventResult}s for calculation. JobGroupId of target system should be set.
	 * @param weightFactors	Factors, {@link EventResult}s should be weighted for.
	 * @return Customer satisfaction index (CSI) for a hole system.
	 */
    public CsiByEventResultsDto retrieveCsi(DateTime start, DateTime end, MvQueryParams queryParams, Set<WeightFactor> weightFactors) {

        List<EventResult> eventResults
		performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[retrieveCsi] get event results', 2){
            eventResults = eventResultDaoService.getByStartAndEndTimeAndMvQueryParams(start.toDate(), end.toDate(), [CachedView.UNCACHED], queryParams)
        }

        log.info("retrieveCsi: ${eventResults.size()} EventResults building database for calculation.")
        List<WeightedCsiValue> weightedCsiValues = []

        if (eventResults.size() > 0) {
            JobGroup jobGroup
            CsiConfiguration csiConfiguration
            performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[retrieveCsi] get JobGroup and CsiConfiguration', 2){
                jobGroup = JobGroup.get(queryParams.jobGroupIds[0])
                csiConfiguration = jobGroup ? jobGroup.csiConfiguration : null
                if(!csiConfiguration) {
                    throw new IllegalArgumentException("there is no csi configuratin for jobGroup with id ${queryParams.jobGroupIds[0]}")
                }
            }
            performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[retrieveCsi] weight event results', 2){
                weightedCsiValues = weightingService.getWeightedCsiValues(eventResults, weightFactors, csiConfiguration)
            }
        }

        CsiByEventResultsDto csiDto
        performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[retrieveCsi] calculate weighted mean and prepare return value', 2){
            if (log.infoEnabled) {log.info("retrieveCsi: ${weightedCsiValues.size()} WeightedCsiValues were determined for ${eventResults.size()} EventResults.")}
            if (weightedCsiValues.size()>0) {
                double weightedValueAsPercentage = meanCalcService.calculateWeightedMean(weightedCsiValues*.weightedValue)
                double targetCsi = 100d
                CsTargetGraph targetGraph = csTargetGraphDaoService.getActualCsTargetGraph()
                if (targetGraph) {
                    targetCsi = targetGraph.getPercentOfDate(end)
                }
                csiDto = new CsiByEventResultsDto(
                        csiValueAsPercentage: weightedValueAsPercentage,
                        targetCsiAsPercentage:  targetCsi,
                        delta: weightedValueAsPercentage - targetCsi,
                        countOfMeasurings: weightedCsiValues*.underlyingEventResultIds.flatten().size()
                )
            }else{
                throw new IllegalArgumentException("For the following query-params a system-csi couldn't be calculated: ${queryParams}")
            }
        }

        return csiDto

	}
}
