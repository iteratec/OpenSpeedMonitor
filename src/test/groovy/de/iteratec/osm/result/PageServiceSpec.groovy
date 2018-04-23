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
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import de.iteratec.osm.csi.Page
import spock.lang.Specification

@TestFor(PageService)
@Mock([Page])
@Build([Page])
class PageServiceSpec extends Specification implements BuildDataTest {

    PageService serviceUnderTest

    def setup() {
        serviceUnderTest = service
        createPages()
    }

    def "test get page name from step name"(String stepName, String expectedPageName) {
        when: "a step name is given"
        String pageName = serviceUnderTest.getPageNameFromStepName(stepName)

        then: "the page name should be extracted if properly delimited"
        pageName == expectedPageName

        where:
        stepName                            | expectedPageName
        'HP:::BV1_IE_homepage'              | 'HP'
        'MES:::BV1_FF_Moduleinstieg'        | 'MES'
        'XFXHVXGT:::BV1_FF_Moduleinstieg'   | 'XFXHVXGT'
        'BV1_FF_Moduleinstieg'              | Page.UNDEFINED
        'HP::HP_entry'                      | Page.UNDEFINED
        'HP:HP_entry'                       | Page.UNDEFINED
    }

    def "test get page by step name"(String stepName, String expectedPageName) {
        when: "a step name is given"
        Page page = serviceUnderTest.getPageByStepName(stepName)

        then: "the corresponding page should be returned if properly delimited"
        page == Page.findByName(expectedPageName)

        where:
        stepName                            | expectedPageName
        'HP:::BV1_IE_homepage'              | 'HP'
        'MES:::BV1_FF_Moduleinstieg'        | 'MES'
        'XFXHVXGT:::BV1_FF_Moduleinstieg'   | 'XFXHVXGT'
        'BV1_FF_Moduleinstieg'              | Page.UNDEFINED
        'HP::HP_entry'                      | Page.UNDEFINED
        'HP:HP_entry'                       | Page.UNDEFINED
    }

    def "test exclude pagename part"(String stepName, String expectedMeasuredEventName) {
        when: "a step name is given"
        String measuredEventName = serviceUnderTest.excludePagenamePart(stepName)

        then: "the measured event name should be extracted if properly delimited"
        measuredEventName == expectedMeasuredEventName

        where:
        stepName                                |   expectedMeasuredEventName
        'stepName'                              |   'stepName'
        'pageName:::stepName'                   |   'stepName'
        'HP:::LH_Homepage'                      |   'LH_Homepage'
        'HP::LH_Homepage'                       |   'HP::LH_Homepage'
        'HP:LH_Homepage'                        |   'HP:LH_Homepage'
    }

    def createPages() {
        List pageNames = ['HP', 'MES', 'SE', 'ADS', 'WKBS', 'WK', 'XFXHVXGT', Page.UNDEFINED]

        pageNames.each { name ->
            Page.build(name: name)
        }
    }
}
