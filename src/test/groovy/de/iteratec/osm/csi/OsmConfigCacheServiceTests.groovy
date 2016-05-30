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
import grails.test.mixin.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfiguration

/**
 * Test-suite of {@link OsmConfigCacheService}.
 */
@TestFor(OsmConfigCacheService)
@Mock([OsmConfiguration])
class OsmConfigCacheServiceTests {

	static transactional = false
	OsmConfigCacheService serviceUnderTest

	def doWithSpring = {
		configService(ConfigService)
	}

	@Before
	void setUp() {
		serviceUnderTest=service
		serviceUnderTest.configService = grailsApplication.mainContext.getBean('configService')

		// creating configuration with default values
		new OsmConfiguration().save(failOnError: true)
	}

	@Test
	void testAccessingCachedConfigs() {
		Assert.assertEquals(250, serviceUnderTest.getCachedMinDocCompleteTimeInMillisecs(24))
		Assert.assertEquals(180000, serviceUnderTest.getCachedMaxDocCompleteTimeInMillisecs(24))
	}
}
