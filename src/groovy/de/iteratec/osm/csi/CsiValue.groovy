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

package de.iteratec.osm.csi;

/**
 * A value for calculation of customer satisfaction indices. 
 * @author nkuhn
 *
 */
public interface CsiValue {
	/**
	 * Delivers csi-relevant value of this {@link CsiValue}.
	 */
	public Double retrieveValue()
	
	/**
	 * Delivers csi-relevant date of this {@link CsiValue}.
	 * @return
	 */
	public Date retrieveDate()
	
	/**
	 * Delivers csi-tag.
	 * @see MeasuredValueTagService
	 */
	public String retrieveTag()
	
	/**
	 * Delivers the id's of all {@link EventResult}s which underly this CsiValue. 
	 * @return
	 */
	public List<Long> retrieveUnderlyingEventResultIds()
	
	/**
	 * Whether or not this value should be factored in csi-calculations.
	 * <ul>
	 * <li>{@link EventResult}s are relevant if they have set a docCompleteTimeInMillisecs and customerSatisfactionInPercent and customerSatisfactionInPercent
	 * is within valid range.</li>
	 * <li>{@link MesauredValue}s are relevant if they have a state of {@link Calculated#Yes}</li>
	 * </ul>
	 * @return
	 * @see CsiConfigCacheService
	 */
	public boolean isCsiRelevant()
	
}
