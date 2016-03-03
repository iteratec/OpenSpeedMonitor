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
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.result.CsiValueService
import org.joda.time.DateTime

import de.iteratec.osm.report.chart.CsiAggregationDaoService
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import de.iteratec.osm.report.chart.CsiAggregationUpdateEventDaoService
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.EventResultService
import de.iteratec.osm.result.JobResultDaoService
import de.iteratec.osm.result.CsiAggregationTagService
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import de.iteratec.osm.measurement.environment.BrowserService

class EventCsiAggregationService {
	
	CsiAggregationTagService csiAggregationTagService
	EventResultService eventResultService
	PerformanceLoggingService performanceLoggingService
	JobService jobService
	OsmConfigCacheService osmConfigCacheService
	JobResultDaoService jobResultDaoService
	CsiAggregationDaoService csiAggregationDaoService
	BrowserService browserService
	WeightingService weightingService
	MeanCalcService meanCalcService
	CsiAggregationUpdateEventDaoService csiAggregationUpdateEventDaoService
	CsiValueService csiValueService
	
	/**
	 * Just gets CsiAggregations from DB. No creation or calculation.
	 * @param fromDate
	 * @param toDate
	 * @param targetInterval
	 * @return
	 */
	List<CsiAggregation> findAll(Date fromDate, Date toDate, CsiAggregationInterval targetInterval, ConnectivityProfile connProfile = null) {
		List<CsiAggregation> result = []
		def query

		if(connProfile == null) {
			query = CsiAggregation.where {
				started >= fromDate
				started <= toDate
				interval == targetInterval
				aggregator == AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
			}
		} else {
			query = CsiAggregation.where {
				started >= fromDate
				started <= toDate
				interval == targetInterval
				aggregator == AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
				connectivityProfile == connProfile
			}
		}
		result = query.list()
		return result
	}

