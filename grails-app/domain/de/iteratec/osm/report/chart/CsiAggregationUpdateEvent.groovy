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

import groovy.transform.EqualsAndHashCode

/**
 * Is written with timestamp of the occurrence of one of the following:
 * <ul>
 * <li>A {@link CsiAggregation} is outdated cause of a new, so far unregarded {@link EventResult} was running into the database.</li>
 * <li>A {@link CsiAggregation} is correctly calculated.</li>
 * </ul>
 */
@EqualsAndHashCode
class CsiAggregationUpdateEvent {
	
	/**
	 * Subject a {@link CsiAggregationUpdateFlag} can have.
	 * @author nkuhn
	 *
	 */
	public enum UpdateCause {
		/**
		 * In the moment of the update the affected {@link CsiAggregation} is outdated cause of a new, so far unregarded {@link EventResult}.
		 */
		OUTDATED (true),
		/**
		 * In the moment of the update the affected {@link CsiAggregation} is correctly calculated. All relevant {@link EventResult}s are factored in.
		 * If no EventResult is relevant for the CsiAggregation the CALCULATED-UpdateEvent is written, too. The value of the affected CsiAggregation will remain null.
		 */
		CALCULATED(false),
		
		private final boolean requiresRecalculation
		UpdateCause(boolean requiresRecalculation){
			this.requiresRecalculation = requiresRecalculation
		}
		public boolean requiresRecalculation(){return this.requiresRecalculation}
	}

	/**
	 * Timestamp the update occured.
	 */
	Date dateOfUpdate
	/**
	 * Cause of the update.
	 */
	UpdateCause updateCause
	/**
	 * ID of affected {@link CsiAggregation}.
	 */
	Long csiAggregationId
	
	
    static mapping = {
        csiAggregationId(index: 'csiAggregationId_idx')
    }
    
	static constraints = {
		dateOfUpdate()
		updateCause()
		csiAggregationId()
    }
	
	/*
	 * Methods of the Domain Class
	 */
	@Override
	public String toString() {
		return "${dateOfUpdate}: ${updateCause} of CsiAggregation with id=${csiAggregationId}";
	}
}
