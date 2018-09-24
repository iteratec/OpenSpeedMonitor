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

package de.iteratec.osm.report.chart

import de.iteratec.osm.csi.CsiSystem
import de.iteratec.osm.csi.CsiValue
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredEvent
import grails.databinding.BindUsing


/**
 * <p>
 * Aggregated values to be shown in diagrams.
 * </p>
 *
 * <p>
 * This value is an aggregation of {@linkplain de.iteratec.osm.result.EventResult
 * event results} identified by the returned database identifiers.
 * </p>
 *
 * <p>
 * FIXME mze-2013-09-13: Architecture improvement:
 * All calculation relevant changes should be placed here, which means:
 * Instead of code like:
 * <pre>
 *  csiAggregation.value = eventResult.aValue
 *  csiAggregation.calculated = Calculated.Yes
 * 	csiAggregation.addToUnderlyingEventResultsByWptDocComplete(eventResult.ident())
 * </pre>
 * better use
 * <pre>
 *  csiAggregation.addResult(eventResult)
 * </pre>
 * A change of {@link #value} and {@link #resultIds} should be done in this
 * class only to guarantee a consistent state.
 * </p>
 *
 * @author nkuhn
 * @author mze
 */
class CsiAggregation implements CsiValue {

    public static final String DELIMITER_RESULTIDS = ','

    Date started
    CsiAggregationInterval interval
    AggregationType aggregationType

    JobGroup jobGroup
    MeasuredEvent measuredEvent
    Page page
    Browser browser
    Location location

    CsiSystem csiSystem
    ConnectivityProfile connectivityProfile
    Double csByWptDocCompleteInPercent
    Double csByWptVisuallyCompleteInPercent
    Collection<EventResult> underlyingEventResultsByVisuallyComplete = []

    static hasMany = [underlyingEventResultsByVisuallyComplete: EventResult]
    /**
     * <p>
     * If the interval of this measured value is actual (that is it contains now) this should always stay false.
     * A nightly quartz-job deletes all {@link CsiAggregationUpdateEvent}s of measured values which interval has expired for at least some hours. These measured values
     * get calculated if their last update event requires a recalculation.
     * </p>
     * <b>Note: </b>Finally their closedAndCalculated attribute is set to true.
     * @see CsiAggregationUpdateEvent
     */
    Boolean closedAndCalculated = false

    /**
     * <p>
     * A comma-separated list of the database identifiers of {@link EventResult}s this value was
     * calculated from. Maintained only for hourly-event- {@link CsiAggregation}s.
     * Used to show a list of raw-data-results (underlying {@link EventResult}s) if users click to data-points of hourly-event- {@link CsiAggregation}s in graphs.
     * </p>
     */
    @BindUsing({
        obj, source -> source['underlyingEventResultsByWptDocComplete']
    })
    String underlyingEventResultsByWptDocComplete = ''

    static transients = ['underlyingEventResultsByWptDocCompleteAsList', 'latestUpdateEvent', 'calculated']

    static constraints = {
        started(nullable: false)
        interval(nullable: false)
        aggregationType(nullable: false)

        // JobGroup can be null if aggregationType == csiSystem
        jobGroup(nullable: true, validator: { val, obj -> return (obj.aggregationType == AggregationType.CSI_SYSTEM) || val })
        // measuredEvent can be null if aggregationType in (csiSystem, jobGroup, page)
        measuredEvent(nullable: true, validator: { val, obj ->
            return (obj.aggregationType == AggregationType.CSI_SYSTEM) || (obj.aggregationType == AggregationType.JOB_GROUP) || (obj.aggregationType == AggregationType.PAGE) || val
        })
        //page can be null if aggregationType in (csiSystem, jobGroup)
        page(nullable: true, validator: { val, obj -> return (obj.aggregationType == AggregationType.CSI_SYSTEM) || (obj.aggregationType == AggregationType.JOB_GROUP) || val })
        // browser can be null if aggregationType in (csiSystem, jobGroup, page)
        browser(nullable: true, validator: { val, obj -> return (obj.aggregationType == AggregationType.CSI_SYSTEM) || (obj.aggregationType == AggregationType.JOB_GROUP) || (obj.aggregationType == AggregationType.PAGE) || val })
        // location can be null if aggregationType in (csiSystem, jobGroup, page)
        location(nullable: true, validator: { val, obj -> return (obj.aggregationType == AggregationType.CSI_SYSTEM) || (obj.aggregationType == AggregationType.JOB_GROUP) || (obj.aggregationType == AggregationType.PAGE) || val })

        csByWptDocCompleteInPercent(nullable: true)
        csByWptVisuallyCompleteInPercent(nullable: true)
        underlyingEventResultsByWptDocComplete(nullable: false)
        closedAndCalculated(nullable: false)
        connectivityProfile(nullable: true)
        csiSystem(nullable: true)
    }
    static mapping = {
        underlyingEventResultsByWptDocComplete(defaultValue: '',type: 'text')
        closedAndCalculated(defaultValue: false, index: 'closedAndCalculated_and_started_idx')
        started(index: 'started_and_iVal_and_aggr_idx,closedAndCalculated_and_started_idx')
        interval(index: 'started_and_iVal_and_aggr_idx')
        aggregationType(index: 'started_and_iVal_and_aggr_idx')
    }

