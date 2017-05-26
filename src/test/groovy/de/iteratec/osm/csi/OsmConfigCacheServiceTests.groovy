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

import de.iteratec.osm.OsmConfigCacheService
import grails.buildtestdata.mixin.Build
import grails.test.mixin.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfiguration
import spock.lang.Specification

/**
 * Test-suite of {@link OsmConfigCacheService}.
 */
@TestFor(OsmConfigCacheService)
@Build([OsmConfiguration])
@Mock([OsmConfiguration])
class OsmConfigCacheServiceTests extends Specification {

    void setup() {
        OsmConfiguration.build()
        service.configService = new ConfigService()
    }

    void "test accessing cached configs min doc complete time"() {
        when: "getting cached min doc complete time"
            Integer time = service.getCachedMinDocCompleteTimeInMillisecs(24)
        then: "the result should be 250ms"
            time == 250
    }

    void "test accessing cached configs max doc complete time"() {
        when: "getting cached max doc complete time"
            Integer time = service.getCachedMaxDocCompleteTimeInMillisecs(24)
        then: "the result should be 3m"
            time == 180000
    }
}
