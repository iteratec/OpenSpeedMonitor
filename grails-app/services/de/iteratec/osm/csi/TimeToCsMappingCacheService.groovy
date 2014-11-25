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

import org.joda.time.DateTime
import org.joda.time.Duration

class TimeToCsMappingCacheService {
	
	private Map<String, Double> timeToCsMappings
	private final Integer fetchingMappingsFrequencyInHours = 24
	private DateTime lastFetchOfMappings = new DateTime(1980,1,1,0,0)
	
	private Map<String, List<Integer>> frustrations
	private final Integer fetchingFrustrationsFrequencyInHours = 24
	private DateTime lastFetchOfFrustrations = new DateTime(1980,1,1,0,0)
	
	public Map<String, Double> getTimeToCsMappings(){
		Duration durationSinceLastFetch = new Duration(lastFetchOfMappings.getMillis(), new DateTime().getMillis())
		if(!timeToCsMappings || durationSinceLastFetch.getStandardHours()>fetchingMappingsFrequencyInHours){
			fetchMappings()
		}
		return timeToCsMappings
	}
	
	public List<Integer> getCustomerFrustrations(Page page){
		Duration durationSinceLastFetch = new Duration(lastFetchOfFrustrations.getMillis(), new DateTime().getMillis())
		if (log.infoEnabled) {
			log.info("lastFetchOfFrustrations=$lastFetchOfFrustrations")
			log.info("durationSinceLastFetch=$durationSinceLastFetch")
		}
		if(!frustrations || durationSinceLastFetch.getStandardHours()>fetchingFrustrationsFrequencyInHours){
			fetchFrustrations()
		}
		return frustrations[page.name]
	}
	
	private fetchMappings(){
		timeToCsMappings = [:]
		def query = TimeToCsMapping.where {
			mappingVersion >= max(mappingVersion)//bug in grails 2.1.1: == doesn't work with subqueries
		  }
		List<TimeToCsMapping> timeToCsMappingsFromDb = query.findAll()
		List actualMapings = TimeToCsMapping.findAllByMappingVersion(timeToCsMappingsFromDb?timeToCsMappingsFromDb[0].mappingVersion:-1)
		Integer i
		for(i=0;i<actualMapings.size();i++){
			if (log.infoEnabled) {
				log.info("key in der mapping-map=${actualMapings[i].page.name}_${actualMapings[i].loadTimeInMilliSecs.toString()}")
			}
			timeToCsMappings["${actualMapings[i].page.name}_${actualMapings[i].loadTimeInMilliSecs.toString()}"]=actualMapings[i].customerSatisfaction
		}
		//grails-/groovy-bug: the following leeds to StackOverflow...
//		actualMapings.each{mapping ->
//			log.info("key in der mapping-map=${mapping.page.name}_${mapping.loadTimeInMilliSecs.toString()}")
//			timeToCsMappings["${mapping.page.name}_${mapping.loadTimeInMilliSecs.toString()}"]=mapping.customerSatisfaction
//		}
		lastFetchOfMappings = new DateTime()
		if(log.debugEnabled){
			timeToCsMappings.each{entry ->
				log.debug "entry.key=${entry.key}, entry.value=${entry.value}"
			}
		}
	}
	
	private fetchFrustrations(){
		frustrations = [:].withDefault {[]}
		def query = CustomerFrustration.where {
			investigationVersion >= max(investigationVersion)//bug in grails 2.1.1: == doesn't work with subqueries
		  }
		List<CustomerFrustration> frustrationsWithMaxVersion = query.findAll()
		frustrationsWithMaxVersion.each{
			frustrations[it.page.name].add(it.loadTimeInMilliSecs)
		}
		lastFetchOfFrustrations = new DateTime()
		if(log.debugEnabled){
			log.debug "fetching of CustomerFrustrations..."
			frustrations.each{entry ->
				log.debug "page=${entry.key}, list of frustration-loadtimes=${entry.value}"
			}
		}
	}
}
