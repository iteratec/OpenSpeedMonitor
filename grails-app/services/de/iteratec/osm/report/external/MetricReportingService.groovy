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

package de.iteratec.osm.report.external

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdater
import grails.transaction.NotTransactional
import grails.transaction.Transactional

import org.joda.time.DateTime

import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.ConfigService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.external.provider.GraphiteSocketProvider
import de.iteratec.osm.csi.EventCsiAggregationService
import de.iteratec.osm.csi.PageCsiAggregationService
import de.iteratec.osm.csi.ShopCsiAggregationService
import de.iteratec.osm.result.Contract
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.CsiAggregationTagService
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.ResultCsiAggregationService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.util.I18nService


/**
 * Reports osm-metrics to external tools.
 */
@Transactional
class MetricReportingService {
	
	private static final List<String> INVALID_GRAPHITE_PATH_CHARACTERS = ['.', ' ']
	private static final String REPLACEMENT_FOR_INVALID_GRAPHITE_PATH_CHARACTERS = '_'

	CsiAggregationTagService csiAggregationTagService
	ResultCsiAggregationService resultCsiAggregationService
	GraphiteSocketProvider graphiteSocketProvider
	I18nService i18nService
	EventCsiAggregationService eventCsiAggregationService
	JobGroupDaoService jobGroupDaoService
	CsiAggregationUtilService csiAggregationUtilService
	PageCsiAggregationService pageCsiAggregationService
	ShopCsiAggregationService shopCsiAggregationService
	ConfigService configService
	InMemoryConfigService inMemoryConfigService
	BatchActivityService batchActivityService

	/**
	 * Reports each measurand of incoming result for that a {@link GraphitePath} is configured.  
	 * @param result
	 * 				This EventResult defines measurands to sent and must not be null.
	 * @throws NullPointerException
	 *             if {@code pathElements} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if at least one of the {@code pathElements} is
	 *             {@linkplain String#isEmpty() empty} or contains at least one
	 *             dot.
	 */
	@NotTransactional
	public void reportEventResultToGraphite(EventResult result) {

		Contract.requiresArgumentNotNull("result", result)

		log.info("reporting Eventresult");

		JobGroup jobGroup = csiAggregationTagService.findJobGroupOfHourlyEventTag(result.tag)
		Collection<GraphiteServer> servers = jobGroup.graphiteServers
		if (servers.size()<1) {
			return
		}

		MeasuredEvent event = csiAggregationTagService.findMeasuredEventOfHourlyEventTag(result.tag);
		Page page = csiAggregationTagService.findPageOfHourlyEventTag(result.tag);
		Browser browser = csiAggregationTagService.findBrowserOfHourlyEventTag(result.tag);
		Location location = csiAggregationTagService.findLocationOfHourlyEventTag(result.tag);

		servers.each{GraphiteServer graphiteServer ->
			log.debug("Sending results to the following GraphiteServer: ${graphiteServer.getServerAdress()}: ")
			
			GraphiteSocket socket
			try {
				log.info("now the graphiteSocket should be retrieved ...")
				socket = graphiteSocketProvider.getSocket(graphiteServer)
			} catch (Exception e) {
				//TODO: java.net.UnknownHostException can't be catched explicitly! Maybe groovy wraps the exception? But the stacktrace says java.net.UnknownHostException  ...
				log.error("GraphiteServer ${graphiteServer} couldn't be reached. The following result couldn't be be sent: ${result}")
				return
			}

			graphiteServer.graphitePaths.each { GraphitePath eachPath ->

				Boolean resultOfSameCachedViewAsGraphitePath = 
					eachPath.getMeasurand().isCachedCriteriaApplicable() &&
					resultCsiAggregationService.getAggregatorTypeCachedViewType(eachPath.getMeasurand()).equals(result.getCachedView());
					
				if(resultOfSameCachedViewAsGraphitePath){

					Double value=resultCsiAggregationService.getEventResultPropertyForCalculation(eachPath.getMeasurand(), result);
					if (value!=null) {

						String measurandName = i18nService.msg(
								"de.iteratec.ispc.report.external.graphite.measurand.${eachPath.getMeasurand().getName()}", eachPath.getMeasurand().getName());
						
						List<String> pathElements = []
						pathElements.addAll(eachPath.getPrefix().tokenize('.'))
						pathElements.add(replaceInvalidGraphitePathCharacters(jobGroup.name))
						pathElements.add('raw')
						pathElements.add(replaceInvalidGraphitePathCharacters(page.name))
						pathElements.add(replaceInvalidGraphitePathCharacters(event.name))
						pathElements.add(replaceInvalidGraphitePathCharacters(browser.name))
						pathElements.add(replaceInvalidGraphitePathCharacters(location.uniqueIdentifierForServer==null ? location.location.toString():location.uniqueIdentifierForServer.toString()))
						pathElements.add(replaceInvalidGraphitePathCharacters(measurandName))
						
						GraphitePathName finalPathName
						try {
							finalPathName=GraphitePathName.valueOf(pathElements.toArray(new String[pathElements.size()]));
						} catch (IllegalArgumentException iae) {
							log.error("Couldn't write result to graphite due to invalid path: ${pathElements}", iae)
							return
						} catch (NullPointerException npe) {
							log.error("Couldn't write result to graphite due to invalid path: ${pathElements}", npe)
							return
						}
						try {
							socket.sendDate(finalPathName, value, result.getJobResultDate())
						} catch (NullPointerException npe) {
							log.error("Couldn't write result to graphite due to invalid path: ${pathElements}", npe)
						} catch (GraphiteComunicationFailureException e) {
							log.error(e)
						} 

						log.debug("Sent date to graphite: path=${finalPathName}, value=${value} time=${result.getJobResultDate().getTime()}")
					}
				}
			}
		}
	}

