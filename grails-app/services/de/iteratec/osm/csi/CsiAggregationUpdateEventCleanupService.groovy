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

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdater
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.CsiAggregationTagService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.DetachedCriteria

/**
 * Contains the logic to ...
 * <ul>
 * <li>calculate (if necessary) and close {@link CsiAggregation}s who aren't already closed and who's interval expired.</li>
 * <li>cleanup associated {@link CsiAggregationUpdateEvent}s.</li>
 * </ul>
 * @author nkuhn
 */
class CsiAggregationUpdateEventCleanupService {

    final int DATACOUNT_BEFORE_WRITING_TO_DB = 100;

    static transactional = false

    CsiAggregationDaoService csiAggregationDaoService
    PageCsiAggregationService pageCsiAggregationService
    ShopCsiAggregationService shopCsiAggregationService
    CsiAggregationTagService csiAggregationTagService
    InMemoryConfigService inMemoryConfigService
    BatchActivityService batchActivityService
    CachingContainerService cachingContainerService

    /**
     * <p>
     * Closes all {@link CsiAggregation}s with closedAndCalculated=false who's time-interval has expired for at least minutes minutes.<br>
     * Closing means:
     * <ul>
     * <li>set attribute closedAndCalculated to true</li>
     * <li>calculate CsiAggregation</li>
     * <li>delete all {@link CsiAggregationUpdateEvent}s of CsiAggregation</li>
     * </ul>
     * Hourly event CsiAggregations should never be closed here because they are set as closed with creation already.
     * </p>
     * @param minutes
     * 					Time for which the CsiAggregation has to be expired.  e.g.
     * 					<ul>
     * 					<li>A DAILY-CsiAggregation with <code>started=2014-07-07 00:00:00</code> and an expiration-time of 180 minutes expires at "2014-07-08 03:00:00"</li>
     * 					<li>A WEEKLY-CsiAggregation with <code>started=2014-07-04 00:00:00</code> and an expiration-time of 300 minutes expires at "2014-07-11 05:00:00"</li>
     * 					</ul>
     */
    void closeCsiAggregationsExpiredForAtLeast(int minutes, boolean createBatchActivity = true) {
        if (!inMemoryConfigService.areMeasurementsGenerallyEnabled()) {
            log.info("No measured value update events are closed cause measurements are generally disabled.")
            return
        }

        BatchActivityUpdater activity
        activity = batchActivityService.getActiveBatchActivity(this.class,Activity.UPDATE, "Close and Calculate CsiAggregations", 2, createBatchActivity)
        List<CsiAggregation> csiAggregationsOpenAndExpired = csiAggregationDaoService.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(minutes)
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${csiAggregationsOpenAndExpired.size()} CsiAggregations identified as open and expired.")
        if (csiAggregationsOpenAndExpired.size() > 0) {
            def lists = csiAggregationsOpenAndExpired.collate(DATACOUNT_BEFORE_WRITING_TO_DB)
            activity.beginNewStage("closing and calculating CsiAggregations", lists.size()).update()
            lists.eachWithIndex { slice, index ->
                CsiAggregation.withNewTransaction {
                    try {
                        closeAndCalculateIfNecessary(slice)
                    } catch (Exception e) {
                        log.error("Quartz controlled cleanup of CsiAggregationUpdateEvents: An error occured during closeAndCalculate csiAggregation: \n" +
                                e.getMessage() +
                                "\n Processing with the next csiAggregations")
                        activity.addFailures().setLastFailureMessage("An error occured during closeAndCalculate csiAggregation").update()
                    }
                }
                activity.addProgressToStage().update()
            }
        }

        deleteUpdateEventsForClosedAndCalculatedMvs(activity)
        activity.done()
    }

