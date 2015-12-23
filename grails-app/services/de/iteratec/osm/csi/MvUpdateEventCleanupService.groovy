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
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueDaoService
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent
import de.iteratec.osm.result.MeasuredValueTagService
import groovyx.gpars.GParsPool
import org.springframework.transaction.TransactionStatus

/**
 * Contains the logic to ...
 * <ul>
 * <li>calculate (if necessary) and close {@link MeasuredValue}s who aren't already closed and who's interval expired.</li>
 * <li>cleanup associated {@link MeasuredValueUpdateEvent}s.</li>
 * </ul>
 * @author nkuhn
 */
class MvUpdateEventCleanupService {

    static transactional = false

    MeasuredValueDaoService measuredValueDaoService
    PageMeasuredValueService pageMeasuredValueService
    ShopMeasuredValueService shopMeasuredValueService
    MeasuredValueTagService measuredValueTagService
    InMemoryConfigService inMemoryConfigService
    BatchActivityService batchActivityService

    /**
     * <p>
     * Closes all {@link MeasuredValue}s with closedAndCalculated=false who's time-interval has expired for at least minutes minutes.<br>
     * Closing means:
     * <ul>
     * <li>set attribute closedAndCalculated to true</li>
     * <li>calculate MeasuredValue</li>
     * <li>delete all {@link MeasuredValueUpdateEvent}s of MeasuredValue</li>
     * </ul>
     * Hourly event MeasuredValues should never be closed here because they are set as closed with creation already.
     * </p>
     * @param minutes
     * 					Time for which the MeasuredValue has to be expired.  e.g.
     * 					<ul>
     * 					<li>A DAILY-MeasuredValue with <code>started=2014-07-07 00:00:00</code> and an expiration-time of 180 minutes expires at "2014-07-08 03:00:00"</li>
     * 					<li>A WEEKLY-MeasuredValue with <code>started=2014-07-04 00:00:00</code> and an expiration-time of 300 minutes expires at "2014-07-11 05:00:00"</li>
     * 					</ul>
     */
    void closeMeasuredValuesExpiredForAtLeast(int minutes, boolean createBatchActivity = true) {

        if (!inMemoryConfigService.areMeasurementsGenerallyEnabled()) {
            log.info("No measured value update events are closed cause measurements are generally disabled.")
            return
        }

        List<MeasuredValue> mvsOpenAndExpired = measuredValueDaoService.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(minutes)
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${mvsOpenAndExpired.size()} MeasuredValues identified as open and expired.")
        if (mvsOpenAndExpired.size() > 0) {
            closeAndCalculateIfNecessary(mvsOpenAndExpired, createBatchActivity)
        }

    }

