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

import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.CsiValueService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import org.joda.time.DateTime

/**
 * Provides methods for calculating and retrieving {@link CsiAggregation}s with {@link AggregatorType#getName()}=={@link AggregatorType#JOB}.
 * @author nkuhn
 *
 */
class JobGroupCsiAggregationService {

    PageCsiAggregationService pageCsiAggregationService
    MeanCalcService meanCalcService
    PerformanceLoggingService performanceLoggingService
    JobService jobService
    CsiAggregationDaoService csiAggregationDaoService
    CsiAggregationUtilService csiAggregationUtilService
    CsiValueService csiValueService
    CsiAggregationUpdateEventDaoService csiAggregationUpdateEventDaoService
    
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
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting csi-results - findAll - getMvs', 1) {
            result = csiAggregationDaoService.getJobGroupCsiAggregations(fromDate, toDate, csiGroups, targetInterval)
        }
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

        if (jobGroup && jobGroup.hasCsiConfiguration()) {
            List<Long> shopCsiAggregationIds = ensurePresence(start, interval, [jobGroup])
            shopCsiAggregationIds.each { csiAggregationId ->
                csiAggregationUpdateEventDaoService.createUpdateEvent(csiAggregationId, CsiAggregationUpdateEvent.UpdateCause.OUTDATED)
            }
        }

    }

    /**
     * Returns calculated shopCsiAggregations
     * @param fromDate
     * @param toDate
     * @param interval the {@link CsiAggregationInterval}
     * @param csiGroups the {@link JobGroup}s to calculate the csiAggregations for
     * @return a list of calculated shopCsiAggregations
     */
    List<CsiAggregation> getOrCalculateShopCsiAggregations(Date fromDate, Date toDate, CsiAggregationInterval interval, List<JobGroup> csiGroups) {
        DateTime toDateTime = new DateTime(toDate)
        DateTime fromDateTime = new DateTime(fromDate)
        if (fromDateTime.isAfter(toDateTime)) {
            throw new IllegalArgumentException("toDate must not be later than fromDate: fromDate=${fromDate}; toDate=${toDate}")
        }

        DateTime currentDateTime = fromDateTime
        List<Long> allCsiAggregationIds = []


        CsiAggregation.withNewSession { session ->

            while (!currentDateTime.isAfter(toDateTime)) {
                List<Long> shopCsiAggregationIds
                List<Long> shopCsiAggregationIdsToCalculate

                shopCsiAggregationIds = ensurePresence(currentDateTime, interval, csiGroups)
                shopCsiAggregationIdsToCalculate = filterCsiAggregationsToCalculate(shopCsiAggregationIds)
                if (shopCsiAggregationIdsToCalculate)
                    calcCsiAggregations(shopCsiAggregationIdsToCalculate)

                allCsiAggregationIds.addAll(shopCsiAggregationIds)

                currentDateTime = csiAggregationUtilService.addOneInterval(currentDateTime, interval.intervalInMinutes)

                session.flush()
                session.clear()
            }

        }

        return CsiAggregation.getAll(allCsiAggregationIds)
    }

    /**
     * Returns the id for each csiAggregation with the startDate and given jobGroup.
     * If a csiAggregation does not exist it gets created.
     * @param startDate
     * @param interval
     * @param csiSystems
     * @return a list of all csiAggregationIds for the csiAggregations with given startDate and jobGroup
     */
    private List<Long> ensurePresence(DateTime startDate, CsiAggregationInterval interval, List<JobGroup> jobGroups) {
        List<Long> result = []
        AggregatorType shopAggregator = AggregatorType.findByName(AggregatorType.SHOP)

        jobGroups.each { currentJobGroup ->
            CsiAggregation csiAggregation
            csiAggregation = CsiAggregation.findByStartedAndIntervalAndAggregatorAndJobGroup(startDate.toDate(), interval, shopAggregator, currentJobGroup)
            if (!csiAggregation) {
                csiAggregation = new CsiAggregation(
                        started: startDate.toDate(),
                        interval: interval,
                        aggregator: shopAggregator,
                        jobGroup: currentJobGroup,
                        csByWptDocCompleteInPercent: null,
                        underlyingEventResultsByWptDocComplete: ''
                ).save(failOnError: true, flush: true)
            }

            result << csiAggregation.id
        }

        return result
    }

    /**
     * Filters given list of csiAggregationIds for csiAggregations that have to be calculated
     * @param allCsiAggregationIds
     * @return a new list of csiAggregationIds containing only ids for csiAggregations that have to be calculated
     */
    private List<Long> filterCsiAggregationsToCalculate(List<Long> allCsiAggregationIds) {
        List<Long> result = []

        List<CsiAggregation> openCsiAggregations = CsiAggregation.findAll {
            id in allCsiAggregationIds && closedAndCalculated == false
        }
        openCsiAggregations.each {
            if (it.hasToBeCalculated()) {
                result << it.id
            }
        }

        return result
    }

    /**
     * calculates all csiAggregations with given ids
     * @param csiAggregationIdsToCalc a list of csiAggregationIds to calculate
     */
    public List<CsiAggregation> calcCsiAggregations(List<Long> csiAggregationIds) {
        Contract.requiresArgumentNotNull("toBeCalculated", csiAggregationIds);

        List<CsiAggregation> csiAggregationsToCalculate = CsiAggregation.getAll(csiAggregationIds)

        csiAggregationsToCalculate.each { toBeCalculated ->
            JobGroup jobGroupOfCsiAggregation = toBeCalculated.jobGroup
            List<CsiAggregation> pageCsiAggregations = pageCsiAggregationService.getOrCalculatePageCsiAggregations(toBeCalculated.started, toBeCalculated.started, toBeCalculated.getInterval(), [jobGroupOfCsiAggregation])


            List<WeightedCsiValue> weightedCsiValuesDocComplete = []
            List<WeightedCsiValue> weightedCsiValuesVisuallyComplete = []

            if (pageCsiAggregations.size() > 0) {
                weightedCsiValuesDocComplete = csiValueService.getWeightedCsiValues(pageCsiAggregations, [WeightFactor.PAGE] as Set, jobGroupOfCsiAggregation.csiConfiguration)
                weightedCsiValuesVisuallyComplete = csiValueService.getWeightedCsiValuesByVisuallyComplete(pageCsiAggregations, [WeightFactor.PAGE] as Set, jobGroupOfCsiAggregation.csiConfiguration)
            }
            if (weightedCsiValuesDocComplete.size() > 0) {
                toBeCalculated.csByWptDocCompleteInPercent = meanCalcService.calculateWeightedMean(weightedCsiValuesDocComplete*.weightedValue)
            }
            if (weightedCsiValuesVisuallyComplete.size() > 0) {
                toBeCalculated.csByWptVisuallyCompleteInPercent = meanCalcService.calculateWeightedMean(weightedCsiValuesVisuallyComplete*.weightedValue)
            }
            csiAggregationUpdateEventDaoService.createUpdateEvent(toBeCalculated.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
            toBeCalculated.save(failOnError: true)
        }
        return csiAggregationsToCalculate
    }
}
