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

package de.iteratec.osm.csi

import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import grails.transaction.Transactional
import org.joda.time.DateTime
import org.springframework.transaction.annotation.Propagation

/**
 * Provides methods for calculating and retrieving {@link CsiAggregation}s.
 * @author nkuhn, fpavkovic
 *
 */
@Transactional
class CsiAggregationUpdateService {

	EventCsiAggregationService eventCsiAggregationService
	PageCsiAggregationService pageCsiAggregationService
	ShopCsiAggregationService shopCsiAggregationService
	CsiSystemCsiAggregationService csiSystemCsiAggregationService
	CsiAggregationUtilService csiAggregationUtilService

	/**
	 *
	 * Calculates or recalculates hourly-job {@link CsiAggregation}s which depend from param newResultId.
	 * Marks weekly {@link CsiAggregation}s which depend from param newResult as CsiAggregation.Calculated.Not.
	 *
	 * @param eventResultId
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	void createOrUpdateDependentMvs(long eventResultId) {
		EventResult erToUpdateCsiAggregationsFor = EventResult.get(eventResultId)
		if (erToUpdateCsiAggregationsFor) createOrUpdateDependentMvs(erToUpdateCsiAggregationsFor)
	}


	/**
	 * Calculates or recalculates hourly-job {@link CsiAggregation}s which depend from param newResult.
	 * Marks weekly {@link CsiAggregation}s which depend from param newResult as CsiAggregation.Calculated.Not.
	 * @param newResult
	 */
	void createOrUpdateDependentMvs(EventResult newResult) {

		JobResult jobResult = newResult.jobResult;
		DateTime testCompletion = new DateTime(jobResult.date)
		
		DateTime hourlyStart = csiAggregationUtilService.resetToStartOfActualInterval(testCompletion, CsiAggregationInterval.HOURLY)
		eventCsiAggregationService.createOrUpdateHourlyValue(hourlyStart, newResult)
		
		CsiAggregationInterval dailyInterval=CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY);
		DateTime dailyStart = csiAggregationUtilService.resetToStartOfActualInterval(testCompletion, CsiAggregationInterval.DAILY)
		markMvs(dailyStart, newResult, dailyInterval)
		
		CsiAggregationInterval weeklyInterval=CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY);
		DateTime weeklyStart = csiAggregationUtilService.resetToStartOfActualInterval(testCompletion, CsiAggregationInterval.WEEKLY)
		markMvs(weeklyStart, newResult, weeklyInterval)
	}
	
	private void markMvs(DateTime startOfInterval, EventResult newResult, CsiAggregationInterval interval){
		
		pageCsiAggregationService.markMvAsOutdated(startOfInterval, newResult, interval)
		shopCsiAggregationService.markMvAsOutdated(startOfInterval, newResult, interval)
		csiSystemCsiAggregationService.markCaAsOutdated(startOfInterval, newResult, interval)

	}
	
}
