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

package de.iteratec.osm.measurement.environment.wptserverproxy

import de.iteratec.osm.result.JobResultService
import de.iteratec.osm.util.PerformanceLoggingService
import groovy.util.slurpersupport.GPathResult

import java.util.zip.GZIPOutputStream

import org.springframework.transaction.TransactionStatus

import de.iteratec.osm.ConfigService
import de.iteratec.osm.csi.MeasuredValueUpdateService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TimeToCsMappingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.external.GraphiteComunicationFailureException
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.EventResultService
import de.iteratec.osm.result.HttpArchive
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.result.PageService
import de.iteratec.osm.result.WptXmlResultVersion
import de.iteratec.osm.result.detail.HarParserService
import de.iteratec.osm.result.detail.WebPerformanceWaterfall

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG

/**
 * Persists locations and results. Observer of ProxyService.
 * @author rschuett, nkuhn
 *grails-app/services/de/iteratec/ispc/LocationAndResultPersisterService.groovy
 */
class LocationAndResultPersisterService implements iListener{

	static transactional = false
	
	public static final String STATIC_PART_WATERFALL_ANCHOR = '#waterfall_view'
	
	BrowserService browserService
	MeasuredValueUpdateService measuredValueUpdateService
	TimeToCsMappingService timeToCsMappingService
	PageService pageService
	JobService jobService
	EventResultService eventResultService
	MeasuredValueTagService measuredValueTagService
	ProxyService proxyService
	MetricReportingService metricReportingService
	HarParserService harParserService
	ConfigService configService
    PerformanceLoggingService performanceLoggingService


    /**
	 * Persisting non-existent locations.
	 */
	@Override
	public void listenToLocations(GPathResult result, WebPageTestServer wptserverForLocation) {
		log.info("Location.count before creating non-existent= ${Location.count()}")
		def query
		result.data.location.each { locationTagInXml ->
			Browser browserOfLocation = browserService.findByNameOrAlias(locationTagInXml.Browser.toString())
			query = Location.where {
				wptServer == wptserverForLocation  && browser == browserOfLocation && uniqueIdentifierForServer == locationTagInXml.id.toString()
			}
			
			List<Location> locations=query.list();
			
			if(locations.size()==0) {
				Location newLocation = new Location(
					active: true,
					valid: 1,
					uniqueIdentifierForServer: locationTagInXml.id.toString(), // z.B. Agent1-wptdriver:Chrome
					location: locationTagInXml.location.toString(),//z.B. Agent1-wptdriver
					label: locationTagInXml.Label.toString(),//z.B. Agent 1: Windows 7 (S008178178)
					browser: browserOfLocation,//z.B. Firefox
					wptServer: wptserverForLocation
					).save(failOnError: true);
				
				log.info("new location written while fetching locations: ${newLocation}")
			} else if(locations.size() > 1) {
				log.error("Multiple Locations (${locations.size()}) found for WPT-Server: ${wptserverForLocation}, Browser: ${browserOfLocation}, Location: ${locationTagInXml.id.toString()} - Skipping work!")
			}
			
		}
		log.info("Location.count after creating non-existent= ${Location.count()}")
	}

	/**
	 * Persisting fetched {@link EventResult}s. If associated JobResults and/or Jobs and/or Locations don't exist they will be persisted, too.
	 * Dependent {@link de.iteratec.isocsi.MeasuredValue}s will be created/marked/calculated.
	 * Persisted {@link EventResult} will be reported to graphite if configured respectively.
	 * <br><b>Note:</b> Persistance of the {@link EventResult}s of one test step (i.e. for one {@link MeasuredEvent}) is wrapped into a transaction. So ANY other downstream operations may not rollback the persistance 
	 * of the {@link EventResult}s
	 */
	@Override
	public void listenToResult(
			GPathResult xmlResultResponse,
			String har,
			WebPageTestServer wptserverOfResult) {
			
		WptResultXml resultXml = new WptResultXml(xmlResultResponse)
			
        Job jobConfig
        performanceLoggingService.logExecutionTime(DEBUG, "get or persist Job ${resultXml.getLabel()} while processing test ${resultXml.getTestId()}...", PerformanceLoggingService.IndentationDepth.FOUR){
            jobConfig = Job.findByLabel(resultXml.getLabel())?:persistNewJobConfig(resultXml, wptserverOfResult).save(failOnError: true)
        }

		if (jobConfig != null) {
            performanceLoggingService.logExecutionTime(DEBUG, "persist JobResult and EventResults for job ${resultXml.getLabel()}, test ${resultXml.getTestId()}...", PerformanceLoggingService.IndentationDepth.FOUR){
                persistJobResultAndAssociatedEventResults(jobConfig, resultXml, wptserverOfResult, har)
            }
        }
			
	}
			
