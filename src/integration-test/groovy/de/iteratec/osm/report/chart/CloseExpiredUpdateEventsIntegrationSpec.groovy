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

package de.iteratec.osm.report.chart

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdaterDummy
import de.iteratec.osm.csi.CsiAggregationUpdateEventCleanupService
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.result.MeasuredEvent
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

@Integration
@Rollback
class CloseExpiredUpdateEventsIntegrationSpec extends NonTransactionalIntegrationSpec {

    CsiAggregationUpdateEventCleanupService csiAggregationUpdateEventCleanupService

    CsiAggregationUtilService csiAggregationUtilService
    CsiAggregationDaoService csiAggregationDaoService
    InMemoryConfigService inMemoryConfigService

    CsiAggregationInterval weeklyInterval
    CsiAggregationInterval dailyInterval
    CsiAggregationInterval hourlyInterval

    final DateTime mockedExecutionTimeOfCleanup = new DateTime(2014, 7, 7, 5, 30, 0, DateTimeZone.UTC)

    DateTime actualWeekStart
    DateTime lastWeekStart
    DateTime secondToLastWeekStart

    DateTime actualDayStart
    DateTime lastDayStart
    DateTime secondToLastDayStart

    DateTime actualHourStart
    DateTime lastHourStart
    DateTime secondLastHourStart

    DateTime dateOfUpdate

    def setup() {
            mocksCommonToAllTests()
            createTestDataCommonToAllTests()
    }

    def cleanup() {
        csiAggregationUpdateEventCleanupService.csiAggregationDaoService.csiAggregationUtilService = grailsApplication.mainContext.getBean('csiAggregationUtilService')
        csiAggregationUpdateEventCleanupService.batchActivityService = grailsApplication.mainContext.getBean('batchActivityService')
    }


    void "Outdated daily page CSI aggregations get closed and calculated correctly"() {
        setup: "Create two outdated CSI aggregations and no event results"
        buildCsiAggregationWithAnOutdatedUpdateEvent(lastDayStart.toDate(), dailyInterval, AggregationType.PAGE, false)
        buildCsiAggregationWithAnOutdatedUpdateEvent(secondToLastDayStart.toDate(), dailyInterval, AggregationType.PAGE, false)

        when: "they are getting closed programmatically"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300, false)

        then: "the outdated update events get deleted and csi aggregations get closed and calculated"
        List<CsiAggregation> csiAggregations = CsiAggregation.list()

