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

import de.iteratec.osm.report.chart.MeasuredValueDaoService
import de.iteratec.osm.report.chart.MeasuredValueUtilService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent
import de.iteratec.osm.report.chart.MeasuredValueUpdateEventDaoService
import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultService
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel

/**
 * Provides methods for calculating and retrieving {@link MeasuredValue}s with {@link AggregatorType#getName()}=={@link AggregatorType#JOB}.
 * @author nkuhn
 *
 */
class ShopMeasuredValueService {
	
	PageMeasuredValueService pageMeasuredValueService
	CsiHelperService csiHelperService
	MeanCalcService meanCalcService
	MeasuredValueTagService measuredValueTagService
	PerformanceLoggingService performanceLoggingService
	JobService jobService
	JobResultService jobResultService
	MeasuredValueDaoService measuredValueDaoService
	MeasuredValueUtilService measuredValueUtilService
	WeightingService weightingService
	MeasuredValueUpdateEventDaoService measuredValueUpdateEventDaoService

    /**
	 * Just gets {@link MeasuredValue}s from DB. No creation or calculation.
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
			aggregator == AggregatorType.findByName(AggregatorType.SHOP)
		}
		result = query.list()
		return result
	}
	/**
	 * Just gets {@link MeasuredValue}s from DB. No creation or calculation.
	 * @param fromDate
	 * @param toDate
	 * @param targetInterval
	 * @param csiGroups
	 * @return
	 */
	List<MeasuredValue> findAll(Date fromDate, Date toDate, MeasuredValueInterval targetInterval, List<JobGroup> csiGroups) {
		List<MeasuredValue> result = []
		if (csiGroups.size() == 0) {
			return result
		}
		String tagPattern = measuredValueTagService.getTagPatternForWeeklyShopMvsWithJobGroups(csiGroups)
		result = measuredValueDaoService.getMvs(fromDate, toDate, tagPattern, targetInterval, AggregatorType.findByName(AggregatorType.SHOP))
		return result
	}
	
	/**
	 * Marks {@link MeasuredValue}s which depend from param newResult and who's interval contains newResult as outdated.
	 * @param start
	 * 				00:00:00 of the respective interval.
	 * @param newResult
	 * 				New {@link EventResult}.
	 */
	void markMvAsOutdated(DateTime start, EventResult newResult, MeasuredValueInterval interval){
		
		JobResult jobResult = newResult.jobResult;
		JobGroup jobGroup = jobService.getCsiJobGroupOf(jobResult.job)
		String shopTag = measuredValueTagService.createShopAggregatorTag(jobGroup)
		
		if (jobGroup && jobGroup.groupType==JobGroupType.CSI_AGGREGATION && shopTag) {
			MeasuredValue shopMv = ensurePresence(start, interval, shopTag)
			measuredValueUpdateEventDaoService.createUpdateEvent(shopMv.ident(), MeasuredValueUpdateEvent.UpdateCause.OUTDATED)
		}
		
	}
	
	/**
	 * Provides all shop-{@link MeasuredValue}s of given csi-{@link JobGroup}s between toDate and fromDate.
	 * Non-existent {@link MeasuredValue}s will be created.
	 * All {@link MeasuredValue}s with @{link MeasuredValue.Calculated.Not} will be calculated and persisted with @{link MeasuredValue.Calculated.Yes}
	 * or @{link MeasuredValue.Calculated.YesNoData}.
	 * @param fromDate
	 * @param toDate
	 * @param csiGroups
	 * @return
	 */
	List<MeasuredValue> getOrCalculateShopMeasuredValues(Date fromDate, Date toDate, MeasuredValueInterval interval, List<JobGroup> csiGroups) {
		if (fromDate>toDate) {
			throw new IllegalArgumentException("toDate must not be later than fromDate: fromDate=${fromDate}; toDate=${toDate}")
		}
		DateTime toDateTime = new DateTime(toDate)
		DateTime fromDateTime = new DateTime(fromDate)
		
		Integer numberOfIntervals = measuredValueUtilService.getNumberOfIntervals(fromDateTime, toDateTime, interval)
		
		List<MeasuredValue> existingMeasuredValues = findAll(fromDateTime.toDate(), toDateTime.toDate(), interval, csiGroups)
		
		List<MeasuredValue> openMeasuredValues = existingMeasuredValues.findAll{ ! it.closedAndCalculated }
		Boolean allMeasuredValuesExist = existingMeasuredValues.size() == numberOfIntervals * csiGroups.size()
		if (allMeasuredValuesExist && openMeasuredValues.size() == 0) {
			return existingMeasuredValues
		}
		
		List<MeasuredValueUpdateEvent> updateEvents = []
		if (openMeasuredValues.size() > 0) updateEvents.addAll(measuredValueDaoService.getUpdateEvents(openMeasuredValues*.ident()))
		List<MeasuredValue> measuredValuesToBeCalculated = openMeasuredValues.findAll{it.hasToBeCalculatedAccordingEvents(updateEvents)}
		
		if (allMeasuredValuesExist && measuredValuesToBeCalculated.size() == 0) {
			
			return existingMeasuredValues
			
		} else {
		
			List<MeasuredValue> calculatedMeasuredvalues = []
			DateTime currentDateTime = fromDateTime
			while (!currentDateTime.isAfter(toDateTime)) {
				performanceLoggingService.logExecutionTime(LogLevel.INFO, " get/create/calculate ${interval.name} shop-MeasureValue for: ${currentDateTime}", IndentationDepth.TWO){
					List<MeasuredValue> existingMvsOfCurrentTime = existingMeasuredValues.findAll{new DateTime(it.started) == currentDateTime}
					List<MeasuredValue> mvsToBeCalculatedOfCurrentTime = measuredValuesToBeCalculated.findAll{new DateTime(it.started) == currentDateTime}
					if (existingMvsOfCurrentTime.size() == csiGroups.size() && mvsToBeCalculatedOfCurrentTime.size() == 0) {
						
						calculatedMeasuredvalues.addAll(existingMvsOfCurrentTime)
						
					} else {
					
						calculatedMeasuredvalues.addAll(getOrCalculateShopMvs(currentDateTime, interval, csiGroups, updateEvents))
						
					}
				}
				currentDateTime = measuredValueUtilService.addOneInterval(currentDateTime, interval.intervalInMinutes)
			}
			return calculatedMeasuredvalues
		}
	}
	
