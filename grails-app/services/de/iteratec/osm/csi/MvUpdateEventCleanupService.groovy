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
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.DetachedCriteria
import org.springframework.transaction.TransactionStatus

/**
 * Contains the logic to ...
 * <ul>
 * <li>calculate (if necessary) and close {@link CsiAggregation}s who aren't already closed and who's interval expired.</li>
 * <li>cleanup associated {@link MeasuredValueUpdateEvent}s.</li>
 * </ul>
 * @author nkuhn
 */
class MvUpdateEventCleanupService {

    final int DATACOUNT_BEFORE_WRITING_TO_DB = 100;

    static transactional = false

    MeasuredValueDaoService measuredValueDaoService
    PageMeasuredValueService pageMeasuredValueService
    ShopMeasuredValueService shopMeasuredValueService
    MeasuredValueTagService measuredValueTagService
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
     * <li>delete all {@link MeasuredValueUpdateEvent}s of CsiAggregation</li>
     * </ul>
     * Hourly event MeasuredValues should never be closed here because they are set as closed with creation already.
     * </p>
     * @param minutes
     * 					Time for which the CsiAggregation has to be expired.  e.g.
     * 					<ul>
     * 					<li>A DAILY-CsiAggregation with <code>started=2014-07-07 00:00:00</code> and an expiration-time of 180 minutes expires at "2014-07-08 03:00:00"</li>
     * 					<li>A WEEKLY-CsiAggregation with <code>started=2014-07-04 00:00:00</code> and an expiration-time of 300 minutes expires at "2014-07-11 05:00:00"</li>
     * 					</ul>
     */
    void closeMeasuredValuesExpiredForAtLeast(int minutes, boolean createBatchActivity = true) {

        if (!inMemoryConfigService.areMeasurementsGenerallyEnabled()) {
            log.info("No measured value update events are closed cause measurements are generally disabled.")
            return
        }

        List<CsiAggregation> mvsOpenAndExpired = measuredValueDaoService.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(minutes)
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${mvsOpenAndExpired.size()} MeasuredValues identified as open and expired.")
        if (mvsOpenAndExpired.size() > 0) {
            closeAndCalculateIfNecessary(mvsOpenAndExpired, createBatchActivity)
        }

        deleteUpdateEventsForClosedAndCalculatedMvs()
    }

    void closeAndCalculateIfNecessary(List<CsiAggregation> mvsOpenAndExpired, boolean createBatchActivity) {
        BatchActivity activity = batchActivityService.getActiveBatchActivity(this.class, 0, Activity.UPDATE, "Close and Calculate MeasuredValues", createBatchActivity)
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${MeasuredValueUpdateEvent.count()} update events in db before cleanup.")
        List<MeasuredValueUpdateEvent> updateEventsToBeDeleted = measuredValueDaoService.getUpdateEvents(mvsOpenAndExpired*.ident())
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${updateEventsToBeDeleted.size()} update events should get deleted.")

        Map<Long, List<MeasuredValueUpdateEvent>> updateEventsForMeasuredValue = [:].withDefault { [] }
        updateEventsToBeDeleted.each { ue ->
            updateEventsForMeasuredValue[ue.measuredValueId].add(ue)
        }

        List<CsiAggregation> toCalculateAndClose = []

        activity.updateStatus(["stage": "Get MeasuredValues which have to be calculated before closing"])
        mvsOpenAndExpired.eachWithIndex { CsiAggregation mvOpenAndExpired, int index ->
            activity.updateStatus(["progressWithinStage": batchActivityService.calculateProgress(mvsOpenAndExpired.size(), index + 1)])
            if (mvOpenAndExpired.hasToBeCalculatedAccordingEvents(updateEventsForMeasuredValue[mvOpenAndExpired.ident()])) {
                toCalculateAndClose.add(mvOpenAndExpired)
            }
        }

        activity.updateStatus(["stage": "Closing all calculated MeasuredValues", "progress": batchActivityService.calculateProgress(100, (100 * (1 / 3)) as Integer)])
        mvsOpenAndExpired.removeAll(toCalculateAndClose)
        if (mvsOpenAndExpired.size() > 0) closeOpenAndExpiredMvs(mvsOpenAndExpired)

        activity.updateStatus(["progress": batchActivityService.calculateProgress(100, (100 * (2 / 3)) as Integer)])
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${toCalculateAndClose.size()} open and expired MeasuredValues should get calculated now.")

        calculateAndCloseMeasuredValues(toCalculateAndClose, activity)

        activity.updateStatus(["stage": "", "endDate": new Date(), "status": Status.DONE, "progress": batchActivityService.calculateProgress(100, 100)])
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${MeasuredValueUpdateEvent.count()} update events in db after cleanup.")
    }

