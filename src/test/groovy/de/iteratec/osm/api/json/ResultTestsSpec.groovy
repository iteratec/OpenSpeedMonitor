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

package de.iteratec.osm.api.json

import de.iteratec.osm.api.dto.EventResultDto
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import spock.lang.Specification
/**
 * Test-suite of {@link EventResultDto}.
 *
 * @author mze
 * @since IT-81
 */
@Build([Page, MeasuredEvent, JobResult, EventResult, WebPageTestServer, Location, Job])
class ResultTestsSpec extends Specification implements BuildDataTest {

    private static final String WPT_SERVER_BASE_URL = 'http://my-wpt-server.com/'
    private static final String TEST_ID = 'my-test-id'
    public static final String PAGE_NAME = 'ADS'
    public static final String EVENT_NAME = 'ADS for article 5'
    public static final String LOCATION_IDENTIFIER = 'agent01:IE'
    public static final String BROWSER_IN_LOCATION = 'Firefox7'

    void setupSpec() {
        mockDomains(ConnectivityProfile, Script)
    }

    def "EventResultDto get constructed correctly for an EventResult with customer satisfaction"() {
        given: "an EventResult with customer satisfaction"
        EventResult eventResult = getEventResult(true)

        when: "EventResultDto get constructed from that EventResult"
        EventResultDto dto = new EventResultDto(eventResult);

        then: "dto contains attibutes of wrapped EventResult"
        dto.csiValue == '1,51'
        dto.page == PAGE_NAME
        dto.step == EVENT_NAME
        dto.browser == BROWSER_IN_LOCATION
        dto.location == LOCATION_IDENTIFIER
        dto.detailUrl == "${WPT_SERVER_BASE_URL}result/${TEST_ID}".toString()
    }

    def "EventResultDto get constructed correctly for an EventResult without customer satisfaction"() {
        given: "an EventResult without customer satisfaction"
        EventResult eventResult = getEventResult(false)

        when: "EventResultDto get constructed from that EventResult"
        EventResultDto out = new EventResultDto(eventResult);

        then: "dto contains attibutes of wrapped EventResult"
        out.csiValue == 'not calculated'
        out.page == PAGE_NAME
        out.step == EVENT_NAME
        out.browser == BROWSER_IN_LOCATION
        out.location == LOCATION_IDENTIFIER
        out.detailUrl == "${WPT_SERVER_BASE_URL}result/${TEST_ID}".toString()
    }

    private EventResult getEventResult(boolean hasCustomerSatisfaction) {
        Map eventResultsAttributes = [
                id: 1,
                measuredEvent: MeasuredEvent.build(save: false,
                        testedPage: Page.build(save: false, name: PAGE_NAME),
                        name: EVENT_NAME
                ),
                jobResult: JobResult.build(save: false,
                        locationLocation: 'agent01',
                        locationUniqueIdentifierForServer: LOCATION_IDENTIFIER,
                        locationBrowser: BROWSER_IN_LOCATION,
                        testId: TEST_ID,
                        job: Job.build(save: false,
                                location: Location.build(save: false,
                                        wptServer: WebPageTestServer.build(save: false, baseUrl: WPT_SERVER_BASE_URL)
                                )
                        )
                )
        ]
        if (hasCustomerSatisfaction){
            eventResultsAttributes.csByWptDocCompleteInPercent = 1.5112d
        }
        return EventResult.build(save: false,eventResultsAttributes)
    }

}
