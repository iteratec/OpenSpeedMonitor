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

package de.iteratec.osm.report.external

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.report.external.provider.DefaultGraphiteSocketProvider
import de.iteratec.osm.report.external.provider.GraphiteSocketProvider
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.EventCsiAggregationService
import de.iteratec.osm.csi.PageCsiAggregationService
import de.iteratec.osm.csi.ShopCsiAggregationService
import de.iteratec.osm.measurement.schedule.*
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.CsiAggregationTagService
import de.iteratec.osm.result.MvQueryParams
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import groovy.mock.interceptor.MockFor
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@TestMixin(GrailsUnitTestMixin)
@TestFor(MetricReportingService)
@Mock([CsiAggregationInterval, OsmConfiguration, BatchActivity])
class QuartzControlledGrailsReportsTests {
	
	static final String jobGroupWithServersName = 'csiGroupWithServers'
	static final String jobGroupWithoutServersName = 'csiGroupWithoutServers'
	static final DateTime hourlyDateExpectedToBeSent = new DateTime(2013, 12, 4, 7, 0, 0, DateTimeZone.UTC)
	static final DateTime dailyDateExpectedToBeSent = new DateTime(2013, 12, 4, 0, 0, 0, DateTimeZone.UTC)
	static final DateTime weeklyDateExpectedToBeSent = new DateTime(2013, 11, 29, 0, 0, 0, DateTimeZone.UTC)
	static final Double firstHourlyValueToSend = 23.3d
	static final Double secondHourlyValueToSend = 123.3d
	static final Double firstDailyValueToSend = 12d
	static final Double secondDailyValueToSend = 14.2
	static final Double firstWeeklyValueToSend = 1223d
	static final Double secondWeeklyValueToSend = 13234.2
	static final String pathPrefix = 'wpt'
	static final String pageName = 'page'
	static final String eventName = 'event'
	static final String browserName = 'browser'
	static final String locationLocation = 'location'
	
	MetricReportingService serviceUnderTest
	public MockedGraphiteSocket graphiteSocketUsedInTests
	
	@Before
	void setUp() {
		serviceUnderTest = service
		serviceUnderTest.csiAggregationUtilService = new CsiAggregationUtilService()
		serviceUnderTest.configService = new ConfigService()
		serviceUnderTest.inMemoryConfigService = new InMemoryConfigService()
		serviceUnderTest.inMemoryConfigService.activateMeasurementsGenerally()
		serviceUnderTest.batchActivityService = new BatchActivityService()
		serviceUnderTest.batchActivityService.timer.cancel() //we don't need any updates for this test
		new OsmConfiguration().save(failOnError: true)
	}

	@After
	void tearDown() {
		// Tear down logic here
	}
	
