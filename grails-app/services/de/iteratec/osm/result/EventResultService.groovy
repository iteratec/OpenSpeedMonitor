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

package de.iteratec.osm.result

import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import org.joda.time.DateTime

import java.util.zip.GZIPInputStream

/**
 * Provides methods to get {@link EventResult}s from db.
 * @author fpavkovic, nkuhn
 *
 */
class EventResultService {
	
	JobResultDaoService jobResultDaoService
	
	List<EventResult> findSinceDate(Date timestamp) {
		
		def query = EventResult.where {
			jobResultDate > timestamp
		}
		return query.list()
	}
	
	List<EventResult> findBetweenDate(Date fromDate, Date toDate) {
		
		def query = EventResult.where {
			jobResultDate >= fromDate
			jobResultDate <= toDate
		}
		return query.list()
	}
	
	List<EventResult> findByJob(Job jobConfig) {
		findByJobId(jobConfig.id)
	}
	
	List<EventResult> findByJobId(long jobConfigId) {
		
//		doesn't work in Version 2.1.x, need to update to version >= 2.2
//		def query = EventResult.where {
//			jobResult.job.id == jobConfigId
//		}
//		return query.list()
		
		def query = EventResult.where {
			jobResultJobConfigId == jobConfigId
		}
		return query.list()
	}
	
	/**
	 * 
	 * @param jobConfig
	 * @param timestamp
	 * @return never null, potentially empty. 
	 * TODO mze-2013-07-12: Sorted? How? Or return a unordered Collection insted of list!
	 */
	List<EventResult> findByJobSinceDate(Job jobConfig, Date timestamp) {
		
//		doesn't work in Version 2.1.x, need to update to version >= 2.2
//		def query = EventResult.where {
//			jobResult.job.id == jobConfig.id
//			jobResult.date > timestamp
//		}
//		return query.list()
		
		def query = EventResult.where {
			jobResultJobConfigId == jobConfig.id
			jobResultDate > timestamp
		}
		return query.list()
	}
	
	/**
	 * 
	 * @param jobConfig
	 * @param fromDate
	 * @param toDate
	 * @param median
	 * @param view
	 * @return never null, potentially empty. 
	 */
	List<EventResult> findByJobBetweenDate(Job jobConfig, Date fromDate, Date toDate, Boolean median, CachedView view) {
		
//		doesn't work in Version 2.1.x, need to update to version >= 2.2
//		def query = EventResult.where {
//			jobResult.job.id == jobConfig.id
//		}
//		return query.list()
		
		def query = EventResult.where {
			jobResultJobConfigId == jobConfig.id
			jobResultDate >= fromDate
			jobResultDate <= toDate
			medianValue == median
			cachedView == view
		}
		return query.list()
	}
	
