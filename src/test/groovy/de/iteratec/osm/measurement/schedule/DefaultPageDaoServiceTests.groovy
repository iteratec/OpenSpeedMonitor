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

package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.Page
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Test-suite for {@link de.iteratec.osm.measurement.schedule.dao.PageDaoService}.
 */
@TestFor(DefaultPageDaoService)
@Mock([Page])
@Build([Page])
class DefaultPageDaoServiceTests extends Specification {

    void "find all returns all pages, also when a new is inserted"() {
        given: "two pages"
        Page page1 = Page.build(name: "Page1")
        Page page2 = Page.build(name: "Page2")

        when: "the service should find all"
        Set<Page> result = service.findAll()

        then: "a set with both is returned"
        result == [page1, page2] as Set

        when: "a third page is added"
        Page page3 = Page.build(name: "Page3")

        then: "it's also returned"
        service.findAll() == [page1, page2, page3] as Set
    }
}
