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
 * Provides methods for calculating and retrieving {@link MeasuredValue}s with {@link AggregatorType#getName()}=={@link AggregatorType#JOB}.
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
            aggregator == AggregatorType.findByName(AggregatorType.CSI_SYSTEM)
        }
        result = query.list()
        return result
    }
    /**
     * Just gets {@link MeasuredValue}s from DB. No creation or calculation.
     * @param fromDate
     * @param toDate
     * @param targetInterval
     * @param csiSystems
     * @return
     */
    List<MeasuredValue> findAll(Date fromDate, Date toDate, MeasuredValueInterval targetInterval, List<CsiSystem> csiSystems) {
        List<MeasuredValue> result = []
        if (csiSystems.empty) {
            return result
        }

        result = measuredValueDaoService.getMvs(fromDate, toDate, targetInterval, AggregatorType.findByName(AggregatorType.CSI_SYSTEM),csiSystems)
        return result
    }

    /**
     * Marks {@link MeasuredValue}s which depend from param newResult and who's interval contains newResult as outdated.
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
                MeasuredValue csiSystemMv = ensurePresence(start, interval, it)
                measuredValueUpdateEventDaoService.
                        createUpdateEvent(csiSystemMv.ident(), MeasuredValueUpdateEvent.UpdateCause.OUTDATED)
            }
        }

    }

    /**
     * Provides all shop-{@link MeasuredValue}s of given csi-{@link JobGroup}s between toDate and fromDate.
     * Non-existent {@link MeasuredValue}s will be created.
     * All {@link MeasuredValue}s with @{link MeasuredValue.Calculated.Not} will be calculated and persisted with @{link MeasuredValue.Calculated.Yes}* or @{link MeasuredValue.Calculated.YesNoData}.
     * @param fromDate
     * @param toDate
     * @param csiSystems
     * @return
     */
    List<MeasuredValue> getOrCalculateCsiSystemMeasuredValues(Date fromDate, Date toDate, MeasuredValueInterval interval, List<CsiSystem> csiSystems) {
        if (fromDate > toDate) {
            throw new IllegalArgumentException("toDate must not be later than fromDate: fromDate=${fromDate}; toDate=${toDate}")
        }
        DateTime toDateTime = new DateTime(toDate)
        DateTime fromDateTime = new DateTime(fromDate)

        Integer numberOfIntervals = measuredValueUtilService.getNumberOfIntervals(fromDateTime, toDateTime, interval)

        List<MeasuredValue> existingMeasuredValues = findAll(fromDateTime.toDate(), toDateTime.toDate(), interval, csiSystems)

        List<MeasuredValue> openMeasuredValues = existingMeasuredValues.findAll { !it.closedAndCalculated }
        Boolean allMeasuredValuesExist = existingMeasuredValues.size() == numberOfIntervals * csiSystems.size()
        if (allMeasuredValuesExist && openMeasuredValues.size() == 0) {
            return existingMeasuredValues
        }

        List<MeasuredValueUpdateEvent> updateEvents = []
        if (openMeasuredValues.size() > 0) updateEvents.addAll(measuredValueDaoService.getUpdateEvents(openMeasuredValues*.ident()))
        List<MeasuredValue> measuredValuesToBeCalculated = openMeasuredValues.findAll {
            it.hasToBeCalculatedAccordingEvents(updateEvents)
        }

        if (allMeasuredValuesExist && measuredValuesToBeCalculated.size() == 0) {

            return existingMeasuredValues

        } else {

            List<MeasuredValue> calculatedMeasuredvalues = []
            DateTime currentDateTime = fromDateTime
            while (!currentDateTime.isAfter(toDateTime)) {
                performanceLoggingService.logExecutionTime(LogLevel.INFO, " get/create/calculate ${interval.name} shop-MeasureValue for: ${currentDateTime}", IndentationDepth.TWO) {
                    List<MeasuredValue> existingMvsOfCurrentTime = existingMeasuredValues.findAll {
                        new DateTime(it.started) == currentDateTime
                    }
                    List<MeasuredValue> mvsToBeCalculatedOfCurrentTime = measuredValuesToBeCalculated.findAll {
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

    private List<MeasuredValue> getOrCalculateCsiSystemMvs(DateTime toGetMvsFor, MeasuredValueInterval interval, List<CsiSystem> csiSystems, List<MeasuredValueUpdateEvent> updateEvents) {
        List<MeasuredValue> smvs = []
        csiSystems.each { csiSystem ->
            smvs.addAll(ensurePresenceAndCalculation(toGetMvsFor, interval, csiSystem, updateEvents))
        }
        return smvs
    }

    /**
     * Creates respective {@link MeasuredValue} if it doesn't exist and calculates it.
     * After calculation status is {@link MeasuredValue.Calculated.Yes} or {@link MeasuredValue.Calculated.YesNoData}.
     * @param startDate
     * @param interval
     * @param csiSystem
     * @return
     */
    MeasuredValue ensurePresenceAndCalculation(DateTime startDate, MeasuredValueInterval interval, CsiSystem csiSystem, List<MeasuredValueUpdateEvent> updateEvents) {
        MeasuredValue toCreateAndOrCalculate
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

    private MeasuredValue ensurePresence(DateTime startDate, MeasuredValueInterval interval, CsiSystem csiSystem) {
        MeasuredValue toCreateAndOrCalculate
        AggregatorType csiSystemAggregator = AggregatorType.findByName(AggregatorType.CSI_SYSTEM)
        String tag = measuredValueTagService.getTagPatternForWeeklyShopMvsWithJobGroups(csiSystem.getAffectedJobGroups())
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "ensurePresence.findByStarted", IndentationDepth.FOUR) {
            toCreateAndOrCalculate = MeasuredValue.findByStartedAndIntervalAndAggregatorAndTagAndCsiSystem(startDate.toDate(), interval, csiSystemAggregator, tag, csiSystem)
            log.debug("MeasuredValue.findByStartedAndIntervalAndAggregatorAndCsiSystem delivered ${toCreateAndOrCalculate ? 'a' : 'no'} result")
        }
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "ensurePresence.createNewMV", IndentationDepth.FOUR) {
            if (!toCreateAndOrCalculate) {
                toCreateAndOrCalculate = new MeasuredValue(
                        started: startDate.toDate(),
                        interval: interval,
                        aggregator: csiSystemAggregator,
                        tag: tag,
                        csiSystem: csiSystem,
                        value: null,
                        resultIds: ''
                ).save(failOnError: true)
            }
        }
        return toCreateAndOrCalculate
    }

    /**
     * Calculates the given {@link MeasuredValue} toBeCalculated.
     * @param toBeCalculated
     * 		The {@link MeasuredValue} to be calculated.
     * @return The calculated {@link MeasuredValue}.
     */
    MeasuredValue calcMv(MeasuredValue toBeCalculated, CsiSystem csiSystem) {

        Contract.requiresArgumentNotNull("toBeCalculated", toBeCalculated);

        List<JobGroup> groupsOfMv = csiSystem.getAffectedJobGroups()
        List<MeasuredValue> shopMeasuredValues = shopMeasuredValueService.getOrCalculateShopMeasuredValues(
                toBeCalculated.started, toBeCalculated.started, toBeCalculated.getInterval(), groupsOfMv)

        List<WeightedCsiValue> weightedCsiValues = []

        if (shopMeasuredValues.size() > 0) {
            weightedCsiValues = weightingService.getWeightedCsiValues(shopMeasuredValues, csiSystem)
        }
        if (weightedCsiValues.size() > 0) {
            toBeCalculated.value = meanCalcService.calculateWeightedMean(weightedCsiValues*.weightedValue)
        }
        measuredValueUpdateEventDaoService.createUpdateEvent(toBeCalculated.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
        return toBeCalculated
    }
}
