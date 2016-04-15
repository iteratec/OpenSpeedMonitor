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

import org.junit.AfterClass
import org.junit.BeforeClass

import de.iteratec.osm.report.chart.CsiAggregationDaoService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval

/**
 * Common base class for all integration tests which empties the database after execution of a test class.
 * 
 * @author dri
 *
 *@Deprecated use SPOCK-Framework for testing instead -> NonTransactionalIntegrationSpec
 */
class IntTestWithDBCleanup {
	
	@BeforeClass
	public static void toRunBeforeClass(){
		substituteGetMvs()
		int numberOfObjects = TestDataUtil.getCountOfAllObjectsInDatabase()
		System.err.println("BeforeClass in integration-test: ${numberOfObjects} objects in db before clean-up")
		cleanUpDomains()
		numberOfObjects = TestDataUtil.getCountOfAllObjectsInDatabase()
		System.err.println("BeforeClass in integration-test: ${numberOfObjects} objects in db after clean-up")
	}
	
	@AfterClass
	public static void toRunAfterClass(){
		int numberOfObjects = TestDataUtil.getCountOfAllObjectsInDatabase()
		System.err.println("AfterClass in integration-test: ${numberOfObjects} objects in db before clean-up")
		cleanUpDomains()
		numberOfObjects = TestDataUtil.getCountOfAllObjectsInDatabase()
		System.err.println("AfterClass in integration-test: ${numberOfObjects} objects in db after clean-up")	
	}
	
	/**
	 * Substitutes function CsiAggregationDaoService.getMvs(...)
	 * The original behavior is retained. Only the rlike clause from the original query is replaced by a
	 * manual regex filtering (grep()) since the tested combination of H2, Hibernate and GORM did not
	 * reliably support the rlike clause.  
	 */
	public static void substituteGetMvs() {
		CsiAggregationDaoService.metaClass.getMvs = { Date fromDate, Date toDate, String rlikePattern, CsiAggregationInterval interval, AggregatorType aggregator ->
			def criteria = CsiAggregation.createCriteria()
			List<CsiAggregation> mvs = criteria.list {
				between("started", fromDate, toDate)
				eq("interval", interval)
				eq("aggregator", aggregator)
			}
			return mvs.grep{ it.tag ==~ rlikePattern };
		}
	}
	/**
	 * Deleting testdata.
	 */
	public static void cleanUpDomains() {
		System.err.println('clearDatabase()...');
		TestDataUtil.cleanUpDatabase();
		System.err.println('clearDatabase()... DONE');
	}
}