    void calculateAndCloseMeasuredValues(List<CsiAggregation> measuredValues, BatchActivity activity) {
        // split into different aggregator types
        List<CsiAggregation> pageMvsToCalculate = measuredValues.findAll {
            it.aggregator.name.equals(AggregatorType.PAGE)
        }
        if (pageMvsToCalculate.size() > 0) calculatePageMvs(pageMvsToCalculate, activity)
        closeOpenAndExpiredMvs(pageMvsToCalculate, activity)

        List<CsiAggregation> shopMvsToCalculate = measuredValues.findAll {
            it.aggregator.name.equals(AggregatorType.SHOP)
        }
        if (shopMvsToCalculate.size() > 0) calculateAndCloseShopMvs(shopMvsToCalculate, activity)
    }

    void closeOpenAndExpiredMvs(List<CsiAggregation> measuredValuesToClose, BatchActivity activity = null) {
        def lists = measuredValuesToClose.collate(DATACOUNT_BEFORE_WRITING_TO_DB)
        lists.eachWithIndex { l, index ->
            CsiAggregation.withTransaction { TransactionStatus status ->
                if (activity)
                    activity.updateStatus(["progressWithinStage": batchActivityService.calculateProgress(measuredValuesToClose.size(), (index + 1) * DATACOUNT_BEFORE_WRITING_TO_DB)])
                l.each { mv ->
                    mv.closedAndCalculated = true
                    mv.save()
                }
                if (activity)
                    activity.updateStatus(["successfulActions": ++activity.getSuccessfulActions()])
            }
        }
    }

