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

    static transactional = false

    CsiAggregationDaoService csiAggregationDaoService
    PageCsiAggregationService pageCsiAggregationService
    ShopCsiAggregationService shopCsiAggregationService
    CsiAggregationTagService csiAggregationTagService
    InMemoryConfigService inMemoryConfigService
    BatchActivityService batchActivityService
    CsiSystemCsiAggregationService csiSystemCsiAggregationService
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
     def liste = CsiAggregationUpdateEvent.createCriteria().list{'in'("csiAggregationId", [4879979l, 4879995l, 4879900l, 4879985l])}println "size: " + liste.size()
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

        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${CsiAggregationUpdateEvent.count()} update events in db before cleanup.")

        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${csiAggregationsOpenAndExpired.size()} CsiAggregations identified as open and expired.")

        BatchActivityUpdater activityUpdater = batchActivityService.getActiveBatchActivity(this.class, Activity.UPDATE, "Close and Calculate CsiAggregations", 2, createBatchActivity)
        activityUpdater.beginNewStage("closing and calculating CsiAggregations", csiAggregationsOpenAndExpired.size())

        if (csiAggregationsOpenAndExpired.size() > 0) {
            CsiAggregation.withSession { session ->
                csiAggregationsOpenAndExpired.each { csiAggregationToCalcAndClose ->

                    log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: Calculating and closing open and expired csi aggregations...")
                    activityUpdater.addProgressToStage()

                    CsiAggregation.withNewTransaction {
                        try {
                            closeAndCalculateIfNecessary(csiAggregationToCalcAndClose)
                        } catch (Exception e) {
                            log.error("Quartz controlled cleanup of CsiAggregationUpdateEvents: An error occured during closeAndCalculate csiAggregation: \n" +
                                    e.getMessage() +
                                    "\n Processing with the next csiAggregations")
                            activityUpdater.addFailures(e.getMessage())
                        }

                    }

                    session.flush()
                }

                log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: Done calculating and closing open and expired csi aggregations.")
            }
        }

        deleteUpdateEventsForClosedAndCalculatedMvs(activityUpdater)
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${CsiAggregationUpdateEvent.count()} update events in db after cleanup.")

        activityUpdater.done()
    }

    void closeAndCalculateIfNecessary(CsiAggregation csiAggregationOpenAndExpired) {
        CsiAggregationUpdateEvent latestUpdateEvent = csiAggregationDaoService.getLatestUpdateEvent(csiAggregationOpenAndExpired.ident())

        if (csiAggregationOpenAndExpired.hasToBeCalculatedAccordingEvents([latestUpdateEvent])) {
            calculateAndCloseCsiAggregation(csiAggregationOpenAndExpired)
        } else {
            closeOpenAndExpiredCsiAggregations(csiAggregationOpenAndExpired)
        }
    }

    void calculateAndCloseCsiAggregation(CsiAggregation csiAggregation) {
        switch (csiAggregation.aggregator.name) {
            case AggregatorType.PAGE:
                calculatePageMvs(csiAggregation)
                closeOpenAndExpiredCsiAggregations(csiAggregation)
                break

            case AggregatorType.SHOP:
                calculateShopMvs(csiAggregation)
                closeOpenAndExpiredCsiAggregations(csiAggregation)
                break

            case AggregatorType.CSI_SYSTEM:
                calculateCsiSystemCsiAggregation(csiAggregation)
                closeOpenAndExpiredCsiAggregations(csiAggregation)
                break
        }
    }

    private void calculateCsiSystemCsiAggregation(CsiAggregation csiAggregationToCalculate) {
        csiSystemCsiAggregationService.calcCa(csiAggregationToCalculate, csiAggregationToCalculate.csiSystem)
    }

    void closeOpenAndExpiredCsiAggregations(CsiAggregation csiAggregationToClose) {
        CsiAggregation.withTransaction {
            csiAggregationToClose.closedAndCalculated = true
            csiAggregationToClose.save()
        }
    }

    void calculatePageMvs(CsiAggregation pageMvsToCalculateAndClose) {
        Map<String, Map<String, List<CsiAggregation>>> hourlyCsiAggregationsForDailyPageMvs
        Map<String, Map<String, List<CsiAggregation>>> hourlyCsiAggregationsForWeeklyPageMvs

        Map<Long, Page> allPages = csiAggregationTagService.getAllPagesFromWeeklyOrDailyPageTags([pageMvsToCalculateAndClose.tag])
        Map<Long, JobGroup> allJobGroups = csiAggregationTagService.getAllJobGroupsFromWeeklyOrDailyPageTags([pageMvsToCalculateAndClose.tag])

        CsiAggregationCachingContainer csiAggregationCachingContainer

        if (pageMvsToCalculateAndClose.interval.intervalInMinutes == CsiAggregationInterval.DAILY) {

            Map<String, List<JobGroup>> dailyJobGroupsByStartDate = cachingContainerService.getDailyJobGroupsByStartDate([pageMvsToCalculateAndClose], allJobGroups)
            Map<String, List<Page>> dailyPagesByStartDate = cachingContainerService.getDailyPagesByStartDate([pageMvsToCalculateAndClose], allPages)
            hourlyCsiAggregationsForDailyPageMvs = cachingContainerService.getDailyHeCsiAggregationMapByStartDate([pageMvsToCalculateAndClose], dailyJobGroupsByStartDate, dailyPagesByStartDate)
            csiAggregationCachingContainer = cachingContainerService.createContainerFor(pageMvsToCalculateAndClose, allJobGroups, allPages, hourlyCsiAggregationsForDailyPageMvs[pageMvsToCalculateAndClose.started.toString()])

        } else if (pageMvsToCalculateAndClose.interval.intervalInMinutes == CsiAggregationInterval.WEEKLY) {

            Map<String, List<JobGroup>> weeklyJobGroupsByStartDate = cachingContainerService.getWeeklyJobGroupsByStartDate([pageMvsToCalculateAndClose], allJobGroups)
            Map<String, List<Page>> weeklyPagesByStartDate = cachingContainerService.getWeeklyPagesByStartDate([pageMvsToCalculateAndClose], allPages)
            hourlyCsiAggregationsForWeeklyPageMvs = cachingContainerService.getWeeklyHeCsiAggregationMapByStartDate([pageMvsToCalculateAndClose], weeklyJobGroupsByStartDate, weeklyPagesByStartDate)
            csiAggregationCachingContainer = cachingContainerService.createContainerFor(pageMvsToCalculateAndClose, allJobGroups, allPages, hourlyCsiAggregationsForWeeklyPageMvs[pageMvsToCalculateAndClose.started.toString()])

        } else {
            throw new IllegalArgumentException("Page CsiAggregations can only have interval DAILY or WEEKLY! This CsiAggregation caused this Exception: ${pageMvsToCalculateAndClose}")
        }

        pageCsiAggregationService.calcMv(pageMvsToCalculateAndClose, csiAggregationCachingContainer)

    }


    void calculateShopMvs(CsiAggregation shopCsiAggregationToCalculate) {
        shopCsiAggregationService.calcCa(shopCsiAggregationToCalculate)
    }

    void deleteUpdateEventsForClosedAndCalculatedMvs(BatchActivityUpdater activityUpdater) {
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: searching for update events which belong to closed and calculated measured values...")

        List<Long> closedAndCalculatedCsiAggregationIds = CsiAggregation.createCriteria().list() {
            projections {
                property('id')
            }
            eq("closedAndCalculated", true)
        }


        int batchSize = 100
        activityUpdater.beginNewStage("Quartz controlled cleanup of CsiAggregationUpdateEvents: deleting update events for closedAndCalculated csiAggregations", closedAndCalculatedCsiAggregationIds.size())

        if (closedAndCalculatedCsiAggregationIds.size() > 0) {
            def lists = closedAndCalculatedCsiAggregationIds.collate(batchSize)
            int total = 0
            lists.each { l ->
                def updateEventsToBeDeletedCriteria = new DetachedCriteria(CsiAggregationUpdateEvent).build {
                    'in' 'csiAggregationId', l
                }

                total += updateEventsToBeDeletedCriteria.deleteAll()

                activityUpdater.addProgressToStage(l.size())
            }
            log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${total} updateEvents deleted")
        }

        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: done deleting csiAggregationUpdateEvents for closedAndCalculated CsiAggregations")
    }
}
