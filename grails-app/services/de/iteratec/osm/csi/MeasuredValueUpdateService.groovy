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

import org.joda.time.DateTime

import de.iteratec.osm.report.chart.MeasuredValueUtilService
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultService

/**
 * Provides methods for calculating and retrieving {@link MeasuredValue}s.
 * @author nkuhn, fpavkovic
 *
 */
class MeasuredValueUpdateService {
	
	EventMeasuredValueService eventMeasuredValueService
	PageMeasuredValueService pageMeasuredValueService
	ShopMeasuredValueService shopMeasuredValueService
	MeasuredValueUtilService measuredValueUtilService
	JobResultService jobResultService
	
	/**
	 * Calculates or recalculates hourly-job {@link MeasuredValue}s which depend from param newResult.
	 * Marks weekly {@link MeasuredValue}s which depend from param newResult as MeasuredValue.Calculated.Not.
	 * @param newResult
	 */
	void createOrUpdateDependentMvs(EventResult newResult) {
		JobResult jobResult = jobResultService.findJobResultByEventResult(newResult)
		DateTime testCompletion = new DateTime(jobResult.date)
		
		DateTime hourlyStart = measuredValueUtilService.resetToStartOfActualInterval(testCompletion, MeasuredValueInterval.HOURLY)
		eventMeasuredValueService.createOrUpdateHourlyValue(hourlyStart, newResult)
		
		MeasuredValueInterval dailyInterval=MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY);
		DateTime dailyStart = measuredValueUtilService.resetToStartOfActualInterval(testCompletion, MeasuredValueInterval.DAILY)
		markMvs(dailyStart, newResult, dailyInterval)
		
		MeasuredValueInterval weeklyInterval=MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY);
		DateTime weeklyStart = measuredValueUtilService.resetToStartOfActualInterval(testCompletion, MeasuredValueInterval.WEEKLY)
		markMvs(weeklyStart, newResult, weeklyInterval)
	}
	
	private void markMvs(DateTime startOfInterval, EventResult newResult, MeasuredValueInterval interval){		
		
		pageMeasuredValueService.markMvAsOutdated(startOfInterval, newResult, interval)
		shopMeasuredValueService.markMvAsOutdated(startOfInterval, newResult, interval)
		
	}
	
}
