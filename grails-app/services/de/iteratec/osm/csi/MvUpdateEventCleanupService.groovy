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

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.Status
import org.joda.time.DateTime
import org.springframework.transaction.TransactionStatus

import de.iteratec.osm.report.chart.MeasuredValueDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.ConfigService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent
import de.iteratec.osm.result.MeasuredValueTagService

/**
 * Contains the logic to ...
 * <ul>
 * <li>calculate (if necessary) and close {@link MeasuredValue}s who aren't already closed and who's interval expired.</li>
 * <li>cleanup associated {@link MeasuredValueUpdateEvent}s.</li>
 * </ul>
 * @author nkuhn
 */
class MvUpdateEventCleanupService {

    static transactional = false
	
	MeasuredValueDaoService measuredValueDaoService
	PageMeasuredValueService pageMeasuredValueService
	ShopMeasuredValueService shopMeasuredValueService
	MeasuredValueTagService measuredValueTagService
	ConfigService configService
	InMemoryConfigService inMemoryConfigService
	BatchActivityService batchActivityService

	/**
	 * <p>
	 * Closes all {@link MeasuredValue}s with closedAndCalculated=false who's time-interval has expired for at least minutes minutes.<br>
	 * Closing means:
	 * <ul>
	 * <li>set attribute closedAndCalculated to true</li>
	 * <li>calculate MeasuredValue</li>
	 * <li>delete all {@link MeasuredValueUpdateEvent}s of MeasuredValue</li>
	 * </ul>
	 * Hourly event MeasuredValues should never be closed here because they are set as closed with creation already.
	 * </p>
	 * @param minutes
	 * 					Time for which the MeasuredValue has to be expired.  e.g.
	 * 					<ul>
	 * 					<li>A DAILY-MeasuredValue with <code>started=2014-07-07 00:00:00</code> and an expiration-time of 180 minutes expires at "2014-07-08 03:00:00"</li>
	 * 					<li>A WEEKLY-MeasuredValue with <code>started=2014-07-04 00:00:00</code> and an expiration-time of 300 minutes expires at "2014-07-11 05:00:00"</li>
	 * 					</ul>
	 */
	void closeMeasuredValuesExpiredForAtLeast(int minutes){
		
		if ( ! inMemoryConfigService.areMeasurementsGenerallyEnabled() ) {
			log.info("No measured value update events are closed cause measurements are generally disabled.")
			return
		}
		
		List<MeasuredValue> mvsOpenAndExpired = measuredValueDaoService.getOpenMeasuredValuesWhosIntervalExpiredForAtLeast(minutes)
		log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${mvsOpenAndExpired.size()} MeasuredValues identified as open and expired.")
		if(mvsOpenAndExpired.size() > 0){
			closeAndCalculateIfNecessary(mvsOpenAndExpired)
		}
		
	}
	void closeAndCalculateIfNecessary(List<MeasuredValue> mvsOpenAndExpired){
		BatchActivity activity = batchActivityService.getActiveBatchActivity(this.class, 0, Activity.UPDATE, "Close and Calculate MeasuredValues")
		List<MeasuredValueUpdateEvent> allUpdateEvents = MeasuredValueUpdateEvent.list()
		log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${allUpdateEvents.size()} update events in db before cleanup.")
		List<MeasuredValueUpdateEvent> updateEventsToBeDeleted = measuredValueDaoService.getUpdateEvents(mvsOpenAndExpired*.ident())
		log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${updateEventsToBeDeleted.size()} update events should get deleted.")
		
		List<MeasuredValue> justToClose = []
		List<MeasuredValue> toCalculateAndClose = []
		//Variables for BatchActivity calculations
		int size = mvsOpenAndExpired.size()
		int overallSize = size * 3

		activity.updateStatus(["stage":"Split into justClose and calculateAndClose"])
		mvsOpenAndExpired.eachWithIndex{MeasuredValue mvOpenAndExpired, int index ->
			activity.updateStatus(["progressWithinStage":batchActivityService.calculateProgress(size,index+1), "progress":batchActivityService.calculateProgress(overallSize,index)])
			if(mvOpenAndExpired.hasToBeCalculatedAccordingEvents(updateEventsToBeDeleted)){
				toCalculateAndClose.add(mvOpenAndExpired)
			}else{
				justToClose.add(mvOpenAndExpired)
			}
		}
		activity.updateStatus(["stage":"Closing all already calculated MeasuredValues"])
		try{
			log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${justToClose.size()} already calculated MeasuredValues should get closed now.")
			justToClose.eachWithIndex {MeasuredValue toClose, int index ->
				activity.updateStatus(["progressWithinStage":batchActivityService.calculateProgress(size,index+1), "progress":batchActivityService.calculateProgress(overallSize,index)])
				MeasuredValue.withTransaction{TransactionStatus status ->
					closeMv(toClose)
					status.flush()
				}
				activity.updateStatus(["successfulActions": ++activity.getSuccessfulActions()])
			}
		}catch (Exception e){
			def message = "An error occurred while closing MeasuredValues who are  calculated already."
			log.error(message, e)
			activity.updateStatus(["lastFailureMessage":message,"failures":++activity.getFailures()])
		}
		
		log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${toCalculateAndClose.size()} open and expired MeasuredValues should get calculated now.")
		if(toCalculateAndClose.size() > 0) closeAndCalculate(toCalculateAndClose, activity, overallSize)
		activity.updateStatus(["stage": "","endDate": new Date(), "status": Status.DONE])
		allUpdateEvents = MeasuredValueUpdateEvent.list()
		log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${allUpdateEvents.size()} update events in db after cleanup.")
	}
	void closeAndCalculate(List<MeasuredValue> mvsToCalculateAndClose, BatchActivity activity, int overallSize){
		
		try{
			List<MeasuredValue> pageMvsToCalculate = mvsToCalculateAndClose.findAll{ it.aggregator.name.equals(AggregatorType.PAGE) }
			log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${pageMvsToCalculate.size()} open and expired page MeasuredValues should get calculated now.")
			if(pageMvsToCalculate.size() > 0) calculateAndClosePageMvs(pageMvsToCalculate, activity, overallSize)
			activity.updateStatus(["successfulActions": ++activity.getSuccessfulActions()])
		} catch(Exception e){
			def message = "An error occurred while calculation and closing page MeasuredValues."
			log.error(message, e)
			activity.updateStatus(["lastFailureMessage":message,"failures":++activity.getFailures()])
		}
		
		try{
			List<MeasuredValue> shopMvsToCalculate = mvsToCalculateAndClose.findAll{ it.aggregator.name.equals(AggregatorType.SHOP) }
			log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${shopMvsToCalculate.size()} open and expired shop MeasuredValues should get calculated now.")
			if(shopMvsToCalculate) calculateAndCloseShopMvs(shopMvsToCalculate, activity, overallSize)
			activity.updateStatus( ["successfulActions": ++activity.getSuccessfulActions()])
		} catch(Exception e){
			def message = "An error occurred while calculation and closing shop MeasuredValues."
			log.error(message, e)
			activity.updateStatus(["lastFailureMessage":message,"failures":++activity.getFailures()])
		}
		
	}
	
