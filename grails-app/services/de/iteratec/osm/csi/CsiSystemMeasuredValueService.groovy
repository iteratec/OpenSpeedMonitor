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

import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResultDaoService
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import org.joda.time.DateTime

/**
 * Provides methods for calculating and retrieving {@link CsiAggregation}s with {@link AggregatorType#getName()}=={@link AggregatorType#JOB}.
 * @author nkuhn
 *
 */
class CsiSystemMeasuredValueService {

    ShopMeasuredValueService shopMeasuredValueService
    MeanCalcService meanCalcService
    MeasuredValueTagService measuredValueTagService
    PerformanceLoggingService performanceLoggingService
    JobService jobService
    JobResultDaoService jobResultDaoService
    MeasuredValueDaoService measuredValueDaoService
    MeasuredValueUtilService measuredValueUtilService
    WeightingService weightingService
    MeasuredValueUpdateEventDaoService measuredValueUpdateEventDaoService

    /**
     * Just gets {@link CsiAggregation}s from DB. No creation or calculation.
     * @param fromDate
     * @param toDate
     * @param targetInterval
     * @return
     */
    List<CsiAggregation> findAll(Date fromDate, Date toDate, MeasuredValueInterval targetInterval) {
        List<CsiAggregation> result = []
        def query = CsiAggregation.where {
            started >= fromDate
            started <= toDate
            interval == targetInterval
            aggregator == AggregatorType.findByName(AggregatorType.CSI_SYSTEM)
        }
        result = query.list()
        return result
    }
    /**
     * Just gets {@link CsiAggregation}s from DB. No creation or calculation.
     * @param fromDate
     * @param toDate
     * @param targetInterval
     * @param csiSystems
     * @return
     */
    List<CsiAggregation> findAll(Date fromDate, Date toDate, MeasuredValueInterval targetInterval, List<CsiSystem> csiSystems) {
        List<CsiAggregation> result = []
        if (csiSystems.empty) {
            return result
        }

        result = measuredValueDaoService.getMvs(fromDate, toDate, targetInterval, AggregatorType.findByName(AggregatorType.CSI_SYSTEM),csiSystems)
        return result
    }

    /**
     * Marks {@link CsiAggregation}s which depend from param newResult and who's interval contains newResult as outdated.
     * @param start
     * 				00:00:00 of the respective interval.
     * @param newResult
     * 				New {@link EventResult}.
     */
    void markMvAsOutdated(DateTime start, EventResult newResult, MeasuredValueInterval interval) {

        List<JobGroupWeight> affectedJobGroupWeights = JobGroupWeight.findAllByJobGroup(newResult.jobResult.job.jobGroup)
        List<CsiSystem> affectedCsiSystems = CsiSystem.findAllByJobGroupWeights(affectedJobGroupWeights)

        if (affectedCsiSystems && jobGroup.groupType == JobGroupType.CSI_AGGREGATION) {
            affectedCsiSystems.each {
                CsiAggregation csiSystemMv = ensurePresence(start, interval, it)
                measuredValueUpdateEventDaoService.
                        createUpdateEvent(csiSystemMv.ident(), MeasuredValueUpdateEvent.UpdateCause.OUTDATED)
            }
        }

    }

    /**
     * Provides all shop-{@link CsiAggregation}s of given csi-{@link JobGroup}s between toDate and fromDate.
     * Non-existent {@link CsiAggregation}s will be created.
     * All {@link CsiAggregation}s with @{link CsiAggregation.Calculated.Not} will be calculated and persisted with @{link CsiAggregation.Calculated.Yes}* or @{link CsiAggregation.Calculated.YesNoData}.
     * @param fromDate
     * @param toDate
     * @param csiSystems
     * @return
     */
    List<CsiAggregation> getOrCalculateCsiSystemMeasuredValues(Date fromDate, Date toDate, MeasuredValueInterval interval, List<CsiSystem> csiSystems) {
        if (fromDate > toDate) {
            throw new IllegalArgumentException("toDate must not be later than fromDate: fromDate=${fromDate}; toDate=${toDate}")
        }
        DateTime toDateTime = new DateTime(toDate)
        DateTime fromDateTime = new DateTime(fromDate)

        Integer numberOfIntervals = measuredValueUtilService.getNumberOfIntervals(fromDateTime, toDateTime, interval)

        List<CsiAggregation> existingMeasuredValues = findAll(fromDateTime.toDate(), toDateTime.toDate(), interval, csiSystems)

        List<CsiAggregation> openMeasuredValues = existingMeasuredValues.findAll { !it.closedAndCalculated }
        Boolean allMeasuredValuesExist = existingMeasuredValues.size() == numberOfIntervals * csiSystems.size()
        if (allMeasuredValuesExist && openMeasuredValues.size() == 0) {
            return existingMeasuredValues
        }

        List<MeasuredValueUpdateEvent> updateEvents = []
        if (openMeasuredValues.size() > 0) updateEvents.addAll(measuredValueDaoService.getUpdateEvents(openMeasuredValues*.ident()))
        List<CsiAggregation> measuredValuesToBeCalculated = openMeasuredValues.findAll {
            it.hasToBeCalculatedAccordingEvents(updateEvents)
        }

        if (allMeasuredValuesExist && measuredValuesToBeCalculated.size() == 0) {

            return existingMeasuredValues

        } else {

            List<CsiAggregation> calculatedMeasuredvalues = []
            DateTime currentDateTime = fromDateTime
            while (!currentDateTime.isAfter(toDateTime)) {
                performanceLoggingService.logExecutionTime(LogLevel.INFO, " get/create/calculate ${interval.name} shop-MeasureValue for: ${currentDateTime}", IndentationDepth.TWO) {
                    List<CsiAggregation> existingMvsOfCurrentTime = existingMeasuredValues.findAll {
                        new DateTime(it.started) == currentDateTime
                    }
                    List<CsiAggregation> mvsToBeCalculatedOfCurrentTime = measuredValuesToBeCalculated.findAll {
                        new DateTime(it.started) == currentDateTime
                    }
                    if (existingMvsOfCurrentTime.size() == csiSystems.size() && mvsToBeCalculatedOfCurrentTime.size() == 0) {

                        calculatedMeasuredvalues.addAll(existingMvsOfCurrentTime)

                    } else {

                        calculatedMeasuredvalues.addAll(getOrCalculateCsiSystemMvs(currentDateTime, interval, csiSystems, updateEvents))

                    }
                }
                currentDateTime = measuredValueUtilService.addOneInterval(currentDateTime, interval.intervalInMinutes)
            }
            return calculatedMeasuredvalues
        }
    }

