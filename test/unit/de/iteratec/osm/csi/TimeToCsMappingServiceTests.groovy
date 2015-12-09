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
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertThat

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(TimeToCsMappingService)
@Mock([Page, TimeToCsMapping])
class TimeToCsMappingServiceTests {

    TimeToCsMappingService serviceUnderTest
    ServiceMocker mocker
    Map<String, List<TimeToCsMapping>> mappings
    static Map<String, List<Double>> frustrations
    static Map<String, List> expectedCustomerSatisfaction

    final double DELTA_BY_RANK = 0.2
    final double DELTA_BY_MAPPING = 0.3

    @Test
    void testUndefinedPage() {
        //test specific mocks
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings.get(0), frustrations)

        Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercent(2000, new Page(name: Page.UNDEFINED))
        assertNull(csCalculated)
    }

    @Test
    void testNoTransformationPossibleForPage() {
        //test specific mocks
        mocker.mockTimeToCsMappingService(serviceUnderTest, [], [100])

        mocker.mockConfigService(serviceUnderTest, 'this.jdbc.driver.wont.support.rlike', 60, CsiTransformation.BY_MAPPING)
        Double csCalculatedByMapping = serviceUnderTest.getCustomerSatisfactionInPercent(2000, new Page(name: "new"))
        assertNull(csCalculatedByMapping)

        mocker.mockConfigService(serviceUnderTest, 'this.jdbc.driver.wont.support.rlike', 60, CsiTransformation.BY_RANK)
        Double csCalculatedByRank = serviceUnderTest.getCustomerSatisfactionInPercent(2000, new Page(name: 'HP'))
        assertNull(csCalculatedByRank)
    }

    @Test
    void testGetCustomerSatisfactionInPercentViaMapping() {

        // run test for each page
        expectedCustomerSatisfaction.each { k, expectedCustomerSatisfactionForPage ->
            //test specific mocks
            mocker.mockTimeToCsMappingService(serviceUnderTest, mappings.get(k), frustrations.get(k))
            expectedCustomerSatisfactionForPage.each { e ->
                Page page = new Page(name: k, weight: 1)
                Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercentViaMapping(e[0], page)
                Double difference = Math.abs(csCalculated - e[1])
                assert (difference < DELTA_BY_MAPPING)
            }
        }
    }

    @Test
    void testGetCustomerSatisfactionPercentRank() {

        // run test for each page
        expectedCustomerSatisfaction.each { k, expectedCustomerSatisfactionForPage ->
            //test specific mocks
            mocker.mockTimeToCsMappingService(serviceUnderTest, mappings.get(k), frustrations.get(k))
            expectedCustomerSatisfactionForPage.each { e ->
                Page page = new Page(name: k, weight: 1)

                Double csCalculated = serviceUnderTest.getCustomerSatisfactionPercentRank(e[0], page)
                Double difference = Math.abs(csCalculated - e[1])
                assert (difference < DELTA_BY_RANK)
            }
        }
    }

    @Test
    void meanDeviationByRank() {

        Double totalDeviation = 0.0
        int runs = 0

        // run test for each page
        expectedCustomerSatisfaction.each { k, expectedCustomerSatisfactionForPage ->
            //test specific mocks
            mocker.mockTimeToCsMappingService(serviceUnderTest, mappings.get(k), frustrations.get(k))
            expectedCustomerSatisfactionForPage.each { e ->
                Page page = new Page(name: k, weight: 1)

                Double csCalculated = serviceUnderTest.getCustomerSatisfactionPercentRank(e[0], page)
                totalDeviation += Math.abs(csCalculated - e[1])
                runs++
            }
        }

        Double meanDiviation = totalDeviation / runs
        assert meanDiviation < DELTA_BY_RANK
    }

    @Test
    void meanDeviationByMapping() {

        Double diviation = 0.0
        int runs = 0

        // run test for each page
        expectedCustomerSatisfaction.each { k, expectedCustomerSatisfactionForPage ->
            //test specific mocks
            mocker.mockTimeToCsMappingService(serviceUnderTest, mappings.get(k), frustrations.get(k))
            expectedCustomerSatisfactionForPage.each { e ->
                Page page = new Page(name: k, weight: 1)

                Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercentViaMapping(e[0], page)
                diviation += Math.abs(csCalculated - e[1])
                runs++
            }
        }

        Double meanDeviation = diviation / runs
        assert meanDeviation < DELTA_BY_MAPPING
    }

    @Test
    void testValidFrustrationsExistFor() {
        Page page = new Page(name: "HP_entry", weight: 1)
        //with valid frustrations for page
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings.get(page.name), frustrations.get(page.name))
        assertThat(service.validFrustrationsExistFor(page), is(true))
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings.get(page.name), [100, 200])
        assertThat(service.validFrustrationsExistFor(page), is(true))

        //without frustrations for page
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings.get(page.name), [])
        assertThat(service.validFrustrationsExistFor(page), is(false))

        //with invalid frustrations for page: just one frustration
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings.get(page.name), [100])
        assertThat(service.validFrustrationsExistFor(page), is(false))

        //with invalid frustrations for page: just one frustration
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings.get(page.name), [1000, 1000, 1000, 1000])
        assertThat(service.validFrustrationsExistFor(page), is(false))

        //with valid frustrations but null as page
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings.get(page.name), frustrations.get(page.name))
        assertThat(service.validFrustrationsExistFor(null), is(false))

        //with valid frustrations but UNDEFINED page
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings.get(page.name), frustrations)
        assertThat(service.validFrustrationsExistFor(new Page(name: Page.UNDEFINED, weight: 0)), is(false))

    }

    @Before
    void setUp() {

        serviceUnderTest = service

        // mocks common for all tests
        mocker = ServiceMocker.create()

        mappings = new HashMap<>()

        int lineCounter = 0
        File csvFile = new File("test/resources/CsiData/TimeToCsMapping/timeToCsMappings.csv")
        new FileInputStream(csvFile).eachLine { line ->
            if (lineCounter >= 1) {
                def tokenized = line.tokenize(';')

                List<TimeToCsMapping> pageMapping = mappings.get(tokenized[0])
                if (!pageMapping) {
                    pageMapping = new ArrayList<>()
                    mappings.put(tokenized[0], pageMapping)
                }
                pageMapping.add(new TimeToCsMapping(page: new Page(name: tokenized[0], weight: 1), loadTimeInMilliSecs: tokenized[1], customerSatisfaction: tokenized[2], mappingVersion: 1).save(failOnError: true))
            }
            lineCounter++
        }

    }

    @BeforeClass
    static void frustrationsSetup() {

        frustrations = new HashMap<>()

        int lineCounter = 0
        File csvFile = new File("test/resources/CsiData/TimeToCsMapping/customerFrustrations.csv")
        new FileInputStream(csvFile).eachLine { line ->
            if (lineCounter >= 1) {
                def tokenized = line.tokenize(';')

                List<Double> pageCustomerFrustrations = frustrations.get(tokenized[0])
                if (!pageCustomerFrustrations) {
                    pageCustomerFrustrations = new ArrayList<>()
                    frustrations.put(tokenized[0], pageCustomerFrustrations)
                }

                pageCustomerFrustrations.add(Integer.parseInt(tokenized[1]))
            }
            lineCounter++
        }

    }

    @BeforeClass
    static void parseExpectedValues() {

        expectedCustomerSatisfaction = new HashMap<>()

        int lineCounter = 0
        File csvFile = new File("test/resources/CsiData/TimeToCsMapping/expectedValues.csv")
        new FileInputStream(csvFile).eachLine { line ->
            if (lineCounter >= 1) {
                def tokenized = line.tokenize(';')

                List<Double> pageCustomerSatisfaction = expectedCustomerSatisfaction.get(tokenized[0])
                if (!pageCustomerSatisfaction) {
                    pageCustomerSatisfaction = new ArrayList<>()
                    expectedCustomerSatisfaction.put(tokenized[0], pageCustomerSatisfaction)
                }

                pageCustomerSatisfaction.add([Integer.parseInt(tokenized[1]), Double.parseDouble(tokenized[2])])
            }
            lineCounter++
        }

    }
}
