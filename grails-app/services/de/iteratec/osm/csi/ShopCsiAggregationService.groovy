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

import de.iteratec.osm.report.chart.CsiAggregationDaoService
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.measurement.schedule.JobGroup

import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import de.iteratec.osm.report.chart.CsiAggregationUpdateEventDaoService
import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultDaoService
import de.iteratec.osm.result.CsiAggregationTagService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel

/**
 * Provides methods for calculating and retrieving {@link CsiAggregation}s with {@link AggregatorType#getName()}=={@link AggregatorType#JOB}.
 * @author nkuhn
 *
 */
class ShopCsiAggregationService {

    PageCsiAggregationService pageCsiAggregationService
    CsiHelperService csiHelperService
    MeanCalcService meanCalcService
    CsiAggregationTagService csiAggregationTagService
    PerformanceLoggingService performanceLoggingService
    JobService jobService
    JobResultDaoService jobResultDaoService
    CsiAggregationDaoService csiAggregationDaoService
    CsiAggregationUtilService csiAggregationUtilService
    WeightingService weightingService
    CsiAggregationUpdateEventDaoService csiAggregationUpdateEventDaoService

    /**
     * Just gets {@link CsiAggregation}s from DB. No creation or calculation.
     * @param fromDate
     * @param toDate
     * @param targetInterval
     * @return
     */
    List<CsiAggregation> findAll(Date fromDate, Date toDate, CsiAggregationInterval targetInterval) {
        List<CsiAggregation> result = []
        def query = CsiAggregation.where {
            started >= fromDate
            started <= toDate
            interval == targetInterval
            aggregator == AggregatorType.findByName(AggregatorType.SHOP)
        }
        result = query.list()
        return result
    }
    /**
     * Just gets {@link CsiAggregation}s from DB. No creation or calculation.
     * @param fromDate
     * @param toDate
     * @param targetInterval
     * @param csiGroups
     * @return
     */
    List<CsiAggregation> findAll(Date fromDate, Date toDate, CsiAggregationInterval targetInterval, List<JobGroup> csiGroups) {
        List<CsiAggregation> result = []
        if (csiGroups.size() == 0) {
            return result
        }
        String tagPattern = csiAggregationTagService.getTagPatternForWeeklyShopCasWithJobGroups(csiGroups)
        result = csiAggregationDaoService.getMvs(fromDate, toDate, tagPattern, targetInterval, AggregatorType.findByName(AggregatorType.SHOP))
        return result
    }

    /**
     * Marks {@link CsiAggregation}s which depend from param newResult and who's interval contains newResult as outdated.
     * @param start
     * 				00:00:00 of the respective interval.
     * @param newResult
     * 				New {@link EventResult}.
     */
    void markMvAsOutdated(DateTime start, EventResult newResult, CsiAggregationInterval interval) {

        JobResult jobResult = newResult.jobResult;
        JobGroup jobGroup = jobService.getCsiJobGroupOf(jobResult.job)
        String shopTag = csiAggregationTagService.createShopAggregatorTag(jobGroup)

        if (jobGroup && jobGroup.hasCsiConfiguration() && shopTag) {
            CsiAggregation shopMv = ensurePresence(start, interval, shopTag)
            csiAggregationUpdateEventDaoService.createUpdateEvent(shopMv.ident(), CsiAggregationUpdateEvent.UpdateCause.OUTDATED)
        }

    }

