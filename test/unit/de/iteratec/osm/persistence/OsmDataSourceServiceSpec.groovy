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

import grails.test.mixin.*

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import org.junit.*

import de.iteratec.osm.util.ServiceMocker

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(OsmDataSourceService)
class OsmDataSourceServiceSpec {
	
	OsmDataSourceService serviceUnderTest 
	ServiceMocker mocker
	//Mocks:
//	ConfigService configServiceMock
		
	
	@Before
	void setUp() {
		serviceUnderTest = service
		mocker = ServiceMocker.create()
		
	}
	
	@Test
    void testGetRLikeSupport() {
		
		mocker.mockConfigService(serviceUnderTest, 'org.apache.derby.jdbc.ClientDriver')
		assertThat(serviceUnderTest.RLikeSupport, is(false))
		
		mocker.mockConfigService(serviceUnderTest, 'org.hsqldb.jdbcDriver')
		assertThat(serviceUnderTest.RLikeSupport, is(false))

		mocker.mockConfigService(serviceUnderTest, 'com.sybase.jdbc.SybDriver')
		assertThat(serviceUnderTest.RLikeSupport, is(false))
		
		mocker.mockConfigService(serviceUnderTest, 'org.sqlite.JDBC')
		assertThat(serviceUnderTest.RLikeSupport, is(false))

		mocker.mockConfigService(serviceUnderTest, 'org.postgresql.Driver')
		assertThat(serviceUnderTest.RLikeSupport, is(false))
		
		mocker.mockConfigService(serviceUnderTest, 'com.microsoft.sqlserver.jdbc.SQLServerDriver')
		assertThat(serviceUnderTest.RLikeSupport, is(false))
		
		mocker.mockConfigService(serviceUnderTest, 'org.h2.Driver')
		assertThat(serviceUnderTest.RLikeSupport, is(false))
		
		mocker.mockConfigService(serviceUnderTest, 'com.mysql.jdbc.Driver')
		assertThat(serviceUnderTest.RLikeSupport, is(true))
		
		mocker.mockConfigService(serviceUnderTest, 'oracle.jdbc.driver.OracleDriver')
		assertThat(serviceUnderTest.RLikeSupport, is(true))
    }
}