	/**
	 * Calculates or recalculates hourly-job {@link CsiAggregation}s which depend from param newResult.
	 * @param newResult
	 */
    void createOrUpdateHourlyValue(DateTime hourlyStart, EventResult newResult){
		String resultTag = newResult.tag 
		if (resultTag != null && csiAggregationTagService.isValidHourlyEventTag(resultTag)) {
			AggregatorType eventAggregator = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
			CsiAggregation hmv = ensurePresence(
				hourlyStart,
				CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY),
				resultTag,
				eventAggregator,
				true,
				newResult.connectivityProfile)
			calcCsiAggregationForJobAggregatorWithoutQueryResultsFromDb(hmv, newResult)
		}
		
	}
	
	/**
	 * Provides all hourly event-{@link CsiAggregation}s between toDate and fromDate for query-params jobs mvQueryParams.
	 * Non-existent {@link CsiAggregation}s will NOT be created and/or calculated. That happens exclusively on arrival of {@link EventResult}s in backgound.
	 * @param fromDate
	 * @param toDate
	 * @param mvQueryParams
	 * 				Contains all parameters necessary for querying {@link CsiAggregation}s from db.
	 * @return
	 */
	List<CsiAggregation> getHourlyCsiAggregations(Date fromDate, Date toDate, MvQueryParams mvQueryParams) {
		List<CsiAggregation> calculatedMvs = []
		if (fromDate>toDate) {
			throw new IllegalArgumentException("toDate must not be later than fromDate: fromDate=${fromDate}; toDate=${toDate}")
		}
		
		if (validateMvQueryParams(mvQueryParams) == false){
			throw new IllegalArgumentException("QuerParams for Event-CsiAggregations aren't valid: ${mvQueryParams}")
		}
		
		DateTime toDateTime = new DateTime(toDate)
		DateTime fromDateTime = new DateTime(fromDate)
		
		calculatedMvs.addAll(getAllCalculatedHourlyCas(mvQueryParams, fromDateTime, toDateTime))
		return calculatedMvs
	}
	private getAllCalculatedHourlyCas(MvQueryParams mvQueryParams, DateTime fromDateTime, DateTime toDateTimeEndOfInterval){
		String queryPattern = csiAggregationTagService.getTagPatternForHourlyCsiAggregations(mvQueryParams).pattern();
		List<ConnectivityProfile> connectivityProfilesInQuery = ConnectivityProfile.findAllByIdInList(new ArrayList<Long>(mvQueryParams.connectivityProfileIds))
		return queryPattern != null ?
			   csiAggregationDaoService.getMvs(
					fromDateTime.toDate(), 
					toDateTimeEndOfInterval.toDate(),
					queryPattern,
					CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY),
					AggregatorType.findByName(AggregatorType.MEASURED_EVENT),
					connectivityProfilesInQuery)
			: []
	}
	
	private CsiAggregation ensurePresence(DateTime startDate, CsiAggregationInterval interval, String tag, AggregatorType eventAggregator, boolean initiallyClosed, ConnectivityProfile connectivityProfile) {
		CsiAggregation toCreateAndOrCalculate
		performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "ensurePresence.findByStarted", IndentationDepth.FOUR){
			toCreateAndOrCalculate = CsiAggregation.findByStartedAndIntervalAndAggregatorAndTagAndConnectivityProfile(startDate.toDate(), interval, eventAggregator, tag, connectivityProfile)
			log.debug("CsiAggregation.findByStartedAndIntervalAndAggregatorAndTagAndConnectivityProfile delivered ${toCreateAndOrCalculate?'a':'no'} result")
		}
		if (!toCreateAndOrCalculate) {
			toCreateAndOrCalculate = new CsiAggregation(
				started: startDate.toDate(),
				interval: interval,
				aggregator: eventAggregator,
				tag: tag,
				csByWptDocCompleteInPercent: null,
				csByWptVisuallyCompleteInPercent: null,
				underlyingEventResultsByWptDocComplete: '',
				closedAndCalculated: initiallyClosed,
				connectivityProfile: connectivityProfile
			).save(failOnError: true)
		}
		return toCreateAndOrCalculate
	}
	/**
	 * Re-calculates {@link CsiAggregation} toBeCalculated cause data-basis changed with new {@link EventResult} newResult.
	 * @param toBeCalculated
	 * @param newResult
	 * @return
	 */
	private CsiAggregation calcCsiAggregationForJobAggregatorWithoutQueryResultsFromDb(CsiAggregation toBeCalculated, EventResult newResult) {
		Integer countUnderlyingEventResultsByWptDocComplete = toBeCalculated.countUnderlyingEventResultsByWptDocComplete()
		Integer countUnderlyingEventResultsByWptVisuallyComplete = toBeCalculated.underlyingEventResultsByVisuallyComplete.size()
		Double newCsByWptDocCompleteInPercent
		Double newCsByWptVisuallyCompleteInPercent
		if(csiValueService.isCsiRelevant(newResult)){
			// add value for csByDocComplete
			if(!toBeCalculated.containsInUnderlyingEventResultsByWptDocComplete(newResult.ident())) {
				if (countUnderlyingEventResultsByWptDocComplete > 0 && newResult.csByWptDocCompleteInPercent != null) {
					Double sumOfPreviousResults = (toBeCalculated.csByWptDocCompleteInPercent ?
												   toBeCalculated.csByWptDocCompleteInPercent :
												   0) * countUnderlyingEventResultsByWptDocComplete
					newCsByWptDocCompleteInPercent = (sumOfPreviousResults + newResult.csByWptDocCompleteInPercent) / (countUnderlyingEventResultsByWptDocComplete + 1)
				} else if (countUnderlyingEventResultsByWptDocComplete == 0) {
					newCsByWptDocCompleteInPercent = newResult.csByWptDocCompleteInPercent
				}
				toBeCalculated.csByWptDocCompleteInPercent = newCsByWptDocCompleteInPercent
				toBeCalculated.addToUnderlyingEventResultsByWptDocComplete(newResult.ident())
			}

			//add value for csByVisuallyComplete
			if(!toBeCalculated.underlyingEventResultsByVisuallyComplete.contains(newResult)) {
				if (countUnderlyingEventResultsByWptVisuallyComplete > 0 && newResult.csByWptVisuallyCompleteInPercent != null) {
					Double sumOfPreviousResults = (toBeCalculated.csByWptVisuallyCompleteInPercent ?
												   toBeCalculated.csByWptVisuallyCompleteInPercent :
												   0) * countUnderlyingEventResultsByWptVisuallyComplete
					newCsByWptVisuallyCompleteInPercent = (sumOfPreviousResults + newResult.csByWptVisuallyCompleteInPercent) / (countUnderlyingEventResultsByWptVisuallyComplete + 1)
				} else if (countUnderlyingEventResultsByWptVisuallyComplete == 0) {
					newCsByWptVisuallyCompleteInPercent = newResult.csByWptVisuallyCompleteInPercent
				}
				toBeCalculated.csByWptVisuallyCompleteInPercent = newCsByWptVisuallyCompleteInPercent
				toBeCalculated.underlyingEventResultsByVisuallyComplete.add(newResult)
			}
		}
		toBeCalculated.save(failOnError:true)
		csiAggregationUpdateEventDaoService.createUpdateEvent(toBeCalculated.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
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