	void persistJobResultAndAssociatedEventResults(Job jobConfig, WptResultXml resultXml, WebPageTestServer wptserverOfResult, String har){
		
		log.debug('updating locations ...')
		updateLocationIfNeededAndPossible(jobConfig, resultXml, wptserverOfResult);
		log.debug('updating locations ... DONE')
		
		String testId = resultXml.getTestId()
		log.debug("test-ID for which results should get persisted now=${testId}")
		if(testId != null){
			
			JobResult jobRun
			JobResult.withTransaction { TransactionStatus status ->
				try{
					log.debug("deleting results marked as pending or running ...")
					deleteResultsMarkedAsPendingAndRunning(resultXml.getLabel(), testId)
					log.debug("deleting results marked as pending or running ... DONE")
					log.debug("persisting job result ...")
					jobRun = JobResult.findByJobConfigLabelAndTestId(resultXml.getLabel(), testId)?:
						persistNewJobRun(jobConfig, resultXml).save(failOnError: true);
					log.debug("persisting job result ... DONE")
				} catch (Exception e) {
					status.setRollbackOnly()
					log.error("An error occurred while deleting pending and persisting new JobResult: " +
                            "job=${jobConfig.label}, test-id=${testId}", e)
				}
			}
					
			if (jobRun != null) persistResultsForAllTeststeps(jobRun, resultXml, jobConfig, har)
			
		}
	}
	
	/**
	 * <p>
	 * Checks ...
	 * <ul>
	 * <li>whether uniqueIdentifierForServer of location associated to job meets location from result-xml.</li>
	 * <li>whether wptserver of location associated to job meets wptserver results are fetched for.</li>
	 * </ul>
	 * If one of the checks fails the correct location is read from db or fetched via wptservers getLocations.php (if it isn't in db already).
	 * Afterwards that location is associated to the job.
	 * </p>
	 * @param job
	 * @param resultXml
	 * @param expectedServer
	 */
	private void updateLocationIfNeededAndPossible(Job job, WptResultXml resultXml, WebPageTestServer expectedServer) {
		String locationStringFromResultXml = resultXml.getLocation()
		if(job.getLocation().getUniqueIdentifierForServer() != locationStringFromResultXml || job.getLocation().getWptServer() != expectedServer) {
			try {
				Location location = getOrFetchLocation(expectedServer, locationStringFromResultXml);
				job.setLocation(location);
				job.save(failOnError: true);
			} catch(IllegalArgumentException e) {
				log.error("Failed to get or update Location!", e);
				throw e;
			}
		}
	}

	protected Job persistNewJobConfig(WptResultXml resultXml, WebPageTestServer wptserverOfResult){
		String jobConfLabel = resultXml.getLabel()
		log.debug("persisting new Job ${jobConfLabel}")
		Location location = getOrFetchLocation(wptserverOfResult, resultXml.getLocation());
		JobGroup jobGroup = JobGroup.findByName(JobGroup.UNDEFINED_CSI)
		log.debug("Incoming Result for unknown Job.")	
			
		Job jobConfig = new Job(
				location: location,
				active: false,
				label: jobConfLabel,
				runs: resultXml.getRunCount(),
				jobGroup: jobGroup,
				script: Script.createDefaultScript(jobConfLabel).save(failOnError: true),
				maxDownloadTimeInMinutes: configService.getDefaultMaxDownloadTimeInMinutes()
				)
		//new 'feature' of grails 2.3: empty strings get converted to null in map-constructors
		jobConfig.setDescription('')
		return jobConfig
	}

