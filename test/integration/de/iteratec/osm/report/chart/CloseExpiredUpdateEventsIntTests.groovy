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
import de.iteratec.osm.csi.IntTestWithDBCleanup
import de.iteratec.osm.csi.MvUpdateEventCleanupService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.schedule.JobGroup
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.everyItem
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

/**
 * Methods in this class test the functionality to close {@link MeasuredValue}s.
 * Closing means: 
 * <ul>
 * <li>set attribute closedAndCalculated to true</li>
 * <li>calculate MeasuredValue if necessary</li>
 * <li>delete all {@link MeasuredValueUpdateEvent}s of MeasuredValue</li>
 * </ul>
 * 
 */
class CloseExpiredUpdateEventsIntTests extends IntTestWithDBCleanup {

	MeasuredValueUtilService measuredValueUtilService
	MvUpdateEventCleanupService mvUpdateEventCleanupService
	MeasuredValueDaoService measuredValueDaoService
	InMemoryConfigService inMemoryConfigService
	
	AggregatorType eventAggregator
	AggregatorType pageAggregator 
	AggregatorType shopAggregator
	MeasuredValueInterval weeklyInterval
	MeasuredValueInterval dailyInterval
	MeasuredValueInterval hourlyInterval
	
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
	
	@Before
    void setUp() {
		
		//mocking common to all tests
		mockedExecTimeOfCleanup = new DateTime(2014,7,7,5,30,0, DateTimeZone.UTC) 
		MeasuredValueUtilService.metaClass.getNowInUtc = { -> mockedExecTimeOfCleanup}
		
		//create test-data common to all tests
		List<AggregatorType> aggregators = TestDataUtil.createAggregatorTypes()
		pageAggregator = aggregators.find{ it.name == AggregatorType.PAGE }
		shopAggregator = aggregators.find{ it.name == AggregatorType.SHOP }
		eventAggregator = aggregators.find{ it.name == AggregatorType.MEASURED_EVENT }
		
		List<MeasuredValueInterval> intervals = TestDataUtil.createMeasuredValueIntervals()
		weeklyInterval = intervals.find{ it.intervalInMinutes == MeasuredValueInterval.WEEKLY }
		dailyInterval = intervals.find{ it.intervalInMinutes == MeasuredValueInterval.DAILY }
		hourlyInterval = intervals.find{ it.intervalInMinutes == MeasuredValueInterval.HOURLY}
		
		if (JobGroup.list().size() == 0) TestDataUtil.createJobGroups()
		if (Page.list().size() == 0) TestDataUtil.createPages(['homepage', 'product list', 'product page'])
		
		actualWeekStart = measuredValueUtilService.resetToStartOfActualInterval(mockedExecTimeOfCleanup, weeklyInterval.intervalInMinutes)
		lastWeekStart = measuredValueUtilService.subtractOneInterval(actualWeekStart, weeklyInterval.intervalInMinutes)
		secondLastWeekStart = measuredValueUtilService.subtractOneInterval(lastWeekStart, weeklyInterval.intervalInMinutes)
		
		actualDayStart = measuredValueUtilService.resetToStartOfActualInterval(mockedExecTimeOfCleanup, dailyInterval.intervalInMinutes)
		lastDayStart = measuredValueUtilService.subtractOneInterval(actualDayStart, dailyInterval.intervalInMinutes)
		secondLastDayStart = measuredValueUtilService.subtractOneInterval(lastDayStart, dailyInterval.intervalInMinutes)
		
		actualHourStart = measuredValueUtilService.resetToStartOfActualInterval(mockedExecTimeOfCleanup, hourlyInterval.intervalInMinutes)
		lastHourStart = measuredValueUtilService.subtractOneInterval(actualHourStart, dailyInterval.intervalInMinutes)
		secondLastHourStart = measuredValueUtilService.subtractOneInterval(lastHourStart, dailyInterval.intervalInMinutes)

		inMemoryConfigService.activateMeasurementsGenerally()
    }

