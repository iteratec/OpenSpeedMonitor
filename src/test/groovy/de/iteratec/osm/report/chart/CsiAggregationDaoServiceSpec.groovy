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

import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification


@TestMixin(GrailsUnitTestMixin)
@TestFor(CsiAggregationDaoService)
@Mock([CsiAggregationUpdateEvent, CsiAggregation, CsiAggregationInterval])
@Build([CsiAggregation])
class CsiAggregationDaoServiceSpec extends Specification {

    CsiAggregationDaoService serviceUnderTest
    CsiAggregationInterval weeklyInterval, dailyInterval, hourlyInterval

    def doWithSpring = {
        csiAggregationUtilService(CsiAggregationUtilService)
    }

    def setup() {

        serviceUnderTest = service

        //test data common to all tests
        weeklyInterval = new CsiAggregationInterval(name: 'weekly', intervalInMinutes: CsiAggregationInterval.WEEKLY).save(failOnError: true)
        dailyInterval = new CsiAggregationInterval(name: 'daily', intervalInMinutes: CsiAggregationInterval.DAILY).save(failOnError: true)
        hourlyInterval = new CsiAggregationInterval(name: 'hourly', intervalInMinutes: CsiAggregationInterval.HOURLY).save(failOnError: true)
    }

    def "test getUpdateEvents"() {
        given:
        Date timestamp1 = new DateTime(2014, 6, 25, 0, 1, 0).toDate()
        Date timestamp2 = new DateTime(2014, 6, 25, 0, 2, 0).toDate()
        Date timestamp3 = new DateTime(2014, 6, 25, 0, 3, 0).toDate()
        new CsiAggregationUpdateEvent( dateOfUpdate: timestamp1, csiAggregationId: 1, updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED ).save(failOnError: true)
        new CsiAggregationUpdateEvent( dateOfUpdate: timestamp2, csiAggregationId: 1, updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED ).save(failOnError: true)
        new CsiAggregationUpdateEvent( dateOfUpdate: timestamp3, csiAggregationId: 1, updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED ).save(failOnError: true)
        List<CsiAggregationUpdateEvent> updateEvents = CsiAggregationUpdateEvent.list()
        List<CsiAggregationUpdateEvent> updateEventsThatRequireRecalculation = updateEvents.findAll {
            it.updateCause.requiresRecalculation
        }
        List<CsiAggregationUpdateEvent> updateEventsThatDoesNotRequireRecalculation = updateEvents.findAll {
            !it.updateCause.requiresRecalculation
        }

        expect:
        updateEventsThatRequireRecalculation.size() == 1
        updateEventsThatDoesNotRequireRecalculation.size() == 2
    }