	void calculateAndClosePageMvs(List<MeasuredValue> pageMvsToCalculateAndClose, BatchActivity activity, int overallSize){
		log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: Creating caching container for calculating page MeasuredValues ...")
		CachingContainerFactory ccFactory = new CachingContainerFactory(pageMvsToCalculateAndClose)
		log.info("... DONE creating caching container")
		int size = pageMvsToCalculateAndClose.size()
		activity.updateStatus(["stage":"Calculate and Close Page MV"])
		pageMvsToCalculateAndClose.eachWithIndex{MeasuredValue dpmvToCalcAndClose, int index ->
			activity.updateStatus(["progressWithinStage":batchActivityService.calculateProgress(size,index+1), "progress":batchActivityService.calculateProgress(overallSize,index)])
			MeasuredValue.withTransaction {TransactionStatus status ->
				pageMeasuredValueService.calcMv(dpmvToCalcAndClose, ccFactory.createContainerFor(dpmvToCalcAndClose))
				closeMv(dpmvToCalcAndClose)
				status.flush()
			}
			activity.updateStatus( ["successfulActions": ++activity.getSuccessfulActions()])
		}
	}
	void calculateAndCloseShopMvs(List<MeasuredValue> shopMvsToCalculate, BatchActivity activity, int overallSize){
		int size = shopMvsToCalculate.size()
		shopMvsToCalculate.eachWithIndex {MeasuredValue smvToCalcAndClose, int index ->
			activity.updateStatus(["progressWithinStage":batchActivityService.calculateProgress(size,index+1), "progress":batchActivityService.calculateProgress(overallSize,index)])
			MeasuredValue.withTransaction {TransactionStatus status ->
				shopMeasuredValueService.calcMv(smvToCalcAndClose)
				closeMv(smvToCalcAndClose)
				status.flush()
			}
			activity.updateStatus(["successfulActions": ++activity.getSuccessfulActions()])
		}
	}
	