	@Test
	void testWritingHourlyCsiCsiAggregationsToGraphite(){
		
		//testdata
		CsiAggregationInterval interval = new CsiAggregationInterval()
		interval.name = 'hourly'
		interval.intervalInMinutes = CsiAggregationInterval.HOURLY
		interval.save()
		AggregatorType event = new AggregatorType()
		event.setName(AggregatorType.MEASURED_EVENT)
		event.setMeasurandGroup(MeasurandGroup.NO_MEASURAND)
		
		CsiAggregation firstHmv = getCsiAggregation(interval, event, firstHourlyValueToSend, hourlyDateExpectedToBeSent, '1,2,3')
		CsiAggregation secondHmv = getCsiAggregation(interval, event, secondHourlyValueToSend, hourlyDateExpectedToBeSent, '4,5,6')
		
		//mocking
		mockJobGroupDaoService()
		mockGraphiteSocketProvider()
		mockEventCsiAggregationService([firstHmv, secondHmv])
		mockCsiAggregationTagService()
		mockPageCsiAggregationService([], CsiAggregationInterval.HOURLY)
		mockShopCsiAggregationService([], CsiAggregationInterval.HOURLY)
		
		//execute test
		DateTime cronjobStartsAt = hourlyDateExpectedToBeSent.plusMinutes(20)
		serviceUnderTest.reportEventCSIValuesOfLastHour(cronjobStartsAt)
		
		//assertions: just for JobGroups with GraphiteServers data is sent
		List<MockedGraphiteSocket.SentDate> sent = graphiteSocketUsedInTests.sentDates
		Assert.assertEquals(0, sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithoutServersName}.+"}.size())
		Assert.assertEquals(sent.size(), sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithServersName}.+"}.size())
		
		//assertions: two CsiAggregations were sent in total
		Integer sentInTotal = 2
		Assert.assertEquals(sentInTotal, sent.size())
		
		//assertions: all CsiAggregations were sent with correct Path
		Assert.assertEquals(sentInTotal, sent.findAll{it.path.stringValueOfPathName.equals(
			pathPrefix + '.' + jobGroupWithServersName + '.hourly.' + pageName + '.' + eventName + '.' + browserName + '.' + locationLocation + '.csi'
			)}.size())
		//assertions: all CsiAggregations were sent with correct timestamp
		Assert.assertEquals(sentInTotal, sent.findAll{it.timestamp.equals(hourlyDateExpectedToBeSent.toDate())}.size())
		//assertions: all CsiAggregations were sent with correct values
		Assert.assertEquals(1, sent.findAll{it.value.equals(firstHourlyValueToSend * 100)}.size())
		Assert.assertEquals(1, sent.findAll{it.value.equals(secondHourlyValueToSend * 100)}.size())
	}
	
	@Test
	void testWritingDailyPageCsiCsiAggregationsToGraphite(){
		
		//testdata
		CsiAggregationInterval interval = new CsiAggregationInterval()
		interval.name = 'daily'
		interval.intervalInMinutes = CsiAggregationInterval.DAILY
		interval.save()
		AggregatorType aggregator = new AggregatorType()
		aggregator.setName(AggregatorType.PAGE)
		aggregator.setMeasurandGroup(MeasurandGroup.NO_MEASURAND)
		
		CsiAggregation firstDpmv = getCsiAggregation(interval, aggregator, firstDailyValueToSend, dailyDateExpectedToBeSent, '1,2,3')
		CsiAggregation secondDpmv = getCsiAggregation(interval, aggregator, secondDailyValueToSend, dailyDateExpectedToBeSent, '4,5,6')
		
		//mocking
		mockJobGroupDaoService()
		mockGraphiteSocketProvider()
		mockEventCsiAggregationService([])
		mockCsiAggregationTagService()
		mockPageCsiAggregationService([firstDpmv, secondDpmv], CsiAggregationInterval.DAILY)
		mockShopCsiAggregationService([], CsiAggregationInterval.DAILY)
		
		//execute test
		DateTime cronjobStartsAt = dailyDateExpectedToBeSent.plusMinutes(20)
		serviceUnderTest.reportPageCSIValuesOfLastDay(cronjobStartsAt)
		
		//assertions: just for JobGroups with GraphiteServers data is sent
		List<MockedGraphiteSocket.SentDate> sent = graphiteSocketUsedInTests.sentDates
		Assert.assertEquals(0, sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithoutServersName}.+"}.size())
		Assert.assertEquals(sent.size(), sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithServersName}.+"}.size())
		
		//assertions: two CsiAggregations were sent in total
		Integer sentInTotal = 2
		Assert.assertEquals(sentInTotal, sent.size())
		
		//assertions: all CsiAggregations were sent with correct Path
		Assert.assertEquals(sentInTotal, sent.findAll{it.path.stringValueOfPathName.equals(
			pathPrefix + '.' + jobGroupWithServersName + '.daily.' + pageName + '.csi'
			)}.size())
		//assertions: all CsiAggregations were sent with correct timestamp
		Assert.assertEquals(sentInTotal, sent.findAll{
			Assert.assertEquals(dailyDateExpectedToBeSent.toDate(), it.timestamp)
			true;
		}.size())
		//assertions: all CsiAggregations were sent with correct values
		Assert.assertEquals(1, sent.findAll{it.value.equals(firstDailyValueToSend * 100)}.size())
		Assert.assertEquals(1, sent.findAll{it.value.equals(secondDailyValueToSend * 100)}.size())
	}
	