    /**
     * Provides all shop-{@link CsiAggregation}s of given csi-{@link JobGroup}s between toDate and fromDate.
     * Non-existent {@link CsiAggregation}s will be created.
     * All {@link CsiAggregation}s with @{link CsiAggregation.Calculated.Not} will be calculated and persisted with @{link CsiAggregation.Calculated.Yes}* or @{link CsiAggregation.Calculated.YesNoData}.
     * @param fromDate
     * @param toDate
     * @param csiGroups
     * @return
     */
    List<CsiAggregation> getOrCalculateShopCsiAggregations(Date fromDate, Date toDate, CsiAggregationInterval interval, List<JobGroup> csiGroups) {
        if (fromDate > toDate) {
            throw new IllegalArgumentException("toDate must not be later than fromDate: fromDate=${fromDate}; toDate=${toDate}")
        }
        DateTime toDateTime = new DateTime(toDate)
        DateTime fromDateTime = new DateTime(fromDate)

        Integer numberOfIntervals = csiAggregationUtilService.getNumberOfIntervals(fromDateTime, toDateTime, interval)

        List<CsiAggregation> existingCsiAggregations = findAll(fromDateTime.toDate(), toDateTime.toDate(), interval, csiGroups)

        List<CsiAggregation> openCsiAggregations = existingCsiAggregations.findAll { !it.closedAndCalculated }
        Boolean allCsiAggregationsExist = existingCsiAggregations.size() == numberOfIntervals * csiGroups.size()
        if (allCsiAggregationsExist && openCsiAggregations.size() == 0) {
            return existingCsiAggregations
        }

        List<CsiAggregationUpdateEvent> updateEvents = []
        if (openCsiAggregations.size() > 0) updateEvents.addAll(csiAggregationDaoService.getUpdateEvents(openCsiAggregations*.ident()))
        List<CsiAggregation> csiAggregationsToBeCalculated = openCsiAggregations.findAll {
            it.hasToBeCalculatedAccordingEvents(updateEvents)
        }

        if (allCsiAggregationsExist && csiAggregationsToBeCalculated.size() == 0) {

            return existingCsiAggregations

        } else {

            List<CsiAggregation> calculatedCsiAggregations = []
            DateTime currentDateTime = fromDateTime
            while (!currentDateTime.isAfter(toDateTime)) {
                performanceLoggingService.logExecutionTime(LogLevel.INFO, " get/create/calculate ${interval.name} shop-CsiAggregation for: ${currentDateTime}", IndentationDepth.TWO) {
                    List<CsiAggregation> existingMvsOfCurrentTime = existingCsiAggregations.findAll {
                        new DateTime(it.started) == currentDateTime
                    }
                    List<CsiAggregation> mvsToBeCalculatedOfCurrentTime = csiAggregationsToBeCalculated.findAll {
                        new DateTime(it.started) == currentDateTime
                    }
                    if (existingMvsOfCurrentTime.size() == csiGroups.size() && mvsToBeCalculatedOfCurrentTime.size() == 0) {

                        calculatedCsiAggregations.addAll(existingMvsOfCurrentTime)

                    } else {

                        calculatedCsiAggregations.addAll(getOrCalculateShopCas(currentDateTime, interval, csiGroups, updateEvents))

                    }
                }
                currentDateTime = csiAggregationUtilService.addOneInterval(currentDateTime, interval.intervalInMinutes)
            }
            return calculatedCsiAggregations
        }
    }

    private List<CsiAggregation> getOrCalculateShopCas(DateTime toGetMvsFor, CsiAggregationInterval interval, List<JobGroup> csiGroups, List<CsiAggregationUpdateEvent> updateEvents) {
        List<CsiAggregation> smvs = []
        csiGroups.each { csiGroup ->
            String tag = csiAggregationTagService.createShopAggregatorTag(csiGroup)
            smvs.addAll(ensurePresenceAndCalculation(toGetMvsFor, interval, tag, updateEvents))
        }
        return smvs
    }

