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
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.dao.MeasuredEventDaoService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import org.joda.time.DateTime

class PageCsiAggregationService {

    EventCsiAggregationService eventCsiAggregationService
    JobService jobService
    MeanCalcService meanCalcService
    PerformanceLoggingService performanceLoggingService
    MeasuredEventDaoService measuredEventDaoService
    CsiAggregationDaoService csiAggregationDaoService
    CsiAggregationUtilService csiAggregationUtilService
    WeightingService weightingService
    CsiAggregationUpdateEventDaoService csiAggregationUpdateEventDaoService

    /**
     * <p>
     * Finds page {@link CsiAggregation}s from DB.
     * No creation or calculation is performed.
     * </p>
     *
     * <p>
     * <em>Note:</em> Passing an empty list to {@code groups} or {@code pages}
     * at least one of the arguments will cause that no result is found.
     * </p>
     *
     * @param fromDate
     *         First {@link Date} (inclusive) of a test start date for that
     *         result CsiAggregations to find,
     *         not <code>null</code>.
     * @param toDate
     *         Last {@link Date} (inclusive) of a test start date for that
     *         result CsiAggregations to find,
     *         not <code>null</code>.
     * @param targetInterval
     *         The interval of the page CsiAggregations to find, currently only
     * {@link CsiAggregationInterval#WEEKLY} is supported,
     *         not <code>null</code> (this argument is deprecated).
     * @param groups
     *         The groups for that CsiAggregations should be found,
     *         not <code>null</code>.
     * @param pages
     *         The pages for that CsiAggregations should be found,
     *         not <code>null</code>.
     *
     * @return Found (weekly) page CsiAggregations, not <code>null</code>.
     *
     * @see CsiAggregation#getStarted()
     */
    public List<CsiAggregation> findAll(Date fromDate, Date toDate,
                                        @Deprecated
                                                CsiAggregationInterval targetInterval,
                                        List<JobGroup> groups, List<Page> pages) {
        List<CsiAggregation> result = []
        if (groups.size() == 0 || pages.size() == 0) {
            return result
        }
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting csi-results - findAll - getMvs', IndentationDepth.ONE) {
            result = csiAggregationDaoService.getPageCsiAggregations(fromDate, toDate, groups, pages, targetInterval)
        }
        return result
    }

    /**
     * Marks {@link CsiAggregation}s which depend from param newResult and who's interval contains newResult as outdated.
     * @param startich bin hier auch gerade nur
     * 			00:00:00 of the respective interval.
     * @param newResult
     * 			New {@link EventResult}.
     */
    void markMvAsOutdated(DateTime start, EventResult newResult, CsiAggregationInterval interval) {
        List<Long> pageCsiAggregationIds = ensurePresence(start, interval, [newResult.jobGroup], [newResult.page])
        pageCsiAggregationIds.each { csiAggregationId ->
            csiAggregationUpdateEventDaoService.createUpdateEvent(csiAggregationId, CsiAggregationUpdateEvent.UpdateCause.OUTDATED)
        }
    }

    /**
     * Returns calculated pageCsiAggregations
     * @param fromDate
     * @param toDate
     * @param interval the {@link CsiAggregationInterval}
     * @param csiGroups the {@link JobGroup}s to calculate the csiAggregations for
     * @param pages the {@link Page}s to calculated the csiAggregations for. If param is not set, all pages are used.
     * @return a list of calculated pageCsiAggregations
     */
    List<CsiAggregation> getOrCalculatePageCsiAggregations(Date fromDate, Date toDate, CsiAggregationInterval interval, List<JobGroup> csiGroups, List<Page> pages = Page.list()) {
        DateTime toDateTime = new DateTime(toDate)
        DateTime fromDateTime = new DateTime(fromDate)
        if (fromDateTime.isAfter(toDateTime)) {
            throw new IllegalArgumentException("toDate must not be later than fromDate: fromDate=${fromDate}; toDate=${toDate}")
        }

        DateTime currentDateTime = fromDateTime
        List<Long> allCsiAggregationIds = []

        CsiAggregation.withNewSession { session ->

            while (!currentDateTime.isAfter(toDateTime)) {
                List<Long> pageCsiAggregationIds
                List<Long> pageCsiAggregationIdsToCalculate
                pageCsiAggregationIds = ensurePresence(currentDateTime, interval, csiGroups, pages)
                pageCsiAggregationIdsToCalculate = filterCsiAggregationsToCalculate(pageCsiAggregationIds)

                if (pageCsiAggregationIdsToCalculate)
                    calcCsiAggregations(pageCsiAggregationIdsToCalculate)

                allCsiAggregationIds.addAll(pageCsiAggregationIds)

                currentDateTime = csiAggregationUtilService.addOneInterval(currentDateTime, interval.intervalInMinutes)

                session.flush()
                session.clear()

            }

        }
        return CsiAggregation.getAll(allCsiAggregationIds)
    }

    /**
     * Returns the id for each csiAggregation with the combination of startDate, jobGroup and page.
     * If a csiAggregation does not exist it gets created.
     * @param startDate
     * @param interval
     * @param csiSystems
     * @return a list of all csiAggregationIds
     */
    private List<Long> ensurePresence(DateTime startDate, CsiAggregationInterval interval, List<JobGroup> jobGroups, List<Page> pages) {
        List<Long> result = []
        AggregatorType pageAggregator = AggregatorType.findByName(AggregatorType.PAGE)

        jobGroups.each { currentJobGroup ->
            pages.each { currentPage ->
                CsiAggregation csiAggregation
                performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "PageCsiAggregationService: ensurePresence.findByStarted", IndentationDepth.FOUR) {
                    csiAggregation = CsiAggregation.findByStartedAndIntervalAndAggregatorAndJobGroupAndPage(startDate.toDate(), interval, pageAggregator, currentJobGroup, currentPage)
                    log.debug("CsiAggregation.findByStartedAndIntervalAndAggregatorAndJobGroupAndPage delivered ${csiAggregation ? 'a' : 'no'} result")
                }
                if (!csiAggregation) {
                    csiAggregation = new CsiAggregation(
                            started: startDate.toDate(),
                            interval: interval,
                            aggregator: pageAggregator,
                            jobGroup: currentJobGroup,
                            page: currentPage,
                            csByWptDocCompleteInPercent: null,
                            underlyingEventResultsByWptDocComplete: ''
                    ).save(failOnError: true, flush: true)
                }

                result << csiAggregation.id
            }
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
    public void calcCsiAggregations(List<Long> allCsiAggregationIds) {
        assert allCsiAggregationIds

        List<CsiAggregation> csiAggregationsToCalculate = CsiAggregation.getAll(allCsiAggregationIds)

        // get all hourlyCsiAggregations to reduce database queries
        DateTime earliestDate = new DateTime(csiAggregationsToCalculate*.started.min())
        DateTime latestDate = new DateTime(csiAggregationsToCalculate*.started.max())
        int longestInterval = csiAggregationsToCalculate*.interval*.getIntervalInMinutes().max()
        List<JobGroup> allJobGroups = csiAggregationsToCalculate*.jobGroup.unique()
        List<JobGroup> allpages = csiAggregationsToCalculate*.page.unique()
        List<CsiAggregation> allHourlyCsiAggregations = getHmvsByCsiGroupAndPage(allJobGroups, allpages, earliestDate, latestDate.plusMinutes(longestInterval))


        csiAggregationsToCalculate.each { toBeCalculated ->
            DateTime started = new DateTime(toBeCalculated.started)
            CsiAggregationInterval interval = toBeCalculated.interval


            JobGroup targetCsiGroup = toBeCalculated.jobGroup
            Page targetPage = toBeCalculated.page
            if (toBeCalculated == null || !targetCsiGroup || !targetPage) {
                log.error("CsiAggregation can't be calculated: ${toBeCalculated}. targetCsiGroup=${targetCsiGroup}, targetPage=${targetPage}")
                return
            }
            List<CsiAggregation> hourlyCsiAggregationsForCsiAggregationToCalculate = allHourlyCsiAggregations.findAll {
                it.jobGroup == targetCsiGroup && it.page == targetPage && it.started >= started.toDate() && it.started <= started.plusMinutes(interval.getIntervalInMinutes()).toDate()
            }

            List<WeightedCsiValue> weightedCsiValuesByDocComplete = []
            List<WeightedCsiValue> weightedCsiValuesByVisuallyComplete = []
            if (hourlyCsiAggregationsForCsiAggregationToCalculate.size() > 0) {
                CsiConfiguration csiConfiguration = targetCsiGroup.csiConfiguration
                weightedCsiValuesByDocComplete = weightingService.getWeightedCsiValues(hourlyCsiAggregationsForCsiAggregationToCalculate, [WeightFactor.HOUROFDAY, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set, csiConfiguration)
                weightedCsiValuesByVisuallyComplete = weightingService.getWeightedCsiValuesByVisuallyComplete(hourlyCsiAggregationsForCsiAggregationToCalculate, [WeightFactor.HOUROFDAY, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set, csiConfiguration)
                log.debug("weightedCsiValuesByDocComplete.size()=${weightedCsiValuesByDocComplete.size()}")
                log.debug("weightedCsiValuesByVisuallyComplete.size()=${weightedCsiValuesByVisuallyComplete.size()}")
            }

            performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "  calcMvForPageAggregator - calculation wmv: calc weighted mean", IndentationDepth.FOUR) {
                if (weightedCsiValuesByDocComplete.size() > 0) {
                    toBeCalculated.csByWptDocCompleteInPercent = meanCalcService.calculateWeightedMean(weightedCsiValuesByDocComplete*.weightedValue)
                }
                if (weightedCsiValuesByVisuallyComplete.size() > 0) {
                    toBeCalculated.csByWptVisuallyCompleteInPercent = meanCalcService.calculateWeightedMean(weightedCsiValuesByVisuallyComplete*.weightedValue)
                }
                csiAggregationUpdateEventDaoService.createUpdateEvent(toBeCalculated.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
            }

            toBeCalculated.save(failOnError: true)
        }
    }

    /**
     * returns a list of hourlyCsiAggregations
     * @param csiGroup the {@link JobGroup}
     * @param csiPage the {@link Page}
     * @param startDateTime the start date as JodaTime
     * @param endDateTime the end date as JodaTime
     * @return a list of hourlyCsiAggregations
     */
    List<CsiAggregation> getHmvsByCsiGroupAndPage(List<JobGroup> csiGroups, List<Page> csiPages, DateTime startDateTime, DateTime endDateTime) {
        List<CsiAggregation> hourlyCsiAggregations
        List<MeasuredEvent> measuredEvents = measuredEventDaoService.getEventsFor(csiPages)
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "  calcMvForPageAggregator - getHmvs: getting", IndentationDepth.FOUR) {
            hourlyCsiAggregations = eventCsiAggregationService.getHourlyCsiAggregations(startDateTime.toDate(), endDateTime.toDate(), csiGroups, csiPages, measuredEvents)
        }
        return hourlyCsiAggregations
    }

}
