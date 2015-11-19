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

import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
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
    Page page
    List<TimeToCsMapping> mappings
    List<Double> hpFrustrations
    def fvDocAndCsFromExcel

    @Test
    void testUndefinedPage() {
        //test specific mocks
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings, hpFrustrations)

        Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercent(fvDocAndCsFromExcel[0][0], new Page(name: Page.UNDEFINED))
        assertNull(csCalculated)
    }

    @Test
    void testNoTransformationPossibleForPage() {
        //test specific mocks
        mocker.mockTimeToCsMappingService(serviceUnderTest, [], [100])

        mocker.mockConfigService(serviceUnderTest, 'this.jdbc.driver.wont.support.rlike', 60, CsiTransformation.BY_MAPPING)
        Double csCalculatedByMapping = serviceUnderTest.getCustomerSatisfactionInPercent(fvDocAndCsFromExcel[0][0], new Page(name: "new"))
        assertNull(csCalculatedByMapping)

        mocker.mockConfigService(serviceUnderTest , 'this.jdbc.driver.wont.support.rlike', 60, CsiTransformation.BY_RANK)
        Double csCalculatedByRank = serviceUnderTest.getCustomerSatisfactionInPercent(fvDocAndCsFromExcel[0][0], page)
        assertNull(csCalculatedByRank)
    }

    @Test
    void testGetCustomerSatisfactionInPercentViaMapping() {

        //test specific mocks
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings, hpFrustrations)

        fvDocAndCsFromExcel.each {
            Double csCalculated = serviceUnderTest.getCustomerSatisfactionInPercentViaMapping(it[0], page)
            Double difference = Math.abs(csCalculated - it[1])
            assert difference < 0.3
        }

    }

    @Test
    void testGetCustomerSatisfactionPercentRank() {

        //test specific mocks
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings, hpFrustrations)

        fvDocAndCsFromExcel.each {
            Double csCalculated = serviceUnderTest.getCustomerSatisfactionPercentRank(it[0], page)
            Double difference = Math.abs(csCalculated - it[1])
            assert difference < 0.2
        }

    }

    @Test
    void testValidFrustrationsExistFor() {

        //with valid frustrations for page
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings, hpFrustrations)
        assertThat(service.validFrustrationsExistFor(page), is(true))
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings, [100, 200])
        assertThat(service.validFrustrationsExistFor(page), is(true))

        //without frustrations for page
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings, [])
        assertThat(service.validFrustrationsExistFor(page), is(false))

        //with invalid frustrations for page: just one frustration
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings, [100])
        assertThat(service.validFrustrationsExistFor(page), is(false))

        //with invalid frustrations for page: just one frustration
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings, [1000, 1000, 1000, 1000])
        assertThat(service.validFrustrationsExistFor(page), is(false))

        //with valid frustrations but null as page
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings, hpFrustrations)
        assertThat(service.validFrustrationsExistFor(null), is(false))

        //with valid frustrations but UNDEFINED page
        mocker.mockTimeToCsMappingService(serviceUnderTest, mappings, hpFrustrations)
        assertThat(service.validFrustrationsExistFor(new Page(name: Page.UNDEFINED, weight: 0)), is(false))

    }

    @Before
    void setUp() {

        serviceUnderTest = service

        // test data common for all tests
        page = new Page(
                name: 'HP')
        fvDocAndCsFromExcel =
                [
                        [2073, 97.6],
                        [2441, 95.2],
                        [2340, 95.8],
                        [4394, 78.3],
                        [2693, 93.8],
                        [2446, 95.1],
                        [2469, 95.1],
                        [2792, 93.1],
                        [2127, 96.9],
                        [2432, 95.2],
                        [2116, 97.0],
                        [1863, 98.8],
                        [1971, 98.3],
                        [2105, 97.1],
                        [1777, 99.0],
                        [2965, 92.5],
                        [1833, 98.8],
                        [2230, 96.5],
                        [1991, 98.2],
                        [1814, 98.8],
                        [1874, 98.8],
                        [1775, 99.1],
                        [1891, 98.7],
                        [2441, 95.2]
                ]
        mappings = [
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 0, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 100, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 200, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 300, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 400, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 500, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 600, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 700, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 800, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 900, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 1000, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 1100, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 1200, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 1300, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 1400, customerSatisfaction: 100, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 1500, customerSatisfaction: 99.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 1600, customerSatisfaction: 99.6, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 1700, customerSatisfaction: 99.3, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 1800, customerSatisfaction: 98.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 1900, customerSatisfaction: 98.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 2000, customerSatisfaction: 98.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 2100, customerSatisfaction: 97.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 2200, customerSatisfaction: 96.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 2300, customerSatisfaction: 95.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 2400, customerSatisfaction: 95.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 2500, customerSatisfaction: 95.0, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 2600, customerSatisfaction: 94.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 2700, customerSatisfaction: 93.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 2800, customerSatisfaction: 93.0, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 2900, customerSatisfaction: 92.6, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 3000, customerSatisfaction: 92.1, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 3100, customerSatisfaction: 91.0, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 3200, customerSatisfaction: 90.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 3300, customerSatisfaction: 89.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 3400, customerSatisfaction: 88.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 3500, customerSatisfaction: 87.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 3600, customerSatisfaction: 86.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 3700, customerSatisfaction: 85.6, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 3800, customerSatisfaction: 84.0, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 3900, customerSatisfaction: 83.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 4000, customerSatisfaction: 82.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 4100, customerSatisfaction: 81.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 4200, customerSatisfaction: 80.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 4300, customerSatisfaction: 79.3, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 4400, customerSatisfaction: 78.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 4500, customerSatisfaction: 77.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 4600, customerSatisfaction: 75.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 4700, customerSatisfaction: 74.6, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 4800, customerSatisfaction: 73.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 4900, customerSatisfaction: 72.3, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 5000, customerSatisfaction: 71.0, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 5100, customerSatisfaction: 69.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 5200, customerSatisfaction: 68.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 5300, customerSatisfaction: 67.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 5400, customerSatisfaction: 66.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 5500, customerSatisfaction: 65.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 5600, customerSatisfaction: 65.1, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 5700, customerSatisfaction: 64.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 5800, customerSatisfaction: 64.0, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 5900, customerSatisfaction: 62.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 6000, customerSatisfaction: 62.0, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 6100, customerSatisfaction: 61.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 6200, customerSatisfaction: 60.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 6300, customerSatisfaction: 59.1, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 6400, customerSatisfaction: 58.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 6500, customerSatisfaction: 57.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 6600, customerSatisfaction: 56.3, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 6700, customerSatisfaction: 55.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 6800, customerSatisfaction: 54.3, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 6900, customerSatisfaction: 53.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 7000, customerSatisfaction: 52.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 7100, customerSatisfaction: 51.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 7200, customerSatisfaction: 51.1, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 7300, customerSatisfaction: 50.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 7400, customerSatisfaction: 50.0, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 7500, customerSatisfaction: 49.3, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 7600, customerSatisfaction: 48.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 7700, customerSatisfaction: 47.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 7800, customerSatisfaction: 47.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 7900, customerSatisfaction: 46.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 8000, customerSatisfaction: 46.6, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 8100, customerSatisfaction: 45.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 8200, customerSatisfaction: 45.1, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 8300, customerSatisfaction: 44.3, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 8400, customerSatisfaction: 43.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 8500, customerSatisfaction: 43.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 8600, customerSatisfaction: 42.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 8700, customerSatisfaction: 41.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 8800, customerSatisfaction: 41.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 8900, customerSatisfaction: 40.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 9000, customerSatisfaction: 39.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 9100, customerSatisfaction: 39.0, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 9200, customerSatisfaction: 38.0, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 9300, customerSatisfaction: 37.1, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 9400, customerSatisfaction: 36.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 9500, customerSatisfaction: 35.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 9600, customerSatisfaction: 35.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 9700, customerSatisfaction: 35.0, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 9800, customerSatisfaction: 34.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 9900, customerSatisfaction: 33.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 10000, customerSatisfaction: 32.6, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 10100, customerSatisfaction: 32.1, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 10200, customerSatisfaction: 31.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 10300, customerSatisfaction: 30.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 10400, customerSatisfaction: 29.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 10500, customerSatisfaction: 29.3, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 10600, customerSatisfaction: 28.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 10700, customerSatisfaction: 28.3, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 10800, customerSatisfaction: 27.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 10900, customerSatisfaction: 27.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 11000, customerSatisfaction: 26.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 11100, customerSatisfaction: 26.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 11200, customerSatisfaction: 25.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 11300, customerSatisfaction: 25.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 11400, customerSatisfaction: 25.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 11500, customerSatisfaction: 24.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 11600, customerSatisfaction: 24.6, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 11700, customerSatisfaction: 23.7, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 11800, customerSatisfaction: 23.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 11900, customerSatisfaction: 22.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 12000, customerSatisfaction: 22.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 12100, customerSatisfaction: 21.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 12200, customerSatisfaction: 21.3, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 12300, customerSatisfaction: 21.0, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 12400, customerSatisfaction: 20.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 12500, customerSatisfaction: 20.3, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 12600, customerSatisfaction: 20.1, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 12700, customerSatisfaction: 19.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 12800, customerSatisfaction: 19.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 12900, customerSatisfaction: 18.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 13000, customerSatisfaction: 18.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 13100, customerSatisfaction: 17.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 13200, customerSatisfaction: 17.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 13300, customerSatisfaction: 16.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 13400, customerSatisfaction: 16.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 13500, customerSatisfaction: 16.1, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 13600, customerSatisfaction: 15.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 13700, customerSatisfaction: 15.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 13800, customerSatisfaction: 15.4, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 13900, customerSatisfaction: 15.1, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 14000, customerSatisfaction: 14.6, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 14100, customerSatisfaction: 14.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 14200, customerSatisfaction: 13.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 14300, customerSatisfaction: 13.6, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 14400, customerSatisfaction: 13.3, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 14500, customerSatisfaction: 13.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 14600, customerSatisfaction: 13.0, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 14700, customerSatisfaction: 12.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 14800, customerSatisfaction: 12.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 14900, customerSatisfaction: 12.6, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 15000, customerSatisfaction: 12.2, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 15100, customerSatisfaction: 11.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 15200, customerSatisfaction: 11.8, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 15300, customerSatisfaction: 11.6, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 15400, customerSatisfaction: 11.5, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 15500, customerSatisfaction: 11.1, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 15600, customerSatisfaction: 11.1, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 15700, customerSatisfaction: 10.9, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 15800, customerSatisfaction: 10.6, mappingVersion: 1).save(failOnError: true),
                new TimeToCsMapping(page: new Page(name: 'HP', weight: 1), loadTimeInMilliSecs: 15900, customerSatisfaction: 10.2, mappingVersion: 1).save(failOnError: true)
        ]
        hpFrustrations = [
                1382, 1445, 1478, 1487, 1560, 1578, 1622, 1628, 1629, 1639, 1684, 1770, 1775, 1778, 1787, 1797, 1833, 1908, 1908, 1919, 1928, 1967, 1968, 1987, 2006, 2020, 2026,
                2037, 2047, 2047, 2060, 2067, 2075, 2079, 2083, 2090, 2092, 2102, 2104, 2122, 2128, 2138, 2163, 2210, 2218, 2222, 2229, 2253, 2274, 2276, 2277, 2282, 2291, 2304,
                2340, 2357, 2382, 2389, 2390, 2402, 2405, 2408, 2414, 2445, 2469, 2508, 2525, 2529, 2529, 2530, 2531, 2601, 2602, 2620, 2624, 2625, 2636, 2643, 2648, 2657, 2672,
                2699, 2712, 2721, 2727, 2745, 2751, 2756, 2770, 2791, 2798, 2810, 2832, 2866, 2867, 2880, 2946, 2948, 2980, 2983, 2988, 2998, 3000, 3013, 3022, 3024, 3035, 3038,
                3040, 3049, 3058, 3067, 3073, 3078, 3080, 3083, 3104, 3108, 3121, 3139, 3140, 3140, 3144, 3163, 3174, 3176, 3204, 3212, 3214, 3216, 3236, 3250, 3266, 3273, 3273,
                3276, 3281, 3303, 3306, 3313, 3321, 3328, 3340, 3346, 3348, 3383, 3384, 3385, 3388, 3389, 3390, 3395, 3403, 3407, 3407, 3408, 3417, 3422, 3431, 3438, 3481, 3513,
                3516, 3557, 3563, 3564, 3589, 3593, 3596, 3601, 3603, 3609, 3618, 3629, 3630, 3636, 3640, 3643, 3656, 3660, 3662, 3668, 3670, 3672, 3688, 3701, 3703, 3703, 3706,
                3717, 3728, 3728, 3732, 3736, 3737, 3748, 3749, 3766, 3766, 3778, 3778, 3781, 3785, 3792, 3796, 3797, 3798, 3805, 3824, 3833, 3836, 3853, 3854, 3918, 3952, 3952,
                3958, 3964, 3964, 3985, 3990, 3991, 3992, 4000, 4006, 4008, 4018, 4030, 4039, 4052, 4055, 4070, 4074, 4078, 4096, 4100, 4104, 4114, 4139, 4145, 4150, 4155, 4167,
                4167, 4172, 4191, 4197, 4204, 4209, 4212, 4218, 4218, 4222, 4232, 4244, 4253, 4258, 4258, 4260, 4266, 4266, 4268, 4284, 4287, 4288, 4292, 4300, 4301, 4304, 4311,
                4315, 4321, 4328, 4340, 4343, 4352, 4357, 4367, 4384, 4394, 4395, 4406, 4407, 4410, 4414, 4446, 4473, 4474, 4485, 4493, 4504, 4505, 4507, 4524, 4527, 4531, 4532,
                4546, 4554, 4556, 4559, 4562, 4565, 4567, 4572, 4574, 4578, 4578, 4582, 4584, 4588, 4594, 4596, 4598, 4606, 4619, 4625, 4639, 4647, 4649, 4657, 4661, 4665, 4668,
                4669, 4674, 4696, 4703, 4705, 4712, 4716, 4718, 4720, 4726, 4737, 4738, 4738, 4745, 4750, 4759, 4766, 4769, 4769, 4778, 4796, 4800, 4814, 4828, 4834, 4840, 4845,
                4859, 4865, 4877, 4879, 4885, 4890, 4903, 4908, 4911, 4917, 4919, 4921, 4932, 4946, 4967, 4979, 4981, 4984, 4988, 4988, 4994, 4997, 5002, 5005, 5006, 5013, 5020,
                5023, 5049, 5051, 5055, 5062, 5085, 5087, 5088, 5093, 5094, 5096, 5109, 5110, 5125, 5129, 5135, 5140, 5141, 5147, 5148, 5169, 5173, 5176, 5178, 5179, 5179, 5186,
                5200, 5202, 5207, 5207, 5211, 5229, 5230, 5232, 5235, 5250, 5251, 5258, 5266, 5278, 5297, 5304, 5310, 5325, 5326, 5338, 5350, 5357, 5364, 5390, 5403, 5415, 5421,
                5422, 5429, 5443, 5445, 5455, 5458, 5479, 5483, 5485, 5490, 5516, 5531, 5532, 5562, 5567, 5580, 5594, 5597, 5610, 5615, 5639, 5640, 5666, 5673, 5679, 5683, 5731,
                5738, 5750, 5758, 5790, 5804, 5807, 5812, 5815, 5820, 5821, 5828, 5830, 5851, 5853, 5855, 5867, 5875, 5881, 5881, 5894, 5899, 5901, 5903, 5912, 5912, 5921, 5933,
                5953, 5953, 5978, 6000, 6004, 6006, 6019, 6027, 6043, 6045, 6049, 6052, 6084, 6117, 6122, 6124, 6131, 6134, 6136, 6148, 6148, 6152, 6157, 6188, 6188, 6197, 6233,
                6234, 6235, 6237, 6238, 6244, 6259, 6262, 6263, 6272, 6281, 6283, 6295, 6297, 6302, 6328, 6328, 6337, 6353, 6358, 6366, 6367, 6372, 6411, 6411, 6413, 6425, 6429,
                6438, 6443, 6456, 6458, 6464, 6470, 6474, 6481, 6482, 6492, 6499, 6501, 6502, 6505, 6519, 6531, 6540, 6549, 6570, 6587, 6590, 6592, 6604, 6606, 6635, 6638, 6645,
                6648, 6650, 6658, 6662, 6670, 6677, 6681, 6695, 6699, 6700, 6712, 6714, 6718, 6724, 6761, 6762, 6780, 6782, 6790, 6797, 6800, 6803, 6815, 6818, 6828, 6830, 6863,
                6875, 6878, 6879, 6882, 6887, 6911, 6916, 6923, 6943, 6953, 6968, 6970, 6973, 6975, 6979, 6980, 6985, 6989, 7009, 7029, 7040, 7062, 7067, 7074, 7099, 7125, 7125,
                7134, 7148, 7153, 7156, 7180, 7186, 7191, 7192, 7203, 7232, 7250, 7253, 7270, 7297, 7297, 7302, 7308, 7309, 7312, 7320, 7384, 7386, 7405, 7422, 7425, 7425, 7440,
                7445, 7452, 7481, 7494, 7500, 7520, 7522, 7526, 7551, 7571, 7575, 7575, 7578, 7578, 7579, 7603, 7613, 7616, 7625, 7629, 7641, 7661, 7662, 7691, 7703, 7712, 7728,
                7781, 7784, 7813, 7819, 7838, 7858, 7862, 7872, 7887, 7950, 7954, 7963, 8011, 8026, 8050, 8053, 8063, 8065, 8065, 8076, 8086, 8088, 8090, 8094, 8112, 8157, 8178,
                8185, 8188, 8194, 8194, 8212, 8218, 8227, 8228, 8244, 8250, 8264, 8278, 8287, 8288, 8301, 8332, 8338, 8360, 8383, 8392, 8395, 8406, 8419, 8438, 8443, 8452, 8454,
                8469, 8484, 8521, 8524, 8533, 8547, 8549, 8563, 8574, 8584, 8588, 8589, 8620, 8654, 8655, 8660, 8672, 8675, 8682, 8708, 8719, 8750, 8751, 8763, 8775, 8786, 8796,
                8816, 8837, 8844, 8851, 8854, 8858, 8864, 8890, 8896, 8907, 8912, 8923, 8926, 8938, 8940, 8982, 8994, 9000, 9025, 9033, 9060, 9063, 9064, 9064, 9075, 9076, 9079,
                9098, 9101, 9124, 9125, 9131, 9141, 9141, 9145, 9152, 9182, 9187, 9189, 9190, 9195, 9202, 9210, 9218, 9228, 9229, 9230, 9235, 9235, 9246, 9260, 9272, 9273, 9307,
                9321, 9322, 9326, 9359, 9385, 9386, 9409, 9415, 9442, 9444, 9464, 9470, 9478, 9485, 9486, 9496, 9531, 9547, 9583, 9598, 9604, 9640, 9672, 9691, 9695, 9701, 9709,
                9719, 9724, 9728, 9730, 9735, 9745, 9773, 9782, 9797, 9811, 9818, 9819, 9828, 9852, 9852, 9859, 9881, 9888, 9897, 9900, 9906, 9937, 9940, 9953, 9958, 9963, 9971,
                9979, 9983, 10010, 10020, 10044, 10047, 10054, 10059, 10081, 10116, 10141, 10143, 10170, 10172, 10178, 10179, 10185, 10187, 10230, 10234, 10244, 10250, 10252,
                10255, 10275, 10296, 10297, 10311, 10311, 10315, 10337, 10339, 10343, 10351, 10355, 10363, 10366, 10375, 10393, 10418, 10433, 10443, 10444, 10489, 10499, 10513,
                10519, 10531, 10538, 10538, 10577, 10577, 10606, 10608, 10626, 10661, 10690, 10691, 10740, 10743, 10757, 10762, 10762, 10823, 10839, 10860, 10875, 10891, 10905,
                10922, 10928, 10951, 10953, 10965, 10979, 10989, 11019, 11021, 11037, 11057, 11060, 11063, 11090, 11091, 11104, 11105, 11155, 11167, 11199, 11252, 11269, 11297, 11324, 11329, 11376, 11377, 11391, 11409,
                11415, 11417, 11443, 11505, 11508, 11535, 11547, 11607, 11608, 11608, 11625, 11630, 11655, 11673, 11678, 11678, 11687, 11688, 11701, 11731, 11744, 11786, 11789, 11816, 11840, 11843, 11854, 11854, 11857,
                11928, 11949, 11968, 11969, 11988, 11998, 12047, 12048, 12055, 12060, 12073, 12083, 12085, 12090, 12125, 12133, 12143, 12151, 12154, 12177, 12184, 12237, 12243, 12279, 12281, 12311, 12343, 12344, 12344,
                12377, 12377, 12417, 12438, 12480, 12550, 12586, 12591, 12658, 12673, 12709, 12716, 12734, 12743, 12756, 12771, 12775, 12779, 12789, 12825, 12851, 12896, 12907, 12915, 12933, 12951, 12973, 12976, 12994,
                13000, 13062, 13072, 13089, 13090, 13096, 13098, 13100, 13122, 13150, 13174, 13175, 13199, 13203, 13206, 13265, 13269, 13278, 13297, 13302, 13307, 13343, 13343, 13391, 13397, 13398, 13418, 13466, 13485,
                13500, 13505, 13529, 13585, 13601, 13625, 13670, 13680, 13711, 13782, 13865, 13870, 13891, 13933, 13965, 13968, 13980, 13984, 13986, 14008, 14024, 14034, 14039, 14040, 14127, 14127, 14159, 14173, 14183,
                14187, 14213, 14218, 14276, 14337, 14358, 14391, 14408, 14428, 14571, 14579, 14640, 14672, 14687, 14844, 14848, 14887, 14933, 14959, 14985, 14994, 15009, 15058, 15071, 15073, 15187, 15203, 15231, 15240,
                15391, 15400, 15441, 15468, 15481, 15485, 15502, 15670, 15678, 15737, 15753, 15765, 15774, 15803, 15804, 15818, 15842, 15855, 15891, 15950, 16000, 16054, 16098, 16100, 16155, 16179, 16214, 16226, 16230,
                16265, 16278, 16281, 16397, 16453, 16463, 16515, 16516, 16604, 16626, 16630, 16632, 16656, 16657, 16674, 16712, 16750, 16781, 16844, 16864, 16875, 16908, 16932, 16941, 16957, 17145, 17148, 17178, 17199,
                17206, 17227, 17301, 17326, 17375, 17376, 17394, 17432, 17448, 17482, 17497, 17599, 17605, 17630, 17649, 17675, 17727, 17742, 17753, 17795, 17796, 17821, 17880, 17883, 17966, 18093, 18096, 18134, 18195,
                18268, 18271, 18365, 18374, 18532, 18572, 18648, 18650, 18706, 18716, 18746, 18763, 18825, 18828, 18828, 18890, 18984, 19016, 19322, 19411, 19502, 19519, 19633, 19640, 19670, 19672, 19766, 19854, 19933,
                20000, 20162, 20170, 20235, 20530, 20531, 20683, 20693, 20711, 21292, 21325, 21390, 21417, 21422, 21427, 21484, 21544, 21805, 21947, 22000, 22010, 22207, 22516, 22647, 22672, 22701, 22833, 22874, 22888,
                23162, 23164, 23896, 23922
        ]

        // mocks common for all tests
        mocker = ServiceMocker.create()
    }

}
