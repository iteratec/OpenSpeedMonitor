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
package de.iteratec.osm.api

import de.iteratec.osm.api.dto.CsiByEventResultsDto
import de.iteratec.osm.csi.*
import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightedValue
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.measurement.schedule.JobGroup

import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.mock.interceptor.MockFor
import org.joda.time.DateTime

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(CsiByEventResultsService)
@Mock([JobGroup, CsiConfiguration])
class CsiByEventResultsServiceTests {
	
	static final double DELTA = 1e-15
	static final double expectedTargetCsi = 34d
	static final DateTime START = new DateTime(2014,1,14,8,0,12) 
	static final DateTime END = new DateTime(2014,1,14,9,2,0)
	CsiByEventResultsService serviceUnderTest
	MvQueryParams queryParamsIrrelevantCauseDbQueriesAreMocked

	def doWithSpring = {
		meanCalcService(MeanCalcService)
		performanceLoggingService(PerformanceLoggingService)
	}

    void setUp() {
		serviceUnderTest = service
        mocksCommonToAllTests()
		CsiConfiguration csiConfiguration = new CsiConfiguration(label: "csi configuration", csiDay: new CsiDay())
		JobGroup jobGroup = new JobGroup(name: "csiJobGroup", csiConfiguration: csiConfiguration).save(failOnError: true)
		queryParamsIrrelevantCauseDbQueriesAreMocked = new MvQueryParams()
		queryParamsIrrelevantCauseDbQueriesAreMocked.jobGroupIds.add(jobGroup.getId())
    }

    void tearDown() {
    }
	
	//tests////////////////////////////////////////////////////////////////////////////////////

	void testRetrieveSystemCsiByRawData_NoEventResults() {
		//test-specific data
		List<WeightedCsiValue> weightedCsiValuesToReturnInMock = []
		
		//test-specific mocks
		mockWeightingService(weightedCsiValuesToReturnInMock)
		
		//test execution
		shouldFail(IllegalArgumentException){
			CsiByEventResultsDto systemCsi =  serviceUnderTest.retrieveCsi(START, END, queryParamsIrrelevantCauseDbQueriesAreMocked, [WeightFactor.PAGE, WeightFactor.BROWSER] as Set)
		}
		
	}
	
