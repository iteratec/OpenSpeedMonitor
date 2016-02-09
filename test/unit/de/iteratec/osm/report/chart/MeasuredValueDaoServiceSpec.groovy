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


package de.iteratec.osm.report.chart

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import grails.test.mixin.*
import grails.test.mixin.support.*

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.*
import de.iteratec.osm.util.ServiceMocker;

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(MeasuredValueDaoService)
@Mock([MeasuredValueUpdateEvent, CsiAggregation, MeasuredValueInterval, AggregatorType])
class MeasuredValueDaoServiceSpec {

	MeasuredValueDaoService serviceUnderTest
	MeasuredValueInterval weeklyInterval, dailyInterval, hourlyInterval
	AggregatorType pageAggregator, shopAggregator, eventAggregator
	ServiceMocker mockGenerator
	
	@Before
    public void setUp() {
		
		serviceUnderTest = service
		
		//test data common to all tests
		weeklyInterval = new MeasuredValueInterval(name: 'weekly', intervalInMinutes: MeasuredValueInterval.WEEKLY).save(failOnError: true)
		dailyInterval = new MeasuredValueInterval(name: 'daily', intervalInMinutes: MeasuredValueInterval.DAILY).save(failOnError: true)
		hourlyInterval = new MeasuredValueInterval(name: 'hourly', intervalInMinutes: MeasuredValueInterval.HOURLY).save(failOnError: true)
		pageAggregator = new AggregatorType(name: AggregatorType.PAGE, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
		shopAggregator = new AggregatorType(name: AggregatorType.SHOP, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
		eventAggregator = new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
    }

	@After
    public void tearDown() {
        // Tear down logic here
    }

	@Test
	public void testGetUpdateEvents(){
		
		//create test-specific data
		
		Date timestamp1 = new DateTime(2014,6,25,0,1,0).toDate()
		Date timestamp2 = new DateTime(2014,6,25,0,2,0).toDate()
		Date timestamp3 = new DateTime(2014,6,25,0,3,0).toDate()
		new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp1,
			measuredValueId: 1,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		).save(failOnError: true)
		new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp2,
			measuredValueId: 1,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		).save(failOnError: true)
		new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp3,
			measuredValueId: 1,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.OUTDATED
		).save(failOnError: true)
		
		//execute tests and assertions
		
		List<MeasuredValueUpdateEvent> updateEvents = MeasuredValueUpdateEvent.list()
		assertThat(updateEvents.size(), is(3))
		
		List<MeasuredValueUpdateEvent> updateEventsThatRequireRecalculation = updateEvents.findAll{it.updateCause.requiresRecalculation}
		assertThat(updateEventsThatRequireRecalculation.size(), is(1))
		