	@Test
	void testWritingDailyShopCsiCsiAggregationsToGraphite(){
		
		//testdata
		CsiAggregationInterval interval = new CsiAggregationInterval()
		interval.name = 'daily'
		interval.intervalInMinutes = CsiAggregationInterval.DAILY
		interval.save()
		AggregatorType aggregator = new AggregatorType()
		aggregator.setName(AggregatorType.SHOP)
		aggregator.setMeasurandGroup(MeasurandGroup.NO_MEASURAND)
		
		CsiAggregation firstDsmv = getCsiAggregation(interval, aggregator, firstDailyValueToSend, dailyDateExpectedToBeSent, '1,2,3')
		CsiAggregation secondDsmv = getCsiAggregation(interval, aggregator, secondDailyValueToSend, dailyDateExpectedToBeSent, '4,5,6')
		
		//mocking
		mockJobGroupDaoService()
		mockGraphiteSocketProvider()
		mockEventCsiAggregationService([])
		mockCsiAggregationTagService()
		mockPageCsiAggregationService([], CsiAggregationInterval.DAILY)
		mockShopCsiAggregationService([firstDsmv, secondDsmv], CsiAggregationInterval.DAILY)
		
		//execute test
		DateTime cronjobStartsAt = dailyDateExpectedToBeSent.plusMinutes(20)
		serviceUnderTest.reportShopCSIValuesOfLastDay(cronjobStartsAt)
		
		//assertions: just for JobGroups with GraphiteServers data is sent
		List<MockedGraphiteSocket.SentDate> sent = graphiteSocketUsedInTests.sentDates
		Assert.assertEquals(0, sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithoutServersName}.+"}.size())
		Assert.assertEquals(sent.size(), sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithServersName}.+"}.size())
		
		//assertions: two CsiAggregations were sent in total
		Integer sentInTotal = 2
		Assert.assertEquals(sentInTotal, sent.size())
		
		//assertions: all CsiAggregations were sent with correct Path
		Assert.assertEquals(sentInTotal, sent.findAll{it.path.stringValueOfPathName.equals(
			pathPrefix + '.' + jobGroupWithServersName + '.daily.csi'
			)}.size())
		//assertions: all CsiAggregations were sent with correct timestamp
		Assert.assertEquals(sentInTotal, sent.findAll{
			Assert.assertEquals(dailyDateExpectedToBeSent.toDate(), it.timestamp)
			true;
		}.size())
		//assertions: all CsiAggregations were sent with correct values
		Assert.assertEquals(1, sent.findAll{it.value.equals(firstDailyValueToSend * 100)}.size())
		Assert.assertEquals(1, sent.findAll{it.value.equals(secondDailyValueToSend * 100)}.size())
	}
	
