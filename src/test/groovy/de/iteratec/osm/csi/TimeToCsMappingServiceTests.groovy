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

import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.lessThan
import static org.junit.Assert.assertThat

/**
 * Helper classes for these tests.
 */
class ExpectedCustomerSatisfaction{
    Integer loadTimeInMillisec
    Double customerSatisfaction
}
class DeviationToBuildMeanFrom {
    Double deviation = 0.0d
    Integer runs = 0i
}

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(TimeToCsMappingService)
@Mock([Page, TimeToCsMapping])
class TimeToCsMappingServiceTests {

    TimeToCsMappingService serviceUnderTest
    ServiceMocker mocker

    static final String FRUSTRATIONS_CSV = 'customerFrustrations.csv'
    static final String MAPPINGS_CSV = 'timeToCsMappings.csv'
    static final String EXPECTED_CUSTOMER_SATISFACTION_CSV = 'expectedValues.csv'
    List<TimeToCsMapping> mappings
    CsiConfiguration csiConfiguration
    static Map<String, List<Double>> frustrations
    static Map<String, List<ExpectedCustomerSatisfaction>> expectedCustomerSatisfactions

    /** Expected values got calculated BY_RANK so there should be no difference (apart from Java's double rounding inaccuracy) */
    final double TOLARABLE_DELTA_BY_RANK = 0.000001
    /** Single values calculated BY_MAPPING may deviate by this from expected ones (calculated BY_RANK) */
    final double TOLARABLE_DELTA_BY_MAPPING = 2 // should be reached with more complete test dataset: 0.9
    /** Mean of values calculated BY_MAPPING may deviate by this from expected ones (calculated BY_RANK) */
    final double TOLARABLE_MEAN_DELTA_BY_MAPPING = 0.5 // should be reached with more complete test dataset: 0.132

    @Test
    void testUndefinedPage() {
        //test specific mocks
        mocker.mockTimeToCsMappingCacheService(serviceUnderTest, mappings.get(0), frustrations)
        //test execution
        Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercent(2000, new Page(name: Page.UNDEFINED))
        //assertions
        assertThat(csCalculated, is(null))
    }

    @Test
    void testNoTransformationPossibleForPage() {
        //test specific mocks
        mocker.mockTimeToCsMappingCacheService(serviceUnderTest, [], [100])

        //test executions and assertions
        mocker.mockConfigService(serviceUnderTest, 'this.jdbc.driver.wont.support.rlike', 60, CsiTransformation.BY_MAPPING)
        Double csCalculatedByMapping = serviceUnderTest.getCustomerSatisfactionInPercent(2000, new Page(name: "new"))
        assertThat(csCalculatedByMapping, is(null))

        mocker.mockConfigService(serviceUnderTest, 'this.jdbc.driver.wont.support.rlike', 60, CsiTransformation.BY_RANK)
        Double csCalculatedByRank = serviceUnderTest.getCustomerSatisfactionInPercent(2000, new Page(name: 'HP'))
        assertThat(csCalculatedByRank, is(null))
    }

    @Test
    void testGetCustomerSatisfactionInPercentViaMapping() {

        def differences = [:].withDefault{[]}

        // run test for each pageAggregator
        expectedCustomerSatisfactions.each { pageName, expectedCustomerSatisfactionForPage ->

            //test specific mocks, test execution and assertions
            mocker.mockTimeToCsMappingCacheService(serviceUnderTest, csiConfiguration.getTimeToCsMappingByPage(new Page(name: pageName)), frustrations.get(pageName))
            expectedCustomerSatisfactionForPage.each { ExpectedCustomerSatisfaction expected ->

                Page page = new Page(name: pageName, weight: 1)
                Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercentViaMapping(expected.loadTimeInMillisec, page, csiConfiguration)
                Double difference = Math.abs(csCalculated - expected.customerSatisfaction)

                //could be used for detailed analyze of differences between BY_RANK and BY_MAPPING:
                //differences[pageName].add([difference, expected.loadTimeInMillisec, expected.customerSatisfaction, csCalculated])
                assertThat(difference, lessThan(TOLARABLE_DELTA_BY_MAPPING))

            }

        }
    }

    @Test
    void testGetCustomerSatisfactionPercentRank() {

        // run test for each pageAggregator
        expectedCustomerSatisfactions.each { pageName, expectedCustomerSatisfactionForPage ->

            //test specific mocks, test execution and assertions
            mocker.mockTimeToCsMappingCacheService(serviceUnderTest, csiConfiguration.getTimeToCsMappingByPage(new Page(name: pageName)), frustrations.get(pageName))
            expectedCustomerSatisfactionForPage.each { ExpectedCustomerSatisfaction expected ->

                Page page = new Page(name: pageName, weight: 1)

                Double csCalculated = serviceUnderTest.getCustomerSatisfactionPercentRank(expected.loadTimeInMillisec, page)
                Double difference = Math.abs(csCalculated - expected.customerSatisfaction)
                assertThat(difference, lessThan(TOLARABLE_DELTA_BY_RANK))

            }

        }
    }