    /**
     * Creates respective {@link CsiAggregation} if it doesn't exist and calculates it.
     * After calculation status is {@link CsiAggregation.Calculated.Yes} or {@link CsiAggregation.Calculated.YesNoData}.
     * @param startDate
     * @param interval
     * @param tag
     * @return
     */
    CsiAggregation ensurePresenceAndCalculation(DateTime startDate, CsiAggregationInterval interval, String tag, List<CsiAggregationUpdateEvent> updateEvents) {
        CsiAggregation toCreateAndOrCalculate
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "ensurePresence", IndentationDepth.THREE) {
            toCreateAndOrCalculate = ensurePresence(startDate, interval, tag)
        }
        if (toCreateAndOrCalculate.hasToBeCalculatedAccordingEvents(updateEvents)) {
            performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "calculateCustomerSatisfactionCsiAggregation (interval=${interval.intervalInMinutes}; aggregator=shop)", IndentationDepth.THREE) {
                toCreateAndOrCalculate = calcCa(toCreateAndOrCalculate)
            }
        }
        return toCreateAndOrCalculate
    }

    private CsiAggregation ensurePresence(DateTime startDate, CsiAggregationInterval interval, String tag) {
        CsiAggregation toCreateAndOrCalculate
        AggregatorType shopAggregator = AggregatorType.findByName(AggregatorType.SHOP)
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "ensurePresence.findByStarted", IndentationDepth.FOUR) {
            toCreateAndOrCalculate = CsiAggregation.findByStartedAndIntervalAndAggregatorAndTag(startDate.toDate(), interval, shopAggregator, tag)
            log.debug("CsiAggregation.findByStartedAndIntervalAndAggregatorAndTag delivered ${toCreateAndOrCalculate ? 'a' : 'no'} result")
        }
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "ensurePresence.createNewMV", IndentationDepth.FOUR) {
            if (!toCreateAndOrCalculate) {
                toCreateAndOrCalculate = new CsiAggregation(
                        started: startDate.toDate(),
                        interval: interval,
                        aggregator: shopAggregator,
                        tag: tag,
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
     * @return The calculated {@link de.iteratec.osm.report.chart.CsiAggregation}.
     */
    CsiAggregation calcCa(CsiAggregation toBeCalculated) {

        Contract.requiresArgumentNotNull("toBeCalculated", toBeCalculated);

        JobGroup groupOfMv = csiAggregationTagService.findJobGroupOfWeeklyShopTag(toBeCalculated.tag)
        List<CsiAggregation> pageCsiAggregations = pageCsiAggregationService.getOrCalculatePageCsiAggregations(
                toBeCalculated.started, toBeCalculated.started, toBeCalculated.getInterval(), [groupOfMv])

        List<WeightedCsiValue> weightedCsiValuesDocComplete = []
        List<WeightedCsiValue> weightedCsiValuesVisuallyComplete = []

        if (pageCsiAggregations.size() > 0) {
            weightedCsiValuesDocComplete = weightingService.getWeightedCsiValues(pageCsiAggregations, [WeightFactor.PAGE] as Set, groupOfMv.csiConfiguration)
            weightedCsiValuesVisuallyComplete = weightingService.getWeightedCsiValuesByVisuallyComplete(pageCsiAggregations, [WeightFactor.PAGE] as Set, groupOfMv.csiConfiguration)
        }
        if (weightedCsiValuesDocComplete.size() > 0) {
            toBeCalculated.csByWptDocCompleteInPercent = meanCalcService.calculateWeightedMean(weightedCsiValuesDocComplete*.weightedValue)
        }
        if(weightedCsiValuesVisuallyComplete.size() > 0) {
            toBeCalculated.csByWptVisuallyCompleteInPercent = meanCalcService.calculateWeightedMean(weightedCsiValuesVisuallyComplete*.weightedValue)
        }
        csiAggregationUpdateEventDaoService.createUpdateEvent(toBeCalculated.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        return toBeCalculated
    }

    private Page getPageFromPageCa(CsiAggregation toGetPageFrom) {
        return csiAggregationTagService.findPageByPageTag(toGetPageFrom.tag)
    }

    /**
     * Provides all weekly shop-{@link CsiAggregation}s of all csi-{@link JobGroup}s between toDate and fromDate.
     * Non-existent {@link CsiAggregation}s will be created.
     * All {@link CsiAggregation}s with @{link CsiAggregation.Calculated.Not} will be calculated and persisted with @{link CsiAggregation.Calculated.Yes}* or @{link CsiAggregation.Calculated.YesNoData}.
     * @param fromDate
     * @param toDate
     * @return
     */
    List<CsiAggregation> getOrCalculateWeeklyShopCsiAggregations(Date fromDate, Date toDate) {
        CsiAggregationInterval mvInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
        return getOrCalculateShopCsiAggregations(fromDate, toDate, mvInterval, JobGroup.findAllByCsiConfigurationIsNotNull())
    }
}