	/**
	 * <p>
	 * Reports the Event CSI values of the last full hour before(!) the given 
	 * reporting time-stamp to an external metric tool.
	 * </p>
	 * 
	 * @param reportingTimeStamp 
	 *         The time-stamp for that the last full interval before 
	 *         should be reported, not <code>null</code>.
	 * @since IT-199
	 */
	public void reportEventCSIValuesOfLastHour(DateTime reportingTimeStamp, boolean createBatchActivity = true) {
		if ( ! inMemoryConfigService.areMeasurementsGenerallyEnabled() ) {
			log.info("No event csi values are reported cause measurements are generally disabled.")
			return
		}
		BatchActivityUpdater activity = batchActivityService.getActiveBatchActivity(this.class, Activity.UPDATE, "Report last hour CSI Values: ${reportingTimeStamp}", 1, createBatchActivity)

		Contract.requiresArgumentNotNull("reportingTimeStamp", reportingTimeStamp)

		if(log.debugEnabled) log.debug('reporting csi-values of last hour')
		activity.beginNewStage("Collecting JobGroups",1).update()
		Collection<JobGroup> csiGroupsWithGraphiteServers = jobGroupDaoService.findCSIGroups().findAll {it.graphiteServers.size()>0}
		activity.addProgressToStage().update()
		if(log.debugEnabled) log.debug("csi-groups to report: ${csiGroupsWithGraphiteServers}")
		activity.beginNewStage("Reporting",csiGroupsWithGraphiteServers.size()).update()
		csiGroupsWithGraphiteServers.eachWithIndex {JobGroup eachJobGroup, int index ->
			activity.addProgressToStage().update()
			MvQueryParams queryParams = new MvQueryParams()
			queryParams.jobGroupIds.add(eachJobGroup.getId())
			Date startOfLastClosedInterval = csiAggregationUtilService.resetToStartOfActualInterval(
				csiAggregationUtilService.subtractOneInterval(reportingTimeStamp, CsiAggregationInterval.HOURLY),
				CsiAggregationInterval.HOURLY)
																	  .toDate();
			List<CsiAggregation> mvs = eventCsiAggregationService.getHourlyCsiAggregations(startOfLastClosedInterval, startOfLastClosedInterval, queryParams).findAll{ CsiAggregation hmv ->
				hmv.csByWptDocCompleteInPercent != null && hmv.countUnderlyingEventResultsByWptDocComplete() > 0
			}

			if(log.debugEnabled) log.debug("CsiAggregations to report for last hour: ${mvs}")
			reportAllCsiAggregationsFor(eachJobGroup, AggregatorType.MEASURED_EVENT, mvs)
		}
		activity.done()
	}