    void testRetrieveSystemCsiByRawData_OneSingleEventResult() {
		//test-specific data 
		List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
			new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [1,2,3])]
		
		//test-specific mocks
		mockWeightingService(weightedCsiValuesToReturnInMock)
		
		//test execution
		CsiByEventResultsDto systemCsi =  serviceUnderTest.retrieveCsi(START, END, queryParamsIrrelevantCauseDbQueriesAreMocked, [WeightFactor.PAGE, WeightFactor.BROWSER] as Set)
		
		//assertions
		assertNotNull(systemCsi)
		assertEquals(12d, systemCsi.csiValueAsPercentage, DELTA)
		assertEquals(expectedTargetCsi, systemCsi.targetCsiAsPercentage, DELTA)
		assertEquals(systemCsi.csiValueAsPercentage - expectedTargetCsi, systemCsi.delta,  DELTA)
		assertEquals(3, systemCsi.countOfMeasurings)
		
    }
	
	void testRetrieveSystemCsiByRawData_EventResultsWithDifferentWeights() {
		//test-specific data

		double valueFirstMv = 10d
		double pageWeightFirstMv = 1d
		double browserWeightFirstMv = 2d
		
		double valueSecondMv = 20d
		double pageWeightSecondMv = 3d
		double browserWeightSecondMv = 4d
		
		double valueThirdMv = 30d
		double pageWeightThirdMv = 5d
		double browserWeightThirdMv = 6d
		
		double sumOfAllWeights = (pageWeightFirstMv * browserWeightFirstMv) + (pageWeightSecondMv * browserWeightSecondMv)+ (pageWeightThirdMv  * browserWeightThirdMv)


		List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
			new WeightedCsiValue(weightedValue: new WeightedValue(value: valueFirstMv, weight: (pageWeightFirstMv * browserWeightFirstMv)), underlyingEventResultIds: [1,2,3]),
			new WeightedCsiValue(weightedValue: new WeightedValue(value: valueSecondMv, weight: (pageWeightSecondMv * browserWeightSecondMv)), underlyingEventResultIds: [4,5,6,7]),
			new WeightedCsiValue(weightedValue: new WeightedValue(value: valueThirdMv, weight: (pageWeightThirdMv * browserWeightThirdMv)), underlyingEventResultIds: [10])]
		int numberOfUnderlyingEventResults = 8

		mockWeightingService(weightedCsiValuesToReturnInMock)
		//test execution
		CsiByEventResultsDto systemCsi =  serviceUnderTest.retrieveCsi(START, END, queryParamsIrrelevantCauseDbQueriesAreMocked, [WeightFactor.PAGE, WeightFactor.BROWSER] as Set)
		
		//assertions
		assertNotNull(systemCsi)
		assertEquals(
			(((valueFirstMv * pageWeightFirstMv * browserWeightFirstMv) + (valueSecondMv * pageWeightSecondMv * browserWeightSecondMv) + (valueThirdMv * pageWeightThirdMv * browserWeightThirdMv)) / sumOfAllWeights),
			systemCsi.csiValueAsPercentage, 
			DELTA)
		assertEquals(expectedTargetCsi, systemCsi.targetCsiAsPercentage, DELTA)
		assertEquals(systemCsi.csiValueAsPercentage - expectedTargetCsi, systemCsi.delta,  DELTA)
		assertEquals(numberOfUnderlyingEventResults, systemCsi.countOfMeasurings)
		
	}


	void testRetrieveSystemCsiByRawDataUsesCorrectCsiConfiguration() {
		CsiConfiguration csiConfiguration = new CsiConfiguration(csiDay: new CsiDay(), label: "a csi configuration").save(failOnError: true)
		JobGroup jobGroup = new JobGroup(name: "csiJobGroup", csiConfiguration: csiConfiguration).save(failOnError: true)
		JobGroup jobGroup2 = new JobGroup(name: "rawDataJobGroup").save(failOnError: true)

		MvQueryParams paramsWithCsiConfiguration = new MvQueryParams()
		paramsWithCsiConfiguration.jobGroupIds.add(jobGroup.getId())

		MvQueryParams paramsWithoutCsiConfiguration = new MvQueryParams()
		paramsWithoutCsiConfiguration.jobGroupIds.add(jobGroup2.getId())

		List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
				new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [1,2,3])]

		//test-specific mocks
		mockWeightingService(weightedCsiValuesToReturnInMock)

		assertNotNull(serviceUnderTest.retrieveCsi(START, END, queryParamsIrrelevantCauseDbQueriesAreMocked,[WeightFactor.PAGE, WeightFactor.BROWSER] as Set))

		shouldFail(IllegalArgumentException) {
			serviceUnderTest.retrieveCsi(START, END, paramsWithoutCsiConfiguration,[WeightFactor.PAGE, WeightFactor.BROWSER] as Set)
		}
	}

	//mocks//////////////////////////////////////////////////////////////////////////////

    private mocksCommonToAllTests(){
        serviceUnderTest.meanCalcService = grailsApplication.mainContext.getBean('meanCalcService')
        mockEventResultDaoService()
        mockCsTargetGraphDaoService()
        serviceUnderTest.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')
    }

	/**
	 * Mocks methods of {@link EventResultDaoService}.
	 */
	private void mockEventResultDaoService(){
		def eventResultDaoService = new MockFor(EventResultDaoService, true)
		eventResultDaoService.demand.getByStartAndEndTimeAndMvQueryParams(0..10000) {
			Date fromDate, Date toDate, Collection<CachedView> cachedViews, MvQueryParams mvQueryParams ->
			List<EventResult> irrelevantCauseRetrievingWeightedCsiValuesIsMockedToo = [new EventResult()]
			return irrelevantCauseRetrievingWeightedCsiValuesIsMockedToo
		}
		serviceUnderTest.eventResultDaoService = eventResultDaoService.proxyInstance();
	}
	/**
	 * Mocks methods of {@link WeightingService}.
	 */
	private void mockWeightingService(List<WeightedCsiValue> toReturnFromGetWeightedCsiValues){
		def weightingService = new MockFor(WeightingService, true)
		weightingService.demand.getWeightedCsiValues(1..10000) {
			List<CsiValue> csiValues, Set<WeightFactor> weightFactors, CsiConfiguration unused->
			return toReturnFromGetWeightedCsiValues
		}
		serviceUnderTest.weightingService = weightingService.proxyInstance()
	}
	/**
	 * Mocks methods of {@link CsTargetGraphDaoService}.
	 */
	private void mockCsTargetGraphDaoService(){
		def csTargetGraphDaoService = new MockFor(CsTargetGraphDaoService, true)
		csTargetGraphDaoService.demand.getActualCsTargetGraph(1..10000) { ->
			CsTargetGraph toReturn = new CsTargetGraph()
			toReturn.metaClass.getPercentOfDate = {DateTime dateTime -> return expectedTargetCsi}
			return toReturn
		}
		serviceUnderTest.csTargetGraphDaoService = csTargetGraphDaoService.proxyInstance()
	}
}
