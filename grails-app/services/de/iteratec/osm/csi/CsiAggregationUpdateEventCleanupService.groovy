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
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationDaoService
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
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
    public static final int BATCH_SIZE = 500
    CsiAggregationDaoService csiAggregationDaoService
    PageCsiAggregationService pageCsiAggregationService
    ShopCsiAggregationService shopCsiAggregationService
    InMemoryConfigService inMemoryConfigService
    BatchActivityService batchActivityService
    CsiSystemCsiAggregationService csiSystemCsiAggregationService

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

        List<Long> csiAggregationsOpenAndExpiredIds = []
        CsiAggregation.withNewSession {
            csiAggregationsOpenAndExpiredIds = csiAggregationDaoService.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(minutes)*.id
        }

        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${CsiAggregationUpdateEvent.count()} update events in db before cleanup.")

        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${csiAggregationsOpenAndExpiredIds.size()} CsiAggregations identified as open and expired.")

        BatchActivityUpdater activityUpdater = batchActivityService.getActiveBatchActivity(this.class, Activity.UPDATE, "Close and Calculate CsiAggregations", 4, createBatchActivity, 50)
        activityUpdater.beginNewStage("closing and calculating CsiAggregations", csiAggregationsOpenAndExpiredIds.size())

        List<Long> csiAggregationIdsStillToClose = []

        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: Calculating and closing open and expired csi aggregations...")
        int calculationCount = 0
        int failureCount = 0
        csiAggregationsOpenAndExpiredIds.each { currentCsiAggregationId ->

            try {
                boolean noCalculationNecessary = !calculateIfNecessary(currentCsiAggregationId)
                if (noCalculationNecessary) {
                    csiAggregationIdsStillToClose << currentCsiAggregationId
                } else {
                    calculationCount++
                }
            } catch (Exception e) {
                log.error("Quartz controlled cleanup of CsiAggregationUpdateEvents: An error occured during closeAndCalculate csiAggregation with id \'${currentCsiAggregationId}\': \n" +
                        e.getMessage() +
                        "\n Processing with the next csiAggregations")
                activityUpdater?.addFailures(e.getMessage())
                failureCount++
            }
            activityUpdater?.addProgressToStage()
        }

        activityUpdater?.addComment("closed and calculated ${calculationCount} csiAggregations (${failureCount} calculations failed).")

        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: Done calculating and closing open and expired csi aggregations.")
        closeAllCsiAggregations(csiAggregationIdsStillToClose, activityUpdater)

        deleteUpdateEventsForClosedAndCalculatedMvs(activityUpdater)
        deleteUpdateEventsForNotExistingCsiAggregations(activityUpdater)

        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${CsiAggregationUpdateEvent.count()} update events in db after cleanup.")
    }

    boolean calculateIfNecessary(Long csiAggregationOpenAndExpiredId) {
        activityUpdater?.beginNewStage("Quartz controlled cleanup of CsiAggregationUpdateEvents: closing calculated csiAggregations", csiAggregationIds.size())

        activityUpdater?.addComment("${csiAggregationIds.size()} csiAggregations marked as closed and calculated.")
        CsiAggregation csiAggregationOpenAndExpired = CsiAggregation.get(csiAggregationOpenAndExpiredId)
        if (csiAggregationOpenAndExpired.hasToBeCalculated()) {
            calculateCsiAggregation(csiAggregationOpenAndExpired)
            return true
        } else {
            return false
        }
    }

    void calculateCsiAggregation(CsiAggregation csiAggregation) {
        CsiAggregation.withNewSession { session ->

            List<CsiAggregation> toClose

            switch (csiAggregation.aggregator.name) {
                case AggregatorType.PAGE:
                    toClose = pageCsiAggregationService.calcCsiAggregations([csiAggregation.id])
                    break

                case AggregatorType.SHOP:
                    toClose = shopCsiAggregationService.calcCsiAggregations([csiAggregation.id])
                    break

                case AggregatorType.CSI_SYSTEM:
                    toClose = csiSystemCsiAggregationService.calcCsiAggregations([csiAggregation.id])
                    break
            }

            toClose.each { CsiAggregation aggregation ->
                aggregation.closedAndCalculated = true
                aggregation.save(failOnError: true)
            }

            session.flush()
            session.clear()
        }
    }

    void closeAllCsiAggregations(List<Long> csiAggregationIds, BatchActivityUpdater activityUpdater) {

        activityUpdater?.beginNewStage("Quartz controlled cleanup of CsiAggregationUpdateEvents: closing calculated csiAggregations", csiAggregationIds.size())

        csiAggregationIds.collate(BATCH_SIZE).each { sublist ->

            // sublist can be an empty list, if csiAggregationIds is an empty list
            if (sublist) {

                List<CsiAggregation> csiAggregationsToClose = CsiAggregation.getAll(sublist)
                csiAggregationsToClose.each { CsiAggregation toClose ->
                    toClose.closedAndCalculated = true
                    toClose.save(failOnError: true)
                }

                activityUpdater?.addProgressToStage(sublist.size())
            }
        }
        activityUpdater?.addComment("${csiAggregationIds.size()} csiAggregations closed (no calculation necessary).")
    }

    void deleteUpdateEventsForClosedAndCalculatedMvs(BatchActivityUpdater activityUpdater) {
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: searching for update events which belong to closed and calculated measured values...")

        List<Long> closedAndCalculatedCsiAggregationIds = CsiAggregation.createCriteria().list() {
            projections {
                property('id')
            }
            eq("closedAndCalculated", true)
        }

        int total = 0
        activityUpdater?.beginNewStage("Quartz controlled cleanup of CsiAggregationUpdateEvents: deleting update events for closedAndCalculated csiAggregations", closedAndCalculatedCsiAggregationIds.size())
        if (closedAndCalculatedCsiAggregationIds.size() > 0) {
            def lists = closedAndCalculatedCsiAggregationIds.collate(BATCH_SIZE)
            lists.each { currentIds ->
                if (currentIds) {
                    def updateEventsToBeDeletedCriteria = new DetachedCriteria(CsiAggregationUpdateEvent).build {
                        'in' 'csiAggregationId', currentIds
                    }

                    total += updateEventsToBeDeletedCriteria.deleteAll()

                    activityUpdater?.addProgressToStage(currentIds.size())

                }
            }

        }
        activityUpdater?.addComment("${total} csiAggregationUpdateEvents deleted.")
        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: ${total} updateEvents deleted")

        log.info("Quartz controlled cleanup of CsiAggregationUpdateEvents: done deleting csiAggregationUpdateEvents for closedAndCalculated CsiAggregations")
    }

    private void deleteUpdateEventsForNotExistingCsiAggregations(BatchActivityUpdater batchActivityUpdater) {
        batchActivityUpdater?.beginNewStage("Quartz controlled cleanup of CsiAggregationUpdateEvents: deleting update events for non-existing csiAggregations", 1)

        CsiAggregationUpdateEvent.executeUpdate("delete from CsiAggregationUpdateEvent where csi_aggregation_id not in (select id from CsiAggregation)")

        batchActivityUpdater?.addProgressToStage(1)
        batchActivityUpdater?.done()
        batchActivityUpdater?.addComment("csiAggregationUpdateEvents for non-existing csiAggregations deleted.")
    }

}
