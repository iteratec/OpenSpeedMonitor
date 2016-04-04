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
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.Status
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.CsiAggregationTagService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.DetachedCriteria
import org.springframework.transaction.TransactionStatus

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

        List<CsiAggregation> csiAggregationsOpenAndExpired = csiAggregationDaoService.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(minutes)
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${csiAggregationsOpenAndExpired.size()} CsiAggregations identified as open and expired.")
        if (csiAggregationsOpenAndExpired.size() > 0) {
            closeAndCalculateIfNecessary(csiAggregationsOpenAndExpired, createBatchActivity)
        }

        deleteUpdateEventsForClosedAndCalculatedMvs()
    }

    void closeAndCalculateIfNecessary(List<CsiAggregation> csiAggregationsOpenAndExpired, boolean createBatchActivity) {
        BatchActivity activity = batchActivityService.getActiveBatchActivity(this.class, 0, Activity.UPDATE, "Close and Calculate CsiAggregations", createBatchActivity)
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${CsiAggregationUpdateEvent.count()} update events in db before cleanup.")
        List<CsiAggregationUpdateEvent> updateEventsToBeDeleted = csiAggregationDaoService.getUpdateEvents(csiAggregationsOpenAndExpired*.ident())
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${updateEventsToBeDeleted.size()} update events should get deleted.")

        Map<Long, List<CsiAggregationUpdateEvent>> updateEventsForCsiAggregation = [:].withDefault { [] }
        updateEventsToBeDeleted.each { ue ->
            updateEventsForCsiAggregation[ue.csiAggregationId].add(ue)
        }

        List<CsiAggregation> toCalculateAndClose = []

        activity.updateStatus(["stage": "Get CsiAggregations which have to be calculated before closing"])
        csiAggregationsOpenAndExpired.eachWithIndex { CsiAggregation csiAggregationOpenAndExpired, int index ->
            activity.updateStatus(["progressWithinStage": batchActivityService.calculateProgress(csiAggregationsOpenAndExpired.size(), index + 1)])
            if (csiAggregationOpenAndExpired.hasToBeCalculatedAccordingEvents(updateEventsForCsiAggregation[csiAggregationOpenAndExpired.ident()])) {
                toCalculateAndClose.add(csiAggregationOpenAndExpired)
            }
        }

        activity.updateStatus(["stage": "Closing all calculated CsiAggregations", "progress": batchActivityService.calculateProgress(100, (100 * (1 / 3)) as Integer)])
        csiAggregationsOpenAndExpired.removeAll(toCalculateAndClose)
        if (csiAggregationsOpenAndExpired.size() > 0) closeOpenAndExpiredCsiAggregations(csiAggregationsOpenAndExpired)

        activity.updateStatus(["progress": batchActivityService.calculateProgress(100, (100 * (2 / 3)) as Integer)])
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${toCalculateAndClose.size()} open and expired CsiAggregations should get calculated now.")

        calculateAndCloseCsiAggregations(toCalculateAndClose, activity)

        activity.updateStatus(["stage": "", "endDate": new Date(), "status": Status.DONE, "progress": batchActivityService.calculateProgress(100, 100)])
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${CsiAggregationUpdateEvent.count()} update events in db after cleanup.")
    }

    void calculateAndCloseCsiAggregations(List<CsiAggregation> csiAggregations, BatchActivity activity) {
        // split into different aggregator types
        List<CsiAggregation> pageMvsToCalculate = csiAggregations.findAll {
            it.aggregator.name.equals(AggregatorType.PAGE)
        }
        if (pageMvsToCalculate.size() > 0) calculatePageMvs(pageMvsToCalculate, activity)
        closeOpenAndExpiredCsiAggregations(pageMvsToCalculate, activity)

        List<CsiAggregation> shopMvsToCalculate = csiAggregations.findAll {
            it.aggregator.name.equals(AggregatorType.SHOP)
        }
        if (shopMvsToCalculate.size() > 0) calculateAndCloseShopMvs(shopMvsToCalculate, activity)
    }

    void closeOpenAndExpiredCsiAggregations(List<CsiAggregation> csiAggregationsToClose, BatchActivity activity = null) {
        def lists = csiAggregationsToClose.collate(DATACOUNT_BEFORE_WRITING_TO_DB)
        lists.eachWithIndex { l, index ->
            CsiAggregation.withTransaction { TransactionStatus status ->
                if (activity)
                    activity.updateStatus(["progressWithinStage": batchActivityService.calculateProgress(csiAggregationsToClose.size(), (index + 1) * DATACOUNT_BEFORE_WRITING_TO_DB)])
                l.each { csiAggregation ->
                    csiAggregation.closedAndCalculated = true
                    csiAggregation.save()
                }
                if (activity)
                    activity.updateStatus(["successfulActions": ++activity.getSuccessfulActions()])
            }
        }
    }

    void calculatePageMvs(List<CsiAggregation> pageMvsToCalculateAndClose, BatchActivity activity) {

        Map<String, Map<String, List<CsiAggregation>>> hourlyCsiAggregationsForDailyPageMvs = [:].withDefault { [:].withDefault { [] } }
        Map<String, Map<String, List<CsiAggregation>>> hourlyCsiAggregationsForWeeklyPageMvs = [:].withDefault { [:].withDefault { [] } }

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

        activity.updateStatus(["stage": "Calculate and Close Page MV"])

        int size = pageMvsToCalculateAndClose.size()
        int counter = 1
        def lists = pageMvsToCalculateAndClose.collate(DATACOUNT_BEFORE_WRITING_TO_DB)
        lists.each { l ->
            CsiAggregation.withTransaction { TransactionStatus status ->
                l.eachWithIndex { CsiAggregation dpCsiAggregationToCalcAndClose, int index ->
                    activity.updateStatus(["progressWithinStage": batchActivityService.calculateProgress(size, counter)])
                    ++counter

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
                activity.updateStatus(["successfulActions": ++activity.getSuccessfulActions()])

            }
        }
    }


    void calculateAndCloseShopMvs(List<CsiAggregation> shopMvsToCalculate, BatchActivity activity) {
        PerformanceLoggingService performanceLoggingService = new PerformanceLoggingService()

        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'csiAggregationUpdateEventCleanupService: calculate shop measured values', PerformanceLoggingService.IndentationDepth.ONE) {
            int size = shopMvsToCalculate.size()
            shopMvsToCalculate.eachWithIndex { CsiAggregation sCsiAggregationToCalcAndClose, int index ->
                performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, 'csiAggregationUpdateEventCleanupService: calculate ONE shop measured value', PerformanceLoggingService.IndentationDepth.ONE) {
                    CsiAggregation.withTransaction { TransactionStatus status ->
                        activity.updateStatus(["progressWithinStage": batchActivityService.calculateProgress(size, index + 1)])
                        shopCsiAggregationService.calcCa(sCsiAggregationToCalcAndClose)
                        activity.updateStatus(["successfulActions": ++activity.getSuccessfulActions()])
                        closeOpenAndExpiredCsiAggregations([sCsiAggregationToCalcAndClose])
                    }
                }
            }
        }

    }

    void deleteUpdateEventsForClosedAndCalculatedMvs() {
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: searching for update events which belong to closed and calculated measured values...")
        List<Long> closedAndCalculatedCsiAggregationUpdateEventIds = CsiAggregation.findAllWhere(closedAndCalculated: true)*.ident()

        if (closedAndCalculatedCsiAggregationUpdateEventIds.size() > 0){
            log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: deleting all update events for closed and calculated measured values")

            def criteria = new DetachedCriteria(CsiAggregationUpdateEvent).build {
                'in' 'csiAggregationId', closedAndCalculatedCsiAggregationUpdateEventIds
            }

            criteria.deleteAll()

            log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: done deleting mesauredValueUpdateEvents for closedAndCalculated CsiAggregations")
        }

    }

}
