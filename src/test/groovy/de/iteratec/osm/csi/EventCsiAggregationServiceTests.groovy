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

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.*
import de.iteratec.osm.util.PerformanceLoggingService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import groovy.mock.interceptor.MockFor
import org.joda.time.DateTime
import org.junit.Test

import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

/**
 * Tests low level-functionality of {@link EventCsiAggregationService}.
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(EventCsiAggregationService)
@Mock([Browser, BrowserAlias, JobGroup, Location, MeasuredEvent, Page, WebPageTestServer, CsiAggregation, CsiAggregationInterval,
        AggregatorType, Location, EventResult, JobResult, Job, CsiAggregationUpdateEvent, ConnectivityProfile])
class EventCsiAggregationServiceTests {

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
    public static final DateTime from = new DateTime(2013, 8, 5, 6, 0, 0)
    public static final DateTime to = new DateTime(2013, 8, 5, 15, 0, 0)
    public static final DateTime inInterval = new DateTime(2013, 8, 5, 8, 0, 0)
    public static final DateTime outOfInterval = new DateTime(2013, 8, 5, 19, 0, 0)

    JobGroup jobGroup1
    JobGroup jobGroup2
    Page page1
    Page page2
    MeasuredEvent measuredEvent1
    MeasuredEvent measuredEvent2
    Browser browser1
    Browser browser2
    Location location1
    Location location2

    EventCsiAggregationService serviceUnderTest
    CsiAggregationInterval hourly
    AggregatorType measuredEventAggr
    ConnectivityProfile connectivityProfile

    void setUp() {
        serviceUnderTest = service
        serviceUnderTest.performanceLoggingService = new PerformanceLoggingService()

        jobGroup1 = new JobGroup(name: jobGroupName1).save(validate: false)
        jobGroup2 = new JobGroup(name: jobGroupName2).save(validate: false)
        page1 = new Page(name: pageName1).save(validate: false)
        page2 = new Page(name: pageName2).save(validate: false)
        measuredEvent1 = new MeasuredEvent(name: eventName1).save(validate: false)
        measuredEvent2 = new MeasuredEvent(name: eventName2).save(validate: false)
        browser1 = new Browser(name: browserName1).save(validate: false)
        browser2 = new Browser(name: browserName2).save(validate: false)
        location1 = new Location(location: locationName1).save(validate: false)
        location2 = new Location(location: locationName2).save(validate: false)
        hourly = new CsiAggregationInterval(intervalInMinutes: CsiAggregationInterval.HOURLY).save(validate: false)
        measuredEventAggr = new AggregatorType(name: AggregatorType.MEASURED_EVENT).save(validate: false)
        connectivityProfile = TestDataUtil.createConnectivityProfile("Conn1")
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
     * Tests querying and calculation of hourly event-{@link CsiAggregation}s without existing {@link EventResult}s.
     * MV's with status {@link Calculated.Not} should have status {@link Calculated.YesNoData} afterwards.
     */
    @Test
    void testGetAllCalculatedHourlyMvs() {

        //creating testdata////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        deleteAllCsiAggregations()
        5.times {
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
        mockCsiAggregationDaoService()
        mockCsiConfigCacheService()
        mockEventResultService()

        //run test////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        List<CsiAggregation> mvs = serviceUnderTest.getAllCalculatedHourlyCas(irrelevantQueryParamsCauseDbQueryIsMocked, from, to)

        //assertions////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        assertThat(mvs.size(), is(10))
        //no additional update-events got written cause no calculation took place in EventCsiAggregationService
        assertThat(CsiAggregationUpdateEvent.list().size(), is(10))

        Integer calcNotMvsExpected = 0
        assertThat(mvs.findAll { !it.isCalculated() }.size(), is(calcNotMvsExpected))
        Integer calcYesNoDataMvsExpected = 5
        assertThat(mvs.findAll { it.isCalculatedWithoutData() }.size(), is(calcYesNoDataMvsExpected))
        Integer calcYesMvsExpected = 5
        assertThat(mvs.findAll { it.isCalculatedWithData() }.size(), is(calcYesMvsExpected))
    }

    //testdata///////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a {@linkplain CsiAggregation} as testdata.
     * {@link CsiAggregationUpdateEvent}s are created respective given params of calculated and withData.
     * @return
     */
    private void createhourlyEventMvWithDefaultTag(boolean calculated, boolean withData) {
        //measured value
        CsiAggregation mv = new CsiAggregation(
                started: inInterval.toDate(),
                interval: hourly,
                aggregator: measuredEventAggr,
                jobGroup: jobGroup1,
                measuredEvent: measuredEvent1,
                page: page1,
                browser: browser1,
                location: location1,
                underlyingEventResultsByWptDocComplete: '').save(failOnError: true)
        //update events
        createUpdateEventsForMv(mv, calculated, withData)
    }

    private void createUpdateEventsForMv(CsiAggregation mv, boolean calculated, boolean withData) {
        CsiAggregationUpdateEvent.UpdateCause cause = CsiAggregationUpdateEvent.UpdateCause.OUTDATED
        if (calculated) {
            cause = CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        }
        if (withData) {
            mv.csByWptDocCompleteInPercent = 0.5
            mv.save(failOnError: true)
        }
        new CsiAggregationUpdateEvent(
                dateOfUpdate: new Date(),
                csiAggregationId: mv.ident(),
                updateCause: cause
        ).save(failOnError: true)
    }
    /**
     * Deletes all {@link CsiAggregation}s in db.
     * @return
     */
    private deleteAllCsiAggregations() {
        CsiAggregation.list()*.delete(flush: true)
    }

    //mocks of inner services///////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Mocks {@linkplain EventCsiAggregationService#csiAggregationDaoService}.
     * Method getMvs(Date fromDate, Date toDate, CsiAggregationInterval interval, AggregatorType aggregator) will return all {@link CsiAggregation}s from db.
     * @param csiGroups
     * @param pages
     */
    private void mockCsiAggregationDaoService() {
        List<CsiAggregation> mvsToReturn = CsiAggregation.list()
        def csiAggregationDaoService = new MockFor(CsiAggregationDaoService, true)
        csiAggregationDaoService.demand.getMvs(1..10000) {
            Date fromDate,
            Date toDate,
            MvQueryParams mvQueryParams,
            CsiAggregationInterval interval,
            AggregatorType aggregator ->
                return mvsToReturn
        }
        serviceUnderTest.csiAggregationDaoService = csiAggregationDaoService.proxyInstance()
    }
    /**
     * Mocks {@linkplain EventCsiAggregationService#eventResultService}
     * @param csiGroups
     * @param pages
     */
    private void mockEventResultService() {
        def eventResultService = new MockFor(EventResultService, true)
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
        serviceUnderTest.eventResultService = eventResultService.proxyInstance()
    }
    /**
     * Mocks {@linkplain EventCsiAggregationService#csiConfigCacheService}
     * @param csiGroups
     * @param pages
     */
    private void mockCsiConfigCacheService() {
        def osmConfigCacheService = new MockFor(OsmConfigCacheService, true)
        Integer timeToExpect = 5
        osmConfigCacheService.demand.getCachedMinDocCompleteTimeInMillisecs(1..10000) {
            Double ageToleranceInHours ->
                return timeToExpect
        }
        osmConfigCacheService.demand.getCachedMaxDocCompleteTimeInMillisecs(1..10000) {
            Double ageToleranceInHours ->
                return timeToExpect
        }
        serviceUnderTest.osmConfigCacheService = osmConfigCacheService.proxyInstance()
    }
}