	protected JobResult persistNewJobRun(Job jobConfig, WptResultXml resultXml){

		String testId = resultXml.getTestId()

		if(!testId){
			return
		}
		log.debug("persisting new JobResult ${testId}")

		Integer jobRunStatus = resultXml.getStatusCodeOfWholeTest()
		Date testCompletion = resultXml.getCompletionDate()
		jobConfig.lastRun = testCompletion
		jobConfig.save(failOnError: true)

		JobResult result = new JobResult(
            job: jobConfig,
            date: testCompletion,
            testId: testId,
            httpStatusCode: jobRunStatus,
            jobConfigLabel: jobConfig.label,
            jobConfigRuns: jobConfig.runs,
            wptServerLabel: jobConfig.location.wptServer.label,
            wptServerBaseurl: jobConfig.location.wptServer.baseUrl,
            locationLabel: jobConfig.location.label,
            locationLocation: jobConfig.location.location,
            locationUniqueIdentifierForServer: jobConfig.location.uniqueIdentifierForServer,
            locationBrowser: jobConfig.location.browser.name,
            jobGroupName: jobConfig.jobGroup.name,
            testAgent:resultXml.getTestAgent()
        )
		
		//new 'feature' of grails 2.3: empty strings get converted to null in map-constructors
		result.setDescription('')
		
		return result
	}
	void persistResultsForAllTeststeps(JobResult jobRun, WptResultXml resultXml, Job job, String har){
		
		Integer testStepCount = resultXml.getTestStepCount()
		
		Map<String, WebPerformanceWaterfall> pageidToWaterfallMap = [:]
		//TODO: enable parsing of waterfalls again, if nightly deletion is working (see de.iteratec.osm.persistence.DbCleanupService)
		/*
		try {
			if (testStepCount>0 && har) {
				pageidToWaterfallMap = harParserService.getWaterfalls(har)
			}
			pageidToWaterfallMap = harParserService.removeWptMonitorSuffixAndPagenamePrefixFromEventnames(pageidToWaterfallMap)
		} catch (Exception e) {
			log.error("Failed to parse http-archive: ${har}\n${e.toString()}")
		}
		*/
		log.debug("starting persistance of ${testStepCount} event results for test steps")
		List<EventResult> resultsOfTeststep = []
		testStepCount.times{nullBasedTeststepIndex ->
			//TODO: possible to catch non median results at this position  and check if they should persist or not

			EventResult.withTransaction { TransactionStatus status ->
                log.debug("Persisting EventResults of jobRun: ${jobRun}: Start of transaction, transaction status=${status}")
				try{
					resultsOfTeststep.addAll(persistResultsOfOneTeststep(nullBasedTeststepIndex, jobRun, resultXml, job, pageidToWaterfallMap))
                    log.debug("Persisting EventResults of jobRun: ${jobRun}: End of transaction, transaction status=${status}")
				} catch (Exception e) {
					status.setRollbackOnly()
					log.error("an error occurred while persisting EventResults of teststep ${nullBasedTeststepIndex}", e)
				}
			}
		}
		informDependents(resultsOfTeststep)
	}
	
