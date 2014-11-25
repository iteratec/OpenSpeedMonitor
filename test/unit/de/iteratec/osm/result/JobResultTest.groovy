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

import static org.junit.Assert.*
import org.junit.*

import java.sql.SQLException;
import grails.test.mixin.*
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.script.Script

@TestFor(JobResult)
@Mock([JobResult, Job, Page,EventResult, Location, JobGroup, Script, Browser, WebPageTestServer, MeasuredEvent])
class JobResultTest {

	@Test
	public void testFindBy_EventResult() {
		// Create test data
		JobResult shouldNotBeFound = new JobResult(testId: "TestJob2").save([failOnError: true, validate: false]);
		JobResult expectedResult = new JobResult(testId: "TestJob").save([failOnError: true, validate: false]);

		EventResult dummyData = new EventResult().save([failOnError: true, validate: false]);
		EventResult searchCondition = new EventResult().save([failOnError: true, validate: false]);

		// Create dependencies
		expectedResult.eventResults.add(dummyData)
		expectedResult.eventResults.add(searchCondition)
		expectedResult.save([failOnError: true, validate: false]);

		// Check database
		assertEquals(1, (JobResult.list().findAll{it.getEventResults().contains(searchCondition)}).size());

		// Run test:
		assertEquals(expectedResult.testId, new JobResultService().findJobResultByEventResult(searchCondition).testId);
	}

	@Test
	public void testFindBy_EventResultInconsistentDB() {
		// Create test data
		JobResult inconsistentElement = new JobResult(testId: "TestJob2").save([failOnError: true, validate: false]);
		JobResult consistentElement = new JobResult(testId: "TestJob").save([failOnError: true, validate: false]);

		EventResult invalidUse = new EventResult().save([failOnError: true, validate: false]);

		// Create dependencies
		consistentElement.eventResults.add(invalidUse)
		consistentElement.save([failOnError: true, validate: false]);

		// -- This is the inconsistency: Never two job-results should reference the same event-result!
		inconsistentElement.eventResults.add(invalidUse)
		inconsistentElement.save([failOnError: true, validate: false]);

		// Check if inconsistency really exists:
		assertEquals(2, (JobResult.list().findAll{it.getEventResults().contains(invalidUse)}).size());

		// Run test:
		try {
			new JobResultService().findJobResultByEventResult(invalidUse);
			fail("SQLException expected")
		} catch(SQLException expected) {}
	}

	@Test
	public void testTryToGetTestsDetailsURL_withURL_wptServerBaseurl_endsWithSlash() {
		JobResult out = new JobResult(wptServerBaseurl: 'https://wpt.example.com/', testId: "TestJob2");
		assertEquals('https://wpt.example.com/result/TestJob2', String.valueOf(out.tryToGetTestsDetailsURL()));
	}
	
	@Test
	public void testTryToGetTestsDetailsURL_withURL_wptServerBaseurl_doNotendsWithSlash() {
		JobResult out = new JobResult(wptServerBaseurl: 'https://wpt.example.com', testId: "TestJob3");
		assertEquals('https://wpt.example.com/result/TestJob3', String.valueOf(out.tryToGetTestsDetailsURL()));
	}
	
	@Test
	public void testTryToGetTestsDetailsURL_URL_not_available() {
		JobResult out = new JobResult(testId: "TestJob2");
		assertNull(out.tryToGetTestsDetailsURL());
	}
}