        CsiAggregationUpdateEvent.list().size() == 0
        csiAggregations.size() == 2
        csiAggregations.every { csiAggregation ->
            csiAggregation.closedAndCalculated &&
            csiAggregation.isCalculatedWithoutData()
        }
    }

    void "Outdated weekly page CSI aggregations get closed and calculated correctly"() {
        setup: "Create two outdated CSI aggregations and no event results"
        buildCsiAggregationWithAnOutdatedUpdateEvent(lastWeekStart.toDate(), weeklyInterval, AggregationType.PAGE, false)
        buildCsiAggregationWithAnOutdatedUpdateEvent(secondToLastWeekStart.toDate(), weeklyInterval, AggregationType.PAGE, false)

        when: "they are getting closed programmatically"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300, false)

        then: "the outdated update events get deleted and csi aggregations get closed and calculated"
        List<CsiAggregation> csiAggregations = CsiAggregation.list()

        CsiAggregationUpdateEvent.list().size() == 0
        csiAggregations.size() == 2
        csiAggregations.every { csiAggregation ->
            csiAggregation.closedAndCalculated &&
            csiAggregation.isCalculatedWithoutData()
        }
    }

    void "Outdated daily job group CSI aggregations get closed and calculated correctly"() {
        setup: "Create two outdated CSI aggregations and no event results"
        buildCsiAggregationWithAnOutdatedUpdateEvent(lastDayStart.toDate(), dailyInterval, AggregationType.JOB_GROUP, false)
        buildCsiAggregationWithAnOutdatedUpdateEvent(secondToLastDayStart.toDate(), dailyInterval, AggregationType.JOB_GROUP, false)

        when: "they are getting closed programmatically"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then: "the outdated update events get deleted and CSI aggregations get closed and calculated"
        CsiAggregation.withNewTransaction {
            List<CsiAggregation> csiAggregations = CsiAggregation.list()

            CsiAggregationUpdateEvent.list().size() == 0
            csiAggregations.size() == 2
            csiAggregations.every { csiAggregation ->
                csiAggregation.closedAndCalculated &&
                        csiAggregation.isCalculatedWithoutData()
            }
        }
    }

    void "Outdated weekly job group CSI aggregations get closed and calculated correctly"() {
        setup: "Create two outdated CSI aggregations and no event results"
        buildCsiAggregationWithAnOutdatedUpdateEvent(lastWeekStart.toDate(), weeklyInterval, AggregationType.JOB_GROUP, false)
        buildCsiAggregationWithAnOutdatedUpdateEvent(secondToLastWeekStart.toDate(), weeklyInterval, AggregationType.JOB_GROUP, false)

        when: "they are getting closed programmatically"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then: "the outdated update events get deleted and CSI aggregations get closed and calculated"
        CsiAggregation.withNewTransaction {
            List<CsiAggregation> csiAggregations = CsiAggregation.list()

            CsiAggregationUpdateEvent.list().size() == 0
            csiAggregations.size() == 2
            csiAggregations.every { csiAggregation ->
                csiAggregation.closedAndCalculated &&
                csiAggregation.isCalculatedWithoutData()
            }
        }
    }


    void "Closing daily job group CSI aggregations once results in appropriate number of page CSI aggregations"() {
        setup: "Create two outdated job group CSI aggregations and no event results"
        final int numberOfPages = 3
        numberOfPages.times {
            Page.build()
        }

        buildCsiAggregationWithAnOutdatedUpdateEvent(lastDayStart.toDate(), dailyInterval, AggregationType.JOB_GROUP, false, Page.list().first())
        buildCsiAggregationWithAnOutdatedUpdateEvent(secondToLastDayStart.toDate(), dailyInterval, AggregationType.JOB_GROUP, false, Page.list().first())

        when: "the job group CSI aggregations are getting closed programmatically once"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then: "there are numberOfPages * numberOfJobGroupCsiAggregations open page CSI aggregations"
        List<CsiAggregation> pageCsiAggregations = CsiAggregation.list().findAll { it.aggregationType == AggregationType.PAGE }
        int numberOfJobGroupCsiAggregations = CsiAggregation.list().findAll { it.aggregationType == AggregationType.JOB_GROUP }.size()

        pageCsiAggregations.size() == numberOfPages * numberOfJobGroupCsiAggregations
        csiAggregationDaoService.getUpdateEvents(pageCsiAggregations*.ident()).size() == numberOfPages * numberOfJobGroupCsiAggregations
        !pageCsiAggregations.every {
            it.closedAndCalculated
        }
    }


    void "Closing daily job group CSI aggregations twice leads to closed and calculated page CSI aggregations"() {
        setup: "Create two outdated job group CSI aggregations and no event results"
        final int numberOfPages = 3
        numberOfPages.times {
            Page.build()
        }

        buildCsiAggregationWithAnOutdatedUpdateEvent(lastDayStart.toDate(), dailyInterval, AggregationType.JOB_GROUP, false, Page.list().first())
        buildCsiAggregationWithAnOutdatedUpdateEvent(secondToLastDayStart.toDate(), dailyInterval, AggregationType.JOB_GROUP, false, Page.list().first())

        when: "the job group CSI aggregations are getting closed programmatically once"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        and: "then a second time"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then: "the page csi aggregations created while closing job group aggregations for the first time are closed and calculated, too."
        List<CsiAggregation> pageCsiAggregations = CsiAggregation.list().findAll { it.aggregationType == AggregationType.PAGE }
        int numberOfJobGroupCsiAggregations = CsiAggregation.list().findAll { it.aggregationType == AggregationType.JOB_GROUP }.size()

        pageCsiAggregations.size() == numberOfPages * numberOfJobGroupCsiAggregations
        csiAggregationDaoService.getUpdateEvents(pageCsiAggregations*.ident()).size() == 0
        pageCsiAggregations.every {
            it.closedAndCalculated
        }
    }


    void "Closing weekly JobGroup CsiAggregations once results in appropriate number of page CSI aggregations"() {
        setup: "Create two outdated job group CSI aggregations and no event results"
        final int numberOfPages = 3
        numberOfPages.times {
            Page.build()
        }

        buildCsiAggregationWithAnOutdatedUpdateEvent(lastWeekStart.toDate(), weeklyInterval, AggregationType.JOB_GROUP, false, Page.list().first())
        buildCsiAggregationWithAnOutdatedUpdateEvent(secondToLastWeekStart.toDate(), weeklyInterval, AggregationType.JOB_GROUP, false, Page.list().first())

        when: "the job group CSI aggregations are closed programmatically once"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then: "the page CSI aggregations created while closing job group aggregations for the first time are closed and calculated, too."
        List<CsiAggregation> pageCsiAggregations = CsiAggregation.list().findAll { it.aggregationType == AggregationType.PAGE }
        int numberOfJobGroupCsiAggregations = CsiAggregation.list().findAll { it.aggregationType == AggregationType.JOB_GROUP }.size()

        pageCsiAggregations.size() == numberOfPages * numberOfJobGroupCsiAggregations
        csiAggregationDaoService.getUpdateEvents(pageCsiAggregations*.ident()).size() == numberOfPages * numberOfJobGroupCsiAggregations
        !pageCsiAggregations.every {
            it.closedAndCalculated
        }
    }

    void "Closing weekly job group CSI aggregations twice leads to closed and calculated page CSI aggregations"() {
        setup: "Create two outdated job group CSI aggregations and no event results"
        final int numberOfPages = 3
        numberOfPages.times {
            Page.build()
        }

        buildCsiAggregationWithAnOutdatedUpdateEvent(lastWeekStart.toDate(), weeklyInterval, AggregationType.JOB_GROUP, false, Page.list().first())
        buildCsiAggregationWithAnOutdatedUpdateEvent(secondToLastWeekStart.toDate(), weeklyInterval, AggregationType.JOB_GROUP, false, Page.list().first())

        when: "the job group CSI aggregations are closed programmatically once"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        and: "then a second time"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then: "Page csi aggregations created while closing JobGroup aggregations for the first time are closed and calculated, too."
        List<CsiAggregation> pageCsiAggregations = CsiAggregation.list().findAll { it.aggregationType == AggregationType.PAGE }
        int numberOfJobGroupCsiAggregations = CsiAggregation.list().findAll { it.aggregationType == AggregationType.JOB_GROUP }.size()

        pageCsiAggregations.size() == numberOfPages * numberOfJobGroupCsiAggregations
        csiAggregationDaoService.getUpdateEvents(pageCsiAggregations*.ident()).size() == 0
        pageCsiAggregations.every {
            it.closedAndCalculated
        }
    }

    void "Daily page CSI aggregation younger than their expire time shouldn't be closed"() {
        setup: "Create daily page CsiAggregation younger than their expire time"
        buildCsiAggregationWithAnOutdatedUpdateEvent(actualDayStart.toDate(), dailyInterval, AggregationType.PAGE, false)

        when: "CSI aggregations are getting closed with expire time > date of created CSI aggregation"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then: "the created CSI aggregation is still open"
        List<CsiAggregation> csiAggregations = CsiAggregation.list()
        CsiAggregationUpdateEvent.list().size() == 1

        csiAggregations.size() == 1
        csiAggregations.every {
            !it.closedAndCalculated &&
            !it.isCalculated()
        }
    }

    void "Weekly page CSI aggregation younger than their expire time shouldn't be closed"() {
        setup: "Create weekly page CSI aggregation younger than their expire time"
        buildCsiAggregationWithAnOutdatedUpdateEvent(actualWeekStart.toDate(), weeklyInterval, AggregationType.PAGE, false)

        when: "CSI aggregations are getting closed with expire time > date of created CSI aggregation"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then: "the created CSI aggregation is still open"
        List<CsiAggregation> csiAggregations = CsiAggregation.list()
        CsiAggregationUpdateEvent.list().size() == 1

        csiAggregations.size() == 1
        csiAggregations.every {
            !it.closedAndCalculated &&
            !it.isCalculated()
        }
    }

    void "Daily job group CSI aggregation younger than their expire time shouldn't be closed"() {
        setup: "Create daily job group CSI aggregation younger than their expire time"
        buildCsiAggregationWithAnOutdatedUpdateEvent(actualDayStart.toDate(), dailyInterval, AggregationType.JOB_GROUP, false)

        when: "CSI aggregations are getting closed with expire time > date of created CSI aggregation"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then: "the created CSI aggregation is still open"
        List<CsiAggregation> csiAggregations = CsiAggregation.list()
        CsiAggregationUpdateEvent.list().size() == 1

        csiAggregations.size() == 1
        csiAggregations.every {
            !it.closedAndCalculated &&
            !it.isCalculated()
        }
    }

    void "Weekly job group CSI aggregation younger than their expire time shouldn't be closed"() {
        setup: "Create weekly job group CSI aggregation younger than their expire time"
        buildCsiAggregationWithAnOutdatedUpdateEvent(actualWeekStart.toDate(), weeklyInterval, AggregationType.JOB_GROUP, false)

        when: "CSI Aggregations are getting closed with expire time > date of created CSI aggregation"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then: "the created CSI aggregation is still open"
        List<CsiAggregation> csiAggregations = CsiAggregation.list()
        CsiAggregationUpdateEvent.list().size() == 1

        csiAggregations.size() == 1
        csiAggregations.every {
            !it.closedAndCalculated &&
            !it.isCalculated()
        }
    }

    private void mocksCommonToAllTests() {
        csiAggregationUpdateEventCleanupService.csiAggregationDaoService.csiAggregationUtilService = Spy(CsiAggregationUtilService) {
            getNowInUtc() >> mockedExecutionTimeOfCleanup
        }
        csiAggregationUpdateEventCleanupService.batchActivityService = Spy(BatchActivityService) {
            getActiveBatchActivity(_, _, _, _, _) >> {
                return new BatchActivityUpdaterDummy("test", "test", Activity.UPDATE, 50, 5000)
            }
        }
    }

    private void createTestDataCommonToAllTests() {
        hourlyInterval = CsiAggregationInterval.build(name: "hourly", intervalInMinutes: CsiAggregationInterval.HOURLY)
        dailyInterval = CsiAggregationInterval.build(name: "daily", intervalInMinutes: CsiAggregationInterval.DAILY)
        weeklyInterval = CsiAggregationInterval.build(name: "weekly", intervalInMinutes: CsiAggregationInterval.WEEKLY)

        actualHourStart = csiAggregationUtilService.resetToStartOfActualInterval(mockedExecutionTimeOfCleanup, hourlyInterval.intervalInMinutes)
        lastHourStart = csiAggregationUtilService.subtractOneInterval(actualHourStart, dailyInterval.intervalInMinutes)
        secondLastHourStart = csiAggregationUtilService.subtractOneInterval(lastHourStart, dailyInterval.intervalInMinutes)

        actualDayStart = csiAggregationUtilService.resetToStartOfActualInterval(mockedExecutionTimeOfCleanup, dailyInterval.intervalInMinutes)
        lastDayStart = csiAggregationUtilService.subtractOneInterval(actualDayStart, dailyInterval.intervalInMinutes)
        secondToLastDayStart = csiAggregationUtilService.subtractOneInterval(lastDayStart, dailyInterval.intervalInMinutes)

        actualWeekStart = csiAggregationUtilService.resetToStartOfActualInterval(mockedExecutionTimeOfCleanup, weeklyInterval.intervalInMinutes)
        lastWeekStart = csiAggregationUtilService.subtractOneInterval(actualWeekStart, weeklyInterval.intervalInMinutes)
        secondToLastWeekStart = csiAggregationUtilService.subtractOneInterval(lastWeekStart, weeklyInterval.intervalInMinutes)

        dateOfUpdate = csiAggregationUtilService.addOneInterval(mockedExecutionTimeOfCleanup, dailyInterval.intervalInMinutes)

        inMemoryConfigService.activateMeasurementsGenerally()
    }

    private void buildCsiAggregationWithAnOutdatedUpdateEvent(Date started, CsiAggregationInterval interval, AggregationType aggregationType, boolean closedAndCalculated, Page page = null) {
        Page pageToUse = page ? page : Page.build()

        CsiAggregation csiAggregation = CsiAggregation.build(
                started: started,
                interval: interval,
                aggregationType: aggregationType,
                page: pageToUse,
                measuredEvent: MeasuredEvent.build(testedPage: pageToUse),
                underlyingEventResultsByWptDocComplete: '1,2,3',
                csByWptDocCompleteInPercent: null,
                closedAndCalculated: closedAndCalculated
        )

        CsiAggregationUpdateEvent.build(
                dateOfUpdate: dateOfUpdate.toDate(),
                updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED,
                csiAggregationId: csiAggregation.ident()
        )
    }
}
