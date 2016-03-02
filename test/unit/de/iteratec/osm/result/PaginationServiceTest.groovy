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

import grails.test.mixin.*

import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.junit.*
import org.mockito.Mockito

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup

import de.iteratec.osm.result.TabularResultPresentationController.ListResultsCommand
import de.iteratec.osm.result.TabularResultPresentationController.ListResultsForSpecificJobCommand
import de.iteratec.osm.report.ui.PaginationListing
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer


/**
 * Test-suite of {@link de.iteratec.osm.result.PaginationService}.
 * 
 * @author rhc
 */
@TestFor(PaginationService)
@Mock([Job, Location, WebPageTestServer, Browser, BrowserAlias, JobGroup, Script])
class PaginationServiceTest {
	
	PaginationService serviceUnderTest
	WebPageTestServer server
	Location ffAgent
	Job job
	String jobGroupName
	
	@Before
	void setUp() {
		serviceUnderTest = service
		
		serviceUnderTest.grailsLinkGenerator = Mockito.mock(LinkGenerator.class);
		
		server = new WebPageTestServer(
				baseUrl: 'http://server1.wpt.server.de',
				active: true,
				label: 'server 1 - wpt server',
				proxyIdentifier: 'server 1 - wpt server'
				).save(failOnError: true)

		String browserName="FF"
		Browser browserFF = Browser.findByName(browserName)?:new Browser(
				name: browserName,
				weight: 55)
				.addToBrowserAliases(alias: "FF")
				.addToBrowserAliases(alias: "FF7")
				.addToBrowserAliases(alias: "Firefox")
				.addToBrowserAliases(alias: "Firefox7")
				.save(failOnError: true)

		ffAgent = new Location(
				active: true,
				valid: 1,
				location: 'physNetLabAgent01-FF',
				label: 'physNetLabAgent01 - FF up to date',
				browser: browserFF,
				wptServer: server
				).save(failOnError: true)

		jobGroupName = 'CSI'
		JobGroup group = new JobGroup(
			name: jobGroupName).save(failOnError: true)
			
		JobGroup nonCSIgroup = new JobGroup(
			name: 'nonCSIgroup').save(failOnError: true)
				
		Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)
		//lastRun: 07.08.2014 - 16:00
		job = new Job(
            active: false,
            label: 'BV1 - Step 01',
            description: 'This is job 01...',
            lastRun: new Date(1407420000000L),
            location: ffAgent,
            frequencyInMin: 5,
            runs: 1,
            jobGroup: group,
            script: script,
            maxDownloadTimeInMinutes: 60,
            customConnectivityProfile: true,
            customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
            bandwidthDown: 6000,
            bandwidthUp: 512,
            latency: 50,
            packetLoss: 0
        ).save(failOnError: true)
	}

	@Test
	void testBuildListResultsPagination(){
		ListResultsCommand cmd = new ListResultsCommand()
		//08.08.2014
		cmd.setFrom(new Date(1407456000000L))
		cmd.setFromHour("4:00")
		//08.08.2014
		cmd.setTo(new Date(1407456000000L))
		cmd.setToHour("6:00")
		cmd.setSelectedFolder([2L])
		cmd.setSelectedPages([1L])
		cmd.setSelectedBrowsers([3L])
		cmd.setSelectedMeasuredEventIds([])
		cmd.setSelectedAllBrowsers(false)
		cmd.setSelectedAllLocations(false)
		cmd.setSelectedAllMeasuredEvents(false)
		
		// Simulate GrailsLinkGenerator
		LinkGenerator grailsLinkGeneratorMock = Mockito.mock(LinkGenerator.class);
		Mockito.when(grailsLinkGeneratorMock.link(Mockito.any(Map.class))).thenReturn("http://example.com/eventResult/listResults?selectedTimeFrameInterval=0&_setFromHour=&setFromHour=on&from=08.08.2014&fromHour=4%3A00&_setToHour=&setToHour=on&to=08.08.2014&toHour=6%3A00&selectedFolder=2&selectedPages=1&selectedBrowsers=3&_selectedAllBrowsers=&_selectedAllMeasuredEvents=&selectedAllMeasuredEvents=on&_selectedAllLocations=&selectedAllLocations=on&_action_listResults=Show");
		
		// Inject relevant services
		serviceUnderTest.grailsLinkGenerator = grailsLinkGeneratorMock;
		
		PaginationListing paginationListing = serviceUnderTest.buildListResultsPagination(cmd, 100)
		
		// put url-params into a map
		Map<String, List<String>> queryParams = getQueryParams(paginationListing.getRows().get(0).pageLink)
		
		// validate params
		assertEquals(2, paginationListing.getRows().size())
		assertEquals(1, paginationListing.getRows().get(0).pageNumber)
		
		assertEquals("08.08.2014", queryParams.get("from").get(0))
		assertEquals(cmd.getFromHour(), queryParams.get("fromHour").get(0))
		assertEquals("08.08.2014", queryParams.get("to").get(0))
		assertEquals(cmd.getToHour(), queryParams.get("toHour").get(0))
		assertEquals(cmd.getSelectedFolder().toString(), queryParams.get("selectedFolder").toString())
		assertEquals(cmd.getSelectedPages().toString(), queryParams.get("selectedPages").toString())
		assertEquals(cmd.getSelectedBrowsers().toString(), queryParams.get("selectedBrowsers").toString())
		assertEquals("[]" ,queryParams.get("_selectedAllBrowsers").toString())
		assertEquals("[]" ,queryParams.get("_selectedAllMeasuredEvents").toString())
		assertEquals("[]" ,queryParams.get("_selectedAllLocations").toString())
	}
	
	@Test
	void testBuildListResultsForJobPagination(){
		ListResultsForSpecificJobCommand cmd = new ListResultsForSpecificJobCommand()
		
		cmd.setJob(job)
		
		//07.08.2014 - 04:00
		cmd.setFrom(new Date(1407376800000L))
		cmd.setFromHour("04:00")
		//08.08.2014 - 04:00
		cmd.setTo(new Date(1407463200000L))
		cmd.setToHour("04:00")
		cmd.setMax(50)
		cmd.setOffset(0)

		// Simulate GrailsLinkGenerator
		LinkGenerator grailsLinkGeneratorMock = Mockito.mock(LinkGenerator.class);
		Mockito.when(grailsLinkGeneratorMock.link(Mockito.any(Map.class))).thenReturn("http://example.com/eventResult/showListResultsForJob?selectedTimeFrameInterval=0&job.id=1&from=07.08.2012&fromHour=04%3A00&to=08.08.2012&toHour=04%3A00");
		
		// Inject relevant services
		serviceUnderTest.grailsLinkGenerator = grailsLinkGeneratorMock;
		
		PaginationListing paginationListing = serviceUnderTest.buildListResultsForJobPagination(cmd, 100)
		
		// put url-params into a map
		Map<String, List<String>> queryParams = getQueryParams(paginationListing.getRows().get(0).pageLink)
		
		// validate params
		assertEquals(2, paginationListing.getRows().size())
		assertEquals(1, paginationListing.getRows().get(0).pageNumber)
		
		assertEquals("07.08.2012", queryParams.get("from").get(0))
		assertEquals(cmd.getFromHour(), queryParams.get("fromHour").get(0))
		assertEquals("08.08.2012", queryParams.get("to").get(0))
		assertEquals(cmd.getToHour(), queryParams.get("toHour").get(0))
		assertEquals(cmd.getJob().getId().toString(), queryParams.get("job.id").get(0).toString())
	}
	
	/**
	 * Gives params from given url {@link String}.
	 * @param url
	 * 		URL as {@link String}
	 * @return
	 */
	private static Map<String, List<String>> getQueryParams(String url) {
		try {
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			String[] urlParts = url.split("\\?");
			if (urlParts.length > 1) {
				String query = urlParts[1];
				for (String param : query.split("&")) {
					String[] pair = param.split("=");
					String key = URLDecoder.decode(pair[0], "UTF-8");
					String value = "";
					if (pair.length > 1) {
						value = URLDecoder.decode(pair[1], "UTF-8");
					}
	
					List<String> values = params.get(key);
					if (values == null) {
						values = new ArrayList<String>();
						params.put(key, values);
					}
					values.add(value);
				}
			}
	
			return params;
		} catch (UnsupportedEncodingException ex) {
			throw new AssertionError(ex);
		}
	}
}
