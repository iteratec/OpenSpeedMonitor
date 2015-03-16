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

package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.script.Script;

/**
 * <p>
 * A measured event is a load of a {@link Page} under specific circumstances.
 * A page could be load in multiple different situations. These situations 
 * defined by the testers and named in the corresponding job {@link Script}.
 * The name measured event was introduced with the definition of the 
 * script command used to define them which is called {@code setEventName}. 
 * The measured events are used to group {@link EventResult}s of multiple 
 * {@link JobResult}s.
 * </p>
 * 
 * <p>
 * This type was introduced for the multistep-measurement project
 * </p>
 * 
 * @author nkuhn
 * @author mze
 * 
 * @see Script
 * @see EventResult
 * @see JobResult
 */
class MeasuredEvent {

	/**
	 * The unique name of this measured event as defined in {@link Script} 
	 * using {@code setEventName} or, if not explicitly specified, the name
	 * which was automatically assigned by the exceuting Web-Page-Test agent;
	 * not <code>null</code>, not {@linkplain String#isEmpty() empty}.
	 */
	String name

	/**
	 * The {@link Page} tested by this measured event; 
	 * Never <code>null</code> (but possible the page {@link Page#UNDEFINED}).
	 */
	Page testedPage

	static constraints = {
		name(nullable: false, blank: false, unique: true, maxSize: 255)
		testedPage(nullable: false)
	}

	@Override
	String toString(){
		return name + ' (page: ' + testedPage.name + ')'
	}
}