	protected List<EventResult> persistResultsOfOneTeststep(
		Integer testStepZeroBasedIndex, JobResult jobRun, WptResultXml resultXml, Job job, Map<String, WebPerformanceWaterfall> pageidToWaterfallMap){

		log.debug('getting event name from xml result ...')
		String measuredEventName = resultXml.getEventName(job, testStepZeroBasedIndex)
		log.debug('getting event name from xml result ... DONE')
		log.debug('getting waterfall anchor ...')
		String waterfallAnchor = "${STATIC_PART_WATERFALL_ANCHOR}${measuredEventName.replace(PageService.STEPNAME_DELIMITTER, '').replace('.', '')}"
		log.debug('getting waterfall anchor ... DONE')
		log.debug("getting MeasuredEvent from eventname '${measuredEventName}' ...")
		MeasuredEvent event = getMeasuredEvent(measuredEventName);
		log.debug("getting MeasuredEvent from eventname '${measuredEventName}' ... DONE")

		log.debug("persisting result for step=${event}")
		Integer runCount = resultXml.getRunCount()
		log.debug("runCount=${runCount}")

		List<EventResult> resultsOfTeststep = []
		resultXml.getRunCount().times {Integer runNumber ->
            if( resultXml.resultExistForRunAndView(runNumber, CachedView.UNCACHED) &&
                    (job.persistNonMedianResults || resultXml.isMedian(runNumber, CachedView.UNCACHED, testStepZeroBasedIndex)) ) {
                EventResult firstViewOfTeststep = persistSingleResult(resultXml, runNumber, CachedView.UNCACHED, testStepZeroBasedIndex, jobRun, event, pageidToWaterfallMap, waterfallAnchor)
                if (firstViewOfTeststep != null) resultsOfTeststep.add(firstViewOfTeststep)
            }
            if( resultXml.resultExistForRunAndView(runNumber, CachedView.CACHED) &&
                    (job.persistNonMedianResults || resultXml.isMedian(runNumber, CachedView.CACHED, testStepZeroBasedIndex)) ) {
                EventResult repeatedViewOfTeststep = persistSingleResult(resultXml, runNumber, CachedView.CACHED, testStepZeroBasedIndex, jobRun, event, pageidToWaterfallMap, waterfallAnchor)
                if (repeatedViewOfTeststep != null) resultsOfTeststep.add(repeatedViewOfTeststep)
            }
		}
		/*
		resultXml.getRunNodes().each{GPathResult run ->
			
			EventResult firstViewOfTeststep = persistSingleRunResult(run.firstView, testStepZeroBasedIndex, jobRun, event, pageidToWaterfallMap, waterfallAnchor)
			if (firstViewOfTeststep != null) resultsOfTeststep.add(firstViewOfTeststep)
			
			EventResult repeatedViewOfTeststep = persistSingleRunResult(run.repeatView, testStepZeroBasedIndex, jobRun, event, pageidToWaterfallMap, waterfallAnchor)
			if (repeatedViewOfTeststep != null) resultsOfTeststep.add(repeatedViewOfTeststep)
			
		}
		*/
		
		return resultsOfTeststep
	}
		
	/**
	 * Persists a single Run result
	 * @param singleViewNode the node of the result
	 * @param medianRunIdentificator the id of the median node corresponding to the 
	 * @param xmlResultVersion
	 * @param testStepZeroBasedIndex
	 * @param jobRungrails-app/services/de/iteratec/ispc/LocationAndResultPersisterService.groovy
	 * @param event
	 * @return Persisted result. Null if the view node is empty, i.e. the test was a "first view only" and this is the repeated view node
	 */
	private EventResult persistSingleResult(
		WptResultXml resultXml, Integer runZeroBasedIndex, CachedView cachedView, Integer testStepZeroBasedIndex, JobResult jobRun, MeasuredEvent event, 
		Map<String, WebPerformanceWaterfall> pageidToWaterfallMap, String waterfallAnchor) {

		EventResult result
        GPathResult viewResultsNodeOfThisRun = resultXml.getResultsContainingNode(runZeroBasedIndex, cachedView, testStepZeroBasedIndex)
        result = persistResult(jobRun, event, cachedView, runZeroBasedIndex+1, resultXml.isMedian(runZeroBasedIndex, cachedView, testStepZeroBasedIndex), viewResultsNodeOfThisRun, pageidToWaterfallMap, waterfallAnchor)

		return result
	}

	/**
	 * Is called for every Step. Persists a Single Step Result.
	 * @param jobRun
	 * @param step
	 * @param view
	 * @param run
	 * @param median
	 * @param viewTag
	 * @return
	 */
	protected EventResult persistResult(
		JobResult jobRun, MeasuredEvent event, CachedView view, Integer run, Boolean median, GPathResult viewTag, 
		Map<String, WebPerformanceWaterfall> pageidToWaterfallMap, String waterfallAnchor){

		EventResult result = jobRun.findEventResult(event, view, run) ?: new EventResult()
		return saveResult(result, jobRun, event, view, run, median, viewTag, pageidToWaterfallMap, waterfallAnchor)

	}
	
