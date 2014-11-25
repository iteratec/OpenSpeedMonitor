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

package de.iteratec.osm.result.detail

import de.iteratec.osm.result.CachedView

/**
 * WebPerformanceWaterfall
 * A domain class describes the data object and it's mapping to the database
 */
class WebPerformanceWaterfall {

	Date	dateCreated
	Date	lastUpdated
	
	Collection<WaterfallEntry> waterfallEntries
	static hasMany		= [waterfallEntries:WaterfallEntry]
	
	String url
	Date startDate
	String title
	String eventName
	Integer numberOfWptRun
	CachedView cachedView
	Integer startRenderInMillisecs
	Integer docCompleteTimeInMillisecs
	Integer domTimeInMillisecs
	Integer fullyLoadedTimeInMillisecs
	
    static mapping = {
    }
    
	static constraints = {
		url()
		startDate()
		title()
		eventName(nullable: true)
		numberOfWptRun()
		cachedView()
		startRenderInMillisecs()
		docCompleteTimeInMillisecs()
		domTimeInMillisecs()
		fullyLoadedTimeInMillisecs()
    }
	
	/*
	 * Methods of the Domain Class
	 */
	@Override 
	public String toString() {
		return title;
	}
	
	/**
	 * Remove Waterfall to delete from all EventResults before deletion.
	 * Note: There is no explicit Hibernate-equivalent for "on update set null" for a foreign-key :-(
	 */
	def beforeDelete() {
		/*
		 * Should be enabled, if large amount of waterfalls are deleted in production:
		 * 
		EventResult.withNewSession {
			EventResult.findAllByWebPerformanceWaterfall(this).each {eventResult ->
			  eventResult.webPerformanceWaterfall = null
			  eventResult.save(failOnError: true)
			}
		}
		*/
   }
}
