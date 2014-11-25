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

import de.iteratec.osm.report.external.GraphiteComunicationFailureException
import de.iteratec.osm.report.external.GraphitePathName
import de.iteratec.osm.report.external.GraphiteSocket
import de.iteratec.osm.report.external.provider.GraphiteSocketProvider
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.EventMeasuredValueService
import de.iteratec.osm.csi.PageMeasuredValueService
import de.iteratec.osm.csi.ShopMeasuredValueService
import de.iteratec.osm.measurement.schedule.*
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.report.external.GraphitePath
import de.iteratec.osm.report.external.GraphiteServer
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.result.MvQueryParams
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@TestMixin(GrailsUnitTestMixin)
@TestFor(MetricReportingService)
@Mock([MeasuredValueInterval, OsmConfiguration])
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
		serviceUnderTest.measuredValueUtilService = new MeasuredValueUtilService() 
		serviceUnderTest.configService = new ConfigService()
		new OsmConfiguration(measurementsGenerallyEnabled: true).save(failOnError: true)
	}

	@After
	void tearDown() {
		// Tear down logic here
	}
	
	@Test
	void testWritingHourlyCsiMeasuredValuesToGraphite(){
		
		//testdata
		MeasuredValueInterval interval = new MeasuredValueInterval()
		interval.name = 'hourly'
		interval.intervalInMinutes = MeasuredValueInterval.HOURLY
		interval.save()
		AggregatorType event = new AggregatorType()
		event.setName(AggregatorType.MEASURED_EVENT)
		event.setMeasurandGroup(MeasurandGroup.NO_MEASURAND)
		
		MeasuredValue firstHmv = getMeasuredValue(interval, event, firstHourlyValueToSend, hourlyDateExpectedToBeSent, '1,2,3')
		MeasuredValue secondHmv = getMeasuredValue(interval, event, secondHourlyValueToSend, hourlyDateExpectedToBeSent, '4,5,6')
		
		//mocking
		mockJobGroupDaoService()
		mockGraphiteSocketProvider()
		mockEventMeasuredValueService([firstHmv, secondHmv])
		mockMeasuredValueTagService()
		mockPageMeasuredValueService([], MeasuredValueInterval.HOURLY)
		mockShopMeasuredValueService([], MeasuredValueInterval.HOURLY)
		
		//execute test
		DateTime cronjobStartsAt = hourlyDateExpectedToBeSent.plusMinutes(20)
		serviceUnderTest.reportEventCSIValuesOfLastHour(cronjobStartsAt)
		
		//assertions: just for JobGroups with GraphiteServers data is sent
		List<MockedGraphiteSocket.SentDate> sent = graphiteSocketUsedInTests.sentDates
		Assert.assertEquals(0, sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithoutServersName}.+"}.size())
		Assert.assertEquals(sent.size(), sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithServersName}.+"}.size())
		
		//assertions: two MeasuredValues were sent in total
		Integer sentInTotal = 2
		Assert.assertEquals(sentInTotal, sent.size())
		
		//assertions: all MeasuredValues were sent with correct Path
		Assert.assertEquals(sentInTotal, sent.findAll{it.path.stringValueOfPathName.equals(
			pathPrefix + '.' + jobGroupWithServersName + '.hourly.' + pageName + '.' + eventName + '.' + browserName + '.' + locationLocation + '.csi'
			)}.size())
		//assertions: all MeasuredValues were sent with correct timestamp
		Assert.assertEquals(sentInTotal, sent.findAll{it.timestamp.equals(hourlyDateExpectedToBeSent.toDate())}.size())
		//assertions: all MeasuredValues were sent with correct values
		Assert.assertEquals(1, sent.findAll{it.value.equals(firstHourlyValueToSend * 100)}.size())
		Assert.assertEquals(1, sent.findAll{it.value.equals(secondHourlyValueToSend * 100)}.size())
	}
	
	@Test
	void testWritingDailyPageCsiMeasuredValuesToGraphite(){
		
		//testdata
		MeasuredValueInterval interval = new MeasuredValueInterval()
		interval.name = 'daily'
		interval.intervalInMinutes = MeasuredValueInterval.DAILY
		interval.save()
		AggregatorType aggregator = new AggregatorType()
		aggregator.setName(AggregatorType.PAGE)
		aggregator.setMeasurandGroup(MeasurandGroup.NO_MEASURAND)
		
		MeasuredValue firstDpmv = getMeasuredValue(interval, aggregator, firstDailyValueToSend, dailyDateExpectedToBeSent, '1,2,3')
		MeasuredValue secondDpmv = getMeasuredValue(interval, aggregator, secondDailyValueToSend, dailyDateExpectedToBeSent, '4,5,6')
		
		//mocking
		mockJobGroupDaoService()
		mockGraphiteSocketProvider()
		mockEventMeasuredValueService([])
		mockMeasuredValueTagService()
		mockPageMeasuredValueService([firstDpmv, secondDpmv], MeasuredValueInterval.DAILY)
		mockShopMeasuredValueService([], MeasuredValueInterval.DAILY)
		
		//execute test
		DateTime cronjobStartsAt = dailyDateExpectedToBeSent.plusMinutes(20)
		serviceUnderTest.reportPageCSIValuesOfLastDay(cronjobStartsAt)
		
		//assertions: just for JobGroups with GraphiteServers data is sent
		List<MockedGraphiteSocket.SentDate> sent = graphiteSocketUsedInTests.sentDates
		Assert.assertEquals(0, sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithoutServersName}.+"}.size())
		Assert.assertEquals(sent.size(), sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithServersName}.+"}.size())
		
		//assertions: two MeasuredValues were sent in total
		Integer sentInTotal = 2
		Assert.assertEquals(sentInTotal, sent.size())
		
		//assertions: all MeasuredValues were sent with correct Path
		Assert.assertEquals(sentInTotal, sent.findAll{it.path.stringValueOfPathName.equals(
			pathPrefix + '.' + jobGroupWithServersName + '.daily.' + pageName + '.csi'
			)}.size())
		//assertions: all MeasuredValues were sent with correct timestamp
		Assert.assertEquals(sentInTotal, sent.findAll{
			Assert.assertEquals(dailyDateExpectedToBeSent.toDate(), it.timestamp)
			true;
		}.size())
		//assertions: all MeasuredValues were sent with correct values
		Assert.assertEquals(1, sent.findAll{it.value.equals(firstDailyValueToSend * 100)}.size())
		Assert.assertEquals(1, sent.findAll{it.value.equals(secondDailyValueToSend * 100)}.size())
	}
	
	@Test
	void testWritingDailyShopCsiMeasuredValuesToGraphite(){
		
		//testdata
		MeasuredValueInterval interval = new MeasuredValueInterval()
		interval.name = 'daily'
		interval.intervalInMinutes = MeasuredValueInterval.DAILY
		interval.save()
		AggregatorType aggregator = new AggregatorType()
		aggregator.setName(AggregatorType.SHOP)
		aggregator.setMeasurandGroup(MeasurandGroup.NO_MEASURAND)
		
		MeasuredValue firstDsmv = getMeasuredValue(interval, aggregator, firstDailyValueToSend, dailyDateExpectedToBeSent, '1,2,3')
		MeasuredValue secondDsmv = getMeasuredValue(interval, aggregator, secondDailyValueToSend, dailyDateExpectedToBeSent, '4,5,6')
		
		//mocking
		mockJobGroupDaoService()
		mockGraphiteSocketProvider()
		mockEventMeasuredValueService([])
		mockMeasuredValueTagService()
		mockPageMeasuredValueService([], MeasuredValueInterval.DAILY)
		mockShopMeasuredValueService([firstDsmv, secondDsmv], MeasuredValueInterval.DAILY)
		
		//execute test
		DateTime cronjobStartsAt = dailyDateExpectedToBeSent.plusMinutes(20)
		serviceUnderTest.reportShopCSIValuesOfLastDay(cronjobStartsAt)
		
		//assertions: just for JobGroups with GraphiteServers data is sent
		List<MockedGraphiteSocket.SentDate> sent = graphiteSocketUsedInTests.sentDates
		Assert.assertEquals(0, sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithoutServersName}.+"}.size())
		Assert.assertEquals(sent.size(), sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithServersName}.+"}.size())
		
		//assertions: two MeasuredValues were sent in total
		Integer sentInTotal = 2
		Assert.assertEquals(sentInTotal, sent.size())
		
		//assertions: all MeasuredValues were sent with correct Path
		Assert.assertEquals(sentInTotal, sent.findAll{it.path.stringValueOfPathName.equals(
			pathPrefix + '.' + jobGroupWithServersName + '.daily.csi'
			)}.size())
		//assertions: all MeasuredValues were sent with correct timestamp
		Assert.assertEquals(sentInTotal, sent.findAll{
			Assert.assertEquals(dailyDateExpectedToBeSent.toDate(), it.timestamp)
			true;
		}.size())
		//assertions: all MeasuredValues were sent with correct values
		Assert.assertEquals(1, sent.findAll{it.value.equals(firstDailyValueToSend * 100)}.size())
		Assert.assertEquals(1, sent.findAll{it.value.equals(secondDailyValueToSend * 100)}.size())
	}
	
	@Test
	void testWritingWeeklyPageCsiMeasuredValuesToGraphite(){
		
		//testdata
		MeasuredValueInterval interval = new MeasuredValueInterval()
		interval.name = 'weekly'
		interval.intervalInMinutes = MeasuredValueInterval.WEEKLY
		interval.save()
		AggregatorType aggregator = new AggregatorType()
		aggregator.setName(AggregatorType.PAGE)
		aggregator.setMeasurandGroup(MeasurandGroup.NO_MEASURAND)
		
		MeasuredValue firstWpmv = getMeasuredValue(interval, aggregator, firstWeeklyValueToSend, weeklyDateExpectedToBeSent, '1,2,3')
		MeasuredValue secondWpmv = getMeasuredValue(interval, aggregator, secondWeeklyValueToSend, weeklyDateExpectedToBeSent, '4,5,6')
		
		//mocking
		mockJobGroupDaoService()
		mockGraphiteSocketProvider()
		mockEventMeasuredValueService([])
		mockMeasuredValueTagService()
		mockPageMeasuredValueService([firstWpmv, secondWpmv], MeasuredValueInterval.WEEKLY)
		mockShopMeasuredValueService([], MeasuredValueInterval.WEEKLY)
		
		//execute test
		DateTime cronjobStartsAt = weeklyDateExpectedToBeSent.plusMinutes(20)
		serviceUnderTest.reportPageCSIValuesOfLastWeek(cronjobStartsAt)
		
		//assertions: just for JobGroups with GraphiteServers data is sent
		List<MockedGraphiteSocket.SentDate> sent = graphiteSocketUsedInTests.sentDates
		Assert.assertEquals(0, sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithoutServersName}.+"}.size())
		Assert.assertEquals(sent.size(), sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithServersName}.+"}.size())
		
		//assertions: two MeasuredValues were sent in total
		Integer sentInTotal = 2
		Assert.assertEquals(sentInTotal, sent.size())
		
		//assertions: all MeasuredValues were sent with correct Path
		Assert.assertEquals(sentInTotal, sent.findAll{it.path.stringValueOfPathName.equals(
			pathPrefix + '.' + jobGroupWithServersName + '.weekly.' + pageName + '.csi'
			)}.size())
		//assertions: all MeasuredValues were sent with correct timestamp
		Assert.assertEquals(sentInTotal, sent.findAll{
			Assert.assertEquals(weeklyDateExpectedToBeSent.toDate(), it.timestamp)
			true;
		}.size())
		//assertions: all MeasuredValues were sent with correct values
		Assert.assertEquals(1, sent.findAll{it.value.equals(firstWeeklyValueToSend * 100)}.size())
		Assert.assertEquals(1, sent.findAll{it.value.equals(secondWeeklyValueToSend * 100)}.size())
	}
	
	@Test
	void testWritingWeeklyShopCsiMeasuredValuesToGraphite(){
		
		//testdata
		MeasuredValueInterval interval = new MeasuredValueInterval()
		interval.name = 'weekly'
		interval.intervalInMinutes = MeasuredValueInterval.WEEKLY
		interval.save()
		AggregatorType aggregator = new AggregatorType()
		aggregator.setName(AggregatorType.SHOP)
		aggregator.setMeasurandGroup(MeasurandGroup.NO_MEASURAND)
		
		MeasuredValue firstWsmv = getMeasuredValue(interval, aggregator, firstWeeklyValueToSend, weeklyDateExpectedToBeSent, '1,2,3')
		MeasuredValue secondWsmv = getMeasuredValue(interval, aggregator, secondWeeklyValueToSend, weeklyDateExpectedToBeSent, '4,5,6')
		
		//mocking
		mockJobGroupDaoService()
		mockGraphiteSocketProvider()
		mockEventMeasuredValueService([])
		mockMeasuredValueTagService()
		mockPageMeasuredValueService([], MeasuredValueInterval.WEEKLY)
		mockShopMeasuredValueService([firstWsmv, secondWsmv], MeasuredValueInterval.WEEKLY)
		
		//execute test
		DateTime cronjobStartsAt = weeklyDateExpectedToBeSent.plusMinutes(20)
		serviceUnderTest.reportShopCSIValuesOfLastWeek(cronjobStartsAt)
		
		//assertions: just for JobGroups with GraphiteServers data is sent

			List<MockedGraphiteSocket.SentDate> sent = graphiteSocketUsedInTests.sentDates
		Assert.assertEquals(0, sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithoutServersName}.+"}.size())
		Assert.assertEquals(sent.size(), sent.findAll{it.path.stringValueOfPathName =~ ".+${jobGroupWithServersName}.+"}.size())
		
		//assertions: two MeasuredValues were sent in total
		Integer sentInTotal = 2
		Assert.assertEquals(sentInTotal, sent.size())
		
		//assertions: all MeasuredValues were sent with correct Path
		Assert.assertEquals(sentInTotal, sent.findAll{it.path.stringValueOfPathName.equals(
			pathPrefix + '.' + jobGroupWithServersName + '.weekly.csi'
			)}.size())
		//assertions: all MeasuredValues were sent with correct timestamp
		Assert.assertEquals(sentInTotal, sent.findAll{
			Assert.assertEquals(weeklyDateExpectedToBeSent.toDate(), it.timestamp)
			true;
		}.size())
		//assertions: all MeasuredValues were sent with correct values
		Assert.assertEquals(1, sent.findAll{it.value.equals(firstWeeklyValueToSend * 100)}.size())
		Assert.assertEquals(1, sent.findAll{it.value.equals(secondWeeklyValueToSend * 100)}.size())
	}
	
	//mocks of inner services///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Mocks {@linkplain de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService#findCSIGroups}
	 */
	private void mockJobGroupDaoService(){
		def jobGroupDaoService = mockFor(JobGroupDaoService, true)
		jobGroupDaoService.demand.findCSIGroups(1..10000) {
			
			JobGroup jobGroupWithGraphiteServers = new JobGroup() {
				public Long getId()
				{
					return 1;
				}
			}
			jobGroupWithGraphiteServers.setName(jobGroupWithServersName)
			jobGroupWithGraphiteServers.setGroupType(JobGroupType.CSI_AGGREGATION)
			jobGroupWithGraphiteServers.setGraphiteServers(getGraphiteServers())
			
			JobGroup jobGroupWithoutGraphiteServers = new JobGroup() {
				public Long getId()
				{
					return 2;
				}
			}
			jobGroupWithoutGraphiteServers.setName(jobGroupWithoutServersName)
			jobGroupWithoutGraphiteServers.setGroupType(JobGroupType.CSI_AGGREGATION)
			jobGroupWithoutGraphiteServers.setGraphiteServers([])
			
			return [
					jobGroupWithGraphiteServers,
					jobGroupWithoutGraphiteServers
				] as Set
		}
		serviceUnderTest.jobGroupDaoService = jobGroupDaoService.createMock()
	}
	/**
	 * Mocks {@linkplain GraphiteSocketProvider#getSocket}.
	 * Field {@link #graphiteSocketUsedInTests} is returned and can be used to proof sent dates.
	 */
	private void mockGraphiteSocketProvider(){
		def graphiteSocketProvider = mockFor(GraphiteSocketProvider, true)
		graphiteSocketProvider.demand.getSocket(1..10000) {GraphiteServer server ->
			graphiteSocketUsedInTests = new MockedGraphiteSocket()
			return graphiteSocketUsedInTests
		}
		serviceUnderTest.graphiteSocketProvider = graphiteSocketProvider.createMock()
	}
	/**
	 * Mocks {@linkplain EventMeasuredValueService#getOrCalculateHourylMeasuredValues}.
	 */
	private void mockEventMeasuredValueService(Collection<MeasuredValue> toReturnFromGetHourylMeasuredValues){
		def eventMeasuredValueService = mockFor(EventMeasuredValueService, true)
		
		// FIXME mze-2013-12-10: Hier muss unterschieden werden, welche Op man mocken möchte!
		// Das mocking von Grails ist echt nicht gut lesbar :-(
		eventMeasuredValueService.demand.getHourylMeasuredValues(1..10000) {
			Date fromDate, Date toDate, MvQueryParams mvQueryParams ->
			
			return toReturnFromGetHourylMeasuredValues
			
		}
		serviceUnderTest.eventMeasuredValueService = eventMeasuredValueService.createMock()
	}
	/**
	 * Mocks methods of {@linkplain MeasuredValueTagService}.
	 */
	private void mockMeasuredValueTagService(){
		def measuredValueTagService = mockFor(MeasuredValueTagService, true)
		measuredValueTagService.demand.findPageOfHourlyEventTag(1..10000) {String hourlyEventMvTag ->
			Page page = new Page()
			page.setName(pageName)
			return page
		}
		measuredValueTagService.demand.findMeasuredEventOfHourlyEventTag(1..10000) {String hourlyEventMvTag ->
			MeasuredEvent event = new MeasuredEvent()
			event.setName(eventName)
			return event
		}
		measuredValueTagService.demand.findBrowserOfHourlyEventTag(1..10000) {String hourlyEventMvTag ->
			Browser browser = new Browser()
			browser.setName(browserName)
			return browser
		}
		measuredValueTagService.demand.findLocationOfHourlyEventTag(1..10000) {String hourlyEventMvTag ->
			Location location = new Location()
			location.setLocation(locationLocation)
			return location
		}
		measuredValueTagService.demand.findPageOfDailyPageTag(1..10000) {String dailyPageMvTag ->
			Page page = new Page()
			page.setName(pageName)
			return page
		}
		measuredValueTagService.demand.findPageOfWeeklyPageTag(1..10000) {String weeklyPageMvTag ->
			Page page = new Page()
			page.setName(pageName)
			return page
		}
		serviceUnderTest.measuredValueTagService = measuredValueTagService.createMock()
	}
	/**
	 * Mocks {@linkplain PageMeasuredValueService#getOrCalculatePageMeasuredValues}.
	 */
	private void mockPageMeasuredValueService(Collection<MeasuredValue> toReturnOnDemandForGetOrCalculateMeasuredValues, Integer expectedIntervalInMinutes){
		def pageMeasuredValueService = mockFor(PageMeasuredValueService, true)
		pageMeasuredValueService.demand.getOrCalculatePageMeasuredValues(1..10000) {
			Date fromDate, Date toDate, MeasuredValueInterval interval, List<JobGroup> csiGroups ->
			if ( ! interval.intervalInMinutes.equals(expectedIntervalInMinutes)) {
				return []
			}
			return toReturnOnDemandForGetOrCalculateMeasuredValues
			
		}
		serviceUnderTest.pageMeasuredValueService = pageMeasuredValueService.createMock()
	}
	/**
	 * Mocks {@linkplain ShopMeasuredValueService#getOrCalculateShopMeasuredValues}.
	 */
	private void mockShopMeasuredValueService(Collection<MeasuredValue> toReturnOnDemandForGetOrCalculateMeasuredValues, Integer expectedIntervalInMinutes){
		def shopMeasuredValueService = mockFor(ShopMeasuredValueService, true)
		shopMeasuredValueService.demand.getOrCalculateShopMeasuredValues(1..10000) {
			Date fromDate, Date toDate, MeasuredValueInterval interval, List<JobGroup> csiGroups ->
			if ( ! interval.intervalInMinutes.equals(expectedIntervalInMinutes)) {
				return []
			}
			return toReturnOnDemandForGetOrCalculateMeasuredValues
		}
		serviceUnderTest.shopMeasuredValueService = shopMeasuredValueService.createMock()
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
	public static getMeasuredValue(MeasuredValueInterval interval, AggregatorType aggregator, Double value, DateTime valueForStated, String resultIds){
		MeasuredValue hmv = new MeasuredValue()
		hmv.started = valueForStated.toDate()
		hmv.interval =  interval
		hmv.aggregator = aggregator
		hmv.tag = ''
		hmv.value = value
		hmv.resultIds = resultIds
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
