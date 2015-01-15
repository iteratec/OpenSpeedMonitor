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

import org.grails.databinding.BindUsing

import de.iteratec.osm.csi.CsiValue

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
 *  measuredValue.value = eventResult.aValue
 *  measuredValue.calculated = Calculated.Yes
 * 	measuredValue.addToResultIds(eventResult.ident())
 * </pre>
 * better use
 * <pre>
 *  measuredValue.addResult(eventResult)
 * </pre>
 * A change of {@link #value} and {@link #resultIds} should be done in this 
 * class only to guarantee a consistent state.
 * </p>
 * 
 * @author nkuhn
 * @author mze
 */
class MeasuredValue implements CsiValue {

	public static final String DELIMITER_RESULTIDS = ','

	Date started
	MeasuredValueInterval interval
	AggregatorType aggregator

	@BindUsing({ obj, source -> source['tag'] })
	String tag
	Double value
	/**
	 * <p>
	 * If the interval of this measured value is actual (that is it contains now) this should always stay false. 
	 * A nightly quartz-job deletes all {@link MeasuredValueUpdateEvent}s of measured values which interval has expired for at least some hours. These measured values  
	 * get calculated if their last update event requires a recalculation. 
	 * </p>
	 * <b>Note: </b>Finally their closedAndCalculated attribute is set to true.
	 * @see MeasuredValueUpdateEvent
	 */
	Boolean closedAndCalculated = false

	/**
	 * <p>
	 * A comma-separated list of the database identifiers of {@link EventResult}s this value was 
	 * calculated from. Maintained only for hourly-event- {@link MeasuredValue}s.
	 * Used to show a list of raw-data-results (underlying {@link EventResult}s) if users click to data-points of hourly-event- {@link MeasuredValue}s in graphs.
	 * </p>
	 */
	@BindUsing({ obj, source -> source['resultIds'] })
	String resultIds
	
	static transients = ['resultIdsAsList', 'latestUpdateEvent', 'calculated']

	static constraints = {
		started()
		interval()
		aggregator()
		tag()
		value(nullable: true)
		resultIds()
		closedAndCalculated()
	}
	static mapping = {
		resultIds(type: 'text')
		started(index: 'started_and_iVal_and_aggr_and_tag_idx')
		interval(index: 'started_and_iVal_and_aggr_and_tag_idx')
		aggregator(index: 'started_and_iVal_and_aggr_and_tag_idx')
		tag(index: 'started_and_iVal_and_aggr_and_tag_idx')
		closedAndCalculated(defaultValue: false)
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
	 * @see #countResultIds()
	 */
	List<Long> getResultIdsAsList(){
		return resultIds ? resultIds.tokenize(DELIMITER_RESULTIDS).collect({ Long.parseLong(it) }) : []
	}
	/**
	 * Adds the newResultId to the list of {@link EventResult}-identifiers, this value was calculated from.
	 * The newResultId is just added, no identifiers are removed previously.
	 * @param newResultId
	 */
	void addToResultIds(Long newResultId){
		List<Long> list = getResultIdsAsList()
		if(list.contains(newResultId)) log.error("Didn't add EventResult to MeasuredValue because it was already in the list! (EventResult-ID=${newResultId}, MeasuredValue-ID=${this.ident()})")
		list.add(newResultId)
		resultIds = list.join(DELIMITER_RESULTIDS)
	}
	/**
	 * Check if the ResultId containing in the list of {@link EventResult}-identifiers
	 * @param resultId
	 * @return true if the result already in the list
	 */
	boolean containsInResultIds(Long resultId){
		return getResultIdsAsList().contains(resultId)
	}
	/**
	 * Adds all {@link EventResult}-identifiers in resultIdsToAddAsList to the list of {@link EventResult}-identifiers, this value was calculated from.
	 * The results are just added, no identifiers are removed previously.
	 * @param resultIdsToAddAsList
	 */
	void addAllToResultIds(List<Long> resultIdsToAddAsList){
		List<Long> list = getResultIdsAsList()
		List<Long> intersection = list.intersect(resultIdsToAddAsList)
		if(intersection.size() > 0) {log.error("EventResults were added to MeasuredValue although some of them were already in the list! (id's which were already in the list=${intersection}, MeasuredValue-ID=${this.ident()})")}
		list.addAll(resultIdsToAddAsList)
		resultIds = list.join(DELIMITER_RESULTIDS)
	}
	/**
	 * Adds all {@link EventResult}-identifiers represented by resultIdsToAddAsString to the list of {@link EventResult}-identifiers, this value was calculated from.
	 * The results are just added, no identifiers are removed previously.
	 * @param resultIdsToAddAsString
	 * @see #resultIds
	 */
	void addAllToResultIds(String resultIdsToAddAsString){
		List<Long> list = getResultIdsAsList()
		List<Long> intersection = list.intersect(resultIdsToAddAsString.tokenize(DELIMITER_RESULTIDS))
		if(intersection.size() > 0) {log.error("EventResults were added to MeasuredValue although some of them were already in the list! (id's which were already in the list=${intersection}, MeasuredValue-ID=${this.ident()})")}
		list.addAll(resultIdsToAddAsString.tokenize(DELIMITER_RESULTIDS))
		resultIds = list.join(DELIMITER_RESULTIDS)
	}
	/**
	 * Removes all {@link EventResult}-identifiers from the list of identifiers, this value was calculated from.
	 */
	void clearResultIds(){
		this.resultIds = ''
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
	 * @see #getResultIdsAsList()
	 */
	int countResultIds(){
		return getResultIdsAsList().size()
	}
	
	/**
	 * Reads latest {@link MeasuredValueUpdateEvent} for this {@link MeasuredValue} from db. If this events
	 * attribute updateCause requires a recalculation or there is no event at all, true is returned. Otherwise false is returned.
	 * @param toProof
	 * @return True if latest {@link MeasuredValueUpdateEvent} for this {@link MeasuredValue} requires recalculation
	 * 	or there is no event at all. Otherwise false.
	 */
	public boolean hasToBeCalculated(){
		if (this.closedAndCalculated) {
			return false
		}
		MeasuredValueUpdateEvent latestUpdateEvent = getLatestUpdateEvent()
		return latestUpdateEvent == null || latestUpdateEvent.updateCause.requiresRecalculation()
	}

	/**
	 * Gets latest {@link MeasuredValueUpdateEvent} for this {@link MeasuredValue} from param updateEvents. If this events
	 * attribute updateCause requires a recalculation or there is no event at all, true is returned. Otherwise false is returned.
	 * <b>Note:</b> This method is implemented for performance reasons: If many MeasuredValues should be checked the UpdateEvents are only read once from db.
	 * @param toProof
	 * @return True if latest {@link MeasuredValueUpdateEvent} for this {@link MeasuredValue} requires recalculation
	 * 	or there is no event at all. Otherwise false.
	 */
	public boolean hasToBeCalculatedAccordingEvents(List<MeasuredValueUpdateEvent> updateEvents){
		if (this.closedAndCalculated) {
			return false
		}
		boolean hasToBeCalculated = true
		
		updateEvents.inject(null){ Long maxDateOfUpdate, MeasuredValueUpdateEvent actualEvent ->
			if (actualEvent.measuredValueId == this.ident() && ( !maxDateOfUpdate || actualEvent.dateOfUpdate.getTime() > maxDateOfUpdate )) {
				hasToBeCalculated = actualEvent.updateCause.requiresRecalculation
				return actualEvent.dateOfUpdate.getTime()
			}else{
				return maxDateOfUpdate
			}
		}
		return hasToBeCalculated
	}
	
	/**
	 * Checks whether this MeasuredValue was calculated at least one time and never outdated afterwards. 
	 * <b>Note:</b> If no underlying data exists the value of the MeasuredValue is null although it is calculated and this method returns true.
	 * @return True if the MeasuredValue was calculated at least one time and never outdated afterwards.
	 */
	public boolean isCalculated(){
		return !hasToBeCalculated()
	}
	
	/**
	 * Checks whether this MeasuredValue was calculated based on underlying data at least one time and never outdated afterwards.
	 * @return True if the MeasuredValue was calculated based on underlying data at least one time and never outdated afterwards.
	 */
	public boolean isCalculatedWithData(){
		return this.isCalculated() && this.value != null
	}
	
	/**
	 * Checks whether this MeasuredValue was calculated without existing underlying data at least one time and never outdated afterwards.
	 * @return True if the MeasuredValue was calculated without existing underlying data at least one time and never outdated afterwards.
	 */
	public boolean isCalculatedWithoutData(){
		return this.isCalculated() && this.value == null
	}
	
	/**
	 * Delivers latest {@link MeasuredValueUpdateEvent} for this {@link MeasuredValue} from db.
	 * @return Latest {@link MeasuredValueUpdateEvent} for this {@link MeasuredValue} or null if no event exists.
	 */
	private MeasuredValueUpdateEvent getLatestUpdateEvent() {
		def c = MeasuredValueUpdateEvent.createCriteria()
		List<MeasuredValueUpdateEvent> listWithLastUpdateEvent = c.list{
			eq("measuredValueId", this.ident())
			maxResults(1)
			order("dateOfUpdate", "desc")
		}
		return listWithLastUpdateEvent.size() == 1 ? listWithLastUpdateEvent[0] : null
	}
	
	@Override
	public boolean isCsiRelevant(){
		return this.isCalculated() && this.value != null
	}
	
	@Override
	public Double retrieveValue() {
		return value
	}
	
	@Override
	Date retrieveDate() {
		return this.started
	}
	
	@Override
	public String retrieveTag() {
		return this.tag
	}
	
	@Override
	public List<Long> retrieveUnderlyingEventResultIds(){
		return this.getResultIdsAsList()
	}
	
}
