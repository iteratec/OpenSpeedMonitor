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

import de.iteratec.osm.measurement.environment.dao.BrowserDaoService
import de.iteratec.osm.measurement.environment.dao.LocationDaoService
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.measurement.schedule.dao.PageDaoService
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.Interval
import spock.lang.Specification

import static org.junit.Assert.*

/**
 * Test-suite of {@link TabularResultPresentationController}.
 *
 * @author mze
 * @since IT-106
 */
@TestFor(TabularResultPresentationController)
class TabularResultPresentationControllerTests extends Specification {

    TabularResultPresentationController controllerUnderTest

    void setup() {
        // The controller under test:
        controllerUnderTest = controller;

        // Mock relevant services:
        controllerUnderTest.jobGroupDaoService = Stub(JobGroupDaoService)
        controllerUnderTest.pageDaoService = Stub(PageDaoService)
        controllerUnderTest.browserDaoService = Stub(BrowserDaoService)
        controllerUnderTest.locationDaoService = Stub(LocationDaoService)
    }

    /**
     * Test for {@link TabularResultListResultsCommand}.
     */
    void testListResultsCommand_emptyCommandIsInvalid() {
        when:
        TabularResultListResultsCommand out = new TabularResultListResultsCommand();
        then:
        assertFalse(out.validate());
        assertNotNull("Collections are never null", out.selectedFolder)
        assertNotNull("Collections are never null", out.selectedPages)
        assertNotNull("Collections are never null", out.selectedMeasuredEventIds)
        assertNotNull("Collections are never null", out.selectedBrowsers)
        assertNotNull("Collections are never null", out.selectedLocations)
    }

    /**
     * Test for  {@link TabularResultListResultsCommand}.
     */
    void testListResultsCommand_BindFromEmptyRequestArgsIsInvalid() {
        given:
        TabularResultListResultsCommand out = new TabularResultListResultsCommand();
        when:
        controllerUnderTest.bindData(out, params)
        then:
        assertFalse(out.validate())
        assertNotNull("Collections are never null", out.selectedFolder)
        assertNotNull("Collections are never null", out.selectedPages)
        assertNotNull("Collections are never null", out.selectedMeasuredEventIds)
        assertNotNull("Collections are never null", out.selectedBrowsers)
        assertNotNull("Collections are never null", out.selectedLocations)
    }

