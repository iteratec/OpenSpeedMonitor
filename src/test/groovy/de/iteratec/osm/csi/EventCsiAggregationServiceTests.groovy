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

import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.MvQueryParams
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Tests low level-functionality of {@link EventCsiAggregationService}.
 */

@TestFor(EventCsiAggregationService)
@Build([JobGroup, Page])
@Mock([JobGroup, Page])
class EventCsiAggregationServiceTests extends Specification{
    Date from
    Date to

    def setup(){
        from = new Date(2017, 2,1)
        to = new Date(2016,2,1)
    }

    void "Hourly aggregation with mv Query: assert failure in case of from > to"(){
        given: "Some query params"
        MvQueryParams mvQueryParams = new MvQueryParams()

        when: "fromDate > toDate"
        service.getHourlyCsiAggregations(from, to, mvQueryParams)

        then: "Expection should be thrown"
        thrown(IllegalArgumentException)
    }

    void "Hourly aggregation with Job Groups and Pages: assert failure in case of from > to"(){
        given: "Some Pages and Job Groups"
        List<JobGroup> jobGroups = [JobGroup.build()]
        List<Page> pages = [ Page.build()]

        when: "fromDate > toDate"
        service.getHourlyCsiAggregations(from, to, jobGroups, pages)

        then: "Expection should be thrown"
        thrown(IllegalArgumentException)
    }
}