    void closeAndCalculateIfNecessary(List<MeasuredValue> mvsOpenAndExpired, boolean createBatchActivity) {
        BatchActivity activity = batchActivityService.getActiveBatchActivity(this.class, 0, Activity.UPDATE, "Close and Calculate MeasuredValues", createBatchActivity)
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${MeasuredValueUpdateEvent.count()} update events in db before cleanup.")
        List<MeasuredValueUpdateEvent> updateEventsToBeDeleted = measuredValueDaoService.getUpdateEvents(mvsOpenAndExpired*.ident())
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${updateEventsToBeDeleted.size()} update events should get deleted.")

        Map<Long, List<MeasuredValueUpdateEvent>> updateEventsForMeasuredValue = new HashMap()
        updateEventsToBeDeleted.each { ue ->
            if (!updateEventsForMeasuredValue.containsKey(ue.measuredValueId)) {
                updateEventsForMeasuredValue[ue.measuredValueId] = new ArrayList<>()
            }
            updateEventsForMeasuredValue[ue.measuredValueId].add(ue)
        }

        List<MeasuredValue> justToClose = []
        List<MeasuredValue> toCalculateAndClose = []

        activity.updateStatus(["stage": "Split into justClose and calculateAndClose"])
        mvsOpenAndExpired.eachWithIndex { MeasuredValue mvOpenAndExpired, int index ->
            activity.updateStatus(["progressWithinStage": batchActivityService.calculateProgress(mvsOpenAndExpired.size(), index + 1)])
            if (mvOpenAndExpired.hasToBeCalculatedAccordingEvents(updateEventsForMeasuredValue[mvOpenAndExpired.ident()])) {
                toCalculateAndClose.add(mvOpenAndExpired)
            } else {
                justToClose.add(mvOpenAndExpired)
            }
        }

        activity.updateStatus(["stage": "Closing all already calculated MeasuredValues", "progress": batchActivityService.calculateProgress(100, (100 * (1 / 3)) as Integer)])
        try {
            log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${justToClose.size()} already calculated MeasuredValues should get closed now.")
            justToClose.eachWithIndex { MeasuredValue toClose, int index ->

                activity.updateStatus(["progressWithinStage": batchActivityService.calculateProgress(justToClose.size(), index + 1)])
                MeasuredValue.withTransaction { TransactionStatus status ->
                    closeMv(toClose, updateEventsForMeasuredValue[toClose.ident()])
                    status.flush()
                }
                activity.updateStatus(["successfulActions": ++activity.getSuccessfulActions()])
            }
        } catch (Exception e) {
            def message = "An error occurred while closing MeasuredValues who are  calculated already."
            log.error(message, e)
            activity.updateStatus(["lastFailureMessage": message, "failures": ++activity.getFailures()])
        }
        activity.updateStatus(["progress": batchActivityService.calculateProgress(100, (100 * (2 / 3)) as Integer)])
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${toCalculateAndClose.size()} open and expired MeasuredValues should get calculated now.")

        // split into different aggregator types
        List<MeasuredValue> pageMvsToCalculate = toCalculateAndClose.findAll {
            it.aggregator.name.equals(AggregatorType.PAGE)
        }
        List<MeasuredValueUpdateEvent> pageUpdateEvents = new ArrayList<>()
        pageMvsToCalculate.each { pageUpdateEvents.addAll(updateEventsForMeasuredValue[it.ident()]) }
        if (pageMvsToCalculate.size() > 0) calculateAndClosePageMvs(pageMvsToCalculate, pageUpdateEvents, activity)

        List<MeasuredValue> shopMvsToCalculate = toCalculateAndClose.findAll {
            it.aggregator.name.equals(AggregatorType.SHOP)
        }
        List<MeasuredValueUpdateEvent> shopUpdateEvents = new ArrayList<>()
        shopMvsToCalculate.each { shopUpdateEvents.addAll(updateEventsForMeasuredValue[it.ident()]) }
        if (shopMvsToCalculate.size() > 0) calculateAndCloseShopMvs(shopMvsToCalculate, shopUpdateEvents, activity)

        activity.updateStatus(["stage": "", "endDate": new Date(), "status": Status.DONE, "progress": batchActivityService.calculateProgress(100, 100)])
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${MeasuredValueUpdateEvent.count()} update events in db after cleanup.")
    }

    void calculateAndClosePageMvs(List<MeasuredValue> pageMvsToCalculateAndClose, List<MeasuredValueUpdateEvent> updateEventsToBeDeleted, BatchActivity activity) {

        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: Creating caching container for calculating page MeasuredValues ...")
        CachingContainerFactory ccFactory = new CachingContainerFactory(pageMvsToCalculateAndClose, measuredValueTagService, pageMeasuredValueService)
        log.info("... DONE creating caching container")
        List<MeasuredValueUpdateEvent> ueToBeDeletedOfPageMvs = updateEventsToBeDeleted.findAll { eventToBeDeleted -> pageMvsToCalculateAndClose*.ident().contains(eventToBeDeleted.measuredValueId) }
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${ueToBeDeletedOfPageMvs.size()} update events of page mv to be calculated should get deleted.")

        int size = pageMvsToCalculateAndClose.size()
        activity.updateStatus(["stage": "Calculate and Close Page MV"])
        pageMvsToCalculateAndClose.eachWithIndex { MeasuredValue dpmvToCalcAndClose, int index ->
            activity.updateStatus(["progressWithinStage": batchActivityService.calculateProgress(size, index + 1)])
            MeasuredValue.withTransaction { TransactionStatus status ->
                pageMeasuredValueService.calcMv(dpmvToCalcAndClose, ccFactory.createContainerFor(dpmvToCalcAndClose))
                closeMv(dpmvToCalcAndClose, ueToBeDeletedOfPageMvs)
                status.flush()
            }
            activity.updateStatus(["successfulActions": ++activity.getSuccessfulActions()])
        }
    }

