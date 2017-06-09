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
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.CsiValueService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.util.PerformanceLoggingService
import org.joda.time.DateTime
/**
 * Provides methods for calculating and retrieving {@link CsiAggregation}s with {@link AggregatorType#getName()}=={@link AggregatorType}.
 * @author nkuhn
 *
 */
class CsiSystemCsiAggregationService {

    ShopCsiAggregationService shopCsiAggregationService
    MeanCalcService meanCalcService
    PerformanceLoggingService performanceLoggingService
    CsiAggregationUtilService csiAggregationUtilService
    CsiValueService csiValueService
    CsiAggregationUpdateEventDaoService csiAggregationUpdateEventDaoService

    /**
     * Just gets {@link CsiAggregation}s from DB. No creation or calculation.
     * @param fromDate
     * @param toDate
     * @param targetInterval
     * @return
     */
    List<CsiAggregation> findAll(Date fromDate, Date toDate, CsiAggregationInterval targetInterval) {
        def query = CsiAggregation.where {
            started >= fromDate
            started <= toDate
            interval == targetInterval
            aggregator == AggregatorType.findByName(AggregatorType.CSI_SYSTEM)
        }
        return query.list()
    }

    /**
     * Marks {@link CsiAggregation}s which depend from param newResult and who's interval contains newResult as outdated.
     * @param start
     * 				00:00:00 of the respective interval.
     * @param newResult
     * 				New {@link EventResult}.
     */
    void markCaAsOutdated(DateTime start, EventResult newResult, CsiAggregationInterval interval) {

        JobGroup jobGroupOfResult = newResult.jobResult.job.jobGroup

        List<CsiSystem> affectedCsiSystems = CsiSystem.where {
            jobGroupWeights*.jobGroup.contains(jobGroupOfResult)
        }.findAll()

        if (affectedCsiSystems && jobGroupOfResult.csiConfiguration != null) {
            List<Long> outdatedCsiAggregationIds = ensurePresence(start, interval, affectedCsiSystems)
            outdatedCsiAggregationIds.each {
                csiAggregationUpdateEventDaoService.
                        createUpdateEvent(it, CsiAggregationUpdateEvent.UpdateCause.OUTDATED)
            }
        }

    }

    /**
     * Returns calculated CsiSystemCsiAggregations
     * @param fromDate
     * @param toDate
     * @param interval the {@link CsiAggregationInterval}
     * @param csiSystems the {@link CsiSystem}s to calculate the csiAggregations for
     * @return a list of calculated csiSystemCsiAggregations
     */
    List<CsiAggregation> getOrCalculateCsiSystemCsiAggregations(Date fromDate, Date toDate, CsiAggregationInterval interval, List<CsiSystem> csiSystems) {
        if (fromDate > toDate) {
            throw new IllegalArgumentException("toDate must not be later than fromDate: fromDate=${fromDate}; toDate=${toDate}")
        }
        DateTime toDateTime = new DateTime(toDate)
        DateTime fromDateTime = new DateTime(fromDate)

        DateTime currentDateTime = fromDateTime
        List<Long> allCsiAggregationIds = []

        CsiAggregation.withNewSession { session ->

            while (!currentDateTime.isAfter(toDateTime)) {
                List<Long> csiSystemCsiAggregationIds = ensurePresence(currentDateTime, interval, csiSystems)
                List<Long> csiSystemCsiAggregationIdsToCalculate = filterCsiAggregationsToCalculate(csiSystemCsiAggregationIds)
                if (csiSystemCsiAggregationIdsToCalculate)
                    calcCsiAggregations(csiSystemCsiAggregationIdsToCalculate)

                allCsiAggregationIds.addAll(csiSystemCsiAggregationIds)

                currentDateTime = csiAggregationUtilService.addOneInterval(currentDateTime, interval.intervalInMinutes)

                session.flush()
                session.clear()
            }
        }


        return CsiAggregation.getAll(allCsiAggregationIds)
    }

    /**
     * Returns the id for each csiAggregation with the startDate and given csiSystem.
     * If a csiAggregation does not exist it gets created.
     * @param startDate
     * @param interval
     * @param csiSystems
     * @return a list of all csiAggregationIds for the csiAggregations with given startDate and csiSystem
     */
    private List<Long> ensurePresence(DateTime startDate, CsiAggregationInterval interval, List<CsiSystem> csiSystems) {
        List<Long> result = []
        AggregatorType csiSystemAggregator = AggregatorType.findByName(AggregatorType.CSI_SYSTEM)

        csiSystems.each { currentCsiSystem ->
            CsiAggregation csiAggregation

            csiAggregation = CsiAggregation.findByStartedAndIntervalAndAggregatorAndCsiSystem(startDate.toDate(), interval, csiSystemAggregator, currentCsiSystem)
            if (!csiAggregation) {
                csiAggregation = new CsiAggregation(
                        started: startDate.toDate(),
                        interval: interval,
                        aggregator: csiSystemAggregator,
                        csiSystem: currentCsiSystem,
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
    public List<CsiAggregation> calcCsiAggregations(List<Long> csiAggregationIdsToCalc) {
        Contract.requiresArgumentNotNull("toBeCalculated", csiAggregationIdsToCalc);

        List<CsiAggregation> csiAggregationsToCalculate = CsiAggregation.getAll(csiAggregationIdsToCalc)

        csiAggregationsToCalculate.each { toBeCalculated ->
            CsiSystem csiSystem = toBeCalculated.csiSystem
            List<JobGroup> groupsOfMv = csiSystem.getAffectedJobGroups()

            List<CsiAggregation> shopCsiAggregations = shopCsiAggregationService.getOrCalculateShopCsiAggregations(
                    toBeCalculated.started, toBeCalculated.started, toBeCalculated.getInterval(), groupsOfMv)

            List<WeightedCsiValue> weightedCsiValues = []
            List<WeightedCsiValue> weightedCsiValuesVisuallyComplete = []

            if (shopCsiAggregations.size() > 0) {
                weightedCsiValues = csiValueService.getWeightedCsiValues(shopCsiAggregations, csiSystem)
                weightedCsiValuesVisuallyComplete = csiValueService.getWeightedCsiValuesByVisuallyComplete(shopCsiAggregations, csiSystem)
            }

            if (weightedCsiValues.size() > 0) {
                toBeCalculated.csByWptDocCompleteInPercent = meanCalcService.calculateWeightedMean(weightedCsiValues*.weightedValue)
            }
            if (weightedCsiValuesVisuallyComplete.size() > 0) {
                toBeCalculated.csByWptVisuallyCompleteInPercent = meanCalcService.calculateWeightedMean(weightedCsiValuesVisuallyComplete*.weightedValue)
            }
            csiAggregationUpdateEventDaoService.createUpdateEvent(toBeCalculated.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
            toBeCalculated.save(failOnError: true, flush: true)
        }
        return csiAggregationsToCalculate
    }
}
