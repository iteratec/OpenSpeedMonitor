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

import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import grails.buildtestdata.BuildDomainTest
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Interval
import spock.lang.Specification

class TabularResultEventResultCommandBaseSpec extends Specification implements BuildDomainTest<ConnectivityProfile>,
        ControllerUnitTest<TabularResultPresentationController> {

    TabularResultPresentationController controllerForTest = controller

    void "empty command is invalid"() {
        when: "An empty command is initialized"
        TabularResultListResultsCommand command = new TabularResultListResultsCommand()

        then: "The command doesn't validate, but collections initialized and empty"
        !command.validate()
        command.selectedFolder == []
        command.selectedPages == []
        command.selectedMeasuredEventIds == []
        command.selectedBrowsers == []
        command.selectedLocations == []
    }

    void "binding empty parameters is invalid"() {
        given: "An empty command"
        TabularResultListResultsCommand command = new TabularResultListResultsCommand()

        when: "empty parameters are bound"
        controllerForTest.bindData(command, params)

        then: "the command doesn't validate, but collections are initialized and empty"
        !command.validate()
        command.selectedFolder == []
        command.selectedPages == []
        command.selectedMeasuredEventIds == []
        command.selectedBrowsers == []
        command.selectedLocations == []
    }

    void "binding various valid parameters validates"() {
        given: "A valid time range and data selection"
        setDefaultParams()

        when: "the parameters are bound"
        TabularResultListResultsCommand command = new TabularResultListResultsCommand()
        controllerForTest.bindData(command, params)

        then: "the command validates and the correct data is bound"
        command.validate()
        command.from == new DateTime(2013, 8, 18, 12, 0, DateTimeZone.UTC)
        command.to == new DateTime(2013, 8, 19, 13, 0, DateTimeZone.UTC)

        command.selectedFolder == [1L]
        command.selectedPages == [1L, 5L]
        command.selectedMeasuredEventIds == [7L, 8L, 9L]
        command.selectedBrowsers == [2L]
        command.selectedLocations == [17L]
        command.selectedTimeFrameInterval == 0
        command.selectedConnectivityProfiles == [1L]
        command.selectedCustomConnectivityNames == ["myconn"]
        !command.includeNativeConnectivity

        Interval timeFrame = command.receiveSelectedTimeFrame()
        timeFrame.start == command.from
        timeFrame.end == command.to
    }

    void "if 'to' is less or equal 'from' the command doesn't validate"(String to) {
        given:
        setDefaultParams()
        params.from = "2013-08-09T12:00:01.000Z"
        params.to = to

        when:
        TabularResultListResultsCommand command = new TabularResultListResultsCommand()
        controllerForTest.bindData(command, params)

        then:
        !command.validate()

        where:
        to                         | _
        "2013-08-09T12:00:01.000Z" | _
        "2013-08-09T12:00:00.000Z" | _
    }

    void "the selectedTimeFrameInterval overwrites selected 'from' and 'to' times"() {
        given:
        int oneHourInSeconds = 60 * 60
        setDefaultParams()
        params.selectedTimeFrameInterval = oneHourInSeconds

        when:
        TabularResultListResultsCommand command = new TabularResultListResultsCommand()
        controllerForTest.bindData(command, params)

        then:
        int nowInMillis = DateTime.now().millis
        Interval timeFrame = command.receiveSelectedTimeFrame()

        command.validate()
        (nowInMillis - timeFrame.endMillis) < 100
        ((nowInMillis - oneHourInSeconds * 1000) - timeFrame.startMillis) < 100


    }


    private setDefaultParams() {
        params.from = '2013-08-18T12:00:00.000Z'
        params.to = '2013-08-19T13:00:00.000Z'
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.selectedTimeFrameInterval = 0
        params.selectedConnectivities= [1, "myconn"]
    }

}
