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
import de.iteratec.osm.measurement.schedule.DefaultJobGroupDaoService
import de.iteratec.osm.report.external.provider.DefaultGraphiteSocketProvider
import groovy.mock.interceptor.StubFor

import static org.junit.Assert.assertEquals

import grails.test.mixin.*
import grails.test.mixin.support.*

import org.joda.time.DateTime
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService

import de.iteratec.osm.csi.Page
import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.external.provider.GraphiteSocketProvider
import de.iteratec.osm.csi.EventCsiAggregationService
import de.iteratec.osm.csi.PageCsiAggregationService
import de.iteratec.osm.csi.ShopCsiAggregationService
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.CsiAggregationTagService
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.ResultCsiAggregationService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.util.I18nService

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(MetricReportingService)
@TestMixin(GrailsUnitTestMixin)
@Mock([EventResult, AggregatorType,JobGroup, BatchActivity, GraphiteServer, GraphitePath, CsiAggregationInterval, Page, MeasuredEvent, Browser, Location, OsmConfiguration])
class MetricReportingServiceTests {
	MetricReportingService serviceUnderTest
	static final double DELTA = 1e-15
	static final DateTime REPORTING_TIMESTAMP = new DateTime(2014,1,22,13,42,0)
	static final DateTime REPORTING_TIMESTAMP_START_OF_DAY = new DateTime(2014,1,22,0,0,0)
	static final DateTime REPORTING_TIMESTAMP_START_OF_WEEK = new DateTime(2014,1,20,0,0,0)
	static final String GRAPHITE_PREFIX = 'my.test.graphite.prefix'
	static final String PAGE_HP_NAME = 'HP'
	static final String SYSTEM_NAME = 'live'
	static final String SYSTEM_NAME_WITH_WHITESPACES = 'live mdot'
	static final String SYSTEM_NAME_WITH_WHITESPACES_REPLACED = 'live_mdot'
	static final String SYSTEM_NAME_WITH_DOTS = 'm.my-shop.com'
	static final String SYSTEM_NAME_WITH_DOTS_REPLACED = 'm_my-shop_com'
	static final String SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES = 'live m.my-shop.com'
	static final String SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES_REPLACED = 'live_m_my-shop_com'
	static final DELIMITTER = '_-_'
	static final String PAGE_NAME = 'HP'
	static final String EVENT_NAME = 'HP_event'
	static final String LOCATION_LOCATION = 'netlab1'
	static final String BROWSER_NAME = 'FF'
	static final String MEASURAND_DOCREADYTIME_NAME = 'docReadyTime'

	def doWithSpring = {
		batchActivityService(BatchActivityService)
		configService(ConfigService)
		inMemoryConfigService(InMemoryConfigService)
	}
    void setUp() {
		serviceUnderTest = service
		serviceUnderTest.configService = grailsApplication.mainContext.getBean('configService')
		serviceUnderTest.inMemoryConfigService = grailsApplication.mainContext.getBean('inMemoryConfigService')
		serviceUnderTest.batchActivityService = grailsApplication.mainContext.getBean('batchActivityService')
		createTestDataCommonToAllTests()
    }
	
	void createTestDataCommonToAllTests(){
		new CsiAggregationInterval(intervalInMinutes: CsiAggregationInterval.HOURLY).save(validate: false)
		new CsiAggregationInterval(intervalInMinutes: CsiAggregationInterval.DAILY).save(validate: false)
		new CsiAggregationInterval(intervalInMinutes: CsiAggregationInterval.WEEKLY).save(validate: false)
		new AggregatorType(name: AggregatorType.MEASURED_EVENT).save(validate: false)
		new AggregatorType(name: AggregatorType.PAGE).save(validate: false)
		new AggregatorType(name: AggregatorType.SHOP).save(validate: false)
		serviceUnderTest.inMemoryConfigService.activateMeasurementsGenerally()
		new OsmConfiguration().save(failOnError: true)
	}

    void tearDown() {
        // Tear down logic here
    }
	
	// test sending event results ////////////////////////////////////////////////////////////////////////////////////////////////////////

    void testSendResultsOfDifferentCachedView() {
		//test-specific data
		int docCompleteTime = 1267i
		EventResult result_1 = new EventResult(cachedView: CachedView.UNCACHED, docCompleteTimeInMillisecs: docCompleteTime, jobResultDate: REPORTING_TIMESTAMP.toDate())
		TestSocket testSocket = new TestSocket()
		
		//test-specific mocks
		mockCsiAggregationTagService(new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES))
		mockGraphiteSocketProvider(testSocket)
		mockResultCsiAggregationService(CachedView.UNCACHED, docCompleteTime)
		mockI18nService()
		