	@Test
	void testWritingWeeklyPageCsiCsiAggregationsToGraphite(){
		
		//testdata
		CsiAggregationInterval interval = new CsiAggregationInterval()
		interval.name = 'weekly'
		interval.intervalInMinutes = CsiAggregationInterval.WEEKLY
		interval.save()
		AggregatorType aggregator = new AggregatorType()
		aggregator.setName(AggregatorType.PAGE)
		aggregator.setMeasurandGroup(MeasurandGroup.NO_MEASURAND)
		
		CsiAggregation firstWpmv = getCsiAggregation(interval, aggregator, firstWeeklyValueToSend, weeklyDateExpectedToBeSent, '1,2,3')
		CsiAggregation secondWpmv = getCsiAggregation(interval, aggregator, secondWeeklyValueToSend, weeklyDateExpectedToBeSent, '4,5,6')
		
		//mocking
		mockJobGroupDaoService()
		mockGraphiteSocketProvider()
		mockEventCsiAggregationService([])
		mockCsiAggregationTagService()
		mockPageCsiAggregationService([firstWpmv, secondWpmv], CsiAggregationInterval.WEEKLY)
		mockShopCsiAggregationService([], CsiAggregationInterval.WEEKLY)
		
		//execute test
		DateTime cronjobStartsAt = weeklyDateExpectedToBeSent.plusMinutes(20)
		serviceUnderTest.reportPageCSIValuesOfLastWeek(cronjobStartsAt)
		
		//assertions: just for JobGroups with GraphiteServers data is sent
		List<MockedGraphiteSocket.SentDate> sent = graphiteSocketUsedInTests.sentDates
		Assert.assertEquals(0, sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithoutServersName}.+"}.size())
		Assert.assertEquals(sent.size(), sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithServersName}.+"}.size())
		
		//assertions: two CsiAggregations were sent in total
		Integer sentInTotal = 2
		Assert.assertEquals(sentInTotal, sent.size())
		
		//assertions: all CsiAggregations were sent with correct Path
		Assert.assertEquals(sentInTotal, sent.findAll{it.path.stringValueOfPathName.equals(
			pathPrefix + '.' + jobGroupWithServersName + '.weekly.' + pageName + '.csi'
			)}.size())
		//assertions: all CsiAggregations were sent with correct timestamp
		Assert.assertEquals(sentInTotal, sent.findAll{
			Assert.assertEquals(weeklyDateExpectedToBeSent.toDate(), it.timestamp)
			true;
		}.size())
		//assertions: all CsiAggregations were sent with correct values
		Assert.assertEquals(1, sent.findAll{it.value.equals(firstWeeklyValueToSend * 100)}.size())
		Assert.assertEquals(1, sent.findAll{it.value.equals(secondWeeklyValueToSend * 100)}.size())
	}
	
	@Test
	void testWritingWeeklyShopCsiCsiAggregationsToGraphite(){
		
		//testdata
		CsiAggregationInterval interval = new CsiAggregationInterval()
		interval.name = 'weekly'
		interval.intervalInMinutes = CsiAggregationInterval.WEEKLY
		interval.save()
		AggregatorType aggregator = new AggregatorType()
		aggregator.setName(AggregatorType.SHOP)
		aggregator.setMeasurandGroup(MeasurandGroup.NO_MEASURAND)
		
		CsiAggregation firstWsmv = getCsiAggregation(interval, aggregator, firstWeeklyValueToSend, weeklyDateExpectedToBeSent, '1,2,3')
		CsiAggregation secondWsmv = getCsiAggregation(interval, aggregator, secondWeeklyValueToSend, weeklyDateExpectedToBeSent, '4,5,6')
		
		//mocking
		mockJobGroupDaoService()
		mockGraphiteSocketProvider()
		mockEventCsiAggregationService([])
		mockCsiAggregationTagService()
		mockPageCsiAggregationService([], CsiAggregationInterval.WEEKLY)
		mockShopCsiAggregationService([firstWsmv, secondWsmv], CsiAggregationInterval.WEEKLY)
		
		//execute test
		DateTime cronjobStartsAt = weeklyDateExpectedToBeSent.plusMinutes(20)
		serviceUnderTest.reportShopCSIValuesOfLastWeek(cronjobStartsAt)
		
		//assertions: just for JobGroups with GraphiteServers data is sent

			List<MockedGraphiteSocket.SentDate> sent = graphiteSocketUsedInTests.sentDates
		Assert.assertEquals(0, sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithoutServersName}.+"}.size())
		Assert.assertEquals(sent.size(), sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithServersName}.+"}.size())
		
		//assertions: two CsiAggregations were sent in total
		Integer sentInTotal = 2
		Assert.assertEquals(sentInTotal, sent.size())
		
		//assertions: all CsiAggregations were sent with correct Path
		Assert.assertEquals(sentInTotal, sent.findAll{it.path.stringValueOfPathName.equals(
			pathPrefix + '.' + jobGroupWithServersName + '.weekly.csi'
			)}.size())
		//assertions: all CsiAggregations were sent with correct timestamp
		Assert.assertEquals(sentInTotal, sent.findAll{
			Assert.assertEquals(weeklyDateExpectedToBeSent.toDate(), it.timestamp)
			true;
		}.size())
		//assertions: all CsiAggregations were sent with correct values
		Assert.assertEquals(1, sent.findAll{it.value.equals(firstWeeklyValueToSend * 100)}.size())
		Assert.assertEquals(1, sent.findAll{it.value.equals(secondWeeklyValueToSend * 100)}.size())
	}
	