    private List<CsiAggregation> getOrCalculateCsiSystemMvs(DateTime toGetMvsFor, MeasuredValueInterval interval, List<CsiSystem> csiSystems, List<MeasuredValueUpdateEvent> updateEvents) {
        List<CsiAggregation> smvs = []
        csiSystems.each { csiSystem ->
            smvs.addAll(ensurePresenceAndCalculation(toGetMvsFor, interval, csiSystem, updateEvents))
        }
        return smvs
    }

    /**
     * Creates respective {@link CsiAggregation} if it doesn't exist and calculates it.
     * After calculation status is {@link CsiAggregation.Calculated.Yes} or {@link CsiAggregation.Calculated.YesNoData}.
     * @param startDate
     * @param interval
     * @param csiSystem
     * @return
     */
    CsiAggregation ensurePresenceAndCalculation(DateTime startDate, MeasuredValueInterval interval, CsiSystem csiSystem, List<MeasuredValueUpdateEvent> updateEvents) {
        CsiAggregation toCreateAndOrCalculate
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "ensurePresence", IndentationDepth.THREE) {
            toCreateAndOrCalculate = ensurePresence(startDate, interval, csiSystem)
        }
        if (toCreateAndOrCalculate.hasToBeCalculatedAccordingEvents(updateEvents)) {
            performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "calculateCustomerSatisfactionMeasuredValue (interval=${interval.intervalInMinutes}; aggregator=csiSystem)", IndentationDepth.THREE) {
                toCreateAndOrCalculate = calcMv(toCreateAndOrCalculate, csiSystem)
            }
        }
        return toCreateAndOrCalculate
    }

    private CsiAggregation ensurePresence(DateTime startDate, MeasuredValueInterval interval, CsiSystem csiSystem) {
        CsiAggregation toCreateAndOrCalculate
        AggregatorType csiSystemAggregator = AggregatorType.findByName(AggregatorType.CSI_SYSTEM)
        String tag = measuredValueTagService.getTagPatternForWeeklyShopMvsWithJobGroups(csiSystem.getAffectedJobGroups())
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "ensurePresence.findByStarted", IndentationDepth.FOUR) {
            toCreateAndOrCalculate = CsiAggregation.findByStartedAndIntervalAndAggregatorAndTagAndCsiSystem(startDate.toDate(), interval, csiSystemAggregator, tag, csiSystem)
            log.debug("CsiAggregation.findByStartedAndIntervalAndAggregatorAndCsiSystem delivered ${toCreateAndOrCalculate ? 'a' : 'no'} result")
        }
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "ensurePresence.createNewMV", IndentationDepth.FOUR) {
            if (!toCreateAndOrCalculate) {
                toCreateAndOrCalculate = new CsiAggregation(
                        started: startDate.toDate(),
                        interval: interval,
                        aggregator: csiSystemAggregator,
                        tag: tag,
                        csiSystem: csiSystem,
                        csByWptDocCompleteInPercent: null,
                        underlyingEventResultsByWptDocComplete: ''
                ).save(failOnError: true)
            }
        }
        return toCreateAndOrCalculate
    }

    /**
     * Calculates the given {@link CsiAggregation} toBeCalculated.
     * @param toBeCalculated
     * 		The {@link CsiAggregation} to be calculated.
     * @return The calculated {@link CsiAggregation}.
     */
    CsiAggregation calcMv(CsiAggregation toBeCalculated, CsiSystem csiSystem) {

        Contract.requiresArgumentNotNull("toBeCalculated", toBeCalculated);

        List<JobGroup> groupsOfMv = csiSystem.getAffectedJobGroups()
        List<CsiAggregation> shopMeasuredValues = shopMeasuredValueService.getOrCalculateShopMeasuredValues(
                toBeCalculated.started, toBeCalculated.started, toBeCalculated.getInterval(), groupsOfMv)

        List<WeightedCsiValue> weightedCsiValues = []

        if (shopMeasuredValues.size() > 0) {
            weightedCsiValues = weightingService.getWeightedCsiValues(shopMeasuredValues, csiSystem)
        }
        if (weightedCsiValues.size() > 0) {
            toBeCalculated.csByWptDocCompleteInPercent = meanCalcService.calculateWeightedMean(weightedCsiValues*.weightedValue)
        }
        measuredValueUpdateEventDaoService.createUpdateEvent(toBeCalculated.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
        return toBeCalculated
    }
}