	protected EventResult saveResult(EventResult result, JobResult jobRun, MeasuredEvent step, CachedView view, Integer run,Boolean median,
			GPathResult viewTag, Map<String, WebPerformanceWaterfall> pageidToWaterfallMap, String waterfallAnchor){


		log.debug("persisting result: jobRun=${jobRun.testId}, run=${run}, cachedView=${view}, medianValue=${median}")
		Integer docCompleteTime = viewTag.docTime.toInteger()
		result.measuredEvent=step
		result.numberOfWptRun=run
		result.cachedView=view
		result.medianValue=median
		result.wptStatus=viewTag.result.toInteger()
		result.docCompleteIncomingBytes=viewTag.bytesInDoc.toInteger()
		result.docCompleteRequests=viewTag.requestsDoc.toInteger()
		result.docCompleteTimeInMillisecs=docCompleteTime
		result.domTimeInMillisecs=viewTag.domTime.toInteger()
		result.firstByteInMillisecs=viewTag.TTFB.toInteger()
		result.fullyLoadedIncomingBytes=viewTag.bytesIn.toInteger()
		result.fullyLoadedRequestCount=viewTag.requests.toInteger()
		result.fullyLoadedTimeInMillisecs=viewTag.fullyLoaded.toInteger()
		result.loadTimeInMillisecs=viewTag.loadTime.toInteger()
		result.startRenderInMillisecs=viewTag.render.toInteger()
		result.lastStatusUpdate=new Date()
		result.jobResult=jobRun
		result.jobResultDate=jobRun.date
		result.jobResultJobConfigId=jobRun.job.ident()
		JobGroup csiGroup = jobRun.job.jobGroup?:JobGroup.findByName(JobGroup.UNDEFINED_CSI)
		result.tag = measuredValueTagService.createEventResultTag(csiGroup, step, step.testedPage, jobRun.job.location.browser, jobRun.job.location)
		if(!viewTag.SpeedIndex.isEmpty() && viewTag.SpeedIndex.toString().isInteger() && viewTag.SpeedIndex.toInteger() > 0 ){
			result.speedIndex = viewTag.SpeedIndex.toInteger()
		}else {
			result.speedIndex = EventResult.SPEED_INDEX_DEFAULT_VALUE
		}
		if(!viewTag.visualComplete.isEmpty() && viewTag.visualComplete.toString().isInteger() && viewTag.visualComplete.toInteger() > 0 ){
			result.visuallyCompleteInMillisecs = viewTag.visualComplete.toInteger()
		}
		//TODO: enable saving of waterfalls again, if nightly deletion is working (see de.iteratec.osm.persistence.DbCleanupService)
		/*
		WebPerformanceWaterfall waterfall = pageidToWaterfallMap[
			harParserService.createPageIdFrom(
				run, 
				xmlResultVersion == WptXmlResultVersion.BEFORE_MULTISTEP ? null : step.name,
				view
			 )
		]
		result.webPerformanceWaterfall = waterfall?waterfall.save(failOnError: true):null
		*/
		result.webPerformanceWaterfall = null
		try {
			result.testDetailsWaterfallURL = result.buildTestDetailsURL(jobRun, waterfallAnchor);
		} catch (MalformedURLException mue) {
			log.error("Failed to build test's detail url for result: ${result}!")
		} catch (Exception e){
			log.error("An unexpected error occurred while trying to build test's detail url (result=${result})!", e)
		}
		try{
			log.debug("step=${step}")
			log.debug("step.testedPage=${step.testedPage}")
			Boolean validFrustrationsExist = timeToCsMappingService.validFrustrationsExistFor(step.testedPage)
			log.debug("validFrustrationsExist=${validFrustrationsExist}")
			if(validFrustrationsExist) {
				result.customerSatisfactionInPercent = timeToCsMappingService.getCustomerSatisfactionInPercent(docCompleteTime, step.testedPage)
			}
		}catch(Exception e){
			log.warn("No customer satisfaction can be written for EventResult: ${result}: ${e.message}", e)
		}
        result.testAgent=jobRun.testAgent

		// FIXME: 2014-01-27-nku
		//The following line is necessary in unit-tests since Grails version 2.3, but isn't in production. Should be removed if this bug s fixed in grails.  
		result.save(failOnError: true)
		jobRun.save(flush: true, failOnError: true)
		
		return result
		
	}
	private void informDependents(List<EventResult> results){
		
		log.debug('informing event result dependents ...')
		results.each {EventResult result ->
			informDependent(result)
		}
		log.debug('informing event result dependents ... DONE')
		
	}
	