	/**
	 * <p>
	 * Reports the Page CSI values of the last Day before(!) the given 
	 * reporting time-stamp to an external metric tool.
	 * </p>
	 * 
	 * @param reportingTimeStamp 
	 *         The time-stamp for that the last full interval before 
	 *         should be reported, not <code>null</code>.
	 * @since IT-201
	 */
	public void reportPageCSIValuesOfLastDay(DateTime reportingTimeStamp, boolean createBatchActivity = true) {
		
		if ( ! inMemoryConfigService.areMeasurementsGenerallyEnabled() ) {
			log.info("No page csi values of last day are reported cause measurements are generally disabled.")
			return
		}

		if (log.infoEnabled) log.info("Start reporting PageCSIValuesOfLastDay for timestamp: ${reportingTimeStamp}");
		Contract.requiresArgumentNotNull("reportingTimeStamp", reportingTimeStamp)

		BatchActivityUpdater activity = batchActivityService.getActiveBatchActivity(this.class,Activity.UPDATE,"Report last day page CSI Values: ${reportingTimeStamp}",1, createBatchActivity)
		reportPageCSIValues(CsiAggregationInterval.DAILY, reportingTimeStamp, activity)
		activity.done()


	}

	/**
	 * <p>
	 * Reports the Page CSI values of the last week before(!) the given
	 * reporting time-stamp to an external metric tool.
	 * </p>
	 *
	 * @param reportingTimeStamp
	 *         The time-stamp for that the last full interval before
	 *         should be reported, not <code>null</code>.
	 * @since IT-205
	 */
	public void reportPageCSIValuesOfLastWeek(DateTime reportingTimeStamp, boolean createBatchActivity = true) {
		
		if ( ! inMemoryConfigService.areMeasurementsGenerallyEnabled() ) {
			log.info("No page csi values of last week are reported cause measurements are generally disabled.")
			return
		}

		Contract.requiresArgumentNotNull("reportingTimeStamp", reportingTimeStamp)

		BatchActivityUpdater activity = batchActivityService.getActiveBatchActivity(this.class,Activity.UPDATE,"Report last week page CSI Values: ${reportingTimeStamp}",1, createBatchActivity)
		reportPageCSIValues(CsiAggregationInterval.WEEKLY, reportingTimeStamp, activity)
		activity.done()
	}

