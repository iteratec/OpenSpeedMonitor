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
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregationType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.CsiValueService
import de.iteratec.osm.result.EventResult
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.joda.time.DateTime
import spock.lang.Shared

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Integration
@Rollback
class JobGroupCsiAggregationServiceIntegrationSpec extends NonTransactionalIntegrationSpec {
    CsiAggregationInterval weeklyInterval, dailyInterval, hourlyInterval
    @Shared JobGroup jobGroup1, jobGroup2, jobGroup3
    Page page

    Browser browser

    static final double DELTA = 1e-15
    DateTime startDate = new DateTime(2013, 5, 16, 0, 0, 0)

    JobGroupCsiAggregationService jobGroupCsiAggregationService

    def setup() {
        createTestDataCommonToAllTests()
    }

    def cleanup() {
        jobGroupCsiAggregationService.csiValueService = grailsApplication.mainContext.getBean('csiValueService')
    }

    void "test findAll"() {
        Integer countCsiAgg = 3

        expect: "find all csiAgg"
        jobGroupCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, JobGroup.list()).size() == countCsiAgg
    }

    void "test findAllByJobGroups"() {
        when: "job groups are searched"
        def numberOfJobGroups = jobGroupCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, jobGroups).size()

        then: "the number of jobs is equal to the expected number"
        numberOfJobGroups == expectedNumberOfJobGroups

        where: "the job groups are searched"
        expectedNumberOfJobGroups | jobGroups
        3                         | [jobGroup1, jobGroup2, jobGroup3]
        2                         | [jobGroup1, jobGroup2]
        2                         | [jobGroup2, jobGroup3]
        1                         | [jobGroup2]

    }

    /**
     * Tests calculation of daily-shopAggregator{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test one single pageAggregator-{@link CsiAggregation}s exists, which should be the database of the calculation of the daily-shopAggregator-{@link CsiAggregation}.
     */
    void "test calculation_DailyInterval_SingleDailyCsiAgg"() {
        given: "a start time and a mocked CsiValueService with weighted csi values"
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [1, 2, 3])]
        mockCsiValueService(weightedCsiValuesToReturnInMock, [])

        List<CsiAggregation> csiAgg = jobGroupCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list())
        assertEquals(0, csiAgg.size())

        when: "a csi is calculated"
        List<CsiAggregation> calculatedCsiAgg = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1])
        csiAgg = jobGroupCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list())

        then: "there is one calculated csiAgg"
        assertEquals(1, calculatedCsiAgg.size())
        assertTrue(calculatedCsiAgg[0].isCalculated())
        assertEquals(12d, calculatedCsiAgg[0].csByWptDocCompleteInPercent, DELTA)
        assertEquals(1, csiAgg.size())
    }

    /**
     * Tests calculation of daily-shopAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test pageAggregator-{@link CsiAggregation}s with different weights exist.
     */
    void "test calculation_DailyInterval_MultipleDailyCsiAgg"() {
        given: "a start time and a mocked CsiValueService with three weighted csi values"
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        double valueFirstCsiAgg = 12d
        double pageWeightFirstCsiAgg = 1d
        double valueSecondCsiAgg = 10d
        double pageWeightSecondCsiAgg = 2d
        double valueThirdCsiAgg = 13d
        double pageWeightThirdCsiAgg = 1d
        double sumOfAllWeights = 4d

        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: valueFirstCsiAgg, weight: pageWeightFirstCsiAgg), underlyingEventResultIds: [1, 2, 3]),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: valueSecondCsiAgg, weight: pageWeightSecondCsiAgg), underlyingEventResultIds: [4]),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: valueThirdCsiAgg, weight: pageWeightThirdCsiAgg), underlyingEventResultIds: [5, 6])]
        mockCsiValueService(weightedCsiValuesToReturnInMock, [])

        List<CsiAggregation> csiAgg = jobGroupCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list())
        assertEquals(0, csiAgg.size())

        when:"a csi is calculated"
        List<CsiAggregation> calculatedCsiAgg = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1])
        csiAgg = jobGroupCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list())

        then: "there is one calculated csiAgg"
        assertEquals(1, calculatedCsiAgg.size())
        assertEquals(AggregationType.JOB_GROUP, calculatedCsiAgg[0].aggregationType)
        assertTrue(calculatedCsiAgg[0].isCalculated())
        assertEquals(
                (((valueFirstCsiAgg * pageWeightFirstCsiAgg) + (valueSecondCsiAgg * pageWeightSecondCsiAgg) + (valueThirdCsiAgg * pageWeightThirdCsiAgg)) / sumOfAllWeights),
                calculatedCsiAgg[0].csByWptDocCompleteInPercent,
                DELTA
        )
        assertEquals(1, csiAgg.size())
    }

    /**
     * Tests calculation of daily-shopAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test no pageAggregator-{@link CsiAggregation}s exist, which are database of the calculation of daily-shopAggregator-{@link CsiAggregation}s. So all calculated values should have state {@link Calculated#YesNoData}
     */
    void "test calculation_DailyInterval_MultipleHourlyCsiAgg_YesCalculatedNoData"() {
        given: "a start time and a mocked CsiValueService with no weighted csi values"
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        mockCsiValueService([], [])
        List<CsiAggregation> csiAgg = jobGroupCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list())
        assertEquals(0, csiAgg.size())

        when:"a csi is calculated"
        List<CsiAggregation> calculatedCsiAgg = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1])
        csiAgg = jobGroupCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list())

        then: "there is one calculated csiAgg with no data"
        assertEquals(1, calculatedCsiAgg.size())
        assertTrue(calculatedCsiAgg[0].isCalculated())
        assertEquals(null, calculatedCsiAgg[0].csByWptDocCompleteInPercent)
        assertEquals(0, calculatedCsiAgg[0].getUnderlyingEventResultsByWptDocComplete().size())
        assertEquals(1, csiAgg.size())
    }


    // create testdata common to all tests /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * All test data created here has to be deleted in cleanup method after every test!!!
     * That's because these integration tests have to run without an own transaction which would be
     * rolled back in the end of every test.
     *
     * Integration tests that test code with own separate transactions wouldn't see test data if creation in test would
     * happen in an own transaction.
     */
    private createTestDataCommonToAllTests() {
        weeklyInterval = CsiAggregationInterval.build(
                intervalInMinutes: CsiAggregationInterval.WEEKLY
        )

        dailyInterval = CsiAggregationInterval.build(
                intervalInMinutes: CsiAggregationInterval.DAILY
        )

        hourlyInterval = CsiAggregationInterval.build(
                intervalInMinutes: CsiAggregationInterval.HOURLY
        )

        jobGroup1 = JobGroup.build()
        jobGroup2 = JobGroup.build()
        jobGroup3 = JobGroup.build()

        page = Page.build(name: "page")
        browser = Browser.build(name: "Test")

        [jobGroup1, jobGroup2, jobGroup3].each { jobGroup ->
            CsiAggregation.build(
                    started: startDate.toDate(),
                    interval: weeklyInterval,
                    aggregationType: AggregationType.JOB_GROUP,
                    jobGroup: jobGroup
            )
        }
    }

    /**
     * Mocks methods of {@link WeightingService}.
     */
    private void mockCsiValueService(List<WeightedCsiValue> toReturnFromGetWeightedCsiValues, List<WeightedCsiValue> toReturnFromGetWeightedCsiValuesByVisuallyComplete) {
        jobGroupCsiAggregationService.csiValueService = Stub(CsiValueService){
            getWeightedCsiValues(_, _, _) >> toReturnFromGetWeightedCsiValues
            getWeightedCsiValuesByVisuallyComplete(_, _, _) >> toReturnFromGetWeightedCsiValuesByVisuallyComplete
        }
    }
}
