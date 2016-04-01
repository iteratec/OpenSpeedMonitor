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



import grails.test.mixin.*
import org.joda.time.DateTime
import org.joda.time.Interval
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(DetailAnalysisDashboardController)
class DetailAnalysisDashboardControllerSpec extends Specification{
    private DetailAnalysisDashboardCommand dashboardCommand
    private DetailAnalysisDashboardController controllerUnderTest

    void "setup"() {
        controllerUnderTest = controller
        dashboardCommand = new DetailAnalysisDashboardCommand()
    }


    void "empty dashboardCommand is not valid"() {
        expect:
        !dashboardCommand.validate()

        // Collections are never null
        dashboardCommand.selectedFolder != null
        dashboardCommand.selectedPages != null
        dashboardCommand.selectedMeasuredEventIds != null
        dashboardCommand.selectedBrowsers != null
        dashboardCommand.selectedLocations != null
        dashboardCommand.selectedLocations != null
    }

    void "dashboardCommand bind from empty requestArgs is invalid "() {
        when:
        controllerUnderTest.bindData(dashboardCommand, params)

        then:
        !dashboardCommand.validate()
    }

    void "dashboardCommand bind from valid requestArgs is valid"()
    {
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
        params.selectedAllBrowsers = false
        params.selectedAllLocations = false
        params.selectedAllMeasuredEvents = false
        params.selectedTimeFrameInterval = 0
        params.includeNativeConnectivity = false
        params.includeCustomConnectivity = true

        when:
        controllerUnderTest.bindData(dashboardCommand, params)

        then:
        dashboardCommand.validate()

        dashboardCommand.from == expectedDateForFrom
        dashboardCommand.fromHour == "12:00"
        dashboardCommand.toHour == "13:00"

        dashboardCommand.selectedFolder.size() == 1
        dashboardCommand.selectedFolder.contains(1L)

        dashboardCommand.selectedPages.size() == 2
        dashboardCommand.selectedPages.contains(1L)
        dashboardCommand.selectedPages.contains(5L)

        !dashboardCommand.selectedAllMeasuredEvents
        dashboardCommand.selectedMeasuredEventIds.size() == 3
        dashboardCommand.selectedMeasuredEventIds.contains(7L)
        dashboardCommand.selectedMeasuredEventIds.contains(8L)
        dashboardCommand.selectedMeasuredEventIds.contains(9L)

        !dashboardCommand.selectedAllBrowsers
        dashboardCommand.selectedBrowsers.size() == 1
        dashboardCommand.selectedBrowsers.contains(2L)

        !dashboardCommand.selectedAllLocations
        dashboardCommand.selectedLocations.size() == 1
        dashboardCommand.selectedLocations.contains(17L)

        // Could we assume the time frame at once?
        Interval timeFrame = dashboardCommand.selectedTimeFrame;

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

    void "dashboardCommand bind from invalid requestArgs is invalid"()
    {
        given:
        params.from = '18.08.2013'
        params.fromHour = '12:00'
        params.to = '19.08.2013'
        params.toHour = '13:00'
        params.selectedFolder = '1'
        params.selectedPages = ['NOT-A-NUMBER']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = 'UGLY'

        when:
        controllerUnderTest.bindData(dashboardCommand, params)

        then: "command is invalid and there are no elements in collections"
        !dashboardCommand.validate()
        dashboardCommand.selectedPages.isEmpty()
        dashboardCommand.selectedLocations.isEmpty()
    }

}
