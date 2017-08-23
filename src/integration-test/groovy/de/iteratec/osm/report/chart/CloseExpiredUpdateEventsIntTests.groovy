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
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.csi.CsiAggregationUpdateEventCleanupService
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.util.mop.ConfineMetaClassChanges

/**
 * Methods in this class test the functionality to close {@link CsiAggregation}s.
 * Closing means: 
 * <ul>
 * <li>set attribute closedAndCalculated to true</li>
 * <li>calculate CsiAggregation if necessary</li>
 * <li>delete all {@link CsiAggregationUpdateEvent}s of CsiAggregation</li>
 * </ul>
 *
 */
@Integration
@Rollback
@ConfineMetaClassChanges([CsiAggregationUtilService, BatchActivityService])
class CloseExpiredUpdateEventsIntTests extends NonTransactionalIntegrationSpec {

    CsiAggregationUtilService csiAggregationUtilService
    CsiAggregationUpdateEventCleanupService csiAggregationUpdateEventCleanupService
    CsiAggregationDaoService csiAggregationDaoService
    InMemoryConfigService inMemoryConfigService

    CsiAggregationInterval weeklyInterval
    CsiAggregationInterval dailyInterval
    CsiAggregationInterval hourlyInterval

    DateTime mockedExecTimeOfCleanup
    DateTime actualWeekStart
    DateTime lastWeekStart
    DateTime secondLastWeekStart
    DateTime actualDayStart
    DateTime lastDayStart
    DateTime secondLastDayStart
    DateTime actualHourStart
    DateTime lastHourStart
    DateTime secondLastHourStart

    final static int NUMBER_OF_PAGES = 3

    def setup() {

        CsiAggregation.withNewSession {
            mocksCommonToAllTests()

            createTestdataCommonToAllTests()
        }
    }

    void "Outdated daily page CsiAggregations get closed and calculated correctly"() {
        setup: "Create two outdated CsiAggregations and no EventResults"
        CsiAggregation.withNewTransaction {
            createCsiAggregationsWithUpdateEventOutdated(lastDayStart.toDate(), dailyInterval, AggregationType.PAGE, false, JobGroup.list()[0], Page.list()[0])
            createCsiAggregationsWithUpdateEventOutdated(secondLastDayStart.toDate(), dailyInterval, AggregationType.PAGE, false, JobGroup.list()[0], Page.list()[0])
        }

        when: "They are closed programmatically"
        CsiAggregation.withNewTransaction {
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300, false)
        }

