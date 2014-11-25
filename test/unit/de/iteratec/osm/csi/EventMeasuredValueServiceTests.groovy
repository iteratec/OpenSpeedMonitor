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

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import grails.test.mixin.*
import grails.test.mixin.support.*

import java.util.regex.Pattern

import org.joda.time.DateTime
import org.junit.*

import de.iteratec.osm.report.chart.MeasuredValueDaoService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.EventResultService
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer

/**
 * Tests low level-functionality of {@link EventMeasuredValueService}.
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([Browser, BrowserAlias, JobGroup, Location, MeasuredEvent, Page, WebPageTestServer, MeasuredValue, MeasuredValueInterval, 
	AggregatorType, Location, EventResult, JobResult, Job, MeasuredValueUpdateEvent])
class EventMeasuredValueServiceTests {
	
	public static final String jobGroupName1 = 'jobGroupName1' 
	public static final String jobGroupName2 = 'jobGroupName2'
	public static final String pageName1 = 'pageName1'
	public static final String pageName2 = 'pageName2'
	public static final String eventName1 = 'eventName1'
	public static final String eventName2 = 'eventName2'
	public static final String browserName1 = 'browserName1'
	public static final String browserName2 = 'browserName2'
	public static final String locationName1 = 'locationName1'
	public static final String locationName2 = 'locationName2'
	public static final DateTime from = new DateTime(2013,8,5,6,0,0)
	public static final DateTime to = new DateTime(2013,8,5,15,0,0)
	public static final DateTime inInterval = new DateTime(2013,8,5,8,0,0)
	public static final DateTime outOfInterval = new DateTime(2013,8,5,19,0,0)
	
	EventMeasuredValueService serviceUnderTest
	MeasuredValueInterval hourly
	AggregatorType measuredEvent
	
    void setUp() {
		serviceUnderTest = service	
//		serviceUnderTest.performanceLoggingService = new PerformanceLoggingService()
		
		new JobGroup(name: jobGroupName1).save(validate: false) 
		new JobGroup(name: jobGroupName2).save(validate: false)
		new Page(name: pageName1).save(validate: false)
		new Page(name: pageName2).save(validate: false)
		new MeasuredEvent(name: eventName1).save(validate: false)
		new MeasuredEvent(name: eventName2).save(validate: false)
		new Browser(name: browserName1).save(validate: false)
		new Browser(name: browserName2).save(validate: false)
		new Location(location: locationName1).save(validate: false)
		new Location(location: locationName2).save(validate: false)
		hourly = new MeasuredValueInterval(intervalInMinutes: MeasuredValueInterval.HOURLY).save(validate: false)
		measuredEvent = new AggregatorType(name: AggregatorType.MEASURED_EVENT).save(validate: false)
    }

    void tearDown() {
        // Tear down logic here
    }
	
	//tests///////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
    void testValidationOfValidMvQueryParams() {
        MvQueryParams validParams = new MvQueryParams()
		validParams.browserIds.add(Browser.findByName(browserName1).id);
		validParams.jobGroupIds.add(JobGroup.findByName(jobGroupName1).id);
		validParams.locationIds.add(Location.findByLocation(locationName1).id);
		validParams.measuredEventIds.add(MeasuredEvent.findByName(eventName1).id);
		validParams.pageIds.add(Page.findByName(pageName1).id);
			
		assertTrue(serviceUnderTest.validateMvQueryParams(validParams))
    }
	
	@Test
	void testValidationOfMvQueryParamsDueToEmptyParamlist() {
		MvQueryParams invalidParams = new MvQueryParams()
		// Kept empty: invalidParams.browserIds
		invalidParams.jobGroupIds.add(JobGroup.findByName(jobGroupName1).id);
		invalidParams.locationIds.add(Location.findByLocation(locationName1).id);
		invalidParams.measuredEventIds.add(MeasuredEvent.findByName(eventName1).id);
		invalidParams.pageIds.add(Page.findByName(pageName1).id);
		
		assertTrue(serviceUnderTest.validateMvQueryParams(invalidParams))
	}
	/**
	 * Tests querying and calculation of hourly event-{@link MeasuredValue}s without existing {@link EventResult}s.
	 * MV's with status {@link Calculated.Not} should have status {@link Calculated.YesNoData} afterwards.
	 */
	@Test
	void testGetAllCalculatedHourlyMvs(){
		
		//creating testdata////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		deleteAllMeasuredValues()
		5.times{
			//calculated, with data
			createhourlyEventMvWithDefaultTag(true, true)
			//calculated, without data
			createhourlyEventMvWithDefaultTag(true, false)
		}
		
		MvQueryParams irrelevantQueryParamsCauseDbQueryIsMocked = new MvQueryParams()
		irrelevantQueryParamsCauseDbQueryIsMocked.browserIds.add(Browser.findByName(browserName1).id);
		irrelevantQueryParamsCauseDbQueryIsMocked.jobGroupIds.add(JobGroup.findByName(jobGroupName1).id);
		irrelevantQueryParamsCauseDbQueryIsMocked.locationIds.add(Location.findByLocation(locationName1).id);
		irrelevantQueryParamsCauseDbQueryIsMocked.measuredEventIds.add(MeasuredEvent.findByName(eventName1).id);
		irrelevantQueryParamsCauseDbQueryIsMocked.pageIds.add(Page.findByName(pageName1).id);
		
		//mock inner services////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		mockMeasuredValueDaoService()
		mockMeasuredValueTagService()
		mockCsiConfigCacheService()
		mockEventResultService()
		
		//run test////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		List<MeasuredValue> mvs = serviceUnderTest.getAllCalculatedHourlyMvs(irrelevantQueryParamsCauseDbQueryIsMocked, from, to)
		
		//assertions////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		assertThat(mvs.size(), is(10))
		//no additional update-events got written cause no calculation took place in EventMeasuredValueService
		assertThat(MeasuredValueUpdateEvent.list().size(), is(10))
		
		Integer calcNotMvsExpected = 0
		assertThat(mvs.findAll{ ! it.isCalculated() }.size(), is(calcNotMvsExpected))
		Integer calcYesNoDataMvsExpected = 5
		assertThat(mvs.findAll{ it.isCalculatedWithoutData() }.size(), is(calcYesNoDataMvsExpected))
		Integer calcYesMvsExpected = 5
		assertThat(mvs.findAll{ it.isCalculatedWithData() }.size(), is(calcYesMvsExpected))
	}
	
	//testdata///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates a {@linkplain MeasuredValue} as testdata.
	 * {@link MeasuredValueUpdateEvent}s are created respective given params of calculated and withData. 
	 * @return
	 */
	private void createhourlyEventMvWithDefaultTag(boolean calculated, boolean withData){
		//measured value
		MeasuredValue mv = new MeasuredValue(
			started:inInterval.toDate(),
			interval: hourly,
			aggregator: measuredEvent,
			tag: '1;1;1;1;1',
			resultIds: '').save(failOnError: true)
		//update events
		createUpdateEventsForMv(mv, calculated, withData)
	}
	private void createUpdateEventsForMv(MeasuredValue mv, boolean calculated, boolean withData){
		MeasuredValueUpdateEvent.UpdateCause cause = MeasuredValueUpdateEvent.UpdateCause.OUTDATED
		if (calculated) {
			cause = MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		}
		if (withData) {
			mv.value = 0.5
			mv.save(failOnError: true)
		}
		new MeasuredValueUpdateEvent(
			dateOfUpdate: new Date(),
			measuredValueId: mv.ident(),
			updateCause: cause
		).save(failOnError: true)
	}
	/**
	 * Deletes all {@link MeasuredValue}s in db.
	 * @return
	 */
	private deleteAllMeasuredValues(){
		MeasuredValue.list()*.delete(flush: true)
	}
	
	//mocks of inner services///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Mocks {@linkplain EventMeasuredValueService#measuredValueDaoService}.
	 * Method getMvs(Date fromDate, Date toDate, String rlikePattern, MeasuredValueInterval interval, AggregatorType aggregator) will return all {@link MeasuredValue}s from db.
	 * @param csiGroups
	 * @param pages
	 */
	private void mockMeasuredValueDaoService(){
		List<MeasuredValue> mvsToReturn = MeasuredValue.list()
		def measuredValueDaoService = mockFor(MeasuredValueDaoService, true)
		measuredValueDaoService.demand.getMvs(1..10000) {
			Date fromDate,
			Date toDate,
			String rlikePattern,
			MeasuredValueInterval interval,
			AggregatorType aggregator ->
			return mvsToReturn
		}
		serviceUnderTest.measuredValueDaoService = measuredValueDaoService.createMock()
	}
	/**
	 * Mocks {@linkplain EventMeasuredValueService#measuredValueTagService}
	 * @param csiGroups
	 * @param pages
	 */
	private void mockMeasuredValueTagService(){
		def measuredValueTagService = mockFor(MeasuredValueTagService, true)
		Pattern irrelevantPatternCauseDbCallIsMockedToo = Pattern.compile('1;1;1;1;1')
		measuredValueTagService.demand.getTagPatternForHourlyMeasuredValues(1..10000) {
			MvQueryParams mvQueryParams ->
			return irrelevantPatternCauseDbCallIsMockedToo
		}
		MeasuredEvent eventToReturn = new MeasuredEvent(name: 'testEvent', testedPage: new Page(name: 'testPage', weight: 0.5))
		measuredValueTagService.demand.findMeasuredEventOfHourlyEventTag(1..10000) {
			String hourlyEventMvTag ->
			return eventToReturn
		}
		JobGroup jobGroupToReturn = new JobGroup(name: 'csiGroup', groupType: JobGroupType.CSI_AGGREGATION)
		measuredValueTagService.demand.findJobGroupOfHourlyEventTag(1..10000) {
			String hourlyEventMvTag ->
			return jobGroupToReturn
		}
		Location locationToReturn = new Location(location: 'location')
		measuredValueTagService.demand.findLocationOfHourlyEventTag(1..10000) {
			String hourlyEventMvTag ->
			return locationToReturn
		}
		serviceUnderTest.measuredValueTagService = measuredValueTagService.createMock()
	}
	/**
	 * Mocks {@linkplain EventMeasuredValueService#eventResultService}
	 * @param csiGroups
	 * @param pages
	 */
	private void mockEventResultService(){
		def eventResultService = mockFor(EventResultService, true)
		List<EventResult> eventResultsToReturn = []
		eventResultService.demand.findByMeasuredEventBetweenDate(1..10000) {
			JobGroup jobGroup, 
			MeasuredEvent msStep, 
			Location location, 
			Date fromDate, 
			Date toDate ->
			return eventResultsToReturn
		}
		Boolean csiRelevanceToReturn = true
		eventResultService.demand.isCsiRelevant(1..10000) {
			EventResult toProof, Integer minDocTimeInMillisecs, Integer maxDocTimeInMillisecs ->
			return csiRelevanceToReturn
		}
		serviceUnderTest.eventResultService = eventResultService.createMock()
	}
	/**
	 * Mocks {@linkplain EventMeasuredValueService#csiConfigCacheService}
	 * @param csiGroups
	 * @param pages
	 */
	private void mockCsiConfigCacheService(){
		def osmConfigCacheService = mockFor(OsmConfigCacheService, true)
		Integer timeToExpect = 5
		osmConfigCacheService.demand.getCachedMinDocCompleteTimeInMillisecs(1..10000) {
			Double ageToleranceInHours ->
			return timeToExpect
		}
		osmConfigCacheService.demand.getCachedMaxDocCompleteTimeInMillisecs(1..10000) {
			Double ageToleranceInHours ->
			return timeToExpect
		}
		serviceUnderTest.osmConfigCacheService = osmConfigCacheService.createMock()
	}
}
