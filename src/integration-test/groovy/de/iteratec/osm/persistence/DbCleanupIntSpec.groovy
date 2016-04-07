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

import de.iteratec.osm.csi.IntTestWithDBCleanup
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import org.junit.Test

import static org.junit.Assert.assertNull

/**
 *
 */
class DbCleanupIntSpec extends NonTransactionalIntegrationSpec{
	
	/*
	 * TODO: The following test-methods test whether db is cleaned between execution of single methods. They are green by itself but tests 
	 * in subsequent test-classes of the whole suite fail if they run (although the database is cleared after execution of the methods). 
	 * 
	 * 2014-07-26, nkuhn: Test in de.iteratec.isocsi.ShopCsiAggregationCalculationIntSpec fails if the following methods run and passes if they don't run.
	 * 	To reproduce call:
	 * 	 
	 */
	
//	@Test
//	void testStartingEachMethodWithEmptyDb_1(){
//		
//		Job.withTransaction {TransactionStatus status ->
//			
//			//assertion: db should be empty
//			assertThat(TestDataUtil.getCountOfAllObjectsInDatabase(), is(0))
//			//creation of some data
//			TestDataUtil.createAtLLeastOneObjectOfEachDomain()
//			assertThat(TestDataUtil.getCountOfAllObjectsInDatabase(), greaterThan(0))
//			
//			status.flush()
//		}
//	}
//	@Test
//	void testStartingEachMethodWithEmptyDb_2(){
//		
//		Job.withTransaction {TransactionStatus status ->
//			
//			//assertion: db should be empty
//			assertThat(TestDataUtil.getCountOfAllObjectsInDatabase(), is(0))
//			//creation of some data
//			TestDataUtil.createAtLLeastOneObjectOfEachDomain()
//			assertThat(TestDataUtil.getCountOfAllObjectsInDatabase(), greaterThan(0))
//			
//			status.flush()
//		}
//	}
//	@Test
//	void testStartingEachMethodWithEmptyDb_3(){
//		
//		Job.withTransaction {TransactionStatus status ->
//			
//			//assertion: db should be empty
//			assertThat(TestDataUtil.getCountOfAllObjectsInDatabase(), is(0))
//			//creation of some data
//			TestDataUtil.createAtLLeastOneObjectOfEachDomain()
//			assertThat(TestDataUtil.getCountOfAllObjectsInDatabase(), greaterThan(0))
//			
//			status.flush()
//		}
//	}

	void testCascadingDeletionOfBrowsers(){
		
		//creating test-specific data
		String browserName="my-browser"
		Browser mybrowser = new Browser(
			name: browserName,
			weight: 55)
			.addToBrowserAliases(alias: "my-browser-a")
			.addToBrowserAliases(alias: "my-browser-b")
			.addToBrowserAliases(alias: "my-browser-c")
			.addToBrowserAliases(alias: "my-browser-d")
			.save(failOnError: true)
			
		//test execution
		mybrowser.delete(failOnError: true)
		
		//assertions
		Browser.findByName(browserName) == null
		BrowserAlias.findByAlias("my-browser-a") == null
		BrowserAlias.findByAlias("my-browser-b") == null
		BrowserAlias.findByAlias("my-browser-c") == null
		BrowserAlias.findByAlias("my-browser-d") == null
	}

}
