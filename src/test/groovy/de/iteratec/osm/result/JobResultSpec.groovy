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

import grails.buildtestdata.BuildDataTest
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import grails.buildtestdata.mixin.Build
import spock.lang.Specification

@TestFor(JobResult)
@Mock([JobResult, EventResult])
@Build([JobResult, EventResult])
class JobResultSpec extends Specification implements BuildDataTest {

    def "find all job results connected to an event result"() {
        given: "two event results and linked to two different job results"
        EventResult.build(jobResult: JobResult.build(testId: "Job Result not to find"))

        JobResult expectedJobResult = JobResult.build(testId: "Job Result to find")
        EventResult eventResultToSearchFor = EventResult.build(jobResult: expectedJobResult)

        when: "one searches for all event results linked to a particular job result"
        List eventResultsLinkedToExpectedJobResult = JobResult.list().findAll {
            it.getEventResults().contains(eventResultToSearchFor)
        }

        then: "one gets only the event results which are linked to that particular job result"
        eventResultsLinkedToExpectedJobResult.size() == 1
        eventResultToSearchFor.jobResult.testId == expectedJobResult.testId
    }

    def "get the correct test details url when the WPT server base url ends with a slash"() {
        given:
        JobResult jobResult = JobResult.build(wptServerBaseurl: 'https://wpt.example.com/', testId: "TestJob2")

        expect:
        jobResult.tryToGetTestsDetailsURL().toString() == 'https://wpt.example.com/result/TestJob2'
    }

    def "get the correct test details url when the WPT server base url ends without a slash"() {
        given:
        JobResult jobResult = JobResult.build(wptServerBaseurl: 'https://wpt.example.com', testId: "TestJob3")

        expect:
        jobResult.tryToGetTestsDetailsURL().toString() == 'https://wpt.example.com/result/TestJob3'
    }

    def "test details url is null when noe WPT server base url is given"() {
        given:
        JobResult jobResult = JobResult.build()

        expect:
        jobResult.tryToGetTestsDetailsURL() == null
    }
}