    void closeAndCalculateIfNecessary(List<CsiAggregation> csiAggregationsOpenAndExpired) {

        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${CsiAggregationUpdateEvent.count()} update events in db before cleanup.")

        List<CsiAggregationUpdateEvent> updateEventsToBeDeleted = csiAggregationDaoService.getUpdateEvents(csiAggregationsOpenAndExpired*.ident())
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${updateEventsToBeDeleted.size()} update events should get deleted.")

        Map<Long, List<CsiAggregationUpdateEvent>> updateEventsForCsiAggregation = [:].withDefault { [] }
        updateEventsToBeDeleted.each { ue ->
            updateEventsForCsiAggregation[ue.csiAggregationId].add(ue)
        }

        List<CsiAggregation> toCalculateAndClose = []

        csiAggregationsOpenAndExpired.each { CsiAggregation csiAggregationOpenAndExpired ->
            if (csiAggregationOpenAndExpired.hasToBeCalculatedAccordingEvents(updateEventsForCsiAggregation[csiAggregationOpenAndExpired.ident()])) {
                toCalculateAndClose.add(csiAggregationOpenAndExpired)
            }
        }

        csiAggregationsOpenAndExpired.removeAll(toCalculateAndClose)
        if (csiAggregationsOpenAndExpired.size() > 0) closeOpenAndExpiredCsiAggregations(csiAggregationsOpenAndExpired)

        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${toCalculateAndClose.size()} open and expired CsiAggregations should get calculated now.")

        calculateAndCloseCsiAggregations(toCalculateAndClose)

        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${CsiAggregationUpdateEvent.count()} update events in db after cleanup.")
    }

    void calculateAndCloseCsiAggregations(List<CsiAggregation> csiAggregations) {
        // split into different aggregator types
        List<CsiAggregation> pageMvsToCalculate = csiAggregations.findAll {
            it.aggregator.name.equals(AggregatorType.PAGE)
        }
        if (pageMvsToCalculate.size() > 0) {
            calculatePageMvs(pageMvsToCalculate)
            closeOpenAndExpiredCsiAggregations(pageMvsToCalculate)
        }

        List<CsiAggregation> shopMvsToCalculate = csiAggregations.findAll {
            it.aggregator.name.equals(AggregatorType.SHOP)
        }
        if (shopMvsToCalculate.size() > 0) calculateAndCloseShopMvs(shopMvsToCalculate)
    }

    void closeOpenAndExpiredCsiAggregations(List<CsiAggregation> csiAggregationsToClose) {
        def lists = csiAggregationsToClose.collate(DATACOUNT_BEFORE_WRITING_TO_DB)
        lists.each { l ->
            CsiAggregation.withTransaction {
                l.each {
                    it.closedAndCalculated = true
                    it.save()
                }
            }
        }
    }