	@Test
	public void testDailyPageShouldBeClosed() {
		//create test-specific data
		String tag = "${JobGroup.list()[0].ident()};${Page.list()[0].ident()}"
		createMvWithUpdateEventOutdated(lastDayStart.toDate(), dailyInterval, pageAggregator, false, tag)
		createMvWithUpdateEventOutdated(secondLastDayStart.toDate(), dailyInterval, pageAggregator, false, tag)
		
		List<MeasuredValue> mvs = MeasuredValue.list()
		assertThat(mvs.size(), is(2))
		List<MeasuredValueUpdateEvent> updateEvents = MeasuredValueUpdateEvent.list()
		assertThat(mvs*.closedAndCalculated, everyItem(is(false)))
		assertThat(mvs*.isCalculated(),  everyItem(is(false)))
		assertThat(updateEvents.size(), is(2))
		
		//test execution
		mvUpdateEventCleanupService.closeMeasuredValuesExpiredForAtLeast(300)
		
		//assertions
		mvs = MeasuredValue.list()
		updateEvents = MeasuredValueUpdateEvent.list()
		assertThat(mvs.size(), is(2))
		assertThat(mvs*.closedAndCalculated, everyItem(is(true)))
		assertThat(mvs*.isCalculatedWithoutData(),  everyItem(is(true)))
		assertThat(updateEvents.size(), is(0))
	}
	
	@Test
    public void testWeeklyPageShouldBeClosed() {
		//create test-specific data
		String tag = "${JobGroup.list()[0].ident()};${Page.list()[0].ident()}"
		createMvWithUpdateEventOutdated(lastWeekStart.toDate(), weeklyInterval, pageAggregator, false, tag)
		createMvWithUpdateEventOutdated(secondLastWeekStart.toDate(), weeklyInterval, pageAggregator, false, tag)
		
		List<MeasuredValue> mvs = MeasuredValue.list()
		List<MeasuredValueUpdateEvent> updateEvents = measuredValueDaoService.getUpdateEvents(mvs*.ident())
		assertThat(mvs.size(), is(2))
		assertThat(mvs*.closedAndCalculated, everyItem(is(false)))
		assertThat(mvs*.isCalculated(),  everyItem(is(false)))
		assertThat(updateEvents.size(), is(2))
		
		//test execution
		mvUpdateEventCleanupService.closeMeasuredValuesExpiredForAtLeast(300)
		
		//assertions
		mvs = MeasuredValue.list()
		updateEvents = measuredValueDaoService.getUpdateEvents(mvs*.ident())
		assertThat(mvs.size(), is(2))
		assertThat(mvs*.closedAndCalculated, everyItem(is(true)))
		assertThat(mvs*.isCalculated(),  everyItem(is(true)))
		assertThat(mvs*.isCalculatedWithoutData(),  everyItem(is(true)))
		assertThat(updateEvents.size(), is(0))
    }

	@Test
	public void testDailyShopShouldBeClosed() {
		//create test-specific data
		String tag = "${JobGroup.list()[0].ident()}"
		int numberOfShopMvs = 2
		createMvWithUpdateEventOutdated(lastDayStart.toDate(), dailyInterval, shopAggregator, false, tag)
		createMvWithUpdateEventOutdated(secondLastDayStart.toDate(), dailyInterval, shopAggregator, false, tag)
		
		List<MeasuredValue> mvs = MeasuredValue.list()
		
		List<MeasuredValue> smvs = mvs.findAll{ it.aggregator.name.equals(AggregatorType.SHOP)}
		List<MeasuredValueUpdateEvent> updateEventsOfSmvs = measuredValueDaoService.getUpdateEvents(smvs*.ident())
		assertThat(smvs.size(), is(2))
		assertThat(smvs*.closedAndCalculated, everyItem(is(false)))
		assertThat(smvs*.isCalculated(),  everyItem(is(false)))
		assertThat(updateEventsOfSmvs.size(), is(2))
		
		//test execution
		mvUpdateEventCleanupService.closeMeasuredValuesExpiredForAtLeast(300)
		
		//assertions
		mvs = MeasuredValue.list()
		
		smvs = mvs.findAll{ it.aggregator.name.equals(AggregatorType.SHOP)}
		updateEventsOfSmvs = measuredValueDaoService.getUpdateEvents(smvs*.ident())
		assertThat(smvs.size(), is(2))
		assertThat(smvs*.closedAndCalculated, everyItem(is(true)))
		assertThat(smvs*.isCalculatedWithoutData(),  everyItem(is(true)))
		assertThat(updateEventsOfSmvs.size(), is(0))
		
		//second call cause the calculation of shop mvs creates new page mvs which get closed and calculated not until subsequent cleanup
		List<MeasuredValue> pmvs = MeasuredValue.list().findAll{ it.aggregator.name.equals(AggregatorType.PAGE)}
		List<MeasuredValueUpdateEvent> updateEventsOfPmvs = measuredValueDaoService.getUpdateEvents(pmvs*.ident())
		assertThat(pmvs.size(), is(NUMBER_OF_PAGES * numberOfShopMvs))
		assertThat(pmvs*.closedAndCalculated, everyItem(is(false)))
		assertThat(pmvs*.isCalculatedWithoutData(),  everyItem(is(true)))
		assertThat(updateEventsOfPmvs.size(), is(NUMBER_OF_PAGES * numberOfShopMvs))
		mvUpdateEventCleanupService.closeMeasuredValuesExpiredForAtLeast(300)
		pmvs = MeasuredValue.list().findAll{ it.aggregator.name.equals(AggregatorType.PAGE)}
		updateEventsOfPmvs = measuredValueDaoService.getUpdateEvents(pmvs*.ident())
		assertThat(pmvs.size(), is(NUMBER_OF_PAGES * numberOfShopMvs))
		assertThat(pmvs*.closedAndCalculated, everyItem(is(true)))
		assertThat(pmvs*.isCalculatedWithoutData(),  everyItem(is(true)))
		assertThat(updateEventsOfPmvs.size(), is(0))
	}
	
