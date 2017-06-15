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

package de.iteratec.osm

import org.joda.time.DateTime
import org.joda.time.Duration
/**
 * Provides cached configs from {@link OsmConfiguration}.
 * @author nkuhn
 *
 */
class OsmConfigCacheService {
	
	//TODO: bad design, remove code-dulplication 
	
	ConfigService configService

    Integer minDocCompleteTimeInMillisecs
	private DateTime lastFetchOfMinDocCompleteTimeInMillisecs = new DateTime(1980,1,1,0,0)
    Integer maxDocCompleteTimeInMillisecs
    private DateTime lastFetchOfMaxDocCompleteTimeInMillisecs = new DateTime(1980,1,1,0,0)

	Integer getCachedMinDocCompleteTimeInMillisecs(Double ageToleranceInHours = 24) {
		Duration durationSinceLastFetch = new Duration(lastFetchOfMinDocCompleteTimeInMillisecs.getMillis(), new DateTime().getMillis())
		if(!minDocCompleteTimeInMillisecs || durationSinceLastFetch.getStandardHours()>ageToleranceInHours){
			minDocCompleteTimeInMillisecs = configService.getMinDocCompleteTimeInMillisecs()
			lastFetchOfMinDocCompleteTimeInMillisecs = new DateTime()
		}
		return minDocCompleteTimeInMillisecs
	}
	
	Integer getCachedMaxDocCompleteTimeInMillisecs(Double ageToleranceInHours = 24) {
		Duration durationSinceLastFetch = new Duration(lastFetchOfMaxDocCompleteTimeInMillisecs.getMillis(), new DateTime().getMillis())
		if(!maxDocCompleteTimeInMillisecs || durationSinceLastFetch.getStandardHours()>ageToleranceInHours){
			maxDocCompleteTimeInMillisecs = configService.getMaxDocCompleteTimeInMillisecs()
			lastFetchOfMaxDocCompleteTimeInMillisecs = new DateTime()
		}
		return maxDocCompleteTimeInMillisecs
	}
}