	//mocks of inner services///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Mocks {@linkplain de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService#findCSIGroups}
	 */
	private void mockJobGroupDaoService(){
		def jobGroupDaoService = new MockFor(DefaultJobGroupDaoService, true)
		jobGroupDaoService.demand.findCSIGroups() {
			JobGroup jobGroupWithGraphiteServers = new JobGroup() {
				public Long getId()
				{
					return 1;
				}
			}
			jobGroupWithGraphiteServers.setName(jobGroupWithServersName)
			jobGroupWithGraphiteServers.setGraphiteServers(getGraphiteServers())
			
			JobGroup jobGroupWithoutGraphiteServers = new JobGroup() {
				public Long getId()
				{
					return 2;
				}
			}
			jobGroupWithoutGraphiteServers.setName(jobGroupWithoutServersName)
			jobGroupWithoutGraphiteServers.setGraphiteServers([])
			
			return [
					jobGroupWithGraphiteServers,
					jobGroupWithoutGraphiteServers
				] as Set
		}
		serviceUnderTest.jobGroupDaoService = jobGroupDaoService.proxyInstance()
	}
	/**
	 * Mocks {@linkplain GraphiteSocketProvider#getSocket}.
	 * Field {@link #graphiteSocketUsedInTests} is returned and can be used to proof sent dates.
	 */
	private void mockGraphiteSocketProvider(){
		def graphiteSocketProvider = new MockFor(DefaultGraphiteSocketProvider, true)
		graphiteSocketProvider.demand.getSocket() {GraphiteServer server ->
			graphiteSocketUsedInTests = new MockedGraphiteSocket()
			return graphiteSocketUsedInTests
		}
		serviceUnderTest.graphiteSocketProvider = graphiteSocketProvider.proxyInstance()
	}
	/**
//	 * Mocks {@linkplain EventCsiAggregationService#getOrCalculateHourylCsiAggregations}.
	 */
	private void mockEventCsiAggregationService(Collection<CsiAggregation> toReturnFromGetHourlyCsiAggregations){
		def eventCsiAggregationService = new EventCsiAggregationService()
		
		// FIXME mze-2013-12-10: Hier muss unterschieden werden, welche Op man mocken möchte!
		// Das mocking von Grails ist echt nicht gut lesbar :-(
		eventCsiAggregationService.metaClass.getHourlyCsiAggregations = {
			Date fromDate, Date toDate, MvQueryParams mvQueryParams ->
			
			return toReturnFromGetHourlyCsiAggregations
			
		}
		serviceUnderTest.eventCsiAggregationService = eventCsiAggregationService
	}
	/**
	 * Mocks methods of {@linkplain de.iteratec.osm.result.CsiAggregationTagService}.
	 */
	private void mockCsiAggregationTagService(){
		def csiAggregationTagService = new CsiAggregationTagService()
		csiAggregationTagService.metaClass.findPageOfHourlyEventTag = {String hourlyEventMvTag ->
			Page page = new Page()
			page.setName(pageName)
			return page
		}
		csiAggregationTagService.metaClass.findMeasuredEventOfHourlyEventTag = {String hourlyEventMvTag ->
			MeasuredEvent event = new MeasuredEvent()
			event.setName(eventName)
			return event
		}
		csiAggregationTagService.metaClass.findBrowserOfHourlyEventTag =  {String hourlyEventMvTag ->
			Browser browser = new Browser()
			browser.setName(browserName)
			return browser
		}
		csiAggregationTagService.metaClass.findLocationOfHourlyEventTag = {String hourlyEventMvTag ->
			Location location = new Location()
			location.setLocation(locationLocation)
			return location
		}
		csiAggregationTagService.metaClass.findPageByPageTag = {String weeklyPageMvTag ->
			Page page = new Page()
			page.setName(pageName)
			return page
		}
		serviceUnderTest.csiAggregationTagService = csiAggregationTagService
	}
	/**
	 * Mocks {@linkplain PageCsiAggregationService#getOrCalculatePageCsiAggregations}.
	 */
	private void mockPageCsiAggregationService(Collection<CsiAggregation> toReturnOnDemandForGetOrCalculateCsiAggregations, Integer expectedIntervalInMinutes){
		def pageCsiAggregationService = new PageCsiAggregationService ()
		pageCsiAggregationService.metaClass.getOrCalculatePageCsiAggregations = {
			Date fromDate, Date toDate, CsiAggregationInterval interval, List<JobGroup> csiGroups ->
			if ( ! interval.intervalInMinutes.equals(expectedIntervalInMinutes)) {
				return []
			}
			return toReturnOnDemandForGetOrCalculateCsiAggregations
			
		}
		serviceUnderTest.pageCsiAggregationService = pageCsiAggregationService
	}
	/**
	 * Mocks {@linkplain ShopCsiAggregationService#getOrCalculateShopCsiAggregations}.
	 */
	private void mockShopCsiAggregationService(Collection<CsiAggregation> toReturnOnDemandForGetOrCalculateCsiAggregations, Integer expectedIntervalInMinutes){
		def shopCsiAggregationService = new ShopCsiAggregationService()
		shopCsiAggregationService.metaClass.getOrCalculateShopCsiAggregations = {
			Date fromDate, Date toDate, CsiAggregationInterval interval, List<JobGroup> csiGroups ->
			if ( ! interval.intervalInMinutes.equals(expectedIntervalInMinutes)) {
				return []
			}
			return toReturnOnDemandForGetOrCalculateCsiAggregations
		}
		serviceUnderTest.shopCsiAggregationService = shopCsiAggregationService
	}
	