	private void informDependent(EventResult result){
		
		if (result.medianValue && !result.measuredEvent.testedPage.isUndefinedPage()) {
			
			if (result.cachedView==CachedView.UNCACHED) {
				log.debug('informing dependent measured values ...')
				informDependentMeasuredValues(result)
				log.debug('informing dependent measured values ... DONE')
			}
			log.debug('reporting persisted event result ...')
			report(result)
			log.debug('reporting persisted event result ... DONE')
			
		}
	}
	
	void informDependentMeasuredValues(EventResult result){
		try{
			boolean isCsiRelevant = measuredValueTagService.findJobGroupOfEventResultTag(result.tag).groupType == JobGroupType.CSI_AGGREGATION
			if (isCsiRelevant) measuredValueUpdateService.createOrUpdateDependentMvs(result)
		}catch(Exception e){
			log.error("An error occurred while creating EventResult-dependent MeasuredValues for result: ${result}", e)
		}
	}
	
	void report(EventResult result){
		try{
			metricReportingService.reportEventResultToGraphite(result)
		}catch(GraphiteComunicationFailureException gcfe){
			log.error("Can't report EventResult to graphite-server: ${gcfe.message}")
		}catch(Exception e){
			log.error("An error occurred while reporting EventResult to graphite.", e)
		}
	}
	
	private Location getOrFetchLocation(WebPageTestServer wptserverOfResult, String locationIdentifier) {
		
		Location location=queryForLocation(wptserverOfResult, locationIdentifier);
		
		if(location==null) {
			log.warn("Location not found trying to refresh ${wptserverOfResult} and ${locationIdentifier}.")
			proxyService.fetchLocations(wptserverOfResult);
			
			location=queryForLocation(wptserverOfResult, locationIdentifier);
		}
		
		if(location==null) {
			throw new IllegalArgumentException("Location not found for LocationIdentifier: ${locationIdentifier}");
		}

		return location;
	}
	
	private Location queryForLocation(WebPageTestServer wptserverOfResult, String locationIdentifier) {
		Location location;
		def query = Location.where {
			wptServer == wptserverOfResult && uniqueIdentifierForServer == locationIdentifier
		}
		
		if(query.count()==0) {
			return null;
		} else {
			return query.get();
		}
	}
	
	/**
	 * Looks for a {@link MeasuredEvent} with the given stepName.
	 * If it exists it will be returned. Otherwise a new one will be created and returned. 
	 * @param stepName
	 * @param jobRun
	 * @return
	 */
	protected MeasuredEvent getMeasuredEvent(String stepName){
		Page page = pageService.getPageByStepName(stepName)
		String stepNameExcludedPagename = pageService.excludePagenamePart(stepName)
		MeasuredEvent step = MeasuredEvent.findByName(stepNameExcludedPagename)?:
				persistNewMeasuredEvent(stepNameExcludedPagename, page)
		return step
	}

	
	protected MeasuredEvent persistNewMeasuredEvent(String stepName, Page page){
		return new MeasuredEvent(name: stepName, testedPage: page).save(failOnError: true)
	}

	protected Byte[] zip(String s){
		def targetStream = new ByteArrayOutputStream()
		def zipStream = new GZIPOutputStream(targetStream)
		zipStream.write(s.getBytes())
		zipStream.close()
		def zipped = targetStream.toByteArray()
		targetStream.close()
		return zipped
	}
	
	/**
	 * Clear pending/running {@link JobResult}s (i.e. wptStatus is 100 or 101) before persisting final {@link EventResult}s.
	 * @param jobLabel
	 * @param testId
	 */
	void deleteResultsMarkedAsPendingAndRunning(String jobLabel, String testId){
		JobResult.findByJobConfigLabelAndTestIdAndHttpStatusCodeLessThan(jobLabel, testId, 200)?.delete(failOnError: true)
	}
	
	public String getName() {
		return "LocationAndResultPersisterService"
	}
}