    void calculatePageMvs(List<CsiAggregation> pageMvsToCalculateAndClose) {
        Map<String, Map<String, List<CsiAggregation>>> hourlyCsiAggregationsForDailyPageMvs = [:].withDefault {
            [:].withDefault { [] }
        }
        Map<String, Map<String, List<CsiAggregation>>> hourlyCsiAggregationsForWeeklyPageMvs = [:].withDefault {
            [:].withDefault { [] }
        }

        List<CsiAggregation> dailyMvsToCalculate = pageMvsToCalculateAndClose.findAll() {
            it.interval.intervalInMinutes == CsiAggregationInterval.DAILY
        }
        List<CsiAggregation> weeklyMvsToCalculate = pageMvsToCalculateAndClose.findAll() {
            it.interval.intervalInMinutes == CsiAggregationInterval.WEEKLY
        }

        Map<Long, Page> allPages = csiAggregationTagService.getAllPagesFromWeeklyOrDailyPageTags(pageMvsToCalculateAndClose*.tag)
        Map<Long, JobGroup> allJobGroups = csiAggregationTagService.getAllJobGroupsFromWeeklyOrDailyPageTags(pageMvsToCalculateAndClose*.tag)

        if (dailyMvsToCalculate.size() > 0) {
            Map<String, List<JobGroup>> dailyJobGroupsByStartDate = cachingContainerService.getDailyJobGroupsByStartDate(dailyMvsToCalculate, allJobGroups)
            Map<String, List<Page>> dailyPagesByStartDate = cachingContainerService.getDailyPagesByStartDate(dailyMvsToCalculate, allPages)
            hourlyCsiAggregationsForDailyPageMvs = cachingContainerService.getDailyHeCsiAggregationMapByStartDate(dailyMvsToCalculate, dailyJobGroupsByStartDate, dailyPagesByStartDate)
        }
        if (weeklyMvsToCalculate.size() > 0) {
            Map<String, List<JobGroup>> weeklyJobGroupsByStartDate = cachingContainerService.getWeeklyJobGroupsByStartDate(weeklyMvsToCalculate, allJobGroups)
            Map<String, List<Page>> weeklyPagesByStartDate = cachingContainerService.getWeeklyPagesByStartDate(weeklyMvsToCalculate, allPages)
            hourlyCsiAggregationsForWeeklyPageMvs = cachingContainerService.getWeeklyHeCsiAggregationMapByStartDate(weeklyMvsToCalculate, weeklyJobGroupsByStartDate, weeklyPagesByStartDate)
        }

        pageMvsToCalculateAndClose.eachWithIndex { CsiAggregation dpCsiAggregationToCalcAndClose, index ->

            CsiAggregationCachingContainer csiAggregationCachingContainer
            if (dpCsiAggregationToCalcAndClose.interval.intervalInMinutes == CsiAggregationInterval.DAILY) {
                csiAggregationCachingContainer = cachingContainerService.createContainerFor(dpCsiAggregationToCalcAndClose, allJobGroups, allPages, hourlyCsiAggregationsForDailyPageMvs[dpCsiAggregationToCalcAndClose.started.toString()])
            } else if (dpCsiAggregationToCalcAndClose.interval.intervalInMinutes == CsiAggregationInterval.WEEKLY) {
                csiAggregationCachingContainer = cachingContainerService.createContainerFor(dpCsiAggregationToCalcAndClose, allJobGroups, allPages, hourlyCsiAggregationsForWeeklyPageMvs[dpCsiAggregationToCalcAndClose.started.toString()])
            } else {
                throw new IllegalArgumentException("Page CsiAggregations can only have interval DAILY or WEEKLY! This CsiAggregation caused this Exception: ${dpCsiAggregationToCalcAndClose}")
            }
            pageCsiAggregationService.calcMv(dpCsiAggregationToCalcAndClose, csiAggregationCachingContainer)

        }
    }


    void calculateAndCloseShopMvs(List<CsiAggregation> shopMvsToCalculate) {
        PerformanceLoggingService performanceLoggingService = new PerformanceLoggingService()

        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'csiAggregationUpdateEventCleanupService: calculate shop measured values', PerformanceLoggingService.IndentationDepth.ONE) {
            shopMvsToCalculate.eachWithIndex { CsiAggregation sCsiAggregationToCalcAndClose, int index ->
                performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, 'csiAggregationUpdateEventCleanupService: calculate ONE shop measured value', PerformanceLoggingService.IndentationDepth.ONE) {
                    shopCsiAggregationService.calcCa(sCsiAggregationToCalcAndClose)
                    closeOpenAndExpiredCsiAggregations([sCsiAggregationToCalcAndClose])
                }
            }
        }

    }

    void deleteUpdateEventsForClosedAndCalculatedMvs(BatchActivityUpdater activity) {
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: searching for update events which belong to closed and calculated measured values...")

        def closedAndCalculatedCsiAggregationsIdsCriteria = new DetachedCriteria(CsiAggregation).build {
            projections {
                distinct('id')
            }
            eq("closedAndCalculated", true)
        }


        List<Long> closedAndCalculatedCsiAggregationIds = closedAndCalculatedCsiAggregationsIdsCriteria.list()
        log.info(closedAndCalculatedCsiAggregationIds.size() + " closedAndCalculated CsiAggregations found")
        activity.beginNewStage("deleting update Events", 1).update()
        if (!closedAndCalculatedCsiAggregationIds.isEmpty()) {
            def updateEventsToBeDeletedCriteria = new DetachedCriteria(CsiAggregationUpdateEvent).build {
                'in' 'csiAggregationId', closedAndCalculatedCsiAggregationIds
            }

            log.info("Deleting " + updateEventsToBeDeletedCriteria.count() + " updateEvents")
            int total = updateEventsToBeDeletedCriteria.deleteAll()

            log.info(total + " updateEvents deleted")
        }
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: done deleting csiAggregationUpdateEvents for closedAndCalculated CsiAggregations")
        activity.addProgressToStage().update()
    }

}