	//test data common to all tests///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static Collection<GraphiteServer> getGraphiteServers(){
		AggregatorType event = new AggregatorType()
		event.setName(AggregatorType.MEASURED_EVENT)
		event.setMeasurandGroup(MeasurandGroup.NO_MEASURAND)
		AggregatorType page = new AggregatorType()
		page.setName(AggregatorType.PAGE)
		page.setMeasurandGroup(MeasurandGroup.NO_MEASURAND)
		AggregatorType shop = new AggregatorType()
		shop.setName(AggregatorType.SHOP)
		shop.setMeasurandGroup(MeasurandGroup.NO_MEASURAND)
		
		GraphitePath pathEvent = new GraphitePath()
		pathEvent.setPrefix(pathPrefix)
		pathEvent.setMeasurand(event)
		GraphitePath pathPage = new GraphitePath()
		pathPage.setPrefix(pathPrefix)
		pathPage.setMeasurand(page)
		GraphitePath pathShop = new GraphitePath()
		pathShop.setPrefix(pathPrefix)
		pathShop.setMeasurand(shop)
		
		GraphiteServer serverWithPaths = new GraphiteServer()
		serverWithPaths.setServerAdress('127.0.0.1')
		serverWithPaths.setPort(2003)
		serverWithPaths.setGraphitePaths([pathEvent, pathPage, pathShop])
		
		return [
			serverWithPaths,
			]
	}
	public static getCsiAggregation(CsiAggregationInterval interval, AggregatorType aggregator, Double value, DateTime valueForStated, String resultIds){
		CsiAggregation hmv = new CsiAggregation()
		hmv.started = valueForStated.toDate()
		hmv.interval =  interval
		hmv.aggregator = aggregator
		hmv.tag = ''
		hmv.csByWptDocCompleteInPercent = value
		hmv.underlyingEventResultsByWptDocComplete = resultIds
		return hmv
	}
	
	class MockedGraphiteSocket implements GraphiteSocket {
		class SentDate{
			GraphitePathName path
			Double value
			Date timestamp
		}
		List<SentDate> sentDates = []
		
		@Override
		void sendDate(GraphitePathName path, double value, Date timestamp) throws NullPointerException, GraphiteComunicationFailureException{
			sentDates.add(new SentDate(path: path, value: value, timestamp: timestamp))
		}
	}
}
