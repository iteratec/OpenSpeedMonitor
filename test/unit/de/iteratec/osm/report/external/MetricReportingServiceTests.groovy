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

import static org.junit.Assert.assertEquals

import grails.test.mixin.*
import grails.test.mixin.support.*

import org.joda.time.DateTime
import de.iteratec.osm.report.chart.MeasuredValueUtilService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.measurement.schedule.JobGroupType;
import de.iteratec.osm.csi.Page
import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.external.provider.GraphiteSocketProvider
import de.iteratec.osm.csi.EventMeasuredValueService
import de.iteratec.osm.csi.PageMeasuredValueService
import de.iteratec.osm.csi.ShopMeasuredValueService
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.ResultMeasuredValueService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.util.I18nService

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(MetricReportingService)
@TestMixin(GrailsUnitTestMixin)
@Mock([EventResult, AggregatorType,JobGroup, BatchActivity, GraphiteServer, GraphitePath, MeasuredValueInterval, Page, MeasuredEvent, Browser, Location, OsmConfiguration])
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

    void setUp() {
		serviceUnderTest = service
		serviceUnderTest.configService = new ConfigService()
		serviceUnderTest.inMemoryConfigService = new InMemoryConfigService()
		serviceUnderTest.batchActivityService = new BatchActivityService()
		serviceUnderTest.batchActivityService.timer.cancel()//we don't need any updates for this test
		createTestDataCommonToAllTests()
    }
	
	void createTestDataCommonToAllTests(){
		new MeasuredValueInterval(intervalInMinutes: MeasuredValueInterval.HOURLY).save(validate: false)
		new MeasuredValueInterval(intervalInMinutes: MeasuredValueInterval.DAILY).save(validate: false)
		new MeasuredValueInterval(intervalInMinutes: MeasuredValueInterval.WEEKLY).save(validate: false)
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
		mockMeasuredValueTagService(new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES))
		mockGraphiteSocketProvider(testSocket)
		mockResultMeasuredValueService(CachedView.UNCACHED, docCompleteTime)
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
		mockMeasuredValueTagService(
			new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES),
			SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES)
		mockGraphiteSocketProvider(testSocket)
		mockResultMeasuredValueService(CachedView.UNCACHED, docCompleteTime)
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
		mockMeasuredValueTagService(
			new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES),
			SYSTEM_NAME_WITH_WHITESPACES)
		mockGraphiteSocketProvider(testSocket)
		mockResultMeasuredValueService(CachedView.UNCACHED, docCompleteTime)
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
		mockMeasuredValueTagService(
			new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES),
			SYSTEM_NAME_WITH_DOTS)
		mockGraphiteSocketProvider(testSocket)
		mockResultMeasuredValueService(CachedView.UNCACHED, docCompleteTime)
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
		MeasuredValueInterval hourly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> emvs = [
			new CsiAggregation(interval: hourly, aggregator: eventAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockEventMeasuredValueService(emvs)
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
		MeasuredValueInterval hourly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> emvs = [
			new CsiAggregation(interval: hourly, aggregator: eventAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockEventMeasuredValueService(emvs)
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
		MeasuredValueInterval hourly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> emvs = [
			new CsiAggregation(interval: hourly, aggregator: eventAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockEventMeasuredValueService(emvs)
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
		MeasuredValueInterval hourly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> emvs = [
			new CsiAggregation(interval: hourly, aggregator: eventAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockEventMeasuredValueService(emvs)
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
		MeasuredValueInterval daily = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: daily, aggregator: pageAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockPageMeasuredValueService(pmvs)
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
		MeasuredValueInterval daily = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: daily, aggregator: pageAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockPageMeasuredValueService(pmvs)
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
		MeasuredValueInterval daily = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: daily, aggregator: pageAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockPageMeasuredValueService(pmvs)
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
		MeasuredValueInterval daily = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: daily, aggregator: pageAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockPageMeasuredValueService(pmvs)
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
		MeasuredValueInterval daily = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY)
		List<CsiAggregation> pmvsWithoutData = [
			new CsiAggregation(interval: daily, aggregator: pageAggr, csByWptDocCompleteInPercent: null, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockPageMeasuredValueService(pmvsWithoutData)
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
		MeasuredValueInterval weeky = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: weeky, aggregator: pageAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
		mockPageMeasuredValueService(pmvs)
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
		MeasuredValueInterval weeky = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: weeky, aggregator: pageAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
		mockPageMeasuredValueService(pmvs)
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
		MeasuredValueInterval weeky = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)
		List<CsiAggregation> pmvs = [
			new CsiAggregation(interval: weeky, aggregator: pageAggr, csByWptDocCompleteInPercent: null, started: REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), underlyingEventResultsByWptDocComplete: '')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
		mockPageMeasuredValueService(pmvs)
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
		MeasuredValueInterval daily = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> smvs = [
			new CsiAggregation(interval: daily, aggregator: shopAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockShopMeasuredValueService(smvs)
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
		MeasuredValueInterval daily = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> smvs = [
			new CsiAggregation(interval: daily, aggregator: shopAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockShopMeasuredValueService(smvs)
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
		MeasuredValueInterval daily = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY)
		List<CsiAggregation> smvs = [
			new CsiAggregation(interval: daily, aggregator: shopAggr, csByWptDocCompleteInPercent: null, started: REPORTING_TIMESTAMP_START_OF_DAY.toDate(), underlyingEventResultsByWptDocComplete: '')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
		mockShopMeasuredValueService(smvs)
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
		MeasuredValueInterval weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> smvs = [
			new CsiAggregation(interval: weekly, aggregator: shopAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
		mockShopMeasuredValueService(smvs)
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
		MeasuredValueInterval weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)
		double csiValuePersistedInOsm = 0.78d
		List<CsiAggregation> smvs = [
			new CsiAggregation(interval: weekly, aggregator: shopAggr, csByWptDocCompleteInPercent: csiValuePersistedInOsm, started: REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), underlyingEventResultsByWptDocComplete: '1,2,3')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
		mockShopMeasuredValueService(smvs)
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
		MeasuredValueInterval weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)
		List<CsiAggregation> smvs = [
			new CsiAggregation(interval: weekly, aggregator: shopAggr, csByWptDocCompleteInPercent: null, started: REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), underlyingEventResultsByWptDocComplete: '')]
		TestSocket testSocket = new TestSocket()
		//test-specific mocks
		AggregatorType irrelevantCauseJobGroupIsntRequestedFromHere = new AggregatorType()
		mockMeasuredValueTagService(irrelevantCauseJobGroupIsntRequestedFromHere)
		mockMeasuredValueUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
		mockShopMeasuredValueService(smvs)
		mockGraphiteSocketProvider(testSocket)
		mockJobGroupDaoService(AggregatorType.SHOP)
		//execution
		serviceUnderTest.reportShopCSIValuesOfLastWeek(REPORTING_TIMESTAMP)
		//assertions
		assertEquals(0, testSocket.sendDates.size())
	}
	
	//mocking inner services////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Mocks methods of {@linkplain MeasuredValueTagService}.
	 * @param csiGroups
	 * @param pages
	 */
	private void mockMeasuredValueTagService(AggregatorType measurandForGraphitePath, String jobGroupName = SYSTEM_NAME){
		def measuredValueTagService = mockFor(MeasuredValueTagService, true)
		measuredValueTagService.demand.findJobGroupOfHourlyEventTag(1..10000) {
			String hourlyEventMvTag ->
			
			JobGroup group = new JobGroup(name: jobGroupName, groupType: JobGroupType.CSI_AGGREGATION)
			
			GraphiteServer graphiteServer = new GraphiteServer(port: 2003)
			graphiteServer.setServerAdress('monitoring.hh.iteratec.de')
			
			GraphitePath graphitePath = new GraphitePath(prefix: GRAPHITE_PREFIX, measurand: measurandForGraphitePath)
			graphiteServer.graphitePaths = [graphitePath]
			
			group.graphiteServers = [graphiteServer]
			return group
		}
		measuredValueTagService.demand.findPageOfHourlyEventTag(1..10000) {
			String hourlyEventMvTag ->
			return new Page(name: PAGE_NAME)
		}
		measuredValueTagService.demand.findMeasuredEventOfHourlyEventTag(1..10000) {
			String hourlyEventMvTag ->
			return new MeasuredEvent(name: EVENT_NAME)
		}
		measuredValueTagService.demand.findBrowserOfHourlyEventTag(1..10000) {
			String hourlyEventMvTag ->
			return new Browser(name: BROWSER_NAME)
		}
		measuredValueTagService.demand.findLocationOfHourlyEventTag(1..10000) {
			String hourlyEventMvTag ->
			return new Location(location: LOCATION_LOCATION)
		}
		measuredValueTagService.demand.findPageByPageTag(1..10000) {
			String weeklyPageTag ->
			return new Page(name: PAGE_NAME)
		}
		serviceUnderTest.measuredValueTagService = measuredValueTagService.createMock()
	}
	/**
	 * Mocks methods of {@linkplain JobGroupDaoService}.
	 * @param csiGroups
	 * @param pages
	 */
	private void mockJobGroupDaoService(String measurandForGraphitePath, String jobGroupName = SYSTEM_NAME){
		def jobGroupDaoService = mockFor(JobGroupDaoService, true)
		jobGroupDaoService.demand.findCSIGroups(1..10000) { ->
			
			JobGroup group = new JobGroup(name: jobGroupName, groupType: JobGroupType.CSI_AGGREGATION)
			
			GraphiteServer graphiteServer = new GraphiteServer(port: 2003)
			graphiteServer.setServerAdress('monitoring.hh.iteratec.de')
			
			GraphitePath graphitePath = new GraphitePath(prefix: GRAPHITE_PREFIX, measurand: new AggregatorType(name: measurandForGraphitePath))
			graphiteServer.graphitePaths = [graphitePath] 
			
			group.graphiteServers = [graphiteServer]
			Set<JobGroup> groupSet = [group.save(validate: false)] as Set
			
			return groupSet
		}
		serviceUnderTest.jobGroupDaoService = jobGroupDaoService.createMock()
	}
	/**
	 * Mocks methods of {@linkplain MeasuredValueUtilService}.
	 * @param csiGroups
	 * @param pages
	 */
	private void mockMeasuredValueUtilService(DateTime toReturnAsStartOfInterval){
		def measuredValueUtilService = mockFor(MeasuredValueUtilService, true)
		measuredValueUtilService.demand.resetToStartOfActualInterval(1..10000) {
			DateTime dateWithinInterval, Integer intervalInMinutes ->
			return toReturnAsStartOfInterval
		}
		measuredValueUtilService.demand.subtractOneInterval(1..10000) {
			DateTime toSubtractFrom, Integer intervalInMinutes ->
			return toReturnAsStartOfInterval
		}
		serviceUnderTest.measuredValueUtilService = measuredValueUtilService.createMock()
	}
	/**
	 * Mocks methods of {@linkplain PageMeasuredValueService}.
	 */
	private void mockEventMeasuredValueService(List<CsiAggregation> toReturnFromGetHourylMeasuredValues){
		def eventMeasuredValueService = mockFor(EventMeasuredValueService, true)
		eventMeasuredValueService.demand.getHourylMeasuredValues(1..10000) {
			Date fromDate, Date toDate, MvQueryParams mvQueryParams ->
			return toReturnFromGetHourylMeasuredValues
		}
		serviceUnderTest.eventMeasuredValueService = eventMeasuredValueService.createMock()
	}
	/**
	 * Mocks methods of {@linkplain PageMeasuredValueService}.
	 */
	private void mockPageMeasuredValueService(List<CsiAggregation> toReturnFromGetOrCalculatePageMeasuredValues){
		def pageMeasuredValueService = mockFor(PageMeasuredValueService, true)
		pageMeasuredValueService.demand.getOrCalculatePageMeasuredValues(1..10000) {
			Date fromDate, Date toDate, MeasuredValueInterval interval, List<JobGroup> csiGroups ->
			return toReturnFromGetOrCalculatePageMeasuredValues
		}
		serviceUnderTest.pageMeasuredValueService = pageMeasuredValueService.createMock()
	}
	/**
	 * Mocks methods of {@linkplain ShopMeasuredValueService}.
	 */
	private void mockShopMeasuredValueService(List<CsiAggregation> toReturnFromGetOrCalculateShopMeasuredValues){
		def shopMeasuredValueService = mockFor(ShopMeasuredValueService, true)
		shopMeasuredValueService.demand.getOrCalculateShopMeasuredValues(1..10000) {
			Date fromDate, Date toDate, MeasuredValueInterval interval, List<JobGroup> csiGroups ->
			return toReturnFromGetOrCalculateShopMeasuredValues
		}
		serviceUnderTest.shopMeasuredValueService = shopMeasuredValueService.createMock()
	}
	/**
	 * Mocks methods of {@linkplain GraphiteSocketProvider}.
	 */
	private void mockGraphiteSocketProvider(GraphiteSocket toReturnFromGetSocket){
		def graphiteSocketProvider = mockFor(GraphiteSocketProvider, true)
		graphiteSocketProvider.demand.getSocket(1..10000) {
			GraphiteServer server ->
			return toReturnFromGetSocket
		}
		serviceUnderTest.graphiteSocketProvider = graphiteSocketProvider.createMock()
	}
	/**
	 * Mocks methods of {@linkplain GraphiteSocketProvider}.
	 */
	private void mockResultMeasuredValueService(CachedView toReturnFromGetAggregatorTypeCachedViewType, int toReturnFromGetEventResultPropertyForCalculation){
		def resultMeasuredValueService = mockFor(ResultMeasuredValueService, true)
		resultMeasuredValueService.demand.getAggregatorTypeCachedViewType(1..10000) {
			AggregatorType aggregator ->
			return toReturnFromGetAggregatorTypeCachedViewType
		}
		resultMeasuredValueService.demand.getEventResultPropertyForCalculation(1..10000) {
			AggregatorType aggType, EventResult result ->
			return Double.valueOf(toReturnFromGetEventResultPropertyForCalculation)
		}
		serviceUnderTest.resultMeasuredValueService = resultMeasuredValueService.createMock()
	}
	/**
	 * Mocks methods of {@linkplain GraphiteSocketProvider}.
	 */
	private void mockI18nService(){
		def i18nService = mockFor(I18nService, true)
		i18nService.demand.msg(1..10000) {
			String msgKey, String defaultMessage ->
			return MEASURAND_DOCREADYTIME_NAME
		}
		serviceUnderTest.i18nService = i18nService.createMock()
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