	/**
	 * Closes {@link MeasuredValue} toClose and deletes all associated {@link MeasuredValueUpdateEvent}s. 
	 * @param toClose
	 */
	void closeMv(MeasuredValue toClose){
		log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: The following MeasuredValue should get closed now: ${toClose}")
		toClose.closedAndCalculated = true
		toClose.save(failOnError: true)
		List<MeasuredValueUpdateEvent> updateEventsToDelete = measuredValueDaoService.getUpdateEvents(toClose.ident())
		log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${updateEventsToDelete.size()} MeasuredValueUpdateEvents should get deleted now.")
		updateEventsToDelete*.delete(failOnError: true)
	}
	
	/**
	 * 
	 * Delivers an instance of {@link MvCachingContainer} for every page {@link MeasuredValue} given when constructed the factory.
	 * The caching container is necessary to calculate a {@link MeasuredValue} with the following service method: {@link PageMeasuredValueService#calcMv}
	 * @author nkuhn
	 * @see PageMeasuredValueService
	 *
	 */
	class CachingContainerFactory{
		
		Map<String, JobGroup> jobGroupCache = [:]
		Map<String, JobGroup> pageCache = [:]
		
		List<MeasuredValue> dailyPageMvsToCalculate
		Map<String, List<JobGroup>> dailyJobGroupsByStartDate = [:].withDefault{ new ArrayList<JobGroup>() }
		Map<String, List<Page>> dailyPagesByStartDate = [:].withDefault{ new ArrayList<Page>() }
		Map<String, Map<String, List<MeasuredValue>>> dailyHemvMapByStartDate = [:]
		
		List<MeasuredValue> weeklyPageMvsToCalculate
		Map<String, List<JobGroup>> weeklyJobGroupsByStartDate = [:].withDefault{ new ArrayList<JobGroup>() }
		Map<String, List<Page>> weeklyPagesByStartDate = [:].withDefault{ new ArrayList<Page>() }
		Map<String, Map<String, List<MeasuredValue>>> weeklyHemvMapByStartDate = [:]
		
