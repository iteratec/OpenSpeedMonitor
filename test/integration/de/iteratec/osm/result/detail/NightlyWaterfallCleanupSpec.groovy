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

package de.iteratec.osm.result.detail

import de.iteratec.osm.csi.IntTestWithDBCleanup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 *
 */
class NightlyWaterfallCleanupSpec extends IntTestWithDBCleanup {

	@Before
    void setUp() {
    }
	
	@Test
	void testDeleteWaterfallsBefore() {
		
		assertTrue(true)
		
		/*
		 * TODO:
		 * 			* enable the following test if nightly deletion of WebPerformanceWaterfalls is implemented, @Ignore doesn't work :-(
		 */
		
//        // test specific data
//		DateTime toDeleteBefore = new DateTime(2014,2,1,0,0,0, DateTimeZone.UTC)
//		def numberOfEntriesInWaterfallsToBeDeleted = 20
//		def numberOfEntriesInWaterfallsNotToBeDeleted = 10
//
//		createWaterfall(toDeleteBefore.minusYears(1), numberOfEntriesInWaterfallsToBeDeleted)
//		createWaterfall(toDeleteBefore.minusMonths(1), numberOfEntriesInWaterfallsToBeDeleted)
//		createWaterfall(toDeleteBefore.minusWeeks(1), numberOfEntriesInWaterfallsToBeDeleted)
//		createWaterfall(toDeleteBefore.minusDays(1), numberOfEntriesInWaterfallsToBeDeleted)
//		createWaterfall(toDeleteBefore.minusHours(1), numberOfEntriesInWaterfallsToBeDeleted)
//		createWaterfall(toDeleteBefore.minusMinutes(1), numberOfEntriesInWaterfallsToBeDeleted)
//		createWaterfall(toDeleteBefore.minusSeconds(1), numberOfEntriesInWaterfallsToBeDeleted)
//
//		createWaterfall(toDeleteBefore.plusYears(1), numberOfEntriesInWaterfallsNotToBeDeleted)
//		createWaterfall(toDeleteBefore.plusMonths(1), numberOfEntriesInWaterfallsNotToBeDeleted)
//		createWaterfall(toDeleteBefore.plusWeeks(1), numberOfEntriesInWaterfallsNotToBeDeleted)
//		createWaterfall(toDeleteBefore.plusDays(1), numberOfEntriesInWaterfallsNotToBeDeleted)
//		createWaterfall(toDeleteBefore.plusHours(1), numberOfEntriesInWaterfallsNotToBeDeleted)
//		createWaterfall(toDeleteBefore.plusMinutes(1), numberOfEntriesInWaterfallsNotToBeDeleted)
//		createWaterfall(toDeleteBefore.plusSeconds(1), numberOfEntriesInWaterfallsNotToBeDeleted)
//
//		Integer numberOfEventResultsWithoutWaterfalls = 5
//		addEventResultsWithoutWaterfalls(numberOfEventResultsWithoutWaterfalls)
//
//		Integer numberOfWaterfallsNotToBeDeleted = 7
//		Integer numberOfWaterfallsToBeDeleted = 7
//
//		//assertions before execution
//		List<WebPerformanceWaterfall> waterfallsBeforeDeletion = WebPerformanceWaterfall.list()
//		assertThat(waterfallsBeforeDeletion.size(), is(numberOfWaterfallsNotToBeDeleted + numberOfWaterfallsToBeDeleted))
//		assertThat(waterfallsBeforeDeletion*.waterfallEntries*.size(), everyItem(greaterThan(9)))
//		assertThat(WaterfallEntry.list().size(), is(
//			numberOfWaterfallsToBeDeleted*numberOfEntriesInWaterfallsToBeDeleted + numberOfWaterfallsNotToBeDeleted*numberOfEntriesInWaterfallsNotToBeDeleted
//			))
//		//test execution
//		dbCleanupService.deleteWaterfallsBefore(toDeleteBefore.toDate())
//		//assertions after execution
//		List<WebPerformanceWaterfall> waterfallsAfterDeletion = WebPerformanceWaterfall.list()
//		assertThat(waterfallsAfterDeletion.size(), is(numberOfWaterfallsNotToBeDeleted))
//		assertThat(waterfallsAfterDeletion.findAll{it.startDate < toDeleteBefore.toDate()}.size(), is(0))
//		assertThat(waterfallsAfterDeletion.findAll{it.startDate >= toDeleteBefore.toDate()}.size(), is(numberOfWaterfallsNotToBeDeleted))
//
//		assertThat(waterfallsAfterDeletion*.waterfallEntries*.size(), everyItem(is(numberOfEntriesInWaterfallsNotToBeDeleted)))
//		assertThat(WaterfallEntry.list().size(), is(numberOfWaterfallsNotToBeDeleted*numberOfEntriesInWaterfallsNotToBeDeleted))
//
//		assertThat(EventResult.list().size(), is(numberOfEventResultsWithoutWaterfalls + numberOfWaterfallsNotToBeDeleted + numberOfWaterfallsToBeDeleted))
	}
	
	void createWaterfall(DateTime startTime, Integer numberOfEntries){
		JobResult jr = new JobResult(testId: "TestJob").save(validate: false);
		
		EventResult result = new EventResult(
			wptStatus: 200,
			medianValue: true,
			numberOfWptRun: 1,
			cachedView: CachedView.UNCACHED,
			speedIndex: 12,
			jobResult: jr,
			jobResultDate: new Date(),
			jobResultJobConfigId: 1)
		
		WebPerformanceWaterfall waterfall = new WebPerformanceWaterfall(
			startDate: startTime.toDate(),
			url: 'http://urlundertest.com',
			title: startTime.toString(),
			eventName: 'my event',
			numberOfWptRun: 1,
			cachedView: CachedView.UNCACHED,
			startRenderInMillisecs: 100,
			docCompleteTimeInMillisecs: 100,
			domTimeInMillisecs: 100,
			fullyLoadedTimeInMillisecs: 100)
		(1..numberOfEntries).each {
			waterfall.addToWaterfallEntries(new WaterfallEntry(
				blocked: false,
				httpStatus: 0,
				path: '/myImage',
				host: 'http://imageserverundertest.com',
				mimeType: 'text/css',
				startOffset: 100,
				oneBasedIndexInWaterfall: 1,
				dnsLookupTimeStartInMillisecs: 100,
				initialConnectTimeStartInMillisecs: 100,
				sslNegotationTimeStartInMillisecs: 100,
				timeToFirstByteStartInMillisecs: 100,
				downloadTimeStartInMillisecs: 100,
				dnsLookupTimeEndInMillisecs: 100,
				initialConnectTimeEndInMillisecs: 100,
				sslNegotationTimeEndInMillisecs: 100,
				timeToFirstByteEndInMillisecs: 100,
				downloadTimeEndInMillisecs: 100,
				downloadedBytes: 100,
				uploadedBytes: 100))
		}
		waterfall.save(failOnError: true, flush: true)
		result.webPerformanceWaterfall=waterfall
		result.save(failOnError: true, flush: true)
	}
	
	void addEventResultsWithoutWaterfalls(int count){
		count.times{
			new EventResult(
				wptStatus: 200,
				medianValue: true,
				numberOfWptRun: 1,
				cachedView: CachedView.UNCACHED,
				speedIndex: 12,
				jobResult: jr,
				jobResultDate: new Date(),
				jobResultJobConfigId: 1).save(failOnError: true, flush: true)
		}
	}

}