    /**
     * <p>
     * Returns a list of the database identifiers of {@linkplain
     * de.iteratec.osm.result.EventResult event results} this value was
     * calculated from. This value is an aggregation of the results
     * identified by the returned database identifiers.
     * </p>
     *
     * @return A list of the identifiers of the event results aggregated to
     *         calculate this value. The size of returned list is >= 0.
     *         The result is never <code>null</code>.
     * @see #countUnderlyingEventResultsByWptDocComplete()
     */
    List<Long> getUnderlyingEventResultsByWptDocCompleteAsList() {
        return underlyingEventResultsByWptDocComplete ? underlyingEventResultsByWptDocComplete.tokenize(DELIMITER_RESULTIDS).collect({
            Long.parseLong(it)
        }) : []
    }
    /**
     * Adds the newResultId to the list of {@link EventResult}-identifiers, this value was calculated from.
     * The newResultId is just added, no identifiers are removed previously.
     * @param newResultId
     */
    void addToUnderlyingEventResultsByWptDocComplete(Long newResultId) {
        List<Long> list = getUnderlyingEventResultsByWptDocCompleteAsList()
        if (list.contains(newResultId)) log.error("Didn't add EventResult to CsiAggregation because it was already in the list! (EventResult-ID=${newResultId}, CsiAggregation-ID=${this.ident()})")
        list.add(newResultId)
        underlyingEventResultsByWptDocComplete = list.join(DELIMITER_RESULTIDS)
    }
    /**
     * Check if the ResultId containing in the list of {@link EventResult}-identifiers
     * @param resultId
     * @return true if the result already in the list
     */
    boolean containsInUnderlyingEventResultsByWptDocComplete(Long resultId) {
        return getUnderlyingEventResultsByWptDocCompleteAsList().contains(resultId)
    }
    /**
     * Adds all {@link EventResult}-identifiers in resultIdsToAddAsList to the list of {@link EventResult}-identifiers, this value was calculated from.
     * The results are just added, no identifiers are removed previously.
     * @param resultIdsToAddAsList
     */
    void addAllToUnderlyingEventResultsByWptDocComplete(List<Long> resultIdsToAddAsList) {
        List<Long> list = getUnderlyingEventResultsByWptDocCompleteAsList()
        List<Long> intersection = list.intersect(resultIdsToAddAsList)
        if (intersection.size() > 0) {
            log.error("EventResults were added to CsiAggregation although some of them were already in the list! (id's which were already in the list=${intersection}, CsiAggregation-ID=${this.ident()})")
        }
        list.addAll(resultIdsToAddAsList)
        underlyingEventResultsByWptDocComplete = list.join(DELIMITER_RESULTIDS)
    }
    /**
     * Adds all {@link EventResult}-identifiers represented by resultIdsToAddAsString to the list of {@link EventResult}-identifiers, this value was calculated from.
     * The results are just added, no identifiers are removed previously.
     * @param resultIdsToAddAsString
     * @see #underlyingEventResultsByWptDocComplete
     */
    void addAllToResultIds(String resultIdsToAddAsString) {
        List<Long> list = getUnderlyingEventResultsByWptDocCompleteAsList()
        List<Long> intersection = list.intersect(resultIdsToAddAsString.tokenize(DELIMITER_RESULTIDS))
        if (intersection.size() > 0) {
            log.error("EventResults were added to CsiAggregation although some of them were already in the list! (id's which were already in the list=${intersection}, CsiAggregation-ID=${this.ident()})")
        }
        list.addAll(resultIdsToAddAsString.tokenize(DELIMITER_RESULTIDS))
        underlyingEventResultsByWptDocComplete = list.join(DELIMITER_RESULTIDS)
    }
    /**
     * Removes all {@link EventResult}-identifiers from the list of identifiers, this value was calculated from.
     */
    void clearUnderlyingEventResultsByWptDocComplete() {
        this.underlyingEventResultsByWptDocComplete = ''
    }

    /**
     * <p>
     * The count of {@linkplain de.iteratec.osm.result.EventResult event results},
     * this value was calculated from. This value is an aggregation of the
     * returned number of results.
     * </p>
     *
     * @return The count of event results aggregated to calculate this value.
     *         The result is >= 0.
     * @see #getUnderlyingEventResultsByWptDocCompleteAsList()
     */
    int countUnderlyingEventResultsByWptDocComplete() {
        return getUnderlyingEventResultsByWptDocCompleteAsList().size()
    }

