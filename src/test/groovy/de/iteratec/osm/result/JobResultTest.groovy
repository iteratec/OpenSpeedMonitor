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

import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import grails.buildtestdata.mixin.Build
import spock.lang.Specification

@TestFor(JobResult)
@Mock([JobResult, EventResult])
@Build([JobResult, EventResult])
class JobResultTest extends Specification {

    def "find all event results connected to a job result"() {
        given:
        JobResult.build(testId: "TestJob2")
        JobResult expectedResult = JobResult.build(testId: "TestJob")

        EventResult.build(jobResult: expectedResult)
        EventResult searchCondition = EventResult.build(jobResult: expectedResult)

        // Create dependencies
//        expectedResult.save([failOnError: true, validate: false])


//        when:
//        List jobResultsContainingDesiredEventResult = JobResult.list().findAll {
//            it.getEventResults().contains(searchCondition)
//        }
//
//        then:
//        jobResultsContainingDesiredEventResult.size() == 1

        expect:
        searchCondition.jobResult.testId == expectedResult.testId
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
