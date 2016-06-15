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

import de.iteratec.osm.api.dto.EventResultDto
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * Test-suite of {@link EventResultDto}.
 *
 * @author mze
 * @since IT-81
 */
class ResultTests {

	private static final String WPT_SERVER_BASE_URL = 'http://my-wpt-server.com/'
	private static final String TEST_ID = 'my-test-id'

	@Test
	public void testAPI_DECIMAL_FORMAT() {
		assertEquals('1,50', String.format("%.2f",1.5d.round(2)).replace('.',','))
		assertEquals('1,52', String.format("%.2f",1.52d.round(2)).replace('.',','))
		assertEquals('1,52', String.format("%.2f",1.521d.round(2)).replace('.',','))
		assertEquals('1,52', String.format("%.2f",1.524d.round(2)).replace('.',','))
		assertEquals('1,53', String.format("%.2f",1.525d.round(2)).replace('.',','))
		assertEquals('1,53', String.format("%.2f",1.529d.round(2)).replace('.',','))

		assertEquals('-1,50', String.format("%.2f",-1.5d.round(2)).replace('.',','))
		assertEquals('-1,52', String.format("%.2f",-1.52d.round(2)).replace('.',','))
		assertEquals('-1,52', String.format("%.2f",-1.521d.round(2)).replace('.',','))
		assertEquals('-1,52', String.format("%.2f",-1.524d.round(2)).replace('.',','))
		assertEquals('-1,53', String.format("%.2f",-1.525d.round(2)).replace('.',','))
		assertEquals('-1,53', String.format("%.2f",-1.529d.round(2)).replace('.',','))
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
		eventResult.csByWptDocCompleteInPercent = 1.5112d;

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
		EventResultDto out = new EventResultDto(eventResult);

		// Verify results
		assertEquals('1,51', out.csiValue);
		assertEquals('ADS', out.page);
		assertEquals('ADS for article 5', out.step);
		assertEquals('Firefox7', out.browser);
		assertEquals('agent01:IE', out.location);
		assertEquals("${WPT_SERVER_BASE_URL}result/${TEST_ID}".toString(), out.detailUrl)
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
		EventResultDto out = new EventResultDto(eventResult);

		// Verify results
		assertEquals('not calculated', out.csiValue);
		assertEquals('ADS', out.page);
		assertEquals('ADS for article 5', out.step);
		assertEquals('Firefox7', out.browser);
		assertEquals('agent01:IE', out.location);
		assertEquals("${WPT_SERVER_BASE_URL}result/${TEST_ID}".toString(), out.detailUrl)
	}

}