		List<MeasuredValueUpdateEvent> updateEventsThatDoesNotRequireRecalculation = updateEvents.findAll{!it.updateCause.requiresRecalculation}
		assertThat(updateEventsThatDoesNotRequireRecalculation.size(), is(2))
	}
	
	@Test
	public void testGetUpdateEventsForSpecificMeasuredValues(){
		
		//create test-specific data
		
		Date date_20140928 = new DateTime(2014,9,28,0,0,0,DateTimeZone.UTC).toDate()
		Date date_20140929 = new DateTime(2014,9,29,0,0,0,DateTimeZone.UTC).toDate()
		Date date_20140930 = new DateTime(2014,9,30,0,0,0,DateTimeZone.UTC).toDate()
		
		def mvWithFiveEvents = createAndSaveMeasuredValue(dailyInterval, pageAggregator, false, date_20140928)
		createUpdateEventForMv(mvWithFiveEvents, true, false)
		createUpdateEventForMv(mvWithFiveEvents, false, false)
		createUpdateEventForMv(mvWithFiveEvents, false, false)
		createUpdateEventForMv(mvWithFiveEvents, false, false)
		createUpdateEventForMv(mvWithFiveEvents, false, false)
		def mvWithoutEvents = createAndSaveMeasuredValue(dailyInterval, pageAggregator, false, date_20140929)
		def mvWithOneEvent = createAndSaveMeasuredValue(dailyInterval, pageAggregator, false, date_20140930)
		createUpdateEventForMv(mvWithOneEvent, false, false)
		
		// proof test-specific data
		
		List<MeasuredValueUpdateEvent> updateEvents = MeasuredValueUpdateEvent.list()
		assertThat(updateEvents.size(), is(6))
		assertThat(CsiAggregation.get(1).started, is(date_20140928))
		assertThat(CsiAggregation.get(2).started, is(date_20140929))
		assertThat(CsiAggregation.get(3).started, is(date_20140930))
		
		//execute tests and assertions
		
		assertThat(serviceUnderTest.getUpdateEvents([mvWithFiveEvents.ident()]).size(), is(5))
		assertThat(serviceUnderTest.getUpdateEvents([mvWithoutEvents.ident()]).size(), is(0))
		assertThat(serviceUnderTest.getUpdateEvents([mvWithOneEvent.ident()]).size(), is(1))
		assertThat(serviceUnderTest.getUpdateEvents([mvWithOneEvent.ident(), mvWithFiveEvents.ident(),mvWithoutEvents.ident()]).size(), is(6))
		
		List<CsiAggregation> emptyList = []
		assertThat(serviceUnderTest.getUpdateEvents(emptyList*.ident()).size(), is(0))
		
	}
	
	@Test
	public void testGetOpenHourlyEventMeasuredValuesWhosIntervalExpiredForAtLeast(){
		
		//create test-specific data
		Date date_20140928_0800 = new DateTime(2014,9,28,8,0,0,DateTimeZone.UTC).toDate()
		Date date_20140928_0900 = new DateTime(2014,9,28,9,0,0,DateTimeZone.UTC).toDate()
		Date date_20140928_1000 = new DateTime(2014,9,28,10,0,0,DateTimeZone.UTC).toDate()
		createAndSaveMeasuredValue(hourlyInterval, eventAggregator, false, date_20140928_0800)
		createAndSaveMeasuredValue(hourlyInterval, eventAggregator, false, date_20140928_0900)
		createAndSaveMeasuredValue(hourlyInterval, eventAggregator, false, date_20140928_1000)
		List<CsiAggregation> openAndExpired
		
		//test specific mocks, test executions and assertions
		
		//no ones are expired
		mockMeasuredValueUtilService(new DateTime(2014,9,28,0,0,0,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(0)
		assertThat(openAndExpired.size(), is(0))
		//some are expired and some not
		mockMeasuredValueUtilService(new DateTime(2014,9,28,9,15,0,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(45)
		assertThat(openAndExpired.size(), is(0))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(16)
		assertThat(openAndExpired.size(), is(0))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(15)
		assertThat(openAndExpired.size(), is(1))
		assertThat(openAndExpired[0].started, equalTo(date_20140928_0800))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(0)
		assertThat(openAndExpired.size(), is(1))
		assertThat(openAndExpired[0].started, equalTo(date_20140928_0800))
		//all expired
		mockMeasuredValueUtilService(new DateTime(2014,9,28,11,30,0,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(30)
		assertThat(openAndExpired.size(), is(3))
		
	}
	
	@Test
	public void testGetOpenDailyPageMeasuredValuesWhosIntervalExpiredForAtLeast(){
		
		//create test-specific data
		Date date_20140928 = new DateTime(2014,9,28,0,0,0,DateTimeZone.UTC).toDate()
		Date date_20140929 = new DateTime(2014,9,29,0,0,0,DateTimeZone.UTC).toDate()
		Date date_20140930 = new DateTime(2014,9,30,0,0,0,DateTimeZone.UTC).toDate()
		createAndSaveMeasuredValue(dailyInterval, pageAggregator, false, date_20140928)
		createAndSaveMeasuredValue(dailyInterval, pageAggregator, false, date_20140929)
		createAndSaveMeasuredValue(dailyInterval, pageAggregator, false, date_20140930)
		List<CsiAggregation> openAndExpired
		
		//test specific mocks, test executions and assertions
		
		//no ones are expired
		mockMeasuredValueUtilService(new DateTime(2014,9,26,0,0,0,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(0)
		assertThat(openAndExpired.size(), is(0))
		//some are expired and some not
		mockMeasuredValueUtilService(new DateTime(2014,9,29,5,1,0,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(500)
		assertThat(openAndExpired.size(), is(0))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(302)
		assertThat(openAndExpired.size(), is(0))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(301)
		assertThat(openAndExpired.size(), is(1))
		assertThat(openAndExpired[0].started, equalTo(date_20140928))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(0)
		assertThat(openAndExpired.size(), is(1))
		assertThat(openAndExpired[0].started, equalTo(date_20140928))
		//all expired
		mockMeasuredValueUtilService(new DateTime(2014,10,1,5,30,0,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(300)
		assertThat(openAndExpired.size(), is(3))
		
	}
	
	@Test
	public void testGetOpenWeeklyPageMeasuredValuesWhosIntervalExpiredForAtLeast(){
		
		//create test-specific data
		Date date_20140905 = new DateTime(2014,9,5,0,0,0,DateTimeZone.UTC).toDate()
		Date date_20140912 = new DateTime(2014,9,12,0,0,0,DateTimeZone.UTC).toDate()
		Date date_20140919 = new DateTime(2014,9,19,0,0,0,DateTimeZone.UTC).toDate()
		createAndSaveMeasuredValue(weeklyInterval, pageAggregator, false, date_20140905)
		createAndSaveMeasuredValue(weeklyInterval, pageAggregator, false, date_20140912)
		createAndSaveMeasuredValue(weeklyInterval, pageAggregator, false, date_20140919)
		List<CsiAggregation> openAndExpired
		
		//test specific mocks, test executions and assertions
		
		//no ones are expired
		mockMeasuredValueUtilService(new DateTime(2014,9,11,23,59,59,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(0)
		assertThat(openAndExpired.size(), is(0))
		//some are expired and some not
		mockMeasuredValueUtilService(new DateTime(2014,9,12,5,0,0,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(1000)
		assertThat(openAndExpired.size(), is(0))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(301)
		assertThat(openAndExpired.size(), is(0))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(300)
		assertThat(openAndExpired.size(), is(1))
		assertThat(openAndExpired[0].started, equalTo(date_20140905))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(100)
		assertThat(openAndExpired.size(), is(1))
		assertThat(openAndExpired[0].started, equalTo(date_20140905))
		//all expired
		mockMeasuredValueUtilService(new DateTime(2014,9,27,5,30,0,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(300)
		assertThat(openAndExpired.size(), is(3))
	}
	
	@Test
	public void testGetDailyOpenShopMeasuredValuesWhosIntervalExpiredForAtLeast(){
		
		//create test-specific data
		Date date_20140928 = new DateTime(2014,9,28,0,0,0,DateTimeZone.UTC).toDate()
		Date date_20140929 = new DateTime(2014,9,29,0,0,0,DateTimeZone.UTC).toDate()
		Date date_20140930 = new DateTime(2014,9,30,0,0,0,DateTimeZone.UTC).toDate()
		createAndSaveMeasuredValue(dailyInterval, shopAggregator, false, date_20140928)
		createAndSaveMeasuredValue(dailyInterval, shopAggregator, false, date_20140929)
		createAndSaveMeasuredValue(dailyInterval, shopAggregator, false, date_20140930)
		List<CsiAggregation> openAndExpired
		
		//test specific mocks, test executions and assertions
		
		//no ones are expired
		mockMeasuredValueUtilService(new DateTime(2014,9,26,0,0,0,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(0)
		assertThat(openAndExpired.size(), is(0))
		//some are expired and some not
		mockMeasuredValueUtilService(new DateTime(2014,9,29,5,1,0,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(500)
		assertThat(openAndExpired.size(), is(0))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(302)
		assertThat(openAndExpired.size(), is(0))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(301)
		assertThat(openAndExpired.size(), is(1))
		assertThat(openAndExpired[0].started, equalTo(date_20140928))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(0)
		assertThat(openAndExpired.size(), is(1))
		assertThat(openAndExpired[0].started, equalTo(date_20140928))
		//all expired
		mockMeasuredValueUtilService(new DateTime(2014,10,1,5,30,0,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(300)
		assertThat(openAndExpired.size(), is(3))
		
	}
	
	@Test
	public void testGetOpenWeeklyShopMeasuredValuesWhosIntervalExpiredForAtLeast(){
		
		//create test-specific data
		Date date_20140905 = new DateTime(2014,9,5,0,0,0,DateTimeZone.UTC).toDate()
		Date date_20140912 = new DateTime(2014,9,12,0,0,0,DateTimeZone.UTC).toDate()
		Date date_20140919 = new DateTime(2014,9,19,0,0,0,DateTimeZone.UTC).toDate()
		createAndSaveMeasuredValue(weeklyInterval, shopAggregator, false, date_20140905)
		createAndSaveMeasuredValue(weeklyInterval, shopAggregator, false, date_20140912)
		createAndSaveMeasuredValue(weeklyInterval, shopAggregator, false, date_20140919)
		List<CsiAggregation> openAndExpired
		
		//test specific mocks, test executions and assertions
		
		//no ones are expired
		mockMeasuredValueUtilService(new DateTime(2014,9,11,23,59,59,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(0)
		assertThat(openAndExpired.size(), is(0))
		//some are expired and some not
		mockMeasuredValueUtilService(new DateTime(2014,9,12,5,0,0,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(1000)
		assertThat(openAndExpired.size(), is(0))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(301)
		assertThat(openAndExpired.size(), is(0))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(300)
		assertThat(openAndExpired.size(), is(1))
		assertThat(openAndExpired[0].started, equalTo(date_20140905))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(100)
		assertThat(openAndExpired.size(), is(1))
		assertThat(openAndExpired[0].started, equalTo(date_20140905))
		//all expired
		mockMeasuredValueUtilService(new DateTime(2014,9,27,5,30,0,DateTimeZone.UTC))
		openAndExpired = serviceUnderTest.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(300)
		assertThat(openAndExpired.size(), is(3))
	}
	
	private CsiAggregation createAndSaveMeasuredValue(MeasuredValueInterval interval, AggregatorType aggregator, boolean closedAndCalculated, Date started){
		double valueNotOfInterestInTheseTests = 42d
		String resultIdsNotOfInterestInTheseTests = '4,2'
		String tagNotOfInterestInTheseTests = '1;2;3;4;5'
		return new CsiAggregation(
			started: started,
			interval: interval,
			aggregator: aggregator,
			tag: tagNotOfInterestInTheseTests,
			csByWptDocCompleteInPercent: valueNotOfInterestInTheseTests,
			underlyingEventResultsByWptDocComplete: resultIdsNotOfInterestInTheseTests,
			closedAndCalculated: closedAndCalculated
		).save(failOnError: true)
	}
	private void createUpdateEventForMv(CsiAggregation mv, boolean calculated, boolean withData){
		MeasuredValueUpdateEvent.UpdateCause cause = MeasuredValueUpdateEvent.UpdateCause.OUTDATED
		if (calculated) {
			cause = MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		}
		if (withData) {
			mv.csByWptDocCompleteInPercent = 0.5
			mv.save(failOnError: true)
		}
		new MeasuredValueUpdateEvent(
			dateOfUpdate: new Date(),
			measuredValueId: mv.ident(),
			updateCause: cause
		).save(failOnError: true)
	}
	
	private void mockMeasuredValueUtilService(DateTime utcNowToReturn){
		
		MeasuredValueUtilService mvuService = new MeasuredValueUtilService()
		mvuService.metaClass.getNowInUtc = {->return utcNowToReturn}
		serviceUnderTest.measuredValueUtilService = mvuService
	}
}