	/**
	 * Gets {@link EventResult}s from db respective given parameters.
	 * @param jobGroup
	 * @param msStep
	 * @param location
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	List<EventResult> findByMeasuredEventBetweenDate(
		JobGroup jobGroup, 
		MeasuredEvent msStep,
		Location location, 
		Date fromDate, 
		Date toDate) {
		
		List<JobResult> jobResults = JobResult.findAllByJobGroupNameAndLocationLocationAndLocationBrowserAndDateBetween(
			jobGroup.name, 
			location.location, 
			location.browser.name,
			fromDate,
			toDate)
		List<EventResult> eventResults = []
		for(JobResult eachJobResult : jobResults)
		{	
			EventResult medianUncachedEventResult = eachJobResult.findMedianUncachedEventResult(msStep)
			if(medianUncachedEventResult) {
				eventResults.add(medianUncachedEventResult)
			}
		}
		return eventResults
	}
	
	List<EventResult> findByJobBetweenDate(Job jobConfig, Date fromDate, Date toDate, Boolean median) {
		
//		doesn't work in Version 2.1.x, need to update to version >= 2.2
//		def query = EventResult.where {
//			jobResult.job.id == jobConfig.id
//		}
//		return query.list()
		
		def query = EventResult.where {
			jobResultJobConfigId == jobConfig.id
			jobResultDate >= fromDate
			jobResultDate <= toDate
			medianValue == median
		}
		return query.list()
	}
	
	
	Map findCsiRelevantByJobConfigsAndBetweenDate(List<Job> jobConfigs, Date fromDate, Date toDate) {
		
		DateTime stopWatchStarted = new DateTime()
		
		def jobConfigIds = []
		jobConfigs.each {
			jobConfigIds.add(it.id)
		}
		
		def query = EventResult.where {
			jobResultJobConfigId in jobConfigIds
			jobResultDate >= fromDate
			jobResultDate <= toDate
			medianValue == true
			cachedView == CachedView.UNCACHED
		}
		List<EventResult> results = query.list()
		
		Map ret = [:]
		results.each {
			if (!ret.containsKey(it.jobResultJobConfigId)) {
				ret.put(it.jobResultJobConfigId, new ArrayList<EventResult>())
			}
			
			ret[it.jobResultJobConfigId].add(it)
		}
		
		if (log.debugEnabled) {
			def elapsedInMillis = new DateTime().getMillis() - stopWatchStarted.getMillis()
			def eleapsedInSeconds = elapsedInMillis / 1000
			log.debug "eventResultService.findCsiRelevantByJobConfigsAndBetweenDate  -> Elapsed Sec: ${eleapsedInSeconds}"
		}
		
		return ret
	}
	
	List<EventResult> findByAgentAndByBrowserSinceDate(String agent, String browser, Date timestamp) {
		
		String agentCondition = agent?agent:'%'
		String browserCondition = browser?browser:'%'
		
		List<EventResult> result = []
		
		List<JobResult> jobResults = JobResult.where {
			locationLocation ==~ agentCondition
			locationBrowser ==~ browserCondition
			date > timestamp
		}.list().each { JobResult eachJobResult ->
			result.addAll(eachJobResult.getEventResults())
		}
		
		return result
	}
	
	
	List<EventResult> findByAgentAndByBrowserBetweenDate(String agent, String browser, Date fromDate, Date toDate) {
		
		String agentCondition = agent?agent:'%'
		String browserCondition = browser?browser:'%'
		
		List<EventResult> result = []
		
		List<JobResult> jobResults = JobResult.where {
			locationLocation ==~ agentCondition
			locationBrowser ==~ browserCondition
			date >= fromDate
			date <= toDate
		}.list().each { JobResult eachJobResult ->
			result.addAll(eachJobResult.getEventResults())
		}
		
		return result
	}

	private String unzip(byte[] zip){
		def inputStream = new ByteArrayInputStream(zip)
		def zipStream = new GZIPInputStream(inputStream)
		return zipStream.text
	}
	
	/**
	 * CSI Dashboard Kramz
	 */	
	Map findByAgentAndByBrowserBetweenDateAsMap(String agent, String browser, Date fromDate, Date toDate) {
		String agentCondition = agent?agent:"'%'"
		String browserCondition = browser?browser:"'%'"
		
		JobResult jobResultsInDateRange = JobResult.findAllByDateBetween(fromDate, toDate)
		Collection<EventResult> temp = []
		
		jobResultsInDateRange.each {temp.addAll(it.getEventResults())}
		
		/**
		 * Convert to Map in format
		 * [
		 *   jobname1: [timestamp1:customerSatisfaction1, ..., timestampN:customerSatisfactionN,],
		 *   ...,
		 *   jobnameN: [...]
		 * ]
		 */
		Map resultMap = new TreeMap();
		if (temp.size() > 0) {
			temp.each { curObj ->
				
				String jobName = curObj[0];
				Long date = ((Date) curObj[1]).getTime();
				Integer customerSatisfactionInPercent = curObj[2];
				
				Map map = resultMap.get(jobName);
				if (map == null) {
					map = [:];
					resultMap.put(jobName, map);
				}
				
				map.put(date, customerSatisfactionInPercent);	
				map.sort()
			}
		}
		
		return resultMap
	}
	
	/**
	 * <p>
	 * Proofs whether toProof is relevant for calculation of customer satisfaction index (csi). 
	 * To be relevant ...
	 * <ul>
	 * <li>customerSatisfactionInPercent of toProof has to be set</li>
	 * <li>loadTimeInMillisecs of toProof has to be set</li>
	 * <li>loadTimeInMillisecs of toProof has to be greater than minDocTimeInMillisecs</li>
	 * <li>loadTimeInMillisecs of toProof has to be smaller than maxDocTimeInMillisecs</li>
	 * </ul> 
	 * </p>
	 * 
	 * @param toProof
	 * @param minDocTimeInMillisecs
	 * @param maxDocTimeInMillisecs
	 * @return
	 * @deprecated Use {@link EventResult#isCsiRelevant()} instead.
	 */
	@Deprecated
	public Boolean isCsiRelevant(EventResult toProof, Integer minDocTimeInMillisecs, Integer maxDocTimeInMillisecs){
		return toProof.csByWptDocCompleteInPercent && toProof.docCompleteTimeInMillisecs &&
			(toProof.docCompleteTimeInMillisecs > minDocTimeInMillisecs &&
			toProof.docCompleteTimeInMillisecs < maxDocTimeInMillisecs)
	}
	
}