	@Test
	public void testWeeklyShopShouldBeClosed() {
		//create test-specific data
		String tag = "${JobGroup.list()[0].ident()}"
		int numberOfShopMvs = 2
		createMvWithUpdateEventOutdated(lastWeekStart.toDate(), weeklyInterval, shopAggregator, false, tag)
		createMvWithUpdateEventOutdated(secondLastWeekStart.toDate(), weeklyInterval, shopAggregator, false, tag)
		
		List<MeasuredValue> mvs = MeasuredValue.list()
		List<MeasuredValue> smvs = mvs.findAll{ it.aggregator.name.equals(AggregatorType.SHOP)}
		List<MeasuredValueUpdateEvent> updateEvents = measuredValueDaoService.getUpdateEvents(smvs*.ident())
		assertThat(smvs.size(), is(2))
		assertThat(smvs*.closedAndCalculated, everyItem(is(false)))
		assertThat(smvs*.isCalculated(),  everyItem(is(false)))
		assertThat(updateEvents.size(), is(2))
		
		//test execution
		mvUpdateEventCleanupService.closeMeasuredValuesExpiredForAtLeast(300)
		
		//assertions
		smvs = MeasuredValue.list().findAll{ it.aggregator.name.equals(AggregatorType.SHOP)}
		updateEvents = measuredValueDaoService.getUpdateEvents(smvs*.ident())
		assertThat(smvs.size(), is(2))
		assertThat(smvs*.closedAndCalculated, everyItem(is(true)))
		assertThat(smvs*.isCalculatedWithoutData(),  everyItem(is(true)))
		assertThat(updateEvents.size(), is(0))
		
		//second call cause the calculation of shop mvs creates new page mvs which get closed and calculated not until subsequent cleanup
		List<MeasuredValue> pmvs = MeasuredValue.list().findAll{ it.aggregator.name.equals(AggregatorType.PAGE)}
		List<MeasuredValueUpdateEvent> updateEventsOfPmvs = measuredValueDaoService.getUpdateEvents(pmvs*.ident())
		assertThat(pmvs.size(), is(NUMBER_OF_PAGES * numberOfShopMvs))
		assertThat(pmvs*.closedAndCalculated, everyItem(is(false)))
		assertThat(pmvs*.isCalculatedWithoutData(),  everyItem(is(true)))
		assertThat(updateEventsOfPmvs.size(), is(NUMBER_OF_PAGES * numberOfShopMvs))
		mvUpdateEventCleanupService.closeMeasuredValuesExpiredForAtLeast(300)
		pmvs = MeasuredValue.list().findAll{ it.aggregator.name.equals(AggregatorType.PAGE)}
		updateEventsOfPmvs = measuredValueDaoService.getUpdateEvents(pmvs*.ident())
		assertThat(pmvs.size(), is(NUMBER_OF_PAGES * numberOfShopMvs))
		assertThat(pmvs*.closedAndCalculated, everyItem(is(true)))
		assertThat(pmvs*.isCalculatedWithoutData(),  everyItem(is(true)))
		assertThat(updateEventsOfPmvs.size(), is(0))
	}
	