        then: "OUTDATED update events get deleted and csi aggregations got closed and calculated"
        CsiAggregation.withNewTransaction {
            List<CsiAggregation> csiAggregations = CsiAggregation.list()
            CsiAggregationUpdateEvent.list().size() == 0
            csiAggregations.size() == 2
            csiAggregations.every {
                it.closedAndCalculated &&
                        it.isCalculatedWithoutData()
            }
        }
    }

    void "Outdated weekly page CsiAggregations get closed and calculated correctly"() {
        setup: "Create two outdated CsiAggregations and no EventResults"
        int csiAggregationCountBefore
        boolean everyCsiAggClosedAndCalculatedBefore
        int updateEventCountBefore
        List<CsiAggregation> csiAggregations
        List<CsiAggregationUpdateEvent> updateEvents
        CsiAggregation.withNewTransaction {
            createCsiAggregationsWithUpdateEventOutdated(lastWeekStart.toDate(), weeklyInterval, AggregationType.PAGE, false, JobGroup.list()[0], Page.list()[0])
            createCsiAggregationsWithUpdateEventOutdated(secondLastWeekStart.toDate(), weeklyInterval, AggregationType.PAGE, false, JobGroup.list()[0], Page.list()[0])

            csiAggregations = CsiAggregation.list()
            updateEvents = csiAggregationDaoService.getUpdateEvents(csiAggregations*.ident())
            csiAggregationCountBefore = csiAggregations.size()
            everyCsiAggClosedAndCalculatedBefore = csiAggregations.every {
                it.closedAndCalculated &&
                        it.isCalculated()
            }
            updateEventCountBefore = updateEvents.size()
        }
        when: "They are closed programmatically"
        CsiAggregation.withNewTransaction {
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300, false)
            updateEvents = csiAggregationDaoService.getUpdateEvents(csiAggregations*.ident())
        }

        then: "OUTDATED update events get deleted and csi aggregations got closed and calculated"
        CsiAggregation.withNewTransaction {
            csiAggregations = CsiAggregation.list()
            updateEventCountBefore == 2
            updateEvents.size() == 0

            csiAggregationCountBefore == 2
            csiAggregations.size() == 2

            everyCsiAggClosedAndCalculatedBefore == false
            csiAggregations.every {
                it.closedAndCalculated &&
                        it.isCalculated() &&
                        it.isCalculatedWithoutData()
            }
        }
    }

    void "Outdated daily JobGroup CsiAggregations get closed and calculated correctly"() {
        setup: "Create two outdated CsiAggregations and no EventResults"
        List<CsiAggregation> jobGroupCsiAggregations
        List<CsiAggregation> csiAggregations
        List<CsiAggregationUpdateEvent> updateEventsOfJobGroupCsiAggregations
        int updateEventCountBefore
        int csiAggregationCountBefore
        boolean everyCsiAggClosedAndCalculatedBefore
        CsiAggregation.withNewSession {
            createCsiAggregationsWithUpdateEventOutdated(lastDayStart.toDate(), dailyInterval, AggregationType.JOB_GROUP, false, JobGroup.list()[0], null)
            createCsiAggregationsWithUpdateEventOutdated(secondLastDayStart.toDate(), dailyInterval, AggregationType.JOB_GROUP, false, JobGroup.list()[0], null)

            csiAggregations = CsiAggregation.list()
            jobGroupCsiAggregations = csiAggregations.findAll {
                it.aggregationType == AggregationType.JOB_GROUP
            }
            csiAggregationCountBefore = jobGroupCsiAggregations.size()
            everyCsiAggClosedAndCalculatedBefore = jobGroupCsiAggregations.every {
                it.closedAndCalculated &&
                        it.isCalculated()
            }
            updateEventsOfJobGroupCsiAggregations = csiAggregationDaoService.getUpdateEvents(jobGroupCsiAggregations*.ident())
            updateEventCountBefore = updateEventsOfJobGroupCsiAggregations.size()
        }

        when: "They are closed programmatically"
        CsiAggregation.withNewSession {
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }
        csiAggregations = CsiAggregation.list()
        jobGroupCsiAggregations = csiAggregations.findAll { it.aggregationType == AggregationType.JOB_GROUP }
        updateEventsOfJobGroupCsiAggregations = csiAggregationDaoService.getUpdateEvents(jobGroupCsiAggregations*.ident())

        then: "OUTDATED update events get deleted and csi aggregations got closed and calculated"
        updateEventCountBefore == 2
        updateEventsOfJobGroupCsiAggregations.size() == 0
        csiAggregationCountBefore == 2
        jobGroupCsiAggregations.size() == 2
        everyCsiAggClosedAndCalculatedBefore == false

        CsiAggregation.withNewSession {
            jobGroupCsiAggregations.every {
                it.closedAndCalculated &&
                        it.isCalculated() &&
                        it.isCalculatedWithoutData()
            }
        }
    }

    void "Closing daily JobGroup CsiAggregations twice leads to closed and calculated page CsiAggregations"() {
        setup: "Create two outdated JobGroup CsiAggregations and no EventResults"
        int numberOfJobGroupCsiAggregations = 2
        int updateEventCountBefore
        int pageCsiAggregationCountBefore
        boolean everyPageCsiAggClosedAndCalculatedBefore
        List<CsiAggregation> pageCsiAggregations
        List<CsiAggregationUpdateEvent> updateEventsOfPageCsiAggregations
        CsiAggregation.withNewSession {
            createCsiAggregationsWithUpdateEventOutdated(lastDayStart.toDate(), dailyInterval, AggregationType.JOB_GROUP, false, JobGroup.list()[0], null)
            createCsiAggregationsWithUpdateEventOutdated(secondLastDayStart.toDate(), dailyInterval, AggregationType.JOB_GROUP, false, JobGroup.list()[0], null)
        }

        when: "The JobGroup CsiAggregations are closed programmatically once"
        CsiAggregation.withNewSession {
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
            pageCsiAggregations = CsiAggregation.list().findAll {
                it.aggregationType == AggregationType.PAGE
            }
            pageCsiAggregationCountBefore = pageCsiAggregations.size()
            everyPageCsiAggClosedAndCalculatedBefore = pageCsiAggregations.every {
                it.closedAndCalculated
            }
            updateEventsOfPageCsiAggregations = csiAggregationDaoService.getUpdateEvents(pageCsiAggregations*.ident())
            updateEventCountBefore = updateEventsOfPageCsiAggregations.size()
        }

        and: "Then a second time"
        CsiAggregation.withNewSession {
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }

        then: "Page csi aggregations created while closing JobGroup aggregations for the first time are closed and calculated, too."
        CsiAggregation.withNewSession {
            pageCsiAggregations = CsiAggregation.list().findAll { it.aggregationType == AggregationType.PAGE }
            updateEventsOfPageCsiAggregations = csiAggregationDaoService.getUpdateEvents(pageCsiAggregations*.ident())
            pageCsiAggregationCountBefore == NUMBER_OF_PAGES * numberOfJobGroupCsiAggregations
            updateEventCountBefore == NUMBER_OF_PAGES * numberOfJobGroupCsiAggregations
            everyPageCsiAggClosedAndCalculatedBefore == false

            pageCsiAggregations.size() == NUMBER_OF_PAGES * numberOfJobGroupCsiAggregations
            updateEventsOfPageCsiAggregations.size() == 0
            pageCsiAggregations.every {
                it.closedAndCalculated
            }
        }
    }

    void "Outdated weekly JobGroup CsiAggregations get closed and calculated correctly"() {
        setup: "Create two outdated CsiAggregations and no EventResults"
        int csiAggregationCountBefore
        int updateEventCountBefore
        boolean everyCsiAggClosedAndCalculatedBefore
        List<CsiAggregation> csiAggregations
        List<CsiAggregation> jobGroupCsiAggregations
        List<CsiAggregationUpdateEvent> updateEventsOfJobGroupCsiAggregations
        CsiAggregation.withNewSession {
            createCsiAggregationsWithUpdateEventOutdated(lastWeekStart.toDate(), weeklyInterval, AggregationType.JOB_GROUP, false, JobGroup.list()[0], null)
            createCsiAggregationsWithUpdateEventOutdated(secondLastWeekStart.toDate(), weeklyInterval, AggregationType.JOB_GROUP, false, JobGroup.list()[0], null)

            csiAggregations = CsiAggregation.list()
            jobGroupCsiAggregations = csiAggregations.findAll {
                it.aggregationType == AggregationType.JOB_GROUP
            }
            csiAggregationCountBefore = jobGroupCsiAggregations.size()
            everyCsiAggClosedAndCalculatedBefore = jobGroupCsiAggregations.every {
                it.closedAndCalculated &&
                        it.isCalculated()
            }
            updateEventsOfJobGroupCsiAggregations = csiAggregationDaoService.getUpdateEvents(jobGroupCsiAggregations*.ident())
            updateEventCountBefore = updateEventsOfJobGroupCsiAggregations.size()
        }

        when: "They are closed programmatically"
        CsiAggregation.withNewSession {
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }
        csiAggregations = CsiAggregation.list()
        jobGroupCsiAggregations = csiAggregations.findAll { it.aggregationType == AggregationType.JOB_GROUP }
        updateEventsOfJobGroupCsiAggregations = csiAggregationDaoService.getUpdateEvents(jobGroupCsiAggregations*.ident())

        then: "OUTDATED update events get deleted and csi aggregations got closed and calculated"
        updateEventCountBefore == 2
        updateEventsOfJobGroupCsiAggregations.size() == 0

        csiAggregationCountBefore == 2
        jobGroupCsiAggregations.size() == 2

        everyCsiAggClosedAndCalculatedBefore == false
        jobGroupCsiAggregations.every {
            it.closedAndCalculated &&
                    it.isCalculated() &&
                    it.isCalculatedWithoutData()
        }
    }

    void "Closing weekly JobGroup CsiAggregations twice leads to closed and calculated page CsiAggregations"() {
        setup: "Create two outdated JobGroup CsiAggregations and no EventResults"
        int numberOfJobGroupCsiAggregations = 2
        boolean everyPageCsiAggClosedAndCalculatedBefore
        int pageCsiAggregationCountBefore
        int updateEventCountBefore
        List<CsiAggregation> pageCsiAggregations
        List<CsiAggregationUpdateEvent> updateEventsOfPageCsiAggregations

        CsiAggregation.withNewSession {
            createCsiAggregationsWithUpdateEventOutdated(lastWeekStart.toDate(), weeklyInterval, AggregationType.JOB_GROUP, false, JobGroup.list()[0], null)
            createCsiAggregationsWithUpdateEventOutdated(secondLastWeekStart.toDate(), weeklyInterval, AggregationType.JOB_GROUP, false, JobGroup.list()[0], null)
        }

        when: "The JobGroup CsiAggregations are closed programmatically once"
        CsiAggregation.withNewSession {
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
            pageCsiAggregations = CsiAggregation.list().findAll {
                it.aggregationType == AggregationType.PAGE
            }
            pageCsiAggregationCountBefore = pageCsiAggregations.size()
            everyPageCsiAggClosedAndCalculatedBefore = pageCsiAggregations.every {
                it.closedAndCalculated
            }
            updateEventsOfPageCsiAggregations = csiAggregationDaoService.getUpdateEvents(pageCsiAggregations*.ident())
            updateEventCountBefore = updateEventsOfPageCsiAggregations.size()
        }

        and: "Then a second time"
        CsiAggregation.withNewSession {
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }
        pageCsiAggregations = CsiAggregation.list().findAll { it.aggregationType == AggregationType.PAGE }
        updateEventsOfPageCsiAggregations = csiAggregationDaoService.getUpdateEvents(pageCsiAggregations*.ident())

        then: "Page csi aggregations created while closing JobGroup aggregations for the first time are closed and calculated, too."
        pageCsiAggregationCountBefore == NUMBER_OF_PAGES * numberOfJobGroupCsiAggregations
        updateEventCountBefore == NUMBER_OF_PAGES * numberOfJobGroupCsiAggregations
        everyPageCsiAggClosedAndCalculatedBefore == false

        pageCsiAggregations.size() == NUMBER_OF_PAGES * numberOfJobGroupCsiAggregations
        updateEventsOfPageCsiAggregations.size() == 0
        pageCsiAggregations.every {
            it.closedAndCalculated &&
                    it.isCalculatedWithoutData()
        }
    }

    void "Daily page CsiAggregation younger expire time shouldn't be closed"() {
        setup: "Create daily page CsiAggregation younger expire time"
        CsiAggregation.withNewSession {
            createCsiAggregationsWithUpdateEventOutdated(actualDayStart.toDate(), dailyInterval, AggregationType.PAGE, false, JobGroup.list()[0], Page.list()[0])
        }

        when: "CsiAggregations get closed with expire time > date of created CsiAggregation"
        CsiAggregation.withNewSession {
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }
        List<CsiAggregation> csiAggregations = CsiAggregation.list()

        then: "Created CsiAggregation is still open"
        csiAggregations.size() == 1
        csiAggregations.every {
            it.closedAndCalculated == false &&
                    it.isCalculated() == false
        }
        CsiAggregationUpdateEvent.list().size() == 1
    }

    void "Weekly page CsiAggregation younger expire time shouldn't be closed"() {
        setup: "Create weekly page CsiAggregation younger expire time"
        CsiAggregation.withNewSession {
            createCsiAggregationsWithUpdateEventOutdated(actualWeekStart.toDate(), weeklyInterval, AggregationType.PAGE, false, JobGroup.list()[0], Page.list()[0])
        }

        when: "CsiAggregations get closed with expire time > date of created CsiAggregation"
        CsiAggregation.withNewSession {
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }
        List<CsiAggregation> csiAggregations = CsiAggregation.list()

        then: "Created CsiAggregation is still open"
        csiAggregations.size() == 1
        csiAggregations.every {
            it.closedAndCalculated == false &&
                    it.isCalculated() == false
        }
        CsiAggregationUpdateEvent.list().size() == 1
    }

    void "Daily JobGroup CsiAggregation younger expire time shouldn't be closed"() {
        setup: "Create daily JobGroup CsiAggregation younger expire time"
        CsiAggregation.withNewSession {
            createCsiAggregationsWithUpdateEventOutdated(actualDayStart.toDate(), dailyInterval, AggregationType.JOB_GROUP, false, JobGroup.list()[0], Page.list()[0])
        }

        when: "CsiAggregations get closed with expire time > date of created CsiAggregation"
        CsiAggregation.withNewSession {
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }
        List<CsiAggregation> csiAggregations = CsiAggregation.list()

        then: "Created CsiAggregation is still open"
        csiAggregations.size() == 1
        csiAggregations.every {
            it.closedAndCalculated == false &&
                    it.isCalculated() == false
        }
        CsiAggregationUpdateEvent.list().size() == 1
    }

    void "Weekly JobGroup CsiAggregation younger expire time shouldn't be closed"() {
        setup: "Create weekly JobGroup CsiAggregation younger expire time"
        CsiAggregation.withNewSession {
            createCsiAggregationsWithUpdateEventOutdated(actualWeekStart.toDate(), weeklyInterval, AggregationType.JOB_GROUP, false, JobGroup.list()[0], Page.list()[0])
        }

        when: "CsiAggregations get closed with expire time > date of created CsiAggregation"
        CsiAggregation.withNewSession {
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }
        List<CsiAggregation> csiAggregations = CsiAggregation.list()

        then: "Created CsiAggregation is still open"
        csiAggregations.size() == 1
        csiAggregations.every {
            it.closedAndCalculated == false &&
                    it.isCalculated() == false
        }
        CsiAggregationUpdateEvent.list().size() == 1
    }

    private void createCsiAggregationsWithUpdateEventOutdated(Date started, CsiAggregationInterval interval, AggregationType aggregationType, boolean closed, JobGroup jobGroup, Page page) {
        String resultIdsIrrelevantInTheseTests = '1,2,3'
        CsiAggregation mv = TestDataUtil.createCsiAggregation(
                started,
                interval,
                aggregationType,
                jobGroup,
                page,
                null,
                resultIdsIrrelevantInTheseTests,
                closed
        )
        TestDataUtil.createUpdateEvent(mv.ident(), CsiAggregationUpdateEvent.UpdateCause.OUTDATED)
    }

    private void mocksCommonToAllTests() {
        mockedExecTimeOfCleanup = new DateTime(2014, 7, 7, 5, 30, 0, DateTimeZone.UTC)
        csiAggregationUtilService.metaClass.getNowInUtc = { -> mockedExecTimeOfCleanup }
        ServiceMocker.create().mockBatchActivityService(csiAggregationUpdateEventCleanupService)
    }

    private void createTestdataCommonToAllTests() {
        List<CsiAggregationInterval> intervals = TestDataUtil.createCsiAggregationIntervals()
        weeklyInterval = intervals.find { it.intervalInMinutes == CsiAggregationInterval.WEEKLY }
        dailyInterval = intervals.find { it.intervalInMinutes == CsiAggregationInterval.DAILY }
        hourlyInterval = intervals.find { it.intervalInMinutes == CsiAggregationInterval.HOURLY }

        if (JobGroup.list().size() == 0) TestDataUtil.createJobGroups()
        if (Page.list().size() == 0) TestDataUtil.createPages(['homepage', 'product list', 'product page'])

        actualWeekStart = csiAggregationUtilService.resetToStartOfActualInterval(mockedExecTimeOfCleanup, weeklyInterval.intervalInMinutes)
        lastWeekStart = csiAggregationUtilService.subtractOneInterval(actualWeekStart, weeklyInterval.intervalInMinutes)
        secondLastWeekStart = csiAggregationUtilService.subtractOneInterval(lastWeekStart, weeklyInterval.intervalInMinutes)

        actualDayStart = csiAggregationUtilService.resetToStartOfActualInterval(mockedExecTimeOfCleanup, dailyInterval.intervalInMinutes)
        lastDayStart = csiAggregationUtilService.subtractOneInterval(actualDayStart, dailyInterval.intervalInMinutes)
        secondLastDayStart = csiAggregationUtilService.subtractOneInterval(lastDayStart, dailyInterval.intervalInMinutes)

        actualHourStart = csiAggregationUtilService.resetToStartOfActualInterval(mockedExecTimeOfCleanup, hourlyInterval.intervalInMinutes)
        lastHourStart = csiAggregationUtilService.subtractOneInterval(actualHourStart, dailyInterval.intervalInMinutes)
        secondLastHourStart = csiAggregationUtilService.subtractOneInterval(lastHourStart, dailyInterval.intervalInMinutes)

        inMemoryConfigService.activateMeasurementsGenerally()
    }
}