    @Test
    void meanDeviationByRank() {

        Double totalDeviation = 0.0
        int runs = 0

        // run test for each pageAggregator
        expectedCustomerSatisfactions.each { pageName, expectedCustomerSatisfactionForPage ->

            //test specific mocks and test execution
            mocker.mockTimeToCsMappingCacheService(serviceUnderTest, csiConfiguration.getTimeToCsMappingByPage(new Page(name: pageName)), frustrations.get(pageName))
            expectedCustomerSatisfactionForPage.each { ExpectedCustomerSatisfaction expected ->

                Page page = new Page(name: pageName, weight: 1)
                Double csCalculated = serviceUnderTest.getCustomerSatisfactionPercentRank(expected.loadTimeInMillisec, page)
                totalDeviation += Math.abs(csCalculated - expected.customerSatisfaction)
                runs++

            }

        }

        //assertions
        Double meanDeviation = totalDeviation / runs
        assertThat(meanDeviation, lessThan(TOLARABLE_DELTA_BY_RANK))

    }

    @Test
    void meanDeviationByMapping() {

        Map<String, DeviationToBuildMeanFrom> deviationsByPage = [:].withDefault { new DeviationToBuildMeanFrom() }

        // run test for each pageAggregator
        expectedCustomerSatisfactions.each { pageName, expectedCustomerSatisfactionForPage ->

            //test specific mocks and test execution
            mocker.mockTimeToCsMappingCacheService(serviceUnderTest, csiConfiguration.getTimeToCsMappingByPage(new Page(name: pageName)), frustrations.get(pageName))
            expectedCustomerSatisfactionForPage.each { ExpectedCustomerSatisfaction expected ->

                Page page = new Page(name: pageName, weight: 1)
                Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercentViaMapping(expected.loadTimeInMillisec, page, csiConfiguration)
                deviationsByPage[pageName].deviation += Math.abs(csCalculated - expected.customerSatisfaction)
                deviationsByPage[pageName].runs++

            }

        }

        //assertions
        deviationsByPage.each { String pageName, DeviationToBuildMeanFrom deviationCsiMethods->
            Double meanDeviation = deviationCsiMethods.deviation / deviationCsiMethods.runs
            assertThat(meanDeviation, lessThan(TOLARABLE_MEAN_DELTA_BY_MAPPING))
        }
    }

    @Test
    void testValidFrustrationsExistFor() {

        //test data
        Page page = new Page(name: "HP_entry", weight: 1)

        //with valid frustrations for pageAggregator
        mocker.mockTimeToCsMappingCacheService(serviceUnderTest, csiConfiguration.getTimeToCsMappingByPage(page), frustrations.get(page.name))
        assertThat(service.validFrustrationsExistFor(page), is(true))
        mocker.mockTimeToCsMappingCacheService(serviceUnderTest, csiConfiguration.getTimeToCsMappingByPage(page), [100, 200])
        assertThat(service.validFrustrationsExistFor(page), is(true))

        //without frustrations for pageAggregator
        mocker.mockTimeToCsMappingCacheService(serviceUnderTest, csiConfiguration.getTimeToCsMappingByPage(page), [])
        assertThat(service.validFrustrationsExistFor(page), is(false))

        //with invalid frustrations for pageAggregator: just one frustration
        mocker.mockTimeToCsMappingCacheService(serviceUnderTest, csiConfiguration.getTimeToCsMappingByPage(page), [100])
        assertThat(service.validFrustrationsExistFor(page), is(false))

        //with invalid frustrations for pageAggregator: just one frustration
        mocker.mockTimeToCsMappingCacheService(serviceUnderTest, csiConfiguration.getTimeToCsMappingByPage(page), [1000, 1000, 1000, 1000])
        assertThat(service.validFrustrationsExistFor(page), is(false))

        //with valid frustrations but null as pageAggregator
        mocker.mockTimeToCsMappingCacheService(serviceUnderTest, csiConfiguration.getTimeToCsMappingByPage(page), frustrations.get(page.name))
        assertThat(service.validFrustrationsExistFor(null), is(false))

        //with valid frustrations but UNDEFINED pageAggregator
        mocker.mockTimeToCsMappingCacheService(serviceUnderTest, csiConfiguration.getTimeToCsMappingByPage(page), frustrations)
        assertThat(service.validFrustrationsExistFor(new Page(name: Page.UNDEFINED, weight: 0)), is(false))

    }

    @Before
    void setUp() {

        serviceUnderTest = service

        // mocks common for all tests
        mocker = ServiceMocker.create()

        csiConfigurationSetup()

    }

    void csiConfigurationSetup() {
        File csvFile = new File("test/resources/CsiData/TimeToCsMapping/${MAPPINGS_CSV}")
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

    @BeforeClass
    static void frustrationsSetup() {

        File csvFile = new File("test/resources/CsiData/TimeToCsMapping/${FRUSTRATIONS_CSV}")
        frustrations = [:].withDefault{[]}

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

    @BeforeClass
    static void parseExpectedValues() {

        File csvFile = new File("test/resources/CsiData/TimeToCsMapping/${EXPECTED_CUSTOMER_SATISFACTION_CSV}")
        expectedCustomerSatisfactions = [:].withDefault{[]}

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

