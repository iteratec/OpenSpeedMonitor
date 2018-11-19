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

import de.iteratec.osm.ConfigService
import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.csi.EventCsiAggregationService
import de.iteratec.osm.csi.JobGroupCsiAggregationService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.PageCsiAggregationService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupService
import de.iteratec.osm.report.chart.AggregationType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.report.external.provider.DefaultGraphiteSocketProvider
import de.iteratec.osm.report.external.provider.GraphiteSocketProvider
import de.iteratec.osm.result.*
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import spock.lang.Specification

@Build([Page,MeasuredEvent,Location, Browser, JobGroup])
class MetricReportingServiceSpec extends Specification implements BuildDataTest,
        ServiceUnitTest<MetricReportingService> {
    MetricReportingService serviceUnderTest
    static final double DELTA = 1e-15
    static final DateTime REPORTING_TIMESTAMP = new DateTime(2014, 1, 22, 13, 42, 0)
    static final DateTime REPORTING_TIMESTAMP_START_OF_DAY = new DateTime(2014, 1, 22, 0, 0, 0)
    static final DateTime REPORTING_TIMESTAMP_START_OF_WEEK = new DateTime(2014, 1, 20, 0, 0, 0)
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
    static final String MEASURAND_DOCREADYTIME_NAME = CachedView.UNCACHED.graphiteLabelPrefix+Measurand.DOC_COMPLETE_TIME.grapthiteLabelSuffix

    Page page
    MeasuredEvent measuredEvent
    Browser browser
    Location location
    TestSocket testSocket

    Closure doWithSpring() {
        return {
            batchActivityService(BatchActivityService)
            configService(ConfigService)
            inMemoryConfigService(InMemoryConfigService)
        }
    }

    void setup() {
        serviceUnderTest = service
        serviceUnderTest.configService = grailsApplication.mainContext.getBean('configService') as ConfigService
        serviceUnderTest.inMemoryConfigService = grailsApplication.mainContext.getBean('inMemoryConfigService') as InMemoryConfigService
        serviceUnderTest.batchActivityService = grailsApplication.mainContext.getBean('batchActivityService') as BatchActivityService
        createTestDataCommonToAllTests()
    }

    void setupSpec() {
        mockDomains(EventResult,  JobGroup, BatchActivity, GraphiteServer,  CsiAggregationInterval, Page, MeasuredEvent,
                Browser, Location, OsmConfiguration, ConnectivityProfile)
    }

    void createTestDataCommonToAllTests() {
        new CsiAggregationInterval(intervalInMinutes: CsiAggregationInterval.HOURLY).save(validate: false)
        new CsiAggregationInterval(intervalInMinutes: CsiAggregationInterval.DAILY).save(validate: false)
        new CsiAggregationInterval(intervalInMinutes: CsiAggregationInterval.WEEKLY).save(validate: false)
        serviceUnderTest.inMemoryConfigService.activateMeasurementsGenerally()
        new OsmConfiguration().save(failOnError: true)

        page = Page.build(name: PAGE_NAME)
        measuredEvent = MeasuredEvent.build(name: EVENT_NAME)
        browser = Browser.build(name: BROWSER_NAME)
        location = Location.build(location: LOCATION_LOCATION)
        testSocket = new TestSocket()
        mockGraphiteSocketProvider(testSocket)
    }

    // test sending event results ////////////////////////////////////////////////////////////////////////////////////////////////////////

    void testSendResultsOfDifferentCachedView() {
        given:
        int docCompleteTime = 1267i
        JobGroup jobGroup = getJobGroup(Measurand.DOC_COMPLETE_TIME, CachedView.UNCACHED, SYSTEM_NAME)
        EventResult result_1 = getEventResult(CachedView.UNCACHED, docCompleteTime, REPORTING_TIMESTAMP.toDate(), jobGroup)

        when:
        serviceUnderTest.reportEventResultToGraphite(result_1)

        then:
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME}.raw.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.${MEASURAND_DOCREADYTIME_NAME}${DELIMITTER}${REPORTING_TIMESTAMP.toDate()}"
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet().first() == expectedKey
        testSocket.sendDates[expectedKey] == docCompleteTime
    }

    void testSendEventResultWithDotsAndWhiteSpacesInJobGroup() {
        given:
        int docCompleteTime = 1267i
        JobGroup jobGroup = getJobGroup(Measurand.DOC_COMPLETE_TIME, CachedView.UNCACHED,
                SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES)
        EventResult result_1 = getEventResult(CachedView.UNCACHED, docCompleteTime, REPORTING_TIMESTAMP.toDate(), jobGroup)

        when:
        serviceUnderTest.reportEventResultToGraphite(result_1)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES_REPLACED}.raw.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.${MEASURAND_DOCREADYTIME_NAME}${DELIMITTER}${REPORTING_TIMESTAMP.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == docCompleteTime
    }

    void testSendEventResultWithWhiteSpacesInJobGroup() {
        given:
        int docCompleteTime = 1267i
        JobGroup jobGroup = getJobGroup(Measurand.DOC_COMPLETE_TIME, CachedView.UNCACHED,
                SYSTEM_NAME_WITH_WHITESPACES)
        EventResult result_1 = getEventResult(CachedView.UNCACHED, docCompleteTime, REPORTING_TIMESTAMP.toDate(), jobGroup)

        when:
        serviceUnderTest.reportEventResultToGraphite(result_1)

        then:
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_WHITESPACES_REPLACED}.raw.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.${MEASURAND_DOCREADYTIME_NAME}${DELIMITTER}${REPORTING_TIMESTAMP.toDate()}"
        1 == testSocket.sendDates.size()
        expectedKey == testSocket.sendDates.keySet()[0]
        docCompleteTime == testSocket.sendDates[expectedKey]
    }

    void testSendEventResultWithDotsInJobGroup() {
        given:
        int docCompleteTime = 1267i
        JobGroup jobGroup = getJobGroup(Measurand.DOC_COMPLETE_TIME, CachedView.UNCACHED,
                SYSTEM_NAME_WITH_DOTS)
        EventResult result_1 = getEventResult(CachedView.UNCACHED, docCompleteTime, REPORTING_TIMESTAMP.toDate(), jobGroup)

        when:
        serviceUnderTest.reportEventResultToGraphite(result_1)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_REPLACED}.raw.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.${MEASURAND_DOCREADYTIME_NAME}${DELIMITTER}${REPORTING_TIMESTAMP.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == docCompleteTime
    }

    // test sending event csi values  ////////////////////////////////////////////////////////////////////////////////////////////////////////

    void testReportEventCSIValuesOfLastHour() {
        given:
        CsiAggregationInterval hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> emvs = [
                getCsiAggregation(hourly, AggregationType.MEASURED_EVENT, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_DAY.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
        mockEventCsiAggregationService(emvs)
        mockJobGroupService(AggregationType.MEASURED_EVENT, SYSTEM_NAME_WITH_DOTS)

        when:
        serviceUnderTest.reportEventCSIValuesOfLastHour(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_REPLACED}.hourly.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == (csiValuePersistedInOsm * 100)
    }

    void testReportEventCSIValuesOfLastHourWithWhitespacesInSystemName() {
        given:
        CsiAggregationInterval hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> emvs = [
                getCsiAggregation(hourly, AggregationType.MEASURED_EVENT, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_DAY.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
        mockEventCsiAggregationService(emvs)
        mockJobGroupService(AggregationType.MEASURED_EVENT, SYSTEM_NAME_WITH_WHITESPACES)

        when:
        serviceUnderTest.reportEventCSIValuesOfLastHour(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_WHITESPACES_REPLACED}.hourly.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == csiValuePersistedInOsm * 100
    }

    void testReportEventCSIValuesOfLastHourWithDotsInSystemName() {
        given:
        CsiAggregationInterval hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> emvs = [
                getCsiAggregation(hourly, AggregationType.MEASURED_EVENT, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_DAY.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
        mockEventCsiAggregationService(emvs)
        mockJobGroupService(AggregationType.MEASURED_EVENT, SYSTEM_NAME_WITH_DOTS)

        when:
        serviceUnderTest.reportEventCSIValuesOfLastHour(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_REPLACED}.hourly.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == csiValuePersistedInOsm * 100
    }

    void testReportEventCSIValuesOfLastHourWithDotsAndWhitespacesInSystemName() {
        given:
        CsiAggregationInterval hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> emvs = [
                getCsiAggregation(hourly, AggregationType.MEASURED_EVENT, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_DAY.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
        mockEventCsiAggregationService(emvs)
        mockJobGroupService(AggregationType.MEASURED_EVENT, SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES)

        when:
        serviceUnderTest.reportEventCSIValuesOfLastHour(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES_REPLACED}.hourly.${PAGE_NAME}.${EVENT_NAME}.${BROWSER_NAME}.${LOCATION_LOCATION}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == csiValuePersistedInOsm * 100
    }

    // test sending page csi values  ////////////////////////////////////////////////////////////////////////////////////////////////////////

    void testReportPageCSIValuesOfLastDay() {
        given:
        CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> pmvs = [
                getCsiAggregation(daily, AggregationType.PAGE, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_DAY.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
        mockPageCsiAggregationService(pmvs)
        mockJobGroupService(AggregationType.PAGE)

        when:
        serviceUnderTest.reportPageCSIValuesOfLastDay(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME}.daily.${PAGE_HP_NAME}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == csiValuePersistedInOsm * 100
    }

    void testReportPageCSIValuesOfLastDayWithDotInSystemName() {
        given:
        CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> pmvs = [
                getCsiAggregation(daily, AggregationType.PAGE, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_DAY.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
        mockPageCsiAggregationService(pmvs)
        mockJobGroupService(AggregationType.PAGE, SYSTEM_NAME_WITH_DOTS)

        when:
        serviceUnderTest.reportPageCSIValuesOfLastDay(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_REPLACED}.daily.${PAGE_HP_NAME}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == csiValuePersistedInOsm * 100
    }

    void testReportPageCSIValuesOfLastDayWithWhitespaceInSystemName() {
        given:
        CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> pmvs = [
                getCsiAggregation(daily, AggregationType.PAGE, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_DAY.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
        mockPageCsiAggregationService(pmvs)
        mockJobGroupService(AggregationType.PAGE, SYSTEM_NAME_WITH_WHITESPACES)

        when:
        serviceUnderTest.reportPageCSIValuesOfLastDay(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_WHITESPACES_REPLACED}.daily.${PAGE_HP_NAME}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == csiValuePersistedInOsm * 100
    }

    void testReportPageCSIValuesOfLastDayWithDotsAndWhitespaceInSystemName() {
        given:
        CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> pmvs = [
                getCsiAggregation(daily, AggregationType.PAGE, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_DAY.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
        mockPageCsiAggregationService(pmvs)
        mockJobGroupService(AggregationType.PAGE, SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES)

        when:
        serviceUnderTest.reportPageCSIValuesOfLastDay(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES_REPLACED}.daily.${PAGE_HP_NAME}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == csiValuePersistedInOsm * 100
    }

    void testReportPageCSIValuesOfLastDayWithoutData() {
        given:
        CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
        List<CsiAggregation> pmvsWithoutData = [
                getCsiAggregation(daily, AggregationType.PAGE, null, REPORTING_TIMESTAMP_START_OF_DAY.toDate(), '')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
        mockPageCsiAggregationService(pmvsWithoutData)
        mockJobGroupService(AggregationType.PAGE)

        when:
        serviceUnderTest.reportPageCSIValuesOfLastDay(REPORTING_TIMESTAMP)

        then:
        testSocket.sendDates.size() == 0
    }

    void testReportPageCSIValuesOfLastWeek() {
        given:
        CsiAggregationInterval weeky = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> pmvs = [
                getCsiAggregation(weeky, AggregationType.PAGE, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
        mockPageCsiAggregationService(pmvs)
        mockJobGroupService(AggregationType.PAGE)

        when:
        serviceUnderTest.reportPageCSIValuesOfLastWeek(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME}.weekly.${PAGE_HP_NAME}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_WEEK.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == csiValuePersistedInOsm * 100
    }

    void testReportPageCSIValuesOfLastWeekWithDotsAndWhitespacesInSystemName() {
        given:
        CsiAggregationInterval weeky = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> pmvs = [
                getCsiAggregation(weeky, AggregationType.PAGE, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
        mockPageCsiAggregationService(pmvs)
        mockJobGroupService(AggregationType.PAGE, SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES)

        when:
        serviceUnderTest.reportPageCSIValuesOfLastWeek(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES_REPLACED}.weekly.${PAGE_HP_NAME}.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_WEEK.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == csiValuePersistedInOsm * 100
    }

    void testReportPageCSIValuesOfLastWeekWithoutData() {
        given:
        CsiAggregationInterval weeky = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
        List<CsiAggregation> pmvs = [
                getCsiAggregation(weeky, AggregationType.PAGE, null, REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), '')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
        mockPageCsiAggregationService(pmvs)
        mockJobGroupService(AggregationType.PAGE)

        when:
        serviceUnderTest.reportPageCSIValuesOfLastWeek(REPORTING_TIMESTAMP)

        then:
        testSocket.sendDates.size() == 0
    }

    // test sending shop csi values ////////////////////////////////////////////////////////////////////////////////////////////////////////

    void testReportShopCSIValuesOfLastDay() {
        given:
        CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> smvs = [
                getCsiAggregation(daily, AggregationType.JOB_GROUP, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_DAY.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
        mockShopCsiAggregationService(smvs)
        mockJobGroupService(AggregationType.JOB_GROUP)

        when:
        serviceUnderTest.reportShopCSIValuesOfLastDay(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME}.daily.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == csiValuePersistedInOsm * 100
    }

    void testReportShopCSIValuesOfLastDayWithDotsAndWhitespacesInSystemName() {
        given:
        CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> smvs = [
                getCsiAggregation(daily, AggregationType.JOB_GROUP, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_DAY.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
        mockShopCsiAggregationService(smvs)
        mockJobGroupService(AggregationType.JOB_GROUP, SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES)

        when:
        serviceUnderTest.reportShopCSIValuesOfLastDay(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES_REPLACED}.daily.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_DAY.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == csiValuePersistedInOsm * 100
    }

    void testReportShopCSIValuesOfLastDayWithoutData() {
        given:
        CsiAggregationInterval daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
        List<CsiAggregation> smvs = [
                getCsiAggregation(daily, AggregationType.JOB_GROUP, null, REPORTING_TIMESTAMP_START_OF_DAY.toDate(), '')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_DAY)
        mockShopCsiAggregationService(smvs)
        mockJobGroupService(AggregationType.JOB_GROUP)

        when:
        serviceUnderTest.reportShopCSIValuesOfLastDay(REPORTING_TIMESTAMP)

        then:
        testSocket.sendDates.size() == 0
    }

    void testReportShopCSIValuesOfLastWeek() {
        given:
        CsiAggregationInterval weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> smvs = [
                getCsiAggregation(weekly, AggregationType.JOB_GROUP, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
        mockShopCsiAggregationService(smvs)
        mockJobGroupService(AggregationType.JOB_GROUP)

        when:
        serviceUnderTest.reportShopCSIValuesOfLastWeek(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME}.weekly.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_WEEK.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == csiValuePersistedInOsm * 100
    }

    void testReportShopCSIValuesOfLastWeekWithDotsAndWhitespacesInSystemName() {
        given:
        CsiAggregationInterval weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
        double csiValuePersistedInOsm = 0.78d
        List<CsiAggregation> smvs = [
                getCsiAggregation(weekly, AggregationType.JOB_GROUP, csiValuePersistedInOsm, REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), '1,2,3')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
        mockShopCsiAggregationService(smvs)
        mockJobGroupService(AggregationType.JOB_GROUP, SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES)

        when:
        serviceUnderTest.reportShopCSIValuesOfLastWeek(REPORTING_TIMESTAMP)
        String expectedKey = "${GRAPHITE_PREFIX}.${SYSTEM_NAME_WITH_DOTS_AND_WHITESPACES_REPLACED}.weekly.csi${DELIMITTER}${REPORTING_TIMESTAMP_START_OF_WEEK.toDate()}"

        then:
        testSocket.sendDates.size() == 1
        testSocket.sendDates.keySet()[0] == expectedKey
        testSocket.sendDates[expectedKey] == csiValuePersistedInOsm * 100
    }

    void testReportShopCSIValuesOfLastWeekWithoutData() {
        given:
        CsiAggregationInterval weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
        List<CsiAggregation> smvs = [
                getCsiAggregation(weekly, AggregationType.JOB_GROUP, null, REPORTING_TIMESTAMP_START_OF_WEEK.toDate(), '')]
        //test-specific mocks
        mockCsiAggregationUtilService(REPORTING_TIMESTAMP_START_OF_WEEK)
        mockShopCsiAggregationService(smvs)
        mockJobGroupService(AggregationType.JOB_GROUP)

        when:
        serviceUnderTest.reportShopCSIValuesOfLastWeek(REPORTING_TIMESTAMP)

        then:
        testSocket.sendDates.size() == 0
    }

    //helper methods //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private JobGroup getJobGroup(Measurand measurandForGraphitePath, CachedView cachedView, String jobGroupName) {
        JobGroup jobGroup = new JobGroup(name: jobGroupName)

        GraphiteServer graphiteServer = new GraphiteServer(port: 2003)
        graphiteServer.setServerAdress('monitoring.hh.iteratec.de')

        GraphitePathRawData graphitePathRawData = new GraphitePathRawData(prefix: GRAPHITE_PREFIX, measurand: measurandForGraphitePath, cachedView: cachedView)
        graphiteServer.graphitePathsRawData = [graphitePathRawData]

        jobGroup.resultGraphiteServers = [graphiteServer]

        return jobGroup
    }

    private EventResult getEventResult(CachedView cachedView, int docCompleteTime, Date jobResultDate, JobGroup jobGroup) {
        return new EventResult(cachedView: cachedView, docCompleteTimeInMillisecs: docCompleteTime, jobResultDate: jobResultDate,
                jobGroup: jobGroup, measuredEvent: measuredEvent, page: page, browser: browser, location: location)
    }

    private CsiAggregation getCsiAggregation(CsiAggregationInterval interval, AggregationType aggregationType, Double csByWptDocCompleteInPercent, Date started, String underlyingEventResultsByWptDocComplete) {
        JobGroup jobGroup = JobGroup.build()
        return new CsiAggregation(interval: interval, aggregationType: aggregationType, csByWptDocCompleteInPercent: csByWptDocCompleteInPercent, started: started, underlyingEventResultsByWptDocComplete: underlyingEventResultsByWptDocComplete,
                jobGroup: jobGroup, measuredEvent: measuredEvent, page: page, browser: browser, location: location
        )
    }

    //mocking inner services////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Mocks methods of {@linkplain JobGroupService}.
     * @param csiGroups
     * @param pages
     */
    private void mockJobGroupService(AggregationType measurandForGraphitePath, String jobGroupName = SYSTEM_NAME) {
        def jobGroupService = Stub(JobGroupService)
        jobGroupService.findCSIGroups() >> {

            JobGroup group = JobGroup.build(name: jobGroupName)

            GraphiteServer graphiteServer = new GraphiteServer(port: 2003)
            graphiteServer.setServerAdress('monitoring.hh.iteratec.de')

            GraphitePathCsiData graphitePathCsiData = new GraphitePathCsiData(prefix: GRAPHITE_PREFIX, aggregationType: measurandForGraphitePath)
            graphiteServer.graphitePathsCsiData = [graphitePathCsiData]

            graphiteServer.reportCsiAggregationsToGraphiteServer = true

            group.resultGraphiteServers = [graphiteServer]
            Set<JobGroup> groupSet = [group.save(validate: false)] as Set

            return groupSet
        }
        serviceUnderTest.jobGroupService = jobGroupService
    }
    /**
     * Mocks methods of {@linkplain CsiAggregationUtilService}.
     * @param csiGroups
     * @param pages
     */
    private void mockCsiAggregationUtilService(DateTime toReturnAsStartOfInterval) {
        def csiAggregationUtilService = Stub(CsiAggregationUtilService) {


            resetToStartOfActualInterval(_ as DateTime,_ as Integer) >> {DateTime dateWithinInterval, Integer intervalInMinutes ->
                    return toReturnAsStartOfInterval
            }
            subtractOneInterval(_ as DateTime,_ as Integer) >> {DateTime toSubtractFrom, Integer intervalInMinutes ->
                    return toReturnAsStartOfInterval
            }
        }
        serviceUnderTest.csiAggregationUtilService = csiAggregationUtilService
    }
    /**
     * Mocks methods of {@linkplain PageCsiAggregationService}.
     */
    private void mockEventCsiAggregationService(List<CsiAggregation> toReturnFromGetHourlyCsiAggregations) {
        def eventCsiAggregationService = Stub(EventCsiAggregationService) {
                getHourlyCsiAggregations(_ as Date,_ as Date,_ as MvQueryParams) >> {Date fromDate, Date toDate, MvQueryParams mvQueryParams ->
                    return toReturnFromGetHourlyCsiAggregations
                }
        }
        serviceUnderTest.eventCsiAggregationService = eventCsiAggregationService
    }
    /**
     * Mocks methods of {@linkplain PageCsiAggregationService}.
     */
    private void mockPageCsiAggregationService(List<CsiAggregation> toReturnFromGetOrCalculatePageCsiAggregations) {
        def pageCsiAggregationService = Stub(PageCsiAggregationService) {
            getOrCalculatePageCsiAggregations(_ as Date, _ as Date, _ as CsiAggregationInterval, _ as List) >> {
                Date fromDate, Date toDate, CsiAggregationInterval interval, List<JobGroup> csiGroups ->
                    return toReturnFromGetOrCalculatePageCsiAggregations
            }
        }
        serviceUnderTest.pageCsiAggregationService = pageCsiAggregationService
    }
    /**
     * Mocks methods of {@linkplain JobGroupCsiAggregationService}.
     */
    private void mockShopCsiAggregationService(List<CsiAggregation> toReturnFromGetOrCalculateShopCsiAggregations) {
        def jobGroupCsiAggregationService = Stub(JobGroupCsiAggregationService) {
            getOrCalculateShopCsiAggregations(_ as Date, _ as Date, _ as CsiAggregationInterval, _ as List) >> {
                Date fromDate, Date toDate, CsiAggregationInterval interval, List<JobGroup> csiGroups ->
                    return toReturnFromGetOrCalculateShopCsiAggregations
            }
        }
        serviceUnderTest.jobGroupCsiAggregationService = jobGroupCsiAggregationService
    }
    /**
     * Mocks methods of {@linkplain GraphiteSocketProvider}.
     */
    private void mockGraphiteSocketProvider(GraphiteSocket toReturnFromGetSocket) {
        def graphiteSocketProvider = Stub(DefaultGraphiteSocketProvider) {
            getSocket(_ as GraphiteServer) >> { GraphiteServer server ->
                return toReturnFromGetSocket
            }
        }
        serviceUnderTest.graphiteSocketProvider = graphiteSocketProvider
    }

    class TestSocket implements GraphiteSocket {
        Map<String, Double> sendDates = [:]

        @Override
        void sendDate(GraphitePathName path, double value, Date timestamp)
                throws NullPointerException, GraphiteComunicationFailureException {
            sendDates[path.stringValueOfPathName + DELIMITTER + timestamp.toString()] = value
        }

    }


}