    /**
     * Test for  {@link TabularResultListResultsCommand}.
     */
    void testListResultsCommand_BindFromValidRequestArgsIsValid_ValuesNearlyDefaults() {
        given:
        // Fill-in request args:
        params.from = '18.08.2013'
        Date expectedDateForFrom = new Date(1376776800000L)

        params.fromHour = '12:00'

        params.to = '19.08.2013'
        Date expectedDateForTo = new Date(1376863200000L)

        params.toHour = '13:00'
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_listResults = 'Show'
        params.selectedAllBrowsers = false
        params.selectedAllLocations = false
        params.selectedAllMeasuredEvents = false
        params.selectedTimeFrameInterval = 0
        params.selectedConnectivityProfiles = []
        params.selectedAllConnectivityProfiles = false
        params.includeNativeConnectivity = false
        params.customConnectivityName = "myconn"
        params.includeCustomConnectivity = true

        when:
        // Create and fill the command:
        TabularResultListResultsCommand out = new TabularResultListResultsCommand()
        controllerUnderTest.bindData(out, params)

        then:
        // Verification:
        assertTrue(out.validate())
        assertNotNull("Collections are never null", out.selectedFolder)
        assertNotNull("Collections are never null", out.selectedPages)
        assertNotNull("Collections are never null", out.selectedMeasuredEventIds)
        assertNotNull("Collections are never null", out.selectedBrowsers)
        assertNotNull("Collections are never null", out.selectedLocations)

        assertEquals(expectedDateForFrom, out.from);
        assertEquals("12:00", out.fromHour);
        assertEquals("13:00", out.toHour);

        assertEquals(1, out.selectedFolder.size())
        assertTrue(out.selectedFolder.contains(1L))

        assertEquals(2, out.selectedPages.size())
        assertTrue(out.selectedPages.contains(1L))
        assertTrue(out.selectedPages.contains(5L))

        assertFalse(out.selectedAllMeasuredEvents as boolean)
        assertEquals(3, out.selectedMeasuredEventIds.size())
        assertTrue(out.selectedMeasuredEventIds.contains(7L))
        assertTrue(out.selectedMeasuredEventIds.contains(8L))
        assertTrue(out.selectedMeasuredEventIds.contains(9L))

        assertFalse(out.selectedAllBrowsers as boolean)
        assertEquals(1, out.selectedBrowsers.size())
        assertTrue(out.selectedBrowsers.contains(2L))

        assertFalse(out.selectedAllLocations as boolean)
        assertEquals(1, out.selectedLocations.size())
        assertTrue(out.selectedLocations.contains(17L))

        // Could we assume the time frame at once?
        Interval timeFrame = out.receiveSelectedTimeFrame();

        DateTime start = timeFrame.getStart();
        DateTime end = timeFrame.getEnd();

        assertEquals(2013, start.getYear())
        assertEquals(8, start.getMonthOfYear())
        assertEquals(18, start.getDayOfMonth())
        assertEquals(12, start.getHourOfDay())
        assertEquals(0, start.getMinuteOfHour())
        assertEquals(0, start.getSecondOfMinute())
        assertEquals(0, start.getMillisOfSecond())

        assertEquals(2013, end.getYear())
        assertEquals(8, end.getMonthOfYear())
        assertEquals(19, end.getDayOfMonth())
        assertEquals(13, end.getHourOfDay())
        assertEquals(0, end.getMinuteOfHour())
        assertEquals(59, end.getSecondOfMinute())
        assertEquals(999, end.getMillisOfSecond())
    }

    /**
     * Test for  {@link TabularResultListResultsCommand}.
     */
    void testListResultsCommand_BindFromValidRequestArgsIsValid_ToDateBeforeFromDate() {
        given:
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '12:00'

        params.to = '17.08.2013'
        params.toHour = '13:00'

        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_listResults = 'Show'
        params.selectedAllBrowsers = false
        params.selectedAllLocations = false
        params.selectedAllMeasuredEvents = false
        params.selectedTimeFrameInterval = 0

        when:
        // Create and fill the command:
        TabularResultListResultsCommand out = new TabularResultListResultsCommand()
        controllerUnderTest.bindData(out, params)

        then:
        // Verification:
        assertFalse(out.validate())

    }

    /**
     * Test for  {@link TabularResultListResultsCommand}.
     */
    void testListResultsCommand_BindFromValidRequestArgsIsValid_EqualDateToHourBeforeFromHour() {
        given:
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '12:00'

        params.to = '18.08.2013'
        params.toHour = '11:00'

        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_listResults = 'Show'
        params.selectedAllBrowsers = false
        params.selectedAllLocations = false
        params.selectedAllMeasuredEvents = false
        params.selectedTimeFrameInterval = 0

        when:
        // Create and fill the command:
        TabularResultListResultsCommand out = new TabularResultListResultsCommand()
        controllerUnderTest.bindData(out, params)

        then:
        // Verification:
        assertFalse(out.validate())
    }

    /**
     * Test for  {@link TabularResultListResultsCommand}.
     */
    void testListResultsCommand_BindFromValidRequestArgsIsValid_EqualDateEqualHourToMinuteBeforeFromMinute() {
        given:
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '12:00'

        params.to = '18.08.2013'
        params.toHour = '12:00'

        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_listResults = 'Show'
        params.selectedAllBrowsers = false
        params.selectedAllLocations = false
        params.selectedAllMeasuredEvents = false
        params.selectedTimeFrameInterval = 0

        when:
        // Create and fill the command:
        TabularResultListResultsCommand out = new TabularResultListResultsCommand()
        controllerUnderTest.bindData(out, params)

        then:
        // Verification:
        assertFalse(out.validate())
    }

}