    void calculateAndCloseShopMvs(List<MeasuredValue> shopMvsToCalculate, List<MeasuredValueUpdateEvent> updateEventsToBeDeleted, BatchActivity activity) {

        List<MeasuredValueUpdateEvent> ueToBeDeletedOfShopMvs = updateEventsToBeDeleted.findAll { eventToBeDeleted -> shopMvsToCalculate*.ident().contains(eventToBeDeleted.measuredValueId) }
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${ueToBeDeletedOfShopMvs.size()} update events of shop mv to be calculated should get deleted.")

        int size = shopMvsToCalculate.size()
        shopMvsToCalculate.eachWithIndex { MeasuredValue smvToCalcAndClose, int index ->
            activity.updateStatus(["progressWithinStage": batchActivityService.calculateProgress(size, index + 1)])
            MeasuredValue.withTransaction { TransactionStatus status ->
                shopMeasuredValueService.calcMv(smvToCalcAndClose)
                closeMv(smvToCalcAndClose, ueToBeDeletedOfShopMvs)
                status.flush()
            }
            activity.updateStatus(["successfulActions": ++activity.getSuccessfulActions()])
        }
    }

    /**
     * Closes {@link MeasuredValue} toClose and deletes all associated {@link MeasuredValueUpdateEvent}s.
     * @param toClose
     */
    void closeMv(MeasuredValue toClose, List<MeasuredValueUpdateEvent> containAllEventsOfToClose) {
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: The following MeasuredValue should get closed now: ${toClose}")
        toClose.closedAndCalculated = true
        toClose.save(failOnError: true)
        List<MeasuredValueUpdateEvent> updateEventsToDelete = containAllEventsOfToClose.findAll { mvue -> mvue.measuredValueId == toClose.ident() }
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${updateEventsToDelete.size()} MeasuredValueUpdateEvents should get deleted now.")

        Iterator itr = updateEventsToDelete.iterator()
        while (itr.hasNext()) {
            def updateEvent = itr.next()
            itr.remove()
            updateEvent.delete()
        }
    }

    void deleteUpdateEventsForClosedMVs() {
        log.info("searching for update events which belong to closed and calculated measured values...")
        List<MeasuredValueUpdateEvent> allUpdateEvents = MeasuredValueUpdateEvent.findAll()
        List<Long> measuredValueIdsForUpdateEvents = allUpdateEvents*.measuredValueId
        List<Long> closedMeasuredValuesWithUpdateEvents = MeasuredValue.findAllByIdInList(measuredValueIdsForUpdateEvents).findAll {
            it.closedAndCalculated == true
        }*.ident()

        List<MeasuredValueUpdateEvent> updateEventsToDelete = []
        allUpdateEvents.each {
            if (closedMeasuredValuesWithUpdateEvents.contains(it.measuredValueId)) {
                updateEventsToDelete.add(it)
            }
        }

        log.info("deleting " + updateEventsToDelete.size() + " update events for closed and calculated measured values")
        MeasuredValueUpdateEvent.withNewSession { session ->
            Iterator itr = updateEventsToDelete.iterator()
            while (itr.hasNext()) {
                def updateEvent = itr.next()
                itr.remove()
                updateEvent.delete()
            }

            session.flush()
        }
        log.info("done")
    }
}
