/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

package de.iteratec.osm.csi

import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

import static spock.util.matcher.HamcrestMatchers.closeTo
/**
 * Helper classes for these tests.
 */
class ExpectedCustomerSatisfaction {
    Integer loadTimeInMillisec
    Double customerSatisfaction
}

@Build([Page, CsiConfiguration])
class TimeToCsMappingServiceTests extends Specification implements BuildDataTest,
        ServiceUnitTest<TimeToCsMappingService> {

    TimeToCsMappingService serviceUnderTest

    static Map<String, List<ExpectedCustomerSatisfaction>> expectedCustomerSatisfactions

    Collection<TimeToCsMapping> mappings
    CsiConfiguration csiConfiguration

    def setup() {
        serviceUnderTest = service

        csiConfigurationSetup()
        parseExpectedValues()
    }

    void setupSpec() {
        mockDomains(Page, CsiConfiguration, TimeToCsMapping)
    }

    def "an undefined page has no customer satisfaction"() {
        given: "an undefined page"
        Page undefinedPage = Page.build(name: Page.UNDEFINED)

        when: "the customer satisfaction of this page gets calculated"
        Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercent(2000, undefinedPage)

        then: "the customer satisfaction is null"
        csCalculated == null
    }

    def "cs calculation is null when page is null"() {
        given: "a valid csiConfiguration"
        CsiConfiguration csiConfiguration = CsiConfiguration.build(timeToCsMappings: mappings)

        when: "the customer satisfaction should be computed, but the page is null"
        Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercent(2000, null, csiConfiguration)

        then: "the calculated customer satisfaction is null"
        csCalculated == null
    }

    def "cs calculation is null when the CSI configuration is null"() {
        given: "a page"
        Page page = Page.build(name: "HP_entry")

        when: "the cs for a loading time gets calculated, but the mapping is null"
        Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercent(2000, page, null)

        then: "the calculated customer satisfaction is null"
        csCalculated == null
    }

    def "cs calculation is null when the CSI configuration doesn't affect the page"() {
        given: "a page and a loading time to cs mapping"
        Page page = Page.build(name: "HP_entry_special")
        CsiConfiguration csiConfiguration = CsiConfiguration.build(timeToCsMappings: mappings)

        when: "the cs for a loading time gets calculated, but the mapping is null"
        Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercent(2000, page, csiConfiguration)

        then: "the calculated customer satisfaction is null"
        csCalculated == null
    }


    def "get customer satisfaction (cs) in percent via mapping"() {
        given: "a page and a loading time to cs mapping"
        Page page = Page.build(name: "HP_entry")
        CsiConfiguration csiConfiguration = CsiConfiguration.build(timeToCsMappings: mappings)

        when: "the cs for a loading time gets calculated via the given mapping"
        Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercent(loadTimeInMillisecs, page, csiConfiguration)

        then: "the cs is close enough to the expected one"
        csCalculated closeTo(expectedCustomerSatisfaction, 1.2)

        where:
        loadTimeInMillisecs | expectedCustomerSatisfaction
        4036                | 50d
        1980                | 73.5d
        3793                | 50d
        6675                | 15.5d
        2541                | 66.33165829000001d
        2173                | 73.5d
        2178                | 73.5d
        2047                | 73.5d
        2272                | 73d
        6630                | 17d
    }

    def "isValid is true if page is neither null nor undefined"() {
        given:
        Page page = name ? Page.build(name: name) : null

        when:
        def state = serviceUnderTest.isValid(page)

        then:
        state == expectedState

        where:
        name            | expectedState
        "Page"          | true
        Page.UNDEFINED  | false
        null            | false
    }

    def csiConfigurationSetup() {
        File csvFile = new File("src/test/resources/CsiData/TimeToCsMapping/timeToCsMappings.csv")
        mappings = []

        int lineCounter = 0
        new FileInputStream(csvFile).eachLine { line ->
            if (lineCounter >= 1) {
                List tokenized = line.tokenize(';')
                String pageName = tokenized[0]
                Integer loadTimeInMilliSecs = Integer.parseInt(tokenized[1])
                Double customerSatisfaction = Double.parseDouble(tokenized[2])

                mappings.add(
                        new TimeToCsMapping(
                                page: new Page(name: pageName),
                                loadTimeInMilliSecs: loadTimeInMilliSecs,
                                customerSatisfaction: customerSatisfaction,
                                mappingVersion: 1
                        ).save(failOnError: true))
            }
            lineCounter++
        }
        csiConfiguration = new CsiConfiguration(label: "TestCsi",
                description: "For Testing",
                timeToCsMappings: mappings)
    }

    def parseExpectedValues() {
        File csvFile = new File("src/test/resources/CsiData/TimeToCsMapping/expectedValues.csv")
        expectedCustomerSatisfactions = [:].withDefault { [] }

        int lineCounter = 0
        new FileInputStream(csvFile).eachLine { line ->
            if (lineCounter >= 1) {
                def tokenized = line.tokenize(';')
                String pageName = tokenized[0]
                Integer loadTimeInMillisec = Integer.parseInt(tokenized[1])
                Double customerSatisfaction = Double.parseDouble(tokenized[2])

                expectedCustomerSatisfactions[pageName].add(
                        new ExpectedCustomerSatisfaction(
                                loadTimeInMillisec: loadTimeInMillisec,
                                customerSatisfaction: customerSatisfaction
                        )
                )
            }
            lineCounter++
        }
    }
}