		public CachingContainerFactory(List<MeasuredValue> pmvs){
			if(pmvs.findAll{ !it.aggregator.name.equals(AggregatorType.PAGE) }.size() > 0) {
				throw new IllegalArgumentException("Class CachingContainerFactory works just with page MeasuredValues!")
			}
			initialize(pmvs)
		}
		void initialize(List<MeasuredValue> pmvs){
			dailyPageMvsToCalculate = pmvs.findAll{ it.interval.intervalInMinutes ==  MeasuredValueInterval.DAILY}
			weeklyPageMvsToCalculate = pmvs.findAll{ it.interval.intervalInMinutes ==  MeasuredValueInterval.WEEKLY }
			log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${dailyPageMvsToCalculate.size()} daily page MeasuredValues in initialization of caching container factory.")
			log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${weeklyPageMvsToCalculate.size()} weekly page MeasuredValues in initialization of caching container factory.")
			
			fillUniqueJobgroupAndPageListsPerStartdate()
			prepareHemvMapsByStartdate()
			
		}
		MvCachingContainer createContainerFor(MeasuredValue mv){
			return new MvCachingContainer(
				csiGroupToCalcMvFor: jobGroupCache[mv.tag],
				pageToCalcMvFor: pageCache[mv.tag],
				hmvsByCsiGroupPageCombination: this.getHemvMapOf(mv))
		}
		Map<String,List<MeasuredValue>> getHemvMapOf(MeasuredValue mv){
			if(mv.interval.intervalInMinutes == MeasuredValueInterval.DAILY) return dailyHemvMapByStartDate[mv.started.toString()]
			else if(mv.interval.intervalInMinutes == MeasuredValueInterval.WEEKLY) return weeklyHemvMapByStartDate[mv.started.toString()]
			else throw new IllegalArgumentException("Page MeasuredValues can only have interval DAILY or WEEKLY! This MeasuredValue caused this Exception: ${mv}")
		}
		void fillUniqueJobgroupAndPageListsPerStartdate(){
			dailyPageMvsToCalculate.each {dpmv ->
				
				JobGroup jobGroup = jobGroupCache[dpmv.tag]
				List<JobGroup> allGroups = JobGroup.list()
				if( jobGroup == null){
					jobGroup = measuredValueTagService.findJobGroupOfWeeklyPageTag(dpmv.tag)
					jobGroupCache[dpmv.tag] = jobGroup
				}
				dailyJobGroupsByStartDate[dpmv.started.toString()].add(jobGroup)
				
				Page page = pageCache[dpmv.tag]
				if(page == null){
					page = measuredValueTagService.findPageOfWeeklyPageTag(dpmv.tag)
					pageCache[dpmv.tag] = page
				}
				dailyPagesByStartDate[dpmv.started.toString()].add(page)
				
			}
			weeklyPageMvsToCalculate.each {wpmv ->
				
				JobGroup jobGroup = jobGroupCache[wpmv.tag]
				if( jobGroup == null){
					jobGroup = measuredValueTagService.findJobGroupOfWeeklyPageTag(wpmv.tag)
					jobGroupCache[wpmv.tag] = jobGroup
				}
				weeklyJobGroupsByStartDate[wpmv.started.toString()].add(jobGroup)
				
				Page page = pageCache[wpmv.tag]
				if(page == null){
					page = measuredValueTagService.findPageOfWeeklyPageTag(wpmv.tag)
					pageCache[wpmv.tag] = page
				}
				weeklyPagesByStartDate[wpmv.started.toString()].add(page)
				
			}
		}
		void prepareHemvMapsByStartdate(){
			dailyPageMvsToCalculate*.started.unique().each{Date uniqueStartDate ->
				
				DateTime startForGettingHemv = new DateTime(uniqueStartDate)
				String uniqueStartDateAsString = uniqueStartDate.toString()
				
				dailyHemvMapByStartDate[uniqueStartDateAsString] = pageMeasuredValueService.getHmvsByCsiGroupPageCombinationMap(
					dailyJobGroupsByStartDate[uniqueStartDateAsString].unique(),
					dailyPagesByStartDate[uniqueStartDateAsString].unique(),
					startForGettingHemv,
					startForGettingHemv.plusMinutes(MeasuredValueInterval.DAILY))
			}
			weeklyPageMvsToCalculate*.started.unique().each{Date uniqueStartDate ->
				
				DateTime startForGettingHemv = new DateTime(uniqueStartDate)
				String uniqueStartDateAsString = uniqueStartDate.toString()
				
				weeklyHemvMapByStartDate[uniqueStartDateAsString] = pageMeasuredValueService.getHmvsByCsiGroupPageCombinationMap(
					weeklyJobGroupsByStartDate[uniqueStartDateAsString].unique(),
					weeklyPagesByStartDate[uniqueStartDateAsString].unique(),
					startForGettingHemv,
					startForGettingHemv.plusMinutes(MeasuredValueInterval.WEEKLY))
			}
		}
	}

}