	private List<MeasuredValue> getOrCalculateShopMvs(DateTime toGetMvsFor, MeasuredValueInterval interval, List<JobGroup> csiGroups, List<MeasuredValueUpdateEvent> updateEvents) {
	 	List<MeasuredValue> smvs = []
		csiGroups.each {csiGroup ->
			String tag = measuredValueTagService.createShopAggregatorTag(csiGroup)
			smvs.addAll(ensurePresenceAndCalculation(toGetMvsFor, interval, tag, updateEvents))
		}
		return smvs
	}
	
	/**
	 * Creates respective {@link MeasuredValue} if it doesn't exist and calculates it.
	 * After calculation status is {@link MeasuredValue.Calculated.Yes} or {@link MeasuredValue.Calculated.YesNoData}.
	 * @param startDate
	 * @param interval
	 * @param tag
	 * @return
	 */
	MeasuredValue ensurePresenceAndCalculation(DateTime startDate, MeasuredValueInterval interval, String tag, List<MeasuredValueUpdateEvent> updateEvents) {
		MeasuredValue toCreateAndOrCalculate
		performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "ensurePresence", IndentationDepth.THREE){
			toCreateAndOrCalculate = ensurePresence(startDate, interval, tag)
		}
		if (toCreateAndOrCalculate.hasToBeCalculatedAccordingEvents(updateEvents)) {
			performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "calculateCustomerSatisfactionMeasuredValue (interval=${interval.intervalInMinutes}; aggregator=shop)", IndentationDepth.THREE){
				toCreateAndOrCalculate = calcMv(toCreateAndOrCalculate)
			}
		}
		return toCreateAndOrCalculate
	}
	
	private MeasuredValue ensurePresence(DateTime startDate, MeasuredValueInterval interval, String tag) {
		MeasuredValue toCreateAndOrCalculate
		AggregatorType shopAggregator = AggregatorType.findByName(AggregatorType.SHOP)
		performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "ensurePresence.findByStarted", IndentationDepth.FOUR){
			toCreateAndOrCalculate = MeasuredValue.findByStartedAndIntervalAndAggregatorAndTag(startDate.toDate(), interval, shopAggregator, tag)
			log.debug("MeasuredValue.findByStartedAndIntervalAndAggregatorAndTag delivered ${toCreateAndOrCalculate?'a':'no'} result")
		}
		if (!toCreateAndOrCalculate) {
			toCreateAndOrCalculate = new MeasuredValue(
				started: startDate.toDate(),
				interval: interval,
				aggregator: shopAggregator,
				tag: tag,
				value: null,
				resultIds: ''
			).save(failOnError: true)
		}
		return toCreateAndOrCalculate
	}
	
	/**
	 * Calculates the given {@link MeasuredValue} toBeCalculated.
	 * @param toBeCalculated 
	 * 		The {@link MeasuredValue} to be calculated.
	 * @return The calculated {@link MeasuredValue}.
	 */
	private MeasuredValue calcMv(MeasuredValue toBeCalculated) {
		
		Contract.requiresArgumentNotNull("toBeCalculated", toBeCalculated);
		
		JobGroup groupOfMv = measuredValueTagService.findJobGroupOfWeeklyShopTag(toBeCalculated.tag)
		List<MeasuredValue> pageMeasuredValues = pageMeasuredValueService.getOrCalculatePageMeasuredValues(
			toBeCalculated.started, toBeCalculated.started, toBeCalculated.getInterval(), [groupOfMv])
		
		List<WeightedCsiValue> weightedCsiValues = []
		
		if (pageMeasuredValues.size() > 0) {
			weightedCsiValues = weightingService.getWeightedCsiValues(pageMeasuredValues, [WeightFactor.PAGE] as Set)
		}
		if (weightedCsiValues.size()>0) {
			toBeCalculated.value = meanCalcService.calculateWeightedMean(weightedCsiValues*.weightedValue)
		}
		measuredValueUpdateEventDaoService.createUpdateEvent(toBeCalculated.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
		return toBeCalculated
	}
	
	private Page getPageFromPageMv(MeasuredValue toGetPageFrom){
		return measuredValueTagService.findPageOfWeeklyPageTag(toGetPageFrom.tag)
	}
	
	/**
	 * Provides all weekly shop-{@link MeasuredValue}s of all csi-{@link JobGroup}s between toDate and fromDate.
	 * Non-existent {@link MeasuredValue}s will be created.
	 * All {@link MeasuredValue}s with @{link MeasuredValue.Calculated.Not} will be calculated and persisted with @{link MeasuredValue.Calculated.Yes}
	 * or @{link MeasuredValue.Calculated.YesNoData}.
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	List<MeasuredValue> getOrCalculateWeeklyShopMeasuredValues(Date fromDate, Date toDate) {
		MeasuredValueInterval mvInterval=MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)
		return getOrCalculateShopMeasuredValues(fromDate, toDate, mvInterval, JobGroup.findAllByGroupType(JobGroupType.CSI_AGGREGATION))
	}
}