		//execution
		serviceUnderTest.reportEventResultToGraphite(result_1)
		
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME}.raw.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.${MEASURAND_DOCREADYTIME_NAME}${DELIMITTER}${REPORTING_TIMESTAMP.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(docCompleteTime, testSocket.sendDates[expectedKey], DELTA)
    }
	
	void testSendEventResultWithDotsAndWhiteSpacesInJobGroup() {
		//test-specific data
		int docCompleteTime = 1267i
		EventResult result_1 = new EventResult(cachedView: CachedView.UNCACHED, docCompleteTimeInMillisecs: docCompleteTime, jobResultDate: REPORTING_TIMESTAMP.toDate())
		TestSocket testSocket = new TestSocket()
		
		//test-specific mocks
		mockCsiAggregationTagService(
			new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES),
			SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES)
		mockGraphiteSocketProvider(testSocket)
		mockResultCsiAggregationService(CachedView.UNCACHED, docCompleteTime)
		mockI18nService()
		
		//execution
		serviceUnderTest.reportEventResultToGraphite(result_1)
		
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES_REPLACED}.raw.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.${MEASURAND_DOCREADYTIME_NAME}${DELIMITTER}${REPORTING_TIMESTAMP.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(docCompleteTime, testSocket.sendDates[expectedKey], DELTA)
	}
	
	void testSendEventResultWithWhiteSpacesInJobGroup() {
		//test-specific data
		int docCompleteTime = 1267i
		EventResult result_1 = new EventResult(cachedView: CachedView.UNCACHED, docCompleteTimeInMillisecs: docCompleteTime, jobResultDate: REPORTING_TIMESTAMP.toDate())
		TestSocket testSocket = new TestSocket()
		
		//test-specific mocks
		mockCsiAggregationTagService(
			new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES),
			SYSTEM_NAME_WITH_WHITESPACES)
		mockGraphiteSocketProvider(testSocket)
		mockResultCsiAggregationService(CachedView.UNCACHED, docCompleteTime)
		mockI18nService()
		
		//execution
		serviceUnderTest.reportEventResultToGraphite(result_1)
		
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_WHITESPACES_REPLACED}.raw.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.${MEASURAND_DOCREADYTIME_NAME}${DELIMITTER}${REPORTING_TIMESTAMP.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(docCompleteTime, testSocket.sendDates[expectedKey], DELTA)
	}
	
	void testSendEventResultWithDotsInJobGroup() {
		//test-specific data
		int docCompleteTime = 1267i
		EventResult result_1 = new EventResult(cachedView: CachedView.UNCACHED, docCompleteTimeInMillisecs: docCompleteTime, jobResultDate: REPORTING_TIMESTAMP.toDate())
		TestSocket testSocket = new TestSocket()
		
		//test-specific mocks
		mockCsiAggregationTagService(
			new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES),
			SYSTEM_NAME_WITH_DOTS)
		mockGraphiteSocketProvider(testSocket)
		mockResultCsiAggregationService(CachedView.UNCACHED, docCompleteTime)
		mockI18nService()
		
		//execution
		serviceUnderTest.reportEventResultToGraphite(result_1)
		
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_REPLACED}.raw.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.${MEASURAND_DOCREADYTIME_NAME}${DELIMITTER}${REPORTING_TIMESTAMP.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(docCompleteTime, testSocket.sendDates[expectedKey], DELTA)
	}
	
	// test sending event csi values  ////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	void testReportEventCSIValuesOfLastHour(){
		//test-specific data
		AggregatorType eventAggr = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
		CsiAggregationInterval hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> emvs = [
			new CsiAggregation(interval: hourly, aggregator: eventAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockEventCsiAggregationService(emvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.MEASURED_EVENT, SYSTEM_NAME_WITH_DOTS)
		//execution
		serviceUnderTest.reportEventCSIValuesOfLastHour(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_REPLACED}.hourly.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	
	void testReportEventCSIValuesOfLastHourWithWhitespacesInSystemName(){
		//test-specific data
		AggregatorType eventAggr = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
		CsiAggregationInterval hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> emvs = [
			new CsiAggregation(interval: hourly, aggregator: eventAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockEventCsiAggregationService(emvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.MEASURED_EVENT, SYSTEM_NAME_WITH_WHITESPACES)
		//execution
		serviceUnderTest.reportEventCSIValuesOfLastHour(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_WHITESPACES_REPLACED}.hourly.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	
	void testReportEventCSIValuesOfLastHourWithDotsInSystemName(){
		//test-specific data
		AggregatorType eventAggr = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
		CsiAggregationInterval hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> emvs = [
			new CsiAggregation(interval: hourly, aggregator: eventAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockEventCsiAggregationService(emvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.MEASURED_EVENT, SYSTEM_NAME_WITH_DOTS)
		//execution
		serviceUnderTest.reportEventCSIValuesOfLastHour(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_REPLACED}.hourly.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	void testReportEventCSIValuesOfLastHourWithDotsAndWhitespacesInSystemName(){
		//test-specific data
		AggregatorType eventAggr = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
		CsiAggregationInterval hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> emvs = [
			new CsiAggregation(interval: hourly, aggregator: eventAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockEventCsiAggregationService(emvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.MEASURED_EVENT, SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES)
		//execution
		serviceUnderTest.reportEventCSIValuesOfLastHour(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES_REPLACED}.hourly.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	
	// test sending page csi values  ////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	void testReportPageCSIValuesOfLastDay(){
		//test-specific data
		AggregatorType pageAggr = AggregatorType.findByName(AggregatorType.PAGE)
		CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: daily, aggregator: pageAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockPageCsiAggregationService(pmvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.PAGE)
		//execution
		serviceUnderTest.reportPageCSIValuesOfLastDay(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME}.daily.${PAGE_HP_NAME}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	
	void testReportPageCSIValuesOfLastDayWithDotInSystemName(){
		//test-specific data
		AggregatorType pageAggr = AggregatorType.findByName(AggregatorType.PAGE)
		CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: daily, aggregator: pageAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockPageCsiAggregationService(pmvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.PAGE, SYSTEM_NAME_WITH_DOTS)
		//execution
		serviceUnderTest.reportPageCSIValuesOfLastDay(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_REPLACED}.daily.${PAGE_HP_NAME}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	
	void testReportPageCSIValuesOfLastDayWithWhitespaceInSystemName(){
		//test-specific data
		AggregatorType pageAggr = AggregatorType.findByName(AggregatorType.PAGE)
		CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: daily, aggregator: pageAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockPageCsiAggregationService(pmvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.PAGE, SYSTEM_NAME_WITH_WHITESPACES)
		//execution
		serviceUnderTest.reportPageCSIValuesOfLastDay(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_WHITESPACES_REPLACED}.daily.${PAGE_HP_NAME}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	void testReportPageCSIValuesOfLastDayWithDotsAndWhitespaceInSystemName(){
		//test-specific data
		AggregatorType pageAggr = AggregatorType.findByName(AggregatorType.PAGE)
		CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: daily, aggregator: pageAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockPageCsiAggregationService(pmvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.PAGE, SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES)
		//execution
		serviceUnderTest.reportPageCSIValuesOfLastDay(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES_REPLACED}.daily.${PAGE_HP_NAME}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	
	void testReportPageCSIValuesOfLastDayWithoutData(){
		//test-specific data
		AggregatorType pageAggr = AggregatorType.findByName(AggregatorType.PAGE)
		CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
		List<CsiAggregation> pmvsWithoutData = [
			new CsiAggregation(interval: daily, aggregator: pageAggr, csByWptDocCompleteInPercent: null, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockPageCsiAggregationService(pmvsWithoutData)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.PAGE)
		//execution
		serviceUnderTest.reportPageCSIValuesOfLastDay(REPORTING_TIMESTAMP)
		//assertions
		assertEquals(0, testSocket.sendDates.size())
	}
	
	void testReportPageCSIValuesOfLastWeek(){
		//test-specific data
		AggregatorType pageAggr = AggregatorType.findByName(AggregatorType.PAGE)
		CsiAggregationInterval weeky = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: weeky, aggregator: pageAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
		mockPageCsiAggregationService(pmvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.PAGE)
		//execution
		serviceUnderTest.reportPageCSIValuesOfLastWeek(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME}.weekly.${PAGE_HP_NAME}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_WEEK.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	
	void testReportPageCSIValuesOfLastWeekWithDotsAndWhitespacesInSystemName(){
		//test-specific data
		AggregatorType pageAggr = AggregatorType.findByName(AggregatorType.PAGE)
		CsiAggregationInterval weeky = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: weeky, aggregator: pageAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
		mockPageCsiAggregationService(pmvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.PAGE, SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES)
		//execution
		serviceUnderTest.reportPageCSIValuesOfLastWeek(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES_REPLACED}.weekly.${PAGE_HP_NAME}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_WEEK.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	
	void testReportPageCSIValuesOfLastWeekWithoutData(){
		//test-specific data
		AggregatorType pageAggr = AggregatorType.findByName(AggregatorType.PAGE)
		CsiAggregationInterval weeky = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: weeky, aggregator: pageAggr, csByWptDocCompleteInPercent: null, started: REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), underlyingEventResultsByWptDocComplete: '')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
		mockPageCsiAggregationService(pmvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.PAGE)
		//execution
		serviceUnderTest.reportPageCSIValuesOfLastWeek(REPORTING_TIMESTAMP)
		//assertions
		assertEquals(0, testSocket.sendDates.size())
	}
	
	// test sending shop csi values ////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	void testReportShopCSIValuesOfLastDay(){
		//test-specific data
		AggregatorType shopAggr = AggregatorType.findByName(AggregatorType.SHOP)
		CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> smvs = [
			new CsiAggregation(interval: daily, aggregator: shopAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockShopCsiAggregationService(smvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.SHOP)
		//execution
		serviceUnderTest.reportShopCSIValuesOfLastDay(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME}.daily.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	
	void testReportShopCSIValuesOfLastDayWithDotsAndWhitespacesInSystemName(){
		//test-specific data
		AggregatorType shopAggr = AggregatorType.findByName(AggregatorType.SHOP)
		CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> smvs = [
			new CsiAggregation(interval: daily, aggregator: shopAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockShopCsiAggregationService(smvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.SHOP, SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES)
		//execution
		serviceUnderTest.reportShopCSIValuesOfLastDay(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES_REPLACED}.daily.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	
	void testReportShopCSIValuesOfLastDayWithoutData(){
		//test-specific data
		AggregatorType shopAggr = AggregatorType.findByName(AggregatorType.SHOP)
		CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
		List<CsiAggregation> smvs = [
			new CsiAggregation(interval: daily, aggregator: shopAggr, csByWptDocCompleteInPercent: null, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockShopCsiAggregationService(smvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.SHOP)
		//execution
		serviceUnderTest.reportShopCSIValuesOfLastDay(REPORTING_TIMESTAMP)
		//assertions
		assertEquals(0, testSocket.sendDates.size())
	}
	
	void testReportShopCSIValuesOfLastWeek(){
		//test-specific data
		AggregatorType shopAggr = AggregatorType.findByName(AggregatorType.SHOP)
		CsiAggregationInterval weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> smvs = [
			new CsiAggregation(interval: weekly, aggregator: shopAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
		mockShopCsiAggregationService(smvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.SHOP)
		//execution
		serviceUnderTest.reportShopCSIValuesOfLastWeek(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME}.weekly.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_WEEK.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	
	void testReportShopCSIValuesOfLastWeekWithDotsAndWhitespacesInSystemName(){
		//test-specific data
		AggregatorType shopAggr = AggregatorType.findByName(AggregatorType.SHOP)
		CsiAggregationInterval weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> smvs = [
			new CsiAggregation(interval: weekly, aggregator: shopAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
		mockShopCsiAggregationService(smvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.SHOP, SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES)
		//execution
		serviceUnderTest.reportShopCSIValuesOfLastWeek(REPORTING_TIMESTAMP)
		//assertions
		String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES_REPLACED}.weekly.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_WEEK.toDate()}"
		assertEquals(1, testSocket.sendDates.size())
		assertEquals(expectedKey, testSocket.sendDates.keySet()[0])
		assertEquals(csiValuePersistedInOsm * 100, testSocket.sendDates[expectedKey], DELTA)
	}
	
	void testReportShopCSIValuesOfLastWeekWithoutData(){
		//test-specific data
		AggregatorType shopAggr = AggregatorType.findByName(AggregatorType.SHOP)
		CsiAggregationInterval weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
		List<CsiAggregation> smvs = [
			new CsiAggregation(interval: weekly, aggregator: shopAggr, csByWptDocCompleteInPercent: null, started: REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), underlyingEventResultsByWptDocComplete: '')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockCsiAggregationTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
		mockShopCsiAggregationService(smvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.SHOP)
		//execution
		serviceUnderTest.reportShopCSIValuesOfLastWeek(REPORTING_TIMESTAMP)
		//assertions
		assertEquals(0, testSocket.sendDates.size())
	}
	
	//mocking inner services////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Mocks methods of {@linkplain CsiAggregationTagService}.
	 * @param csiGroups
	 * @param pages
	 */
	private void mockCsiAggregationTagService(AggregatorType measurandForGraphitePath, String jobGroupName = SYSTEM_NAME){
		def csiAggregationTagService = new StubFor(CsiAggregationTagService, true)
		csiAggregationTagService.demand.findJobGroupOfHourlyEventTag {
			String hourlyEventMvTag ->
			
			JobGroup group = new JobGroup(name: jobGroupName)
			
			GraphiteServer graphiteServer = new GraphiteServer(port: 2003)
			graphiteServer.setServerAdress('monitoring.hh.iteratec.de')
			
			GraphitePath graphitePath = new GraphitePath(prefix: GRAPHITE_PREFIX, measurand: measurandForGraphitePath)
			graphiteServer.graphitePaths = [graphitePath]
			
			group.graphiteServers = [graphiteServer]
			return group
		}
		csiAggregationTagService.demand.findPageOfHourlyEventTag{
			String hourlyEventMvTag ->
			return new Page(name: PAGE_NAME)
		}
		csiAggregationTagService.demand.findMeasuredEventOfHourlyEventTag {
			String hourlyEventMvTag ->
			return new MeasuredEvent(name: EVENT_NAME)
		}
		csiAggregationTagService.demand.findBrowserOfHourlyEventTag {
			String hourlyEventMvTag ->
			return new Browser(name: BROWSER_NAME)
		}
		csiAggregationTagService.demand.findLocationOfHourlyEventTag{
			String hourlyEventMvTag ->
			return new Location(location: LOCATION_LOCATION)
		}
		csiAggregationTagService.demand.findPageByPageTag {
			String weeklyPageTag ->
			return new Page(name: PAGE_NAME)
		}
		serviceUnderTest.csiAggregationTagService = csiAggregationTagService.proxyInstance()
	}
	/**
	 * Mocks methods of {@linkplain JobGroupDaoService}.
	 * @param csiGroups
	 * @param pages
	 */
	private void mockJobGroupDaoService(String measurandForGraphitePath, String jobGroupName = SYSTEM_NAME){
		def jobGroupDaoService = new StubFor(DefaultJobGroupDaoService, true)
		jobGroupDaoService.demand.findCSIGroups { ->
			
			JobGroup group = new JobGroup(name: jobGroupName)
			
			GraphiteServer graphiteServer = new GraphiteServer(port: 2003)
			graphiteServer.setServerAdress('monitoring.hh.iteratec.de')
			
			GraphitePath graphitePath = new GraphitePath(prefix: GRAPHITE_PREFIX, measurand: new AggregatorType(name: measurandForGraphitePath))
			graphiteServer.graphitePaths = [graphitePath] 
			
			group.graphiteServers = [graphiteServer]
			Set<JobGroup> groupSet = [group.save(validate: false)] as Set
			
			return groupSet
		}
		serviceUnderTest.jobGroupDaoService = jobGroupDaoService.proxyInstance()
	}
	/**
	 * Mocks methods of {@linkplain CsiAggregationUtilService}.
	 * @param csiGroups
	 * @param pages
	 */
	private void mockCsiAggregationUtilService(DateTime toReturnAsStartOfInterval){
		def csiAggregationUtilService = new StubFor(CsiAggregationUtilService, true)
		csiAggregationUtilService.demand.resetToStartOfActualInterval {
			DateTime dateWithinInterval, Integer intervalInMinutes ->
			return toReturnAsStartOfInterval
		}
		csiAggregationUtilService.demand.subtractOneInterval{
			DateTime toSubtractFrom, Integer intervalInMinutes ->
			return toReturnAsStartOfInterval
		}
		serviceUnderTest.csiAggregationUtilService = csiAggregationUtilService.proxyInstance()
	}
	/**
	 * Mocks methods of {@linkplain PageCsiAggregationService}.
	 */
	private void mockEventCsiAggregationService(List<CsiAggregation> toReturnFromGetHourlyCsiAggregations){
		def eventCsiAggregationService = new StubFor(EventCsiAggregationService, true)
		eventCsiAggregationService.demand.getHourlyCsiAggregations{
			Date fromDate, Date toDate, MvQueryParams mvQueryParams ->
			return toReturnFromGetHourlyCsiAggregations
		}
		serviceUnderTest.eventCsiAggregationService = eventCsiAggregationService.proxyInstance()
	}
	/**
	 * Mocks methods of {@linkplain PageCsiAggregationService}.
	 */
	private void mockPageCsiAggregationService(List<CsiAggregation> toReturnFromGetOrCalculatePageCsiAggregations){
		def pageCsiAggregationService = new StubFor(PageCsiAggregationService, true)
		pageCsiAggregationService.demand.getOrCalculatePageCsiAggregations {
			Date fromDate, Date toDate, CsiAggregationInterval interval, List<JobGroup> csiGroups ->
			return toReturnFromGetOrCalculatePageCsiAggregations
		}
		serviceUnderTest.pageCsiAggregationService = pageCsiAggregationService.proxyInstance()
	}
	/**
	 * Mocks methods of {@linkplain ShopCsiAggregationService}.
	 */
	private void mockShopCsiAggregationService(List<CsiAggregation> toReturnFromGetOrCalculateShopCsiAggregations){
		def shopCsiAggregationService = new StubFor(ShopCsiAggregationService, true)
		shopCsiAggregationService.demand.getOrCalculateShopCsiAggregations {
			Date fromDate, Date toDate, CsiAggregationInterval interval, List<JobGroup> csiGroups ->
			return toReturnFromGetOrCalculateShopCsiAggregations
		}
		serviceUnderTest.shopCsiAggregationService = shopCsiAggregationService.proxyInstance()
	}
	/**
	 * Mocks methods of {@linkplain GraphiteSocketProvider}.
	 */
	private void mockGraphiteSocketProvider(GraphiteSocket toReturnFromGetSocket){
		def graphiteSocketProvider = new StubFor(DefaultGraphiteSocketProvider, true)
		graphiteSocketProvider.demand.getSocket {
			GraphiteServer server ->
			return toReturnFromGetSocket
		}
		serviceUnderTest.graphiteSocketProvider = graphiteSocketProvider.proxyInstance()
	}
	/**
	 * Mocks methods of {@linkplain GraphiteSocketProvider}.
	 */
	private void mockResultCsiAggregationService(CachedView toReturnFromGetAggregatorTypeCachedViewType, int toReturnFromGetEventResultPropertyForCalculation){
		def resultCsiAggregationService = new StubFor(ResultCsiAggregationService, true)
		resultCsiAggregationService.demand.getAggregatorTypeCachedViewType {
			AggregatorType aggregator ->
			return toReturnFromGetAggregatorTypeCachedViewType
		}
		resultCsiAggregationService.demand.getEventResultPropertyForCalculation {
			AggregatorType aggType, EventResult result ->
			return Double.valueOf(toReturnFromGetEventResultPropertyForCalculation)
		}
		serviceUnderTest.resultCsiAggregationService = resultCsiAggregationService.proxyInstance()
	}
	/**
	 * Mocks methods of {@linkplain GraphiteSocketProvider}.
	 */
	private void mockI18nService(){
		def i18nService = new StubFor(I18nService, true)
		i18nService.demand.msg {
			String msgKey, String defaultMessage ->
			return MEASURAND_DOCREADYTIME_NAME
		}
		serviceUnderTest.i18nService = i18nService.proxyInstance()
	}
	
	/**
	 * <p>
	 * Just for testing-purposes. Instances can be used to be returned in Mock of 
	 * {@link GraphiteSocketProvider#getSocket(GraphiteServer, GraphiteSocketProvider.Protocol)}.
	 * <br>Map {@link #sendDates} contains the informations about calls to {@link #sendDate(GraphitePathName, double, Date)} for assertions.
	 * </p>
	 *  
	 * @author nkuhn
	 *
	 */
	class TestSocket implements GraphiteSocket{
		/** This map contains one entry for every call to {@link #sendDate} of this socket during the test.
		 * <ul>
		 * <li>key		= <br>[GraphitePathName.stringValueOfPathName of the call] + {@link #DELIMITTER}<br>[timestamp.toString() of the call]</li>
		 * <li>value	= <br>(double-)value of the call</li>
		 * </ul>
		 *   */
		Map<String, Double> sendDates = [:]

		@Override
		public void sendDate(GraphitePathName path, double value, Date timestamp)
				throws NullPointerException, GraphiteComunicationFailureException {
				sendDates[path.stringValueOfPathName + DELIMITTER + timestamp.toString()] = value
		}
		
	}
	
	
}
