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

import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import grails.buildtestdata.mixin.Build
import spock.lang.Specification
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.csi.transformation.TimeToCsMappingCacheService

import static spock.util.matcher.HamcrestMatchers.closeTo

/**
 * Helper classes for these tests.
 */
class ExpectedCustomerSatisfaction {
    Integer loadTimeInMillisec
    Double customerSatisfaction
}

@TestFor(TimeToCsMappingService)
@Mock([Page, CsiConfiguration, TimeToCsMapping])
@Build([Page, CsiConfiguration])
class TimeToCsMappingServiceTests extends Specification {

    TimeToCsMappingService serviceUnderTest

    static Map<String, List<Double>> frustrations
    static Map<String, List<ExpectedCustomerSatisfaction>> expectedCustomerSatisfactions

    Collection<TimeToCsMapping> mappings
    CsiConfiguration csiConfiguration

    def setup() {
        serviceUnderTest = service

        csiConfigurationSetup()
        frustrationsSetup()
        parseExpectedValues()
    }

    def "an undefined page has no customer satisfaction"() {
        given: "an undefined page"
        Page undefinedPage = Page.build(name: Page.UNDEFINED)

        when: "the customer satisfaction of this page gets calculated"
        Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercent(2000, undefinedPage)

        then: "the customer satisfaction is null"
        csCalculated == null
    }

    def "cs calculation is null when no transformation is possible for a page"() {
        setup:
        serviceUnderTest = Spy(TimeToCsMappingService)
        serviceUnderTest.noTransformationPossibleFor(_, _) >> true

        when:
        Double csCalculatedByMapping = serviceUnderTest.getCustomerSatisfactionInPercent(2000, Page.build())

        then:
        csCalculatedByMapping == null
    }

    def "get customer satisfaction (cs) in percent via mapping"() {
        given: "a page and a loading time to cs mapping"
        Page page = Page.build(name: "HP_entry")
        CsiConfiguration csiConfiguration = CsiConfiguration.build(timeToCsMappings: mappings)

        when: "the cs for a loading time gets calculated via the given mapping"
        def csCalculated = serviceUnderTest.getCustomerSatisfactionInPercentViaMapping(loadTimeInMillisecs, page, csiConfiguration)

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

    def "get customer satisfaction (cs) in percent via the rank"() {
        given: "a page and loading times"
        Page page = Page.build(name: "HP_entry")
        serviceUnderTest.timeToCsMappingCacheService = Stub(TimeToCsMappingCacheService) {
            getCustomerFrustrations(_) >> frustrations.get("HP_entry")
        }

        when: "the cs for a loading time gets calculated via the ranking in the loading times"
        def csCalculated = serviceUnderTest.getCustomerSatisfactionPercentRank(loadTimeInMillisecs, page)

        then: "the cs is close enough to the expected one"
        csCalculated closeTo(expectedCustomerSatisfaction, 0.000001)

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

    def "valid frustrations exist for a page"() {
        given: "a page"
        Page page = Page.build(name: pageName)
        serviceUnderTest.timeToCsMappingCacheService = Stub(TimeToCsMappingCacheService) {
            getCustomerFrustrations(_) >> frustrationList
        }

        when: "the page gets validated and checked for a valid frustrations list"
        def validFrustrationsExists = serviceUnderTest.validFrustrationsExistFor(page)

        then: "the expected result should be obtained"
        validFrustrationsExists == expectedValidation

        where: "valid/invalid pages and valid/invalid frustration lists are used"
        expectedValidation  | pageName          | frustrationList
        true                | "Page"            | [1000, 2000]
        true                | "Page"            | [1000, 1000, 2000]
        false               | "Page"            | [1000, 1000]
        false               | "Page"            | [1000]
        false               | "Page"            | []
        false               | Page.UNDEFINED    | [1000, 2000]
        false               | Page.UNDEFINED    | [1000, 1000, 2000]
        false               | Page.UNDEFINED    | [1000, 1000]
        false               | Page.UNDEFINED    | [1000]
        false               | Page.UNDEFINED    | []
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

    def frustrationsSetup() {
        File csvFile = new File("src/test/resources/CsiData/TimeToCsMapping/customerFrustrations.csv")
        frustrations = [:].withDefault { [] }

        int lineCounter = 0
        new FileInputStream(csvFile).eachLine { line ->
            if (lineCounter >= 1) {
                def tokenized = line.tokenize(';')
                String pageName = tokenized[0]
                Integer customerFrustration = Integer.parseInt(tokenized[1])

                frustrations[pageName].add(customerFrustration)
            }
            lineCounter++
        }
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