	@Test
	public void testDailyPageShouldNotBeClosed() {
		//create test-specific data
		String tag = "${JobGroup.list()[0].ident()};${Page.list()[0].ident()}"
		createMvWithUpdateEventOutdated(actualDayStart.toDate(), dailyInterval, pageAggregator, false, tag)
		
		//test execution
		mvUpdateEventCleanupService.closeMeasuredValuesExpiredForAtLeast(300)
		
		//assertions
		List<MeasuredValue> mvs = MeasuredValue.list()
		List<MeasuredValueUpdateEvent> updateEvents = MeasuredValueUpdateEvent.list()
		assertThat(mvs.size(), is(1))
		assertThat(mvs*.closedAndCalculated, everyItem(is(false)))
		assertThat(mvs*.isCalculated(), everyItem(is(false)))
		assertThat(updateEvents.size(), is(1))
	}
	
	@Test
	public void testWeeklyPageShouldNotBeClosed() {
		//create test-specific data
		String tag = "${JobGroup.list()[0].ident()};${Page.list()[0].ident()}"
		createMvWithUpdateEventOutdated(actualWeekStart.toDate(), weeklyInterval, pageAggregator, false, tag)
		
		//test execution
		mvUpdateEventCleanupService.closeMeasuredValuesExpiredForAtLeast(300)
		
		//assertions
		List<MeasuredValue> mvs = MeasuredValue.list()
		List<MeasuredValueUpdateEvent> updateEvents = measuredValueDaoService.getUpdateEvents(mvs*.ident())
		assertThat(mvs.size(), is(1))
		assertThat(mvs*.closedAndCalculated, everyItem(is(false)))
		assertThat(mvs*.isCalculated(), everyItem(is(false)))
		assertThat(updateEvents.size(), is(1))
	}
	
	@Test
	public void testDailyShopShouldNotBeClosed() {
		//create test-specific data
		String tag = "${JobGroup.list()[0].ident()}"
		createMvWithUpdateEventOutdated(actualDayStart.toDate(), dailyInterval, shopAggregator, false, tag)
		
		//test execution
		mvUpdateEventCleanupService.closeMeasuredValuesExpiredForAtLeast(300)
		
		//assertions
		List<MeasuredValue> smvs = MeasuredValue.list().findAll{ it.aggregator.name.equals(AggregatorType.SHOP)}
		List<MeasuredValueUpdateEvent> updateEvents = measuredValueDaoService.getUpdateEvents(smvs*.ident())
		assertThat(smvs.size(), is(1))
		assertThat(smvs[0].closedAndCalculated, is(false))
		assertThat(smvs[0].isCalculated(),  is(false))
		assertThat(updateEvents.size(), is(1))
	}
	
	@Test
	public void testWeeklyShopShouldNotBeClosed() {
		//create test-specific data
		String tag = "${JobGroup.list()[0].ident()}"
		createMvWithUpdateEventOutdated(actualWeekStart.toDate(), weeklyInterval, shopAggregator, false, tag)
		
		//test execution
		mvUpdateEventCleanupService.closeMeasuredValuesExpiredForAtLeast(300)
		
		//assertions
		List<MeasuredValue> smvs = MeasuredValue.list().findAll{ it.aggregator.name.equals(AggregatorType.SHOP)}
		List<MeasuredValueUpdateEvent> updateEvents = measuredValueDaoService.getUpdateEvents(smvs*.ident())
		assertThat(smvs.size(), is(1))
		assertThat(smvs[0].closedAndCalculated, is(false))
		assertThat(smvs[0].isCalculated(),  is(false))
		assertThat(updateEvents.size(), is(1))
	}
	
	void createMvWithUpdateEventOutdated(Date started, MeasuredValueInterval interval, AggregatorType aggregator, boolean closed, String tag){
		String resultIdsIrrelevantInTheseTests = '1,2,3'
		MeasuredValue mv = TestDataUtil.createMeasuredValue(
			started,
			interval,
			aggregator,
			tag,
			null,
			resultIdsIrrelevantInTheseTests,
			closed
		)
		TestDataUtil.createUpdateEvent(mv.ident(), MeasuredValueUpdateEvent.UpdateCause.OUTDATED)
	}
}