    def "test getUpdateEvents for specific csiAggregations"() {
        given:
        Date date_20140928 = new DateTime(2014, 9, 28, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140929 = new DateTime(2014, 9, 29, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140930 = new DateTime(2014, 9, 30, 0, 0, 0, DateTimeZone.UTC).toDate()

        def mvWithFiveEvents = createAndSaveCsiAggregation(dailyInterval, AggregationType.PAGE, false, date_20140928)
        createUpdateEventForCsiAggregation(mvWithFiveEvents, true, false)
        createUpdateEventForCsiAggregation(mvWithFiveEvents, false, false)
        createUpdateEventForCsiAggregation(mvWithFiveEvents, false, false)
        createUpdateEventForCsiAggregation(mvWithFiveEvents, false, false)
        createUpdateEventForCsiAggregation(mvWithFiveEvents, false, false)
        def mvWithoutEvents = createAndSaveCsiAggregation(dailyInterval, AggregationType.PAGE, false, date_20140929)
        def mvWithOneEvent = createAndSaveCsiAggregation(dailyInterval, AggregationType.PAGE, false, date_20140930)
        createUpdateEventForCsiAggregation(mvWithOneEvent, false, false)

        expect:
        serviceUnderTest.getUpdateEvents(mvWithFiveEvents*.id).size() == 5
        serviceUnderTest.getUpdateEvents(mvWithoutEvents*.id).size() == 0
        serviceUnderTest.getUpdateEvents(mvWithOneEvent*.id).size() == 1
        serviceUnderTest.getUpdateEvents([mvWithOneEvent.id, mvWithFiveEvents.id, mvWithoutEvents.id]).size() == 6
    }

    def "test getUpdateEvents with an empty list"(){
        given: "some data that hopefully won't change our result"
        Date date_20140928 = new DateTime(2014, 9, 28, 0, 0, 0, DateTimeZone.UTC).toDate()
        def mvWithFiveEvents = createAndSaveCsiAggregation(dailyInterval, AggregationType.PAGE, false, date_20140928)
        createUpdateEventForCsiAggregation(mvWithFiveEvents, true, false)
        createUpdateEventForCsiAggregation(mvWithFiveEvents, false, false)

        expect:
        serviceUnderTest.getUpdateEvents([]).size() == 0
    }

    def 'test getOpenHourlyEventCsiAggregationsWhosIntervalExpiredForAtLeast with non expired'() {
        given:
        Date date_20140928_0800 = new DateTime(2014, 9, 28, 8, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140928_0900 = new DateTime(2014, 9, 28, 9, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140928_1000 = new DateTime(2014, 9, 28, 10, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(hourlyInterval, AggregationType.MEASURED_EVENT, false, date_20140928_0800)
        createAndSaveCsiAggregation(hourlyInterval, AggregationType.MEASURED_EVENT, false, date_20140928_0900)
        createAndSaveCsiAggregation(hourlyInterval, AggregationType.MEASURED_EVENT, false, date_20140928_1000)
        List<CsiAggregation> openAndExpired

        when:
        mockCsiAggregationUtilService(new DateTime(2014, 9, 28, 0, 0, 0, DateTimeZone.UTC))
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(0)
        then:
        openAndExpired.size() == 0
    }

    def 'test getOpenHourlyEventCsiAggregationsWhosIntervalExpiredForAtLeast with some expired'() {
        given:
        Date date_20140928_0800 = new DateTime(2014, 9, 28, 8, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140928_0900 = new DateTime(2014, 9, 28, 9, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140928_1000 = new DateTime(2014, 9, 28, 10, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(hourlyInterval, AggregationType.MEASURED_EVENT, false, date_20140928_0800)
        createAndSaveCsiAggregation(hourlyInterval, AggregationType.MEASURED_EVENT, false, date_20140928_0900)
        createAndSaveCsiAggregation(hourlyInterval, AggregationType.MEASURED_EVENT, false, date_20140928_1000)
        List<CsiAggregation> openAndExpired
        mockCsiAggregationUtilService(new DateTime(2014, 9, 28, 9, 15, 0, DateTimeZone.UTC))

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(45)
        then:
        openAndExpired.size() == 0

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(16)
        then:
        openAndExpired.size() == 0

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(15)
        then:
        openAndExpired.size() == 1
        openAndExpired[0].started == date_20140928_0800

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(0)
        then:
        openAndExpired.size() == 1
        openAndExpired[0].started == date_20140928_0800
    }

    def 'test getOpenHourlyEventCsiAggregationsWhosIntervalExpiredForAtLeast with all expired'() {
        given:
        Date date_20140928_0800 = new DateTime(2014, 9, 28, 8, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140928_0900 = new DateTime(2014, 9, 28, 9, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140928_1000 = new DateTime(2014, 9, 28, 10, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(hourlyInterval, AggregationType.MEASURED_EVENT, false, date_20140928_0800)
        createAndSaveCsiAggregation(hourlyInterval, AggregationType.MEASURED_EVENT, false, date_20140928_0900)
        createAndSaveCsiAggregation(hourlyInterval, AggregationType.MEASURED_EVENT, false, date_20140928_1000)
        List<CsiAggregation> openAndExpired

        when:
        mockCsiAggregationUtilService(new DateTime(2014, 9, 28, 11, 30, 0, DateTimeZone.UTC))
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(30)

        then:
        openAndExpired.size() == 3
    }

    def "test GetOpenDailyPageCsiAggregationsWhosIntervalExpiredForAtLeast with non expired"() {
        given:
        Date date_20140928 = new DateTime(2014, 9, 28, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140929 = new DateTime(2014, 9, 29, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140930 = new DateTime(2014, 9, 30, 0, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(dailyInterval, AggregationType.PAGE, false, date_20140928)
        createAndSaveCsiAggregation(dailyInterval, AggregationType.PAGE, false, date_20140929)
        createAndSaveCsiAggregation(dailyInterval, AggregationType.PAGE, false, date_20140930)
        List<CsiAggregation> openAndExpired

        when:
        mockCsiAggregationUtilService(new DateTime(2014, 9, 26, 0, 0, 0, DateTimeZone.UTC))
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(0)

        then:
        openAndExpired.size() == 0
    }

    def "test GetOpenDailyPageCsiAggregationsWhosIntervalExpiredForAtLeast with some expired"() {
        given:
        Date date_20140928 = new DateTime(2014, 9, 28, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140929 = new DateTime(2014, 9, 29, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140930 = new DateTime(2014, 9, 30, 0, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(dailyInterval, AggregationType.PAGE, false, date_20140928)
        createAndSaveCsiAggregation(dailyInterval, AggregationType.PAGE, false, date_20140929)
        createAndSaveCsiAggregation(dailyInterval, AggregationType.PAGE, false, date_20140930)
        List<CsiAggregation> openAndExpired
        mockCsiAggregationUtilService(new DateTime(2014, 9, 29, 5, 1, 0, DateTimeZone.UTC))

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(500)
        then:
        openAndExpired.size() == 0

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(302)
        then:
        openAndExpired.size() == 0

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(301)
        then:
        openAndExpired.size() == 1
        openAndExpired[0].started == date_20140928

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(0)
        then:
        openAndExpired.size() == 1
        openAndExpired[0].started == date_20140928
    }

    def "test GetOpenDailyPageCsiAggregationsWhosIntervalExpiredForAtLeast with all expired"() {
        given:
        Date date_20140928 = new DateTime(2014, 9, 28, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140929 = new DateTime(2014, 9, 29, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140930 = new DateTime(2014, 9, 30, 0, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(dailyInterval, AggregationType.PAGE, false, date_20140928)
        createAndSaveCsiAggregation(dailyInterval, AggregationType.PAGE, false, date_20140929)
        createAndSaveCsiAggregation(dailyInterval, AggregationType.PAGE, false, date_20140930)
        List<CsiAggregation> openAndExpired

        when:
        mockCsiAggregationUtilService(new DateTime(2014, 10, 1, 5, 30, 0, DateTimeZone.UTC))
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(300)
        then:
        openAndExpired.size() == 3
    }

    def "testGetOpenWeeklyPageCsiAggregationsWhosIntervalExpiredForAtLeast with non expired"() {

        //create test-specific data
        Date date_20140905 = new DateTime(2014, 9, 5, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140912 = new DateTime(2014, 9, 12, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140919 = new DateTime(2014, 9, 19, 0, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.PAGE, false, date_20140905)
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.PAGE, false, date_20140912)
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.PAGE, false, date_20140919)
        List<CsiAggregation> openAndExpired

        when:
        mockCsiAggregationUtilService(new DateTime(2014, 9, 11, 23, 59, 59, DateTimeZone.UTC))
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(0)
        then:
        openAndExpired.size() == 0
    }

    def "testGetOpenWeeklyPageCsiAggregationsWhosIntervalExpiredForAtLeast with some expired"() {

        //create test-specific data
        Date date_20140905 = new DateTime(2014, 9, 5, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140912 = new DateTime(2014, 9, 12, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140919 = new DateTime(2014, 9, 19, 0, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.PAGE, false, date_20140905)
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.PAGE, false, date_20140912)
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.PAGE, false, date_20140919)
        List<CsiAggregation> openAndExpired
        mockCsiAggregationUtilService(new DateTime(2014, 9, 12, 5, 0, 0, DateTimeZone.UTC))

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(1000)
        then:
        openAndExpired.size() == 0

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(301)
        then:
        openAndExpired.size() == 0

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(300)
        then:
        openAndExpired.size() == 1
        openAndExpired[0].started == date_20140905

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(100)
        then:
        openAndExpired.size() == 1
        openAndExpired[0].started == date_20140905
    }

    def "testGetOpenWeeklyPageCsiAggregationsWhosIntervalExpiredForAtLeast with all expired"() {
        given:
        Date date_20140905 = new DateTime(2014, 9, 5, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140912 = new DateTime(2014, 9, 12, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140919 = new DateTime(2014, 9, 19, 0, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.PAGE, false, date_20140905)
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.PAGE, false, date_20140912)
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.PAGE, false, date_20140919)
        List<CsiAggregation> openAndExpired

        when:
        mockCsiAggregationUtilService(new DateTime(2014, 9, 27, 5, 30, 0, DateTimeZone.UTC))
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(300)
        then:
        openAndExpired.size() == 3
    }

    def "testGetDailyOpenShopCsiAggregationsWhosIntervalExpiredForAtLeast with non expired"() {
        given:
        Date date_20140928 = new DateTime(2014, 9, 28, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140929 = new DateTime(2014, 9, 29, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140930 = new DateTime(2014, 9, 30, 0, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(dailyInterval, AggregationType.JOB_GROUP, false, date_20140928)
        createAndSaveCsiAggregation(dailyInterval, AggregationType.JOB_GROUP, false, date_20140929)
        createAndSaveCsiAggregation(dailyInterval, AggregationType.JOB_GROUP, false, date_20140930)
        List<CsiAggregation> openAndExpired

        when:
        mockCsiAggregationUtilService(new DateTime(2014, 9, 26, 0, 0, 0, DateTimeZone.UTC))
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(0)
        then:
        openAndExpired.size() == 0

    }

    def "testGetDailyOpenShopCsiAggregationsWhosIntervalExpiredForAtLeast with some expired"() {
        given:
        Date date_20140928 = new DateTime(2014, 9, 28, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140929 = new DateTime(2014, 9, 29, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140930 = new DateTime(2014, 9, 30, 0, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(dailyInterval, AggregationType.JOB_GROUP, false, date_20140928)
        createAndSaveCsiAggregation(dailyInterval, AggregationType.JOB_GROUP, false, date_20140929)
        createAndSaveCsiAggregation(dailyInterval, AggregationType.JOB_GROUP, false, date_20140930)
        List<CsiAggregation> openAndExpired
        mockCsiAggregationUtilService(new DateTime(2014, 9, 29, 5, 1, 0, DateTimeZone.UTC))

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(500)
        then:
        openAndExpired.size() == 0

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(302)
        then:
        openAndExpired.size() == 0

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(301)
        then:
        openAndExpired.size() == 1
        openAndExpired[0].started == date_20140928

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(0)
        then:
        openAndExpired.size() == 1
        openAndExpired[0].started == date_20140928
    }

    def "testGetDailyOpenShopCsiAggregationsWhosIntervalExpiredForAtLeast with al expired"() {
        given:
        Date date_20140928 = new DateTime(2014, 9, 28, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140929 = new DateTime(2014, 9, 29, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140930 = new DateTime(2014, 9, 30, 0, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(dailyInterval, AggregationType.JOB_GROUP, false, date_20140928)
        createAndSaveCsiAggregation(dailyInterval, AggregationType.JOB_GROUP, false, date_20140929)
        createAndSaveCsiAggregation(dailyInterval, AggregationType.JOB_GROUP, false, date_20140930)
        List<CsiAggregation> openAndExpired

        when:
        mockCsiAggregationUtilService(new DateTime(2014, 10, 1, 5, 30, 0, DateTimeZone.UTC))
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(300)
        then:
        openAndExpired.size() == 3
    }

    def "testGetOpenWeeklyShopCsiAggregationsWhosIntervalExpiredForAtLeast with none expired"() {
        given:
        Date date_20140905 = new DateTime(2014, 9, 5, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140912 = new DateTime(2014, 9, 12, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140919 = new DateTime(2014, 9, 19, 0, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.JOB_GROUP, false, date_20140905)
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.JOB_GROUP, false, date_20140912)
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.JOB_GROUP, false, date_20140919)
        List<CsiAggregation> openAndExpired
        mockCsiAggregationUtilService(new DateTime(2014, 9, 11, 23, 59, 59, DateTimeZone.UTC))

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(0)
        then:
        openAndExpired.size() == 0
    }

    def "testGetOpenWeeklyShopCsiAggregationsWhosIntervalExpiredForAtLeast with some expired"() {
        given:
        Date date_20140905 = new DateTime(2014, 9, 5, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140912 = new DateTime(2014, 9, 12, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140919 = new DateTime(2014, 9, 19, 0, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.JOB_GROUP, false, date_20140905)
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.JOB_GROUP, false, date_20140912)
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.JOB_GROUP, false, date_20140919)
        List<CsiAggregation> openAndExpired
        mockCsiAggregationUtilService(new DateTime(2014, 9, 12, 5, 0, 0, DateTimeZone.UTC))

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(1000)
        then:
        openAndExpired.size() == 0

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(301)
        then:
        openAndExpired.size() == 0

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(300)
        then:
        openAndExpired.size() == 1
        openAndExpired[0].started == date_20140905

        when:
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(100)
        then:
        openAndExpired.size() == 1
        openAndExpired[0].started == date_20140905
    }

    def "testGetOpenWeeklyShopCsiAggregationsWhosIntervalExpiredForAtLeast with all expired"() {
        given:
        Date date_20140905 = new DateTime(2014, 9, 5, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140912 = new DateTime(2014, 9, 12, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140919 = new DateTime(2014, 9, 19, 0, 0, 0, DateTimeZone.UTC).toDate()
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.JOB_GROUP, false, date_20140905)
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.JOB_GROUP, false, date_20140912)
        createAndSaveCsiAggregation(weeklyInterval, AggregationType.JOB_GROUP, false, date_20140919)
        List<CsiAggregation> openAndExpired

        when:
        mockCsiAggregationUtilService(new DateTime(2014, 9, 27, 5, 30, 0, DateTimeZone.UTC))
        openAndExpired = serviceUnderTest.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(300)
        then:
        openAndExpired.size() == 3
    }

    def "test getLatestUpdateEvent"() {
        given:
        Date date_20140905 = new DateTime(2014, 9, 5, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140912 = new DateTime(2014, 9, 12, 0, 0, 0, DateTimeZone.UTC).toDate()
        Date date_20140919 = new DateTime(2014, 9, 19, 0, 0, 0, DateTimeZone.UTC).toDate()
        CsiAggregation csiAggregation = createAndSaveCsiAggregation(weeklyInterval, AggregationType.JOB_GROUP, false, date_20140905)
        new CsiAggregationUpdateEvent( dateOfUpdate: date_20140905,
                csiAggregationId: csiAggregation.id,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        ).save(failOnError: true)

        new CsiAggregationUpdateEvent(
                dateOfUpdate: date_20140912,
                csiAggregationId: csiAggregation.id,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        ).save(failOnError: true)

        new CsiAggregationUpdateEvent(
                dateOfUpdate: date_20140919,
                csiAggregationId: csiAggregation.id,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED
        ).save(failOnError: true)

        when:
        CsiAggregationUpdateEvent updateEvent = serviceUnderTest.getLatestUpdateEvent(csiAggregation.id)

        then:
        date_20140919 == updateEvent.dateOfUpdate
        CsiAggregationUpdateEvent.UpdateCause.OUTDATED == updateEvent.updateCause
    }

    private CsiAggregation createAndSaveCsiAggregation(CsiAggregationInterval interval, AggregationType aggregationType, boolean closedAndCalculated, Date started) {
        return CsiAggregation.build(interval: interval, aggregationType: aggregationType, closedAndCalculated: closedAndCalculated, started: started)
    }

    private void createUpdateEventForCsiAggregation(CsiAggregation csiAggregation, boolean calculated, boolean withData) {
        CsiAggregationUpdateEvent.UpdateCause cause = CsiAggregationUpdateEvent.UpdateCause.OUTDATED
        if (calculated) {
            cause = CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        }
        if (withData) {
            csiAggregation.csByWptDocCompleteInPercent = 0.5
            csiAggregation.save(failOnError: true)
        }
        new CsiAggregationUpdateEvent(
                dateOfUpdate: new Date(),
                csiAggregationId: csiAggregation.ident(),
                updateCause: cause
        ).save(failOnError: true)
    }

    private void mockCsiAggregationUtilService(DateTime utcNowToReturn) {
        CsiAggregationUtilService mvuService = Spy(CsiAggregationUtilService)//still need some methods, so we use a spy
        mvuService.getNowInUtc() >> {return utcNowToReturn }
        serviceUnderTest.csiAggregationUtilService = mvuService
    }
}
