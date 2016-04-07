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
import de.iteratec.osm.csi.CsiAggregationUpdateEventCleanupService
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.schedule.JobGroup
import grails.test.spock.IntegrationSpec
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Shared

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
class CloseExpiredUpdateEventsIntTests extends NonTransactionalIntegrationSpec {

	CsiAggregationUtilService csiAggregationUtilService
	CsiAggregationUpdateEventCleanupService csiAggregationUpdateEventCleanupService
	CsiAggregationDaoService csiAggregationDaoService
	InMemoryConfigService inMemoryConfigService
	
	AggregatorType eventAggregator
	AggregatorType pageAggregator 
	AggregatorType shopAggregator
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
        super.setupSpec()

        mocksCommonToAllTests()

        createTestdataCommonToAllTests()
    }

    def cleanup() {
        super.cleanupSpec()
    }

    void "Outdated daily page CsiAggregations get closed and calculated correctly"() {
        setup: "Create two outdated CsiAggregations and no EventResults"
		String tag = "${JobGroup.list()[0].ident()};${Page.list()[0].ident()}"
		createCsiAggregationsWithUpdateEventOutdated(lastDayStart.toDate(), dailyInterval, pageAggregator, false, tag)
		createCsiAggregationsWithUpdateEventOutdated(secondLastDayStart.toDate(), dailyInterval, pageAggregator, false, tag)

		when: "They are closed programmatically"
		csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

		then: "OUTDATED update events get deleted and csi aggregations got closed and calculated"
        CsiAggregationUpdateEvent.list().size() == 0
        List<CsiAggregation> csiAggregations = CsiAggregation.list()
		csiAggregations.size() == 2
        csiAggregations.every{
            it.closedAndCalculated &&
            it.isCalculatedWithoutData()
        }

	}

    void "Outdated weekly page CsiAggregations get closed and calculated correctly"() {
        setup: "Create two outdated CsiAggregations and no EventResults"
		String tag = "${JobGroup.list()[0].ident()};${Page.list()[0].ident()}"
		createCsiAggregationsWithUpdateEventOutdated(lastWeekStart.toDate(), weeklyInterval, pageAggregator, false, tag)
		createCsiAggregationsWithUpdateEventOutdated(secondLastWeekStart.toDate(), weeklyInterval, pageAggregator, false, tag)

        List<CsiAggregation> csiAggregations = CsiAggregation.list()
        List<CsiAggregationUpdateEvent> updateEvents = csiAggregationDaoService.getUpdateEvents(csiAggregations*.ident())
        int csiAggregationCountBefore = csiAggregations.size()
        boolean everyCsiAggClosedAndCalculatedBefore = csiAggregations.every {
            it.closedAndCalculated &&
            it.isCalculated()
        }
        int updateEventCountBefore = updateEvents.size()

        when: "They are closed programmatically"
		csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        updateEvents = csiAggregationDaoService.getUpdateEvents(csiAggregations*.ident())
        csiAggregations = CsiAggregation.list()

        then: "OUTDATED update events get deleted and csi aggregations got closed and calculated"
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

    void "Outdated daily JobGroup CsiAggregations get closed and calculated correctly"() {
		setup: "Create two outdated CsiAggregations and no EventResults"
		String tag = "${JobGroup.list()[0].ident()}"
		createCsiAggregationsWithUpdateEventOutdated(lastDayStart.toDate(), dailyInterval, shopAggregator, false, tag)
		createCsiAggregationsWithUpdateEventOutdated(secondLastDayStart.toDate(), dailyInterval, shopAggregator, false, tag)

		List<CsiAggregation> csiAggregations = CsiAggregation.list()
		List<CsiAggregation> jobGroupCsiAggregations = csiAggregations.findAll{ it.aggregator.name.equals(AggregatorType.SHOP)}
        int csiAggregationCountBefore = jobGroupCsiAggregations.size()
        boolean everyCsiAggClosedAndCalculatedBefore = jobGroupCsiAggregations.every {
            it.closedAndCalculated &&
            it.isCalculated()
        }
        List<CsiAggregationUpdateEvent> updateEventsOfJobGroupCsiAggregations = csiAggregationDaoService.getUpdateEvents(jobGroupCsiAggregations*.ident())
        int updateEventCountBefore = updateEventsOfJobGroupCsiAggregations.size()

		when: "They are closed programmatically"
		csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
		csiAggregations = CsiAggregation.list()
        jobGroupCsiAggregations = csiAggregations.findAll{ it.aggregator.name.equals(AggregatorType.SHOP)}
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

    void "Closing daily JobGroup CsiAggregations twice leads to closed and calculated page CsiAggregations"() {
        setup: "Create two outdated JobGroup CsiAggregations and no EventResults"
        String tag = "${JobGroup.list()[0].ident()}"
        int numberOfJobGroupCsiAggregations = 2
        createCsiAggregationsWithUpdateEventOutdated(lastDayStart.toDate(), dailyInterval, shopAggregator, false, tag)
        createCsiAggregationsWithUpdateEventOutdated(secondLastDayStart.toDate(), dailyInterval, shopAggregator, false, tag)

        when: "The JobGroup CsiAggregations are closed programmatically once"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        List<CsiAggregation> pageCsiAggregations = CsiAggregation.list().findAll{ it.aggregator.name.equals(AggregatorType.PAGE)}
        int pageCsiAggregationCountBefore = pageCsiAggregations.size()
        boolean everyPageCsiAggClosedAndCalculatedBefore = pageCsiAggregations.every {
            it.closedAndCalculated
        }
        List<CsiAggregationUpdateEvent> updateEventsOfPageCsiAggregations = csiAggregationDaoService.getUpdateEvents(pageCsiAggregations*.ident())
        int updateEventCountBefore = updateEventsOfPageCsiAggregations.size()

        and: "Then a second time"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        pageCsiAggregations = CsiAggregation.list().findAll{ it.aggregator.name.equals(AggregatorType.PAGE)}
        updateEventsOfPageCsiAggregations = csiAggregationDaoService.getUpdateEvents(pageCsiAggregations*.ident())

        then: "Page csi aggregations created while closing JobGroup aggregations for the first time are closed and calculated, too."
        pageCsiAggregationCountBefore == NUMBER_OF_PAGES * numberOfJobGroupCsiAggregations
        updateEventCountBefore == NUMBER_OF_PAGES * numberOfJobGroupCsiAggregations
        everyPageCsiAggClosedAndCalculatedBefore == false

        pageCsiAggregations.size() == NUMBER_OF_PAGES * numberOfJobGroupCsiAggregations
        updateEventsOfPageCsiAggregations.size() == 0
        pageCsiAggregations.every {
            it.closedAndCalculated
        }
    }

    void "Outdated weekly JobGroup CsiAggregations get closed and calculated correctly"() {
        setup: "Create two outdated CsiAggregations and no EventResults"
        String tag = "${JobGroup.list()[0].ident()}"
        createCsiAggregationsWithUpdateEventOutdated(lastWeekStart.toDate(), weeklyInterval, shopAggregator, false, tag)
        createCsiAggregationsWithUpdateEventOutdated(secondLastWeekStart.toDate(), weeklyInterval, shopAggregator, false, tag)

        List<CsiAggregation> csiAggregations = CsiAggregation.list()
        List<CsiAggregation> jobGroupCsiAggregations = csiAggregations.findAll{ it.aggregator.name.equals(AggregatorType.SHOP)}
        int csiAggregationCountBefore = jobGroupCsiAggregations.size()
        boolean everyCsiAggClosedAndCalculatedBefore = jobGroupCsiAggregations.every {
            it.closedAndCalculated &&
            it.isCalculated()
        }
        List<CsiAggregationUpdateEvent> updateEventsOfJobGroupCsiAggregations = csiAggregationDaoService.getUpdateEvents(jobGroupCsiAggregations*.ident())
        int updateEventCountBefore = updateEventsOfJobGroupCsiAggregations.size()

        when: "They are closed programmatically"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        csiAggregations = CsiAggregation.list()
        jobGroupCsiAggregations = csiAggregations.findAll{ it.aggregator.name.equals(AggregatorType.SHOP)}
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
        String tag = "${JobGroup.list()[0].ident()}"
        int numberOfJobGroupCsiAggregations = 2
        createCsiAggregationsWithUpdateEventOutdated(lastWeekStart.toDate(), weeklyInterval, shopAggregator, false, tag)
        createCsiAggregationsWithUpdateEventOutdated(secondLastWeekStart.toDate(), weeklyInterval, shopAggregator, false, tag)

        when: "The JobGroup CsiAggregations are closed programmatically once"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        List<CsiAggregation> pageCsiAggregations = CsiAggregation.list().findAll{ it.aggregator.name.equals(AggregatorType.PAGE)}
        int pageCsiAggregationCountBefore = pageCsiAggregations.size()
        boolean everyPageCsiAggClosedAndCalculatedBefore = pageCsiAggregations.every {
            it.closedAndCalculated
        }
        List<CsiAggregationUpdateEvent> updateEventsOfPageCsiAggregations = csiAggregationDaoService.getUpdateEvents(pageCsiAggregations*.ident())
        int updateEventCountBefore = updateEventsOfPageCsiAggregations.size()

        and: "Then a second time"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        pageCsiAggregations = CsiAggregation.list().findAll{ it.aggregator.name.equals(AggregatorType.PAGE)}
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
		String tag = "${JobGroup.list()[0].ident()};${Page.list()[0].ident()}"
		createCsiAggregationsWithUpdateEventOutdated(actualDayStart.toDate(), dailyInterval, pageAggregator, false, tag)

		when: "CsiAggregations get closed with expire time > date of created CsiAggregation"
		csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
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
        String tag = "${JobGroup.list()[0].ident()};${Page.list()[0].ident()}"
        createCsiAggregationsWithUpdateEventOutdated(actualWeekStart.toDate(), weeklyInterval, pageAggregator, false, tag)

        when: "CsiAggregations get closed with expire time > date of created CsiAggregation"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
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
        String tag = "${JobGroup.list()[0].ident()};${Page.list()[0].ident()}"
        createCsiAggregationsWithUpdateEventOutdated(actualDayStart.toDate(), dailyInterval, shopAggregator, false, tag)

        when: "CsiAggregations get closed with expire time > date of created CsiAggregation"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
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
        String tag = "${JobGroup.list()[0].ident()};${Page.list()[0].ident()}"
        createCsiAggregationsWithUpdateEventOutdated(actualWeekStart.toDate(), weeklyInterval, shopAggregator, false, tag)

        when: "CsiAggregations get closed with expire time > date of created CsiAggregation"
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        List<CsiAggregation> csiAggregations = CsiAggregation.list()

        then: "Created CsiAggregation is still open"
        csiAggregations.size() == 1
        csiAggregations.every {
            it.closedAndCalculated == false &&
            it.isCalculated() == false
        }
        CsiAggregationUpdateEvent.list().size() == 1
    }

	private void createCsiAggregationsWithUpdateEventOutdated(Date started, CsiAggregationInterval interval, AggregatorType aggregator, boolean closed, String tag){
		String resultIdsIrrelevantInTheseTests = '1,2,3'
		CsiAggregation mv = TestDataUtil.createCsiAggregation(
			started,
			interval,
			aggregator,
			tag,
			null,
			resultIdsIrrelevantInTheseTests,
			closed
		)
		TestDataUtil.createUpdateEvent(mv.ident(), CsiAggregationUpdateEvent.UpdateCause.OUTDATED)
	}

    private void mocksCommonToAllTests(){
        mockedExecTimeOfCleanup = new DateTime(2014,7,7,5,30,0, DateTimeZone.UTC)
        CsiAggregationUtilService.metaClass.getNowInUtc = { -> mockedExecTimeOfCleanup}
    }

    private void createTestdataCommonToAllTests(){
        List<AggregatorType> aggregators = TestDataUtil.createAggregatorTypes()
        pageAggregator = aggregators.find{ it.name == AggregatorType.PAGE }
        shopAggregator = aggregators.find{ it.name == AggregatorType.SHOP }
        eventAggregator = aggregators.find{ it.name == AggregatorType.MEASURED_EVENT }

        List<CsiAggregationInterval> intervals = TestDataUtil.createCsiAggregationIntervals()
        weeklyInterval = intervals.find{ it.intervalInMinutes == CsiAggregationInterval.WEEKLY }
        dailyInterval = intervals.find{ it.intervalInMinutes == CsiAggregationInterval.DAILY }
        hourlyInterval = intervals.find{ it.intervalInMinutes == CsiAggregationInterval.HOURLY}

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
