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

import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightedValue
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.CsiValueService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Specification

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

@TestFor(CsiSystemCsiAggregationService)
@Build([CsiAggregation, CsiAggregationInterval, JobGroupWeight, EventResult, JobResult, Job, CsiConfiguration])
@Mock([CsiAggregation, CsiAggregationInterval, JobGroupWeight, EventResult, JobResult, Job, CsiConfiguration,
        CsiSystem, JobGroup, CsiAggregationUpdateEvent])
class CsiSystemCsiAggregationServiceSpec extends Specification {

    CsiAggregationInterval dailyInterval

    def doWithSpring = {
        csiAggregationUpdateEventDaoService(CsiAggregationUpdateEventDaoService)
        csiAggregationUtilService(CsiAggregationUtilService)
        meanCalcService(MeanCalcService)
    }

    void setup() {
        dailyInterval = CsiAggregationInterval.build(intervalInMinutes: CsiAggregationInterval.DAILY)
        service.jobGroupCsiAggregationService = Stub(JobGroupCsiAggregationService) {
            getOrCalculateShopCsiAggregations(_, _, _, _) >> [CsiAggregation.buildWithoutSave()]
        }
    }

    void "calculate CsiAggregation from single weighted value"() {
        setup:
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        service.csiValueService = Stub(CsiValueService) {
            getWeightedCsiValues(_, _) >> [
                    new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d))
            ]
        }
        CsiSystem csiSystem = TestDataUtil.buildCsiSystem()

        when: "the csiAggregations should be retrieved or calculated"
        List<CsiAggregation> calculated = service.getOrCalculateCsiSystemCsiAggregations(startedTime.toDate(),
                startedTime.toDate(), dailyInterval, [csiSystem])
        List<CsiAggregation> mvs = service.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)

        then: "a new one is created and calculated from the weighted value"
        calculated.size() == 1
        calculated[0].isCalculated()
        calculated[0].csByWptDocCompleteInPercent == 12d
        mvs == calculated
    }


    void "calculate CsiAggregation from multiple weighted values"() {
        setup:
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        service.csiValueService = Stub(CsiValueService) {
            getWeightedCsiValues(_, _) >> [
                    new WeightedCsiValue(weightedValue: new WeightedValue(value: 3d, weight: 3d)),
                    new WeightedCsiValue(weightedValue: new WeightedValue(value: 2d, weight: 2d)),
                    new WeightedCsiValue(weightedValue: new WeightedValue(value: 1d, weight: 5d))
            ]
        }
        CsiSystem csiSystem = TestDataUtil.buildCsiSystem()

        when: "the csiAggregations should be retrieved or calculated"
        List<CsiAggregation> calculated = service.getOrCalculateCsiSystemCsiAggregations(startedTime.toDate(),
                startedTime.toDate(), dailyInterval, [csiSystem])
        List<CsiAggregation> mvs = service.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)

        then: "a new one is created and calculated from several weighted values"
        calculated.size() == 1
        calculated[0].isCalculated()
        that calculated[0].csByWptDocCompleteInPercent, closeTo(1.8, 0.0001)
        mvs == calculated
    }

    void "create CsiAggregation without values if no weighted value is given"() {
        given:
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        service.csiValueService = Stub(CsiValueService) {
            getWeightedCsiValues(_, _) >> []
        }
        CsiSystem csiSystem = TestDataUtil.buildCsiSystem()

        when: "the csiAggregations should be retrieved or calculated"
        List<CsiAggregation> calculatedMvs = service.getOrCalculateCsiSystemCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [csiSystem])
        List<CsiAggregation> mvs = service.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)

        then: "a new CsiAggregation is calculated, but with no value"
        calculatedMvs.size() == 1
        calculatedMvs[0].aggregationType == AggregationType.CSI_SYSTEM
        calculatedMvs[0].isCalculated()
        calculatedMvs[0].csByWptDocCompleteInPercent == null
        calculatedMvs[0].underlyingEventResultsByWptDocCompleteAsList.isEmpty()
        mvs == calculatedMvs
    }

    void "mark outdated CsiAggregation as outdated"() {
        setup:
        DateTime startDate = new DateTime(2013, 5, 3, 0, 0)
        CsiSystem csiSystem = TestDataUtil.buildCsiSystem()
        EventResult eventResult = EventResult.build(jobResult: JobResult.build(job: Job.build(jobGroup: csiSystem.jobGroupWeights[1].jobGroup)))
        CsiAggregation csiAggregation = CsiAggregation.build(started: startDate.toDate(), csiSystem: csiSystem)

        when: "CsiAggregations in a given interval, for a given eventResult should be marked as outdated"
        service.markCaAsOutdated(startDate, eventResult, dailyInterval)

        then: "a new UpdateEvent with cause OUTDATED is created for that CsiAggregation"
        List<CsiAggregationUpdateEvent> updateEvents = CsiAggregationUpdateEvent.list()
        updateEvents.size() == 1
        updateEvents*.updateCause == [CsiAggregationUpdateEvent.UpdateCause.OUTDATED]
        updateEvents*.id == [csiAggregation.id]
    }
}