    void calculatePageMvs(List<CsiAggregation> pageMvsToCalculateAndClose, BatchActivity activity) {

        Map<String, Map<String, List<CsiAggregation>>> hemvsForDailyPageMvs = [:].withDefault { [:].withDefault { [] } }
        Map<String, Map<String, List<CsiAggregation>>> hemvsForWeeklyPageMvs = [:].withDefault { [:].withDefault { [] } }

        List<CsiAggregation> dailyMvsToCalculate = pageMvsToCalculateAndClose.findAll() {
            it.interval.intervalInMinutes == MeasuredValueInterval.DAILY
        }
        List<CsiAggregation> weeklyMvsToCalculate = pageMvsToCalculateAndClose.findAll() {
            it.interval.intervalInMinutes == MeasuredValueInterval.WEEKLY
        }

        Map<Long, Page> allPages = measuredValueTagService.getAllPagesFromWeeklyOrDailyPageTags(pageMvsToCalculateAndClose*.tag)
        Map<Long, JobGroup> allJobGroups = measuredValueTagService.getAllJobGroupsFromWeeklyOrDailyPageTags(pageMvsToCalculateAndClose*.tag)

        if (dailyMvsToCalculate.size() > 0) {
            Map<String, List<JobGroup>> dailyJobGroupsByStartDate = cachingContainerService.getDailyJobGroupsByStartDate(dailyMvsToCalculate, allJobGroups)
            Map<String, List<Page>> dailyPagesByStartDate = cachingContainerService.getDailyPagesByStartDate(dailyMvsToCalculate, allPages)
            hemvsForDailyPageMvs = cachingContainerService.getDailyHemvMapByStartDate(dailyMvsToCalculate, dailyJobGroupsByStartDate, dailyPagesByStartDate)
        }
        if (weeklyMvsToCalculate.size() > 0) {
            Map<String, List<JobGroup>> weeklyJobGroupsByStartDate = cachingContainerService.getWeeklyJobGroupsByStartDate(weeklyMvsToCalculate, allJobGroups)
            Map<String, List<Page>> weeklyPagesByStartDate = cachingContainerService.getWeeklyPagesByStartDate(weeklyMvsToCalculate, allPages)
            hemvsForWeeklyPageMvs = cachingContainerService.getWeeklyHemvMapByStartDate(weeklyMvsToCalculate, weeklyJobGroupsByStartDate, weeklyPagesByStartDate)
        }

        activity.updateStatus(["stage": "Calculate and Close Page MV"])

        int size = pageMvsToCalculateAndClose.size()
        int counter = 1
        def lists = pageMvsToCalculateAndClose.collate(DATACOUNT_BEFORE_WRITING_TO_DB)
        lists.each { l ->
            CsiAggregation.withTransaction { TransactionStatus status ->
                l.eachWithIndex { CsiAggregation dpmvToCalcAndClose, int index ->
                    activity.updateStatus(["progressWithinStage": batchActivityService.calculateProgress(size, counter)])
                    ++counter

                    MvCachingContainer mvCachingContainer
                    if (dpmvToCalcAndClose.interval.intervalInMinutes == MeasuredValueInterval.DAILY) {
                        mvCachingContainer = cachingContainerService.createContainerFor(dpmvToCalcAndClose, allJobGroups, allPages, hemvsForDailyPageMvs[dpmvToCalcAndClose.started.toString()])
                    } else if (dpmvToCalcAndClose.interval.intervalInMinutes == MeasuredValueInterval.WEEKLY) {
                        mvCachingContainer = cachingContainerService.createContainerFor(dpmvToCalcAndClose, allJobGroups, allPages, hemvsForWeeklyPageMvs[dpmvToCalcAndClose.started.toString()])
                    } else {
                        throw new IllegalArgumentException("Page MeasuredValues can only have interval DAILY or WEEKLY! This CsiAggregation caused this Exception: ${mv}")
                    }
                    pageMeasuredValueService.calcMv(dpmvToCalcAndClose, mvCachingContainer)
                }
                activity.updateStatus(["successfulActions": ++activity.getSuccessfulActions()])

            }
        }
    }


    void calculateAndCloseShopMvs(List<CsiAggregation> shopMvsToCalculate, BatchActivity activity) {
        PerformanceLoggingService performanceLoggingService = new PerformanceLoggingService()

        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'mvUpdateEventCleanupService: calculate shop measured values', PerformanceLoggingService.IndentationDepth.ONE) {
            int size = shopMvsToCalculate.size()
            shopMvsToCalculate.eachWithIndex { CsiAggregation smvToCalcAndClose, int index ->
                performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, 'mvUpdateEventCleanupService: calculate ONE shop measured value', PerformanceLoggingService.IndentationDepth.ONE) {
                    CsiAggregation.withTransaction { TransactionStatus status ->
                        activity.updateStatus(["progressWithinStage": batchActivityService.calculateProgress(size, index + 1)])
                        shopMeasuredValueService.calcMv(smvToCalcAndClose)
                        activity.updateStatus(["successfulActions": ++activity.getSuccessfulActions()])
                        closeOpenAndExpiredMvs([smvToCalcAndClose])
                    }
                }
            }
        }

    }

    void deleteUpdateEventsForClosedAndCalculatedMvs() {
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: searching for update events which belong to closed and calculated measured values...")
        List<Long> closedAndCalculatedMeasuredValueUpdateEventIds = CsiAggregation.findAllWhere(closedAndCalculated: true)*.ident()

        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: deleting all update events for closed and calculated measured values")

        def criteria = new DetachedCriteria(MeasuredValueUpdateEvent).build {
            'in' 'measuredValueId', closedAndCalculatedMeasuredValueUpdateEventIds
        }

        criteria.deleteAll()

        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: done deleting mesauredValueUpdateEvents for closedAndCalculated MeasuredValues")
    }

}
