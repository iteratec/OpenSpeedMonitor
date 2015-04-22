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

package de.iteratec.osm.api.json

import de.iteratec.osm.api.json.Result;

import static org.junit.Assert.*

import org.junit.*

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.csi.Page
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer

/**
 * Test-suite of {@link de.iteratec.osm.api.json.Result}.
 *
 * @author mze
 * @since IT-81
 */
class ResultTests {

	private static final String WPT_SERVER_BASE_URL = 'http://my-wpt-server.com/'
	private static final String TEST_ID = 'my-test-id'

	@Test
	public void testAPI_DECIMAL_FORMAT() {
		assertEquals('1,50', Result.API_DECIMAL_FORMAT.format((double) 1.5d));
		assertEquals('1,52', Result.API_DECIMAL_FORMAT.format((double) 1.52d));
		assertEquals('1,52', Result.API_DECIMAL_FORMAT.format((double) 1.521d));
		assertEquals('1,52', Result.API_DECIMAL_FORMAT.format((double) 1.524d));
		assertEquals('1,53', Result.API_DECIMAL_FORMAT.format((double) 1.525d));
		assertEquals('1,53', Result.API_DECIMAL_FORMAT.format((double) 1.529d));

		assertEquals('-1,50', Result.API_DECIMAL_FORMAT.format((double) -1.5d));
		assertEquals('-1,52', Result.API_DECIMAL_FORMAT.format((double) -1.52d));
		assertEquals('-1,52', Result.API_DECIMAL_FORMAT.format((double) -1.521d));
		assertEquals('-1,52', Result.API_DECIMAL_FORMAT.format((double) -1.524d));
		assertEquals('-1,53', Result.API_DECIMAL_FORMAT.format((double) -1.525d));
		assertEquals('-1,53', Result.API_DECIMAL_FORMAT.format((double) -1.529d));
	}

	@Test
	public void testResultWithCustomerSatisfaction() {
		// Create some data
		Page testedPage = new Page();
		testedPage.name = 'ADS';

		MeasuredEvent event = new MeasuredEvent();
		event.testedPage = testedPage;
		event.name = 'ADS for article 5';

		EventResult eventResult = new EventResult() {
					@Override
					public Long getId() {
						return 1;
					}
				};
		eventResult.measuredEvent = event;
		eventResult.customerSatisfactionInPercent = 1.5112d;

		JobResult jobResult = new JobResult();
		eventResult.jobResult = jobResult;
		jobResult.locationLocation = 'agent01';
		jobResult.locationUniqueIdentifierForServer = 'agent01:IE';
		jobResult.locationBrowser = 'Firefox7';
		jobResult.testId = TEST_ID
		WebPageTestServer wptServer = new WebPageTestServer(baseUrl: WPT_SERVER_BASE_URL) 
		Job job =new Job(location: new Location(wptServer: wptServer))
		jobResult.job = job 

		// Run the test
		Result out = new Result(eventResult);

		// Verify results
		assertEquals('1,51', out.csiValue);
		assertEquals('ADS', out.page);
		assertEquals('ADS for article 5', out.step);
		assertEquals('Firefox7', out.browser);
		assertEquals('agent01:IE', out.location);
		assertEquals("${WPT_SERVER_BASE_URL}result/${TEST_ID}".toString(), out.detailUrl)
		assertEquals("${WPT_SERVER_BASE_URL}export.php?test=${TEST_ID}".toString(), out.httpArchiveUrl)
	}

	@Test
	public void testResultWithoutCustomerSatisfaction() {
		// Create some data
		Page testedPage = new Page();
		testedPage.name = 'ADS';

		MeasuredEvent event = new MeasuredEvent();
		event.testedPage = testedPage;
		event.name = 'ADS for article 5';

		EventResult eventResult = new EventResult() {
			@Override
			public Long getId() {
				return 1;
			}
		};
		eventResult.measuredEvent = event;

		JobResult jobResult = new JobResult();
		eventResult.jobResult = jobResult;
		jobResult.locationLocation = 'agent01';
		jobResult.locationUniqueIdentifierForServer = 'agent01:IE';
		jobResult.locationBrowser = 'Firefox7';
		jobResult.testId = TEST_ID
		WebPageTestServer wptServer = new WebPageTestServer(baseUrl: WPT_SERVER_BASE_URL)
		Job job =new Job(location: new Location(wptServer: wptServer))
		jobResult.job = job

		// Run the test
		Result out = new Result(eventResult);

		// Verify results
		assertEquals('not calculated', out.csiValue);
		assertEquals('ADS', out.page);
		assertEquals('ADS for article 5', out.step);
		assertEquals('Firefox7', out.browser);
		assertEquals('agent01:IE', out.location);
		assertEquals("${WPT_SERVER_BASE_URL}result/${TEST_ID}".toString(), out.detailUrl)
		assertEquals("${WPT_SERVER_BASE_URL}export.php?test=${TEST_ID}".toString(), out.httpArchiveUrl)
	}

}
