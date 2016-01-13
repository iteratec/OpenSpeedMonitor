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

import de.iteratec.osm.OsmConfigCacheService
import org.joda.time.DateTime

import de.iteratec.osm.report.chart.MeasuredValueDaoService
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent
import de.iteratec.osm.report.chart.MeasuredValueUpdateEventDaoService
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.EventResultService
import de.iteratec.osm.result.JobResultDaoService
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import de.iteratec.osm.measurement.environment.BrowserService

class EventMeasuredValueService {
	
	MeasuredValueTagService measuredValueTagService
	EventResultService eventResultService
	PerformanceLoggingService performanceLoggingService
	JobService jobService
	OsmConfigCacheService osmConfigCacheService
	JobResultDaoService jobResultDaoService
	MeasuredValueDaoService measuredValueDaoService
	BrowserService browserService
	WeightingService weightingService
	MeanCalcService meanCalcService
	MeasuredValueUpdateEventDaoService measuredValueUpdateEventDaoService
	
	/**
	 * Just gets MeasuredValues from DB. No creation or calculation.
	 * @param fromDate
	 * @param toDate
	 * @param targetInterval
	 * @return
	 */
	List<MeasuredValue> findAll(Date fromDate, Date toDate, MeasuredValueInterval targetInterval) {
		List<MeasuredValue> result = []

		def query = MeasuredValue.where {
			started >= fromDate
			started <= toDate
			interval == targetInterval
			aggregator == AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
		}
		result = query.list()
		return result
	}

	/**
	 * Calculates or recalculates hourly-job {@link MeasuredValue}s which depend from param newResult.
	 * @param newResult
	 */
    void createOrUpdateHourlyValue(DateTime hourlyStart, EventResult newResult){
		String resultTag = newResult.tag 
		if (resultTag != null && measuredValueTagService.isValidHourlyEventTag(resultTag)) {
			AggregatorType eventAggregator = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
			MeasuredValue hmv = ensurePresence(
				hourlyStart,
				MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY),
				resultTag,
				eventAggregator,
				true)
			calcMvForJobAggregatorWithoutQueryResultsFromDb(hmv, newResult)
		}
		
	}
	
	/**
	 * Provides all hourly event-{@link MeasuredValue}s between toDate and fromDate for query-params jobs mvQueryParams.
	 * Non-existent {@link MeasuredValue}s will NOT be created and/or calculated. That happens exclusively on arrival of {@link EventResult}s in backgound.
	 * @param fromDate
	 * @param toDate
	 * @param mvQueryParams
	 * 				Contains all parameters necessary for querying {@link MeasuredValue}s from db.
	 * @return
	 */
	List<MeasuredValue> getHourylMeasuredValues(Date fromDate, Date toDate, MvQueryParams mvQueryParams) {
		List<MeasuredValue> calculatedMvs = []
		if (fromDate>toDate) {
			throw new IllegalArgumentException("toDate must not be later than fromDate: fromDate=${fromDate}; toDate=${toDate}")
		}
		
		if (validateMvQueryParams(mvQueryParams) == false){
			throw new IllegalArgumentException("QuerParams for Event-MeasuredValues aren't valid: ${mvQueryParams}")
		}
		
		DateTime toDateTime = new DateTime(toDate)
		DateTime fromDateTime = new DateTime(fromDate)
		
		calculatedMvs.addAll(getAllCalculatedHourlyMvs(mvQueryParams, fromDateTime, toDateTime))
		return calculatedMvs
	}
	private getAllCalculatedHourlyMvs(MvQueryParams mvQueryParams, DateTime fromDateTime, DateTime toDateTimeEndOfInterval){
		String queryPattern = measuredValueTagService.getTagPatternForHourlyMeasuredValues(mvQueryParams).pattern();
		return queryPattern != null ?
			measuredValueDaoService.getMvs(
					fromDateTime.toDate(), 
					toDateTimeEndOfInterval.toDate(),
					queryPattern,
					MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY),
					AggregatorType.findByName(AggregatorType.MEASURED_EVENT))
			: []
	}
	
	private MeasuredValue ensurePresence(DateTime startDate, MeasuredValueInterval interval, String tag, AggregatorType eventAggregator, boolean initiallyClosed) {
		MeasuredValue toCreateAndOrCalculate
		performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "ensurePresence.findByStarted", IndentationDepth.FOUR){
			toCreateAndOrCalculate = MeasuredValue.findByStartedAndIntervalAndAggregatorAndTag(startDate.toDate(), interval, eventAggregator, tag)
			log.debug("MeasuredValue.findByStartedAndIntervalAndAggregatorAndTag delivered ${toCreateAndOrCalculate?'a':'no'} result")
		}
		if (!toCreateAndOrCalculate) {
			toCreateAndOrCalculate = new MeasuredValue(
				started: startDate.toDate(),
				interval: interval,
				aggregator: eventAggregator,
				tag: tag,
				value: null,
				resultIds: '',
				closedAndCalculated: initiallyClosed
			).save(failOnError: true)
		}
		return toCreateAndOrCalculate
	}
	/**
	 * Re-calculates {@link MeasuredValue} toBeCalculated cause data-basis changed with new {@link EventResult} newResult.
	 * @param toBeCalculated
	 * @param newResult
	 * @return
	 */
	private MeasuredValue calcMvForJobAggregatorWithoutQueryResultsFromDb(MeasuredValue toBeCalculated, EventResult newResult) {
		Integer countResults = toBeCalculated.countResultIds()
		Double newValue
		Boolean csiRelevance = eventResultService.isCsiRelevant(
			newResult, 
			osmConfigCacheService.getCachedMinDocCompleteTimeInMillisecs(24), 
			osmConfigCacheService.getCachedMaxDocCompleteTimeInMillisecs(24)) 
		if(csiRelevance && !toBeCalculated.containsInResultIds(newResult.ident())){
			if (countResults > 0 && newResult.customerSatisfactionInPercent != null) {
				Double sumOfPreviousResults = (toBeCalculated.value?toBeCalculated.value:0) * countResults
				newValue = (sumOfPreviousResults + newResult.customerSatisfactionInPercent) / (countResults + 1)
			} else if (countResults == 0) {
				newValue = newResult.customerSatisfactionInPercent
			}
			toBeCalculated.value = newValue
			toBeCalculated.addToResultIds(newResult.ident())
		}
		toBeCalculated.save(failOnError:true)
		measuredValueUpdateEventDaoService.createUpdateEvent(toBeCalculated.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
		return toBeCalculated
	}
	
	/**
	 * Proofs whether all attributes of mvQueryParams are initialized, non <code>null</code>.
	 * @param mvQueryParams
	 * @return
	 */
	private Boolean validateMvQueryParams (MvQueryParams mvQueryParams){
		mvQueryParams.jobGroupIds != null && 
		mvQueryParams.browserIds != null && 
		mvQueryParams.locationIds != null && 
		mvQueryParams.measuredEventIds != null && 
		mvQueryParams.pageIds != null ? 
			true : false
	}
	
}
