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
import de.iteratec.osm.csi.CsTargetGraph
import de.iteratec.osm.csi.CsTargetGraphDaoService
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.MeanCalcService
import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightedValue
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.CsiValueService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import spock.lang.Specification

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

@Build([EventResult, CsiConfiguration, JobGroup])
class CsiByEventResultsServiceSpec extends Specification implements BuildDataTest,
        ServiceUnitTest<CsiByEventResultsService> {

    static final double DELTA = 1e-15
    static final double EXPECTED_TARGET_CSI = 34d
    static final DateTime START = new DateTime(2014, 1, 14, 8, 0, 12)
    static final DateTime END = new DateTime(2014, 1, 14, 9, 2, 0)
    CsiByEventResultsService serviceUnderTest
    MvQueryParams queryParamsIrrelevantCauseDbQueriesAreMocked

    Closure doWithSpring() {
        return {
            meanCalcService(MeanCalcService)
            performanceLoggingService(PerformanceLoggingService)
        }
    }

    void setup() {
        serviceUnderTest = service
        initFullFunctionalSpringBeanServices()
        mocksCommonToAllTests()
        createTestDataCommonToAllTests()
    }

    void setupSpec() {
        mockDomains(JobGroup, CsiConfiguration, ConnectivityProfile, Script)
    }

    //tests////////////////////////////////////////////////////////////////////////////////////

    void "get system csi without any EventResults"() {
        given: "no EventResults match query"
        serviceUnderTest.eventResultDaoService = Stub(EventResultDaoService){
            getByStartAndEndTimeAndMvQueryParams(_, _, _, _) >> []
        }
        serviceUnderTest.csiValueService = Stub(CsiValueService){
            getWeightedCsiValues(_, _, _) >> []
        }

        when: "EventResult based csi is calculated"
        serviceUnderTest.retrieveCsi(START, END, queryParamsIrrelevantCauseDbQueriesAreMocked, [WeightFactor.PAGE, WeightFactor.BROWSER] as Set)

        then: "an IllegalArgumentException with correct message is thrown"
        IllegalArgumentException iae = thrown IllegalArgumentException
        iae.message.startsWith("For the following query-params a system-csi couldn't be calculated: ")
    }

    void "get system csi with one single EventResult"() {
        given: "one EventResult matches query"
        serviceUnderTest.eventResultDaoService = Stub(EventResultDaoService){
            getByStartAndEndTimeAndMvQueryParams(_, _, _, _) >> [EventResult.build(save: false)]
        }
        serviceUnderTest.csiValueService = Stub(CsiValueService){
            getWeightedCsiValues(_, _, _) >> [new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [1, 2, 3])]
        }

        when: "EventResult based csi is calculated"
        CsiByEventResultsDto systemCsi = serviceUnderTest.retrieveCsi(START, END, queryParamsIrrelevantCauseDbQueriesAreMocked, [WeightFactor.PAGE, WeightFactor.BROWSER] as Set)

        then: "csi is correctly calculated"
        systemCsi != null
        systemCsi.countOfMeasurings == 3
        that systemCsi.csiValueAsPercentage, closeTo(12d, DELTA)
        that systemCsi.targetCsiAsPercentage, closeTo(EXPECTED_TARGET_CSI, DELTA)
        that systemCsi.delta, closeTo(systemCsi.csiValueAsPercentage - EXPECTED_TARGET_CSI, DELTA)

    }

    void "get system csi for multiple EventResults with different weights"() {
        given: "multiple EventResults match query and they get weighted differently in csi calculation"

        double valueFirstMv = 10d
        double pageWeightFirstMv = 1d
        double browserWeightFirstMv = 2d

        double valueSecondMv = 20d
        double pageWeightSecondMv = 3d
        double browserWeightSecondMv = 4d

        double valueThirdMv = 30d
        double pageWeightThirdMv = 5d
        double browserWeightThirdMv = 6d

        double csiValueFirst = valueFirstMv * pageWeightFirstMv * browserWeightFirstMv
        double csiValieSecond = valueSecondMv * pageWeightSecondMv * browserWeightSecondMv
        double csiValueThird = valueThirdMv * pageWeightThirdMv * browserWeightThirdMv
        double sumOfAllWeights = (pageWeightFirstMv * browserWeightFirstMv) + (pageWeightSecondMv * browserWeightSecondMv) + (pageWeightThirdMv * browserWeightThirdMv)

        double expectedCsiValue = ((csiValueFirst + csiValieSecond + csiValueThird) / sumOfAllWeights)
        int numberOfUnderlyingEventResults = 8

        serviceUnderTest.eventResultDaoService = Stub(EventResultDaoService){
            List<EventResult> nonEmptyEventResultListNotUsedInTest = [EventResult.build(save: false)]
            getByStartAndEndTimeAndMvQueryParams(_, _, _, _) >> nonEmptyEventResultListNotUsedInTest
        }
        serviceUnderTest.csiValueService = Stub(CsiValueService){
            getWeightedCsiValues(_, _, _) >> [
                new WeightedCsiValue(
                    weightedValue: new WeightedValue(value: valueFirstMv, weight: (pageWeightFirstMv * browserWeightFirstMv)),
                    underlyingEventResultIds: [1, 2, 3]
                ),
                new WeightedCsiValue(
                    weightedValue: new WeightedValue(value: valueSecondMv, weight: (pageWeightSecondMv * browserWeightSecondMv)),
                    underlyingEventResultIds: [4, 5, 6, 7]
                ),
                new WeightedCsiValue(
                    weightedValue: new WeightedValue(value: valueThirdMv, weight: (pageWeightThirdMv * browserWeightThirdMv)),
                    underlyingEventResultIds: [10]
                )
            ]
        }

        when: "EventResult based csi is calculated"
        CsiByEventResultsDto systemCsi = serviceUnderTest.retrieveCsi(START, END, queryParamsIrrelevantCauseDbQueriesAreMocked, [WeightFactor.PAGE, WeightFactor.BROWSER] as Set)

        then: "csi is correctly calculatedrespective different weights"
        systemCsi != null
        systemCsi.countOfMeasurings == numberOfUnderlyingEventResults
        that systemCsi.csiValueAsPercentage, closeTo(expectedCsiValue, DELTA)
        that systemCsi.targetCsiAsPercentage, closeTo(EXPECTED_TARGET_CSI, DELTA)
        that systemCsi.delta, closeTo(systemCsi.csiValueAsPercentage - EXPECTED_TARGET_CSI, DELTA)

    }


    void "get system csi for system without CsiConfiguration"() {
        given: "MvQueryParams contain JobGroup without associated CsiConfiguration"
        JobGroup jobGroupWithoutCsiConfig = JobGroup.build(name: "rawDataJobGroup")
        MvQueryParams queryParamsWithoutCsiConfiguration = new MvQueryParams()
        queryParamsWithoutCsiConfiguration.jobGroupIds.add(jobGroupWithoutCsiConfig.getId())
        serviceUnderTest.eventResultDaoService = Stub(EventResultDaoService){
            getByStartAndEndTimeAndMvQueryParams(_, _, _, _) >> [EventResult.build(save: false)]
        }
        serviceUnderTest.csiValueService = Stub(CsiValueService){
            getWeightedCsiValues(_, _, _) >> [new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [1, 2, 3])]
        }

        when: "EventResult based csi is calculated"
        serviceUnderTest.retrieveCsi(START, END, queryParamsWithoutCsiConfiguration, [WeightFactor.PAGE, WeightFactor.BROWSER] as Set)

        then: "an IllegalArgumentException with correct message is thrown"
        IllegalArgumentException iae = thrown(IllegalArgumentException)
        iae.message.startsWith("there is no csi configuratin for jobGroup with id")
    }

    //mocks//////////////////////////////////////////////////////////////////////////////

    private initFullFunctionalSpringBeanServices(){
        serviceUnderTest.meanCalcService = grailsApplication.mainContext.getBean('meanCalcService')
        serviceUnderTest.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')
    }

    private mocksCommonToAllTests() {
        serviceUnderTest.csTargetGraphDaoService = Stub(CsTargetGraphDaoService)
        serviceUnderTest.csTargetGraphDaoService.getActualCsTargetGraph() >> {
            CsTargetGraph csTargetGraph = Stub(CsTargetGraph)
            csTargetGraph.getPercentOfDate(_) >> EXPECTED_TARGET_CSI
            return csTargetGraph
        }
    }

    private void createTestDataCommonToAllTests() {
        CsiConfiguration csiConfiguration = CsiConfiguration.build(save: false)
        JobGroup jobGroup = JobGroup.build(csiConfiguration: csiConfiguration)
        queryParamsIrrelevantCauseDbQueriesAreMocked = new MvQueryParams()
        queryParamsIrrelevantCauseDbQueriesAreMocked.jobGroupIds.add(jobGroup.getId())
    }

}