    /**
     * Reads latest {@link CsiAggregationUpdateEvent} for this {@link CsiAggregation} from db. If this events
     * attribute updateCause requires a recalculation or there is no event at all, true is returned. Otherwise false is returned.
     * @param toProof
     * @return True if latest {@link CsiAggregationUpdateEvent} for this {@link CsiAggregation} requires recalculation
     * 	or there is no event at all. Otherwise false.
     */
    public boolean hasToBeCalculated() {
        if (this.closedAndCalculated) {
            return false
        }
        CsiAggregationUpdateEvent latestUpdateEvent = getLatestUpdateEvent()
        return latestUpdateEvent == null || latestUpdateEvent.updateCause.requiresRecalculation()
    }

    /**
     * Gets latest {@link CsiAggregationUpdateEvent} for this {@link CsiAggregation} from param updateEvents. If this events
     * attribute updateCause requires a recalculation or there is no event at all, true is returned. Otherwise false is returned.
     * <b>Note:</b> This method is implemented for performance reasons: If many CsiAggregations should be checked the UpdateEvents are only read once from db.
     * @param toProof
     * @return True if latest {@link CsiAggregationUpdateEvent} for this {@link CsiAggregation} requires recalculation
     * 	or there is no event at all. Otherwise false.
     */
    public boolean hasToBeCalculatedAccordingEvents(List<CsiAggregationUpdateEvent> updateEvents) {
        if (this.closedAndCalculated) {
            return false
        }
        boolean hasToBeCalculated = true

        updateEvents.inject(null) { Long maxDateOfUpdate, CsiAggregationUpdateEvent actualEvent ->
            if (actualEvent.csiAggregationId == this.ident() && (!maxDateOfUpdate || actualEvent.dateOfUpdate.getTime() > maxDateOfUpdate)) {
                hasToBeCalculated = actualEvent.updateCause.requiresRecalculation
                return actualEvent.dateOfUpdate.getTime()
            } else {
                return maxDateOfUpdate
            }
        }
        return hasToBeCalculated
    }

    /**
     * Checks whether this CsiAggregation was calculated at least one time and never outdated afterwards.
     * <b>Note:</b> If no underlying data exists the value of the CsiAggregation is null although it is calculated and this method returns true.
     * @return True if the CsiAggregation was calculated at least one time and never outdated afterwards.
     */
    public boolean isCalculated() {
        return !hasToBeCalculated()
    }

    /**
     * Checks whether this CsiAggregation was calculated based on underlying data at least one time and never outdated afterwards.
     * @return True if the CsiAggregation was calculated based on underlying data at least one time and never outdated afterwards.
     */
    public boolean isCalculatedWithData() {
        return this.isCalculated() && this.csByWptDocCompleteInPercent != null
    }

    /**
     * Checks whether this CsiAggregation was calculated without existing underlying data at least one time and never outdated afterwards.
     * @return True if the CsiAggregation was calculated without existing underlying data at least one time and never outdated afterwards.
     */
    public boolean isCalculatedWithoutData() {
        return this.isCalculated() && this.csByWptDocCompleteInPercent == null
    }

    /**
     * Delivers latest {@link CsiAggregationUpdateEvent} for this {@link CsiAggregation} from db.
     * @return Latest {@link CsiAggregationUpdateEvent} for this {@link CsiAggregation} or null if no event exists.
     */
    private CsiAggregationUpdateEvent getLatestUpdateEvent() {
        def c = CsiAggregationUpdateEvent.createCriteria()
        List<CsiAggregationUpdateEvent> listWithLastUpdateEvent = c.list {
            eq("csiAggregationId", this.ident())
            maxResults(1)
            order("dateOfUpdate", "desc")
        }
        return listWithLastUpdateEvent.size() == 1 ? listWithLastUpdateEvent[0] : null
    }

    @Override
    public Double retrieveCsByWptDocCompleteInPercent() {
        return csByWptDocCompleteInPercent
    }

    @Override
    public Double retrieveCsByWptVisuallyCompleteInPercent() {
        return csByWptVisuallyCompleteInPercent
    }

    @Override
    Date retrieveDate() {
        return this.started
    }

    @Override
    ConnectivityProfile retrieveConnectivityProfile() {
        return this.connectivityProfile
    }

    @Override
    public List<Long> retrieveUnderlyingEventResultsByDocComplete() {
        return this.getUnderlyingEventResultsByWptDocCompleteAsList()
    }

    @Override
    public List<EventResult> retrieveUnderlyingEventResultsByVisuallyComplete() {
        return this.underlyingEventResultsByVisuallyComplete.toList()
    }

    @Override
    JobGroup retrieveJobGroup() {
        return jobGroup
    }

    @Override
    Page retrievePage() {
        return page
    }

    @Override
    Browser retrieveBrowser() {
        return browser
    }

    public List<CsiAggregationUpdateEvent> getCsiAggregationUpdateEvents() {
        return CsiAggregationUpdateEvent.findAllByCsiAggregationId(this.ident())
    }

    public String toString() {
        return "${aggregationType.toString()} | ${interval} | ${started} | ${csByWptDocCompleteInPercent}"
    }
}
