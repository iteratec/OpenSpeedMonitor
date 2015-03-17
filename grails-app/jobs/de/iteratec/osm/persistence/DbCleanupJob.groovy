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

package de.iteratec.osm.persistence

import de.iteratec.osm.persistence.DbCleanupService
import org.joda.time.DateTime

import de.iteratec.osm.ConfigService



class DbCleanupJob {
	
	DbCleanupService dbCleanupService
	ConfigService configService
	
    static triggers = {
    	/**
    	 * Each Day at 3:00 am.
    	 */
    	cron(name: 'DailyDbCleanup', cronExpression: '0 0 3 ? * *')
    }

    def execute() {
		Date toDeleteWaterfallsBefore = new DateTime().minusWeeks(configService.getDetailDataStorageTimeInWeeks()).toDate()
        dbCleanupService.deleteWaterfallsBefore(toDeleteWaterfallsBefore)
	}
}