	private void reportPageCSIValues(Integer intervalInMinutes, DateTime reportingTimeStamp, BatchActivityUpdater activity) {
		if(log.debugEnabled) log.debug("reporting page csi-values with intervalInMinutes ${intervalInMinutes} for reportingTimestamp: ${reportingTimeStamp}")

		def groups = jobGroupDaoService.findCSIGroups().findAll {it.graphiteServers.size()>0}
		int size = groups.size()
		activity.beginNewStage("Report page CSI Values", size).update()
		groups.eachWithIndex {JobGroup eachJobGroup, int index ->
			activity.addProgressToStage().update()
			Date startOfLastClosedInterval = csiAggregationUtilService.resetToStartOfActualInterval(
				csiAggregationUtilService.subtractOneInterval(reportingTimeStamp, intervalInMinutes),
				intervalInMinutes)
																	  .toDate();

			if(log.debugEnabled) log.debug("getting page csi-values to report to graphite: startOfLastClosedInterval=${startOfLastClosedInterval}")
			CsiAggregationInterval interval = CsiAggregationInterval.findByIntervalInMinutes(intervalInMinutes)
			List<CsiAggregation> pmvsWithData = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startOfLastClosedInterval, startOfLastClosedInterval, interval, [eachJobGroup]).findAll{ CsiAggregation pmv ->
				pmv.csByWptDocCompleteInPercent != null && pmv.countUnderlyingEventResultsByWptDocComplete() > 0
			}

			if(log.debugEnabled) log.debug("reporting ${pmvsWithData.size()} page csi-values with intervalInMinutes ${intervalInMinutes} for JobGroup: ${eachJobGroup}");
			reportAllCsiAggregationsFor(eachJobGroup, AggregatorType.PAGE, pmvsWithData)
		}
	}

	/**
	 * <p>
	 * Reports the Shop CSI values of the last Day before(!) the given 
	 * reporting time-stamp to an external metric tool.
	 * </p>
	 * 
	 * @param reportingTimeStamp 
	 *         The time-stamp for that the last full interval before 
	 *         should be reported, not <code>null</code>.
	 * @since IT-203
	 */
	public void reportShopCSIValuesOfLastDay(DateTime reportingTimeStamp, boolean createBatchActivity = true) {
		
		if ( ! inMemoryConfigService.areMeasurementsGenerallyEnabled() ) {
			log.info("No shop csi values of last day are reported cause measurements are generally disabled.")
			return
		}

		Contract.requiresArgumentNotNull("reportingTimeStamp", reportingTimeStamp)
		BatchActivityUpdater activity = batchActivityService.getActiveBatchActivity(this.class,Activity.UPDATE,"Report last day shop CSI Values: ${reportingTimeStamp}",1, createBatchActivity)
		reportShopCSICsiAggregations(CsiAggregationInterval.DAILY, reportingTimeStamp,activity)
		activity.done()
	}

	/**
	 * <p>
	 * Reports the Shop CSI values of the last week before(!) the given
	 * reporting time-stamp to an external metric tool.
	 * </p>
	 *
	 * @param reportingTimeStamp
	 *         The time-stamp for that the last full interval before
	 *         should be reported, not <code>null</code>.
	 * @since IT-205
	 */
	public void reportShopCSIValuesOfLastWeek(DateTime reportingTimeStamp, boolean createBatchActivity = true) {
		
		if ( ! inMemoryConfigService.areMeasurementsGenerallyEnabled() ) {
			log.info("No shop csi values of last week are reported cause measurements are generally disabled.")
			return
		}

		Contract.requiresArgumentNotNull("reportingTimeStamp", reportingTimeStamp)

		BatchActivityUpdater activity = batchActivityService.getActiveBatchActivity(this.class,Activity.UPDATE,"Report last week shop CSI Values: ${reportingTimeStamp}",1, createBatchActivity)
		reportShopCSICsiAggregations(CsiAggregationInterval.WEEKLY, reportingTimeStamp, activity)
		activity.done()
	}

	private void reportShopCSICsiAggregations(Integer intervalInMinutes, DateTime reportingTimeStamp, BatchActivityUpdater activity) {
		if(log.debugEnabled) log.debug("reporting shop csi-values with intervalInMinutes ${intervalInMinutes} for reportingTimestamp: ${reportingTimeStamp}")
		def groups = jobGroupDaoService.findCSIGroups().findAll {it.graphiteServers.size()>0}
		int size = groups.size()
		activity.beginNewStage("Report CSI-Values", size).update()
		groups.eachWithIndex {JobGroup eachJobGroup, int index ->
			activity.addProgressToStage().update()
			Date startOfLastClosedInterval = csiAggregationUtilService.resetToStartOfActualInterval(
				csiAggregationUtilService.subtractOneInterval(reportingTimeStamp, intervalInMinutes),
				intervalInMinutes)
																	  .toDate();

			if(log.debugEnabled) log.debug("getting shop csi-values to report to graphite: startOfLastClosedInterval=${startOfLastClosedInterval}")
			CsiAggregationInterval interval = CsiAggregationInterval.findByIntervalInMinutes(intervalInMinutes)
			List<CsiAggregation> smvsWithData = shopCsiAggregationService.getOrCalculateShopCsiAggregations(startOfLastClosedInterval, startOfLastClosedInterval, interval, [eachJobGroup]).findAll { CsiAggregation smv ->
				smv.csByWptDocCompleteInPercent != null && smv.countUnderlyingEventResultsByWptDocComplete() > 0
			}

			reportAllCsiAggregationsFor(eachJobGroup, AggregatorType.SHOP, smvsWithData)
		}
	}

	private void reportAllCsiAggregationsFor(JobGroup jobGroup, String aggregatorName, List<CsiAggregation> mvs) {
		jobGroup.graphiteServers.each {eachGraphiteServer ->
			eachGraphiteServer.graphitePaths.findAll { it.measurand.name.equals(aggregatorName) }.each {GraphitePath measuredEventGraphitePath ->

				GraphiteSocket socket
				try {
					socket = graphiteSocketProvider.getSocket(eachGraphiteServer)
				} catch (Exception e){
					//TODO: java.net.UnknownHostException can't be catched explicitly! Maybe groovy wraps the exception? But the stacktrace says java.net.UnknownHostException  ...
					if (log.errorEnabled) {log.error("GraphiteServer ${eachGraphiteServer} couldn't be reached. ${mvs.size()} CsiAggregations couldn't be sent.")}
					return
				}
				
				if(log.debugEnabled) log.debug("${mvs.size()} CsiAggregations should be sent to:\nJobGroup=${jobGroup}\nGraphiteServer=${eachGraphiteServer.getServerAdress()}\nGraphitePath=${measuredEventGraphitePath}")
				mvs.each { CsiAggregation mv ->
					if(log.debugEnabled) log.debug("Sending ${mv.interval.name} ${aggregatorName}-csi-value for:\nJobGroup=${jobGroup}\nGraphiteServer=${eachGraphiteServer.getServerAdress()}\nGraphitePath=${measuredEventGraphitePath}")
					reportCsiAggregation(measuredEventGraphitePath.getPrefix(), jobGroup, mv, socket)
				}
			}
		}
	}


	private void reportCsiAggregation(String prefix, JobGroup jobGroup, CsiAggregation mv, GraphiteSocket socket){
		if (mv.interval.intervalInMinutes == CsiAggregationInterval.HOURLY && mv.aggregator.name.equals(AggregatorType.MEASURED_EVENT)) {
			reportHourlyCsiAggregation(prefix, jobGroup, mv, socket)
		}else if (mv.interval.intervalInMinutes == CsiAggregationInterval.DAILY){
			if (mv.aggregator.name.equals(AggregatorType.PAGE)) {
				reportDailyPageCsiAggregation(prefix, jobGroup, mv, socket)
			} else if (mv.aggregator.name.equals(AggregatorType.SHOP)) {
				reportDailyShopCsiAggregation(prefix, jobGroup, mv, socket)
			}
		} else if (mv.interval.intervalInMinutes == CsiAggregationInterval.WEEKLY){
			if (mv.aggregator.name.equals(AggregatorType.PAGE)) {
				reportWeeklyPageCsiAggregation(prefix, jobGroup, mv, socket)
			} else if (mv.aggregator.name.equals(AggregatorType.SHOP)) {
				reportWeeklyShopCsiAggregation(prefix, jobGroup, mv, socket)
			}
		}
	}

	private void reportHourlyCsiAggregation(String prefix, JobGroup jobGroup, CsiAggregation mv, GraphiteSocket socket) {
		Page page = csiAggregationTagService.findPageOfHourlyEventTag(mv.tag)
		MeasuredEvent event = csiAggregationTagService.findMeasuredEventOfHourlyEventTag(mv.tag)
		Browser browser = csiAggregationTagService.findBrowserOfHourlyEventTag(mv.tag)
		Location location = csiAggregationTagService.findLocationOfHourlyEventTag(mv.tag)
		
		List<String> pathElements = []
		pathElements.addAll(prefix.tokenize('.'))
		pathElements.add(replaceInvalidGraphitePathCharacters(jobGroup.name))
		pathElements.add('hourly')
		pathElements.add(replaceInvalidGraphitePathCharacters(page.name))
		pathElements.add(replaceInvalidGraphitePathCharacters(event.name))
		pathElements.add(replaceInvalidGraphitePathCharacters(browser.name))
		pathElements.add(replaceInvalidGraphitePathCharacters(location.uniqueIdentifierForServer==null ? location.location.toString():location.uniqueIdentifierForServer.toString()))
		pathElements.add('csi')
		
		GraphitePathName finalPathName=GraphitePathName.valueOf(pathElements.toArray(new String[pathElements.size()]));
		double valueAsPercentage = mv.csByWptDocCompleteInPercent * 100
		if(log.debugEnabled) log.debug("Sending ${mv.started}|${valueAsPercentage} as hourly CsiAggregation to graphite-path ${finalPathName}")
		socket.sendDate(finalPathName, valueAsPercentage, mv.started)
	}

	private void reportDailyPageCsiAggregation(String prefix, JobGroup jobGroup, CsiAggregation mv, GraphiteSocket socket) {
		Page page = csiAggregationTagService.findPageByPageTag(mv.tag)

		List<String> pathElements = []
		pathElements.addAll(prefix.tokenize('.'))
		pathElements.add(replaceInvalidGraphitePathCharacters(jobGroup.name))
		pathElements.add('daily')
		pathElements.add(replaceInvalidGraphitePathCharacters(page.name))
		pathElements.add('csi')
		
		GraphitePathName finalPathName=GraphitePathName.valueOf(pathElements.toArray(new String[pathElements.size()]));
		double valueAsPercentage = mv.csByWptDocCompleteInPercent * 100
		if(log.debugEnabled) log.debug("Sending ${mv.started}|${valueAsPercentage} as daily page-CsiAggregation to graphite-path ${finalPathName}")
		socket.sendDate(finalPathName, valueAsPercentage, mv.started)
	}

	private void reportDailyShopCsiAggregation(String prefix, JobGroup jobGroup, CsiAggregation mv, GraphiteSocket socket) {
		List<String> pathElements = []
		pathElements.addAll(prefix.tokenize('.'))
		pathElements.add(replaceInvalidGraphitePathCharacters(jobGroup.name))
		pathElements.add('daily')
		pathElements.add('csi')
		
		GraphitePathName finalPathName=GraphitePathName.valueOf(pathElements.toArray(new String[pathElements.size()]));
		double valueAsPercentage = mv.csByWptDocCompleteInPercent * 100
		if(log.debugEnabled) log.debug("Sending ${mv.started}|${valueAsPercentage} as daily shop- CsiAggregation to graphite-path ${finalPathName}")
		socket.sendDate(finalPathName, valueAsPercentage, mv.started)
	}

	private void reportWeeklyPageCsiAggregation(String prefix, JobGroup jobGroup, CsiAggregation mv, GraphiteSocket socket) {
		Page page = csiAggregationTagService.findPageByPageTag(mv.tag)
		
		List<String> pathElements = []
		pathElements.addAll(prefix.tokenize('.'))
		pathElements.add(replaceInvalidGraphitePathCharacters(jobGroup.name))
		pathElements.add('weekly')
		pathElements.add(replaceInvalidGraphitePathCharacters(page.name))
		pathElements.add('csi')
		
		GraphitePathName finalPathName=GraphitePathName.valueOf(pathElements.toArray(new String[pathElements.size()]));
		double valueAsPercentage = mv.csByWptDocCompleteInPercent * 100
		if(log.debugEnabled) log.debug("Sending ${mv.started}|${valueAsPercentage} as weekly page-CsiAggregation to graphite-path ${finalPathName}")
		socket.sendDate(finalPathName, valueAsPercentage, mv.started)
	}

	private void reportWeeklyShopCsiAggregation(String prefix, JobGroup jobGroup, CsiAggregation mv, GraphiteSocket socket) {
		List<String> pathElements = []
		pathElements.addAll(prefix.tokenize('.'))
		pathElements.add(replaceInvalidGraphitePathCharacters(jobGroup.name))
		pathElements.add('weekly')
		pathElements.add('csi')
		
		GraphitePathName finalPathName=GraphitePathName.valueOf(pathElements.toArray(new String[pathElements.size()]));
		double valueAsPercentage = mv.csByWptDocCompleteInPercent * 100
		if(log.debugEnabled) log.debug("Sending ${mv.started}|${valueAsPercentage} as weekly shop-CsiAggregation to graphite-path ${finalPathName}")
		socket.sendDate(finalPathName, valueAsPercentage, mv.started)
	}
	
	private String replaceInvalidGraphitePathCharacters(String graphitePathElement){
		String replaced = graphitePathElement
		INVALID_GRAPHITE_PATH_CHARACTERS.each {String invalidChar ->
			replaced = replaced.replace(invalidChar, REPLACEMENT_FOR_INVALID_GRAPHITE_PATH_CHARACTERS)
		}
		return replaced
	}
}
