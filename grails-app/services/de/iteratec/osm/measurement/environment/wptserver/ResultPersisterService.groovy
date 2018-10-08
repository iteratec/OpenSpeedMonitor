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

package de.iteratec.osm.measurement.environment.wptserver

import de.iteratec.osm.csi.CsiAggregationUpdateService
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.external.GraphiteComunicationFailureException
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.*
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.transactions.Transactional
import grails.web.mapping.LinkGenerator
import groovy.util.slurpersupport.GPathResult
import org.springframework.transaction.annotation.Propagation

import java.util.zip.GZIPOutputStream

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG
/**
 * Persists locations and results. Observer of WptInstructionService.
 * @author rschuett , nkuhn
 * grails-app/services/de/iteratec/ispc/ResultPersisterService.groovy
 */
class ResultPersisterService implements iResultListener {

    public static final String STATIC_PART_WATERFALL_ANCHOR = '#waterfall_view'

    private boolean callListenerAsync = false

    CsiAggregationUpdateService csiAggregationUpdateService
    TimeToCsMappingService timeToCsMappingService
    PageService pageService
    WptInstructionService wptInstructionService
    MetricReportingService metricReportingService
    PerformanceLoggingService performanceLoggingService
    CsiValueService csiValueService
    LinkGenerator grailsLinkGenerator
    JobDaoService jobDaoService

    /**
     * Persisting fetched {@link EventResult}s. If associated JobResults and/or Jobs and/or Locations don't exist they will be persisted, too.
     * Dependent {@link de.iteratec.osm.report.chart.CsiAggregation}s will be created/marked/calculated.
     * Persisted {@link EventResult} will be reported to graphite if configured respectively.
     * <br><b>Note:</b> Persistance of the {@link EventResult}s of one test step (i.e. for one {@link MeasuredEvent}) is wrapped into a transaction. So ANY other downstream operations may not rollback the persistance
     * of the {@link EventResult}s
     */
    @Override
    String getListenerName() {
        return "ResultPersisterService"
    }

    @Override
    public void listenToResult(
            WptResultXml resultXml,
            WebPageTestServer wptserverOfResult,
            long jobId) {

        try {
            checkJobAndLocation(resultXml, wptserverOfResult, jobId)
            persistJobResult(resultXml, jobId)
            persistResultsForAllTeststeps(resultXml, jobId)
            informDependents(resultXml, jobId)

        } catch (OsmResultPersistanceException e) {
            log.error(e.message, e)
        }

    }

    @Override
    boolean callListenerAsync() {
        return callListenerAsync
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void checkJobAndLocation(WptResultXml resultXml, WebPageTestServer wptserverOfResult, long jobId) throws OsmResultPersistanceException {
        Job job
        performanceLoggingService.logExecutionTime(DEBUG, "get or persist Job ${resultXml.getLabel()} while processing test ${resultXml.getTestId()}...", 4) {
            job = jobDaoService.getJob(jobId)
            if (job == null) throw new OsmResultPersistanceException("No measurement job could be found for id ${jobId} from result xml")
        }
        performanceLoggingService.logExecutionTime(DEBUG, "updateLocationIfNeededAndPossible while processing test ${resultXml.getTestId()}...", 4) {
            updateLocationIfNeededAndPossible(job, resultXml, wptserverOfResult);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void persistJobResult(WptResultXml resultXml, long jobId) throws OsmResultPersistanceException {

        performanceLoggingService.logExecutionTime(DEBUG, "persist JobResult for job ${resultXml.getLabel()}, test ${resultXml.getTestId()}...", 4) {
            String testId = resultXml.getTestId()
            log.debug("test-ID for which results should get persisted now=${testId}")

            if (testId == null) {
                throw new OsmResultPersistanceException("No test id in result xml file from wpt server!")
            }

            log.debug("Deleting pending JobResults and create finished ...")
            removePendingAndCreateFinishedJobResult(resultXml, testId, jobId)
            log.debug("Deleting pending JobResults and create finished ... DONE")
        }

    }

    private void removePendingAndCreateFinishedJobResult(resultXml, String testId, long jobId) {
        Job job = jobDaoService.getJob(jobId)
        if (job == null) throw new RuntimeException("No measurement job could be found for id from result xml: ${jobid}")
        deleteResultsMarkedAsPendingAndRunning(job, testId)

        JobResult jobResult = JobResult.findByJobAndTestId(job, testId)
        if (!jobResult) {
            persistNewJobRun(job, resultXml)
        } else {
            updateJobResult(jobResult, resultXml)
        }
    }

    private void updateJobResult(JobResult jobResult, WptResultXml resultXml) {
        jobResult.testAgent = resultXml.getTestAgent()
        jobResult.wptVersion = resultXml.version.toString()
        jobResult.save(failOnError: true, flush: true)
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
        if (job.getLocation().getUniqueIdentifierForServer() != locationStringFromResultXml || job.getLocation().getWptServer() != expectedServer) {
            try {
                Location location = getOrFetchLocation(expectedServer, locationStringFromResultXml);
                job.setLocation(location);
                job.save(failOnError: true);
            } catch (IllegalArgumentException e) {
                log.error("Failed to get or update Location!", e);
                throw e;
            }
        }
    }

    protected void persistNewJobRun(Job job, WptResultXml resultXml) {

        String testId = resultXml.getTestId()

        if (!testId) {
            return
        }
        log.debug("persisting new JobResult ${testId}")

        Integer jobRunStatus = resultXml.getStatusCodeOfWholeTest()
        Date testCompletion = resultXml.getCompletionDate()
        job.lastRun = testCompletion
        job.merge(failOnError: true)

        JobResult result = new JobResult(
                job: job,
                date: testCompletion,
                testId: testId,
                httpStatusCode: jobRunStatus,
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
                wptServerLabel: job.location.wptServer.label,
                wptServerBaseurl: job.location.wptServer.baseUrl,
                locationLabel: job.location.label,
                locationLocation: job.location.location,
                locationUniqueIdentifierForServer: job.location.uniqueIdentifierForServer,
                locationBrowser: job.location.browser.name,
                jobGroupName: job.jobGroup.name,
                testAgent: resultXml.getTestAgent(),
                wptVersion: resultXml.version.toString()
        )

        //new 'feature' of grails 2.3: empty strings get converted to null in map-constructors
        result.setDescription('')
        result.save(failOnError: true, flush: true)
    }

    void persistResultsForAllTeststeps(WptResultXml resultXml, long jobId) {

        Integer testStepCount = resultXml.getTestStepCount()

        log.debug("starting persistance of ${testStepCount} event results for test steps")
        //TODO: possible to catch non median results at this position  and check if they should persist or not

        for (int zeroBasedTeststepIndex = 0; zeroBasedTeststepIndex < testStepCount; zeroBasedTeststepIndex++) {
            if (resultXml.getStepNode(zeroBasedTeststepIndex)) {
                try {
                    persistResultsOfOneTeststep(zeroBasedTeststepIndex, resultXml, jobId)
                } catch (Exception e) {
                    log.error("an error occurred while persisting EventResults of testId ${resultXml.getTestId()} of teststep ${zeroBasedTeststepIndex}", e)
                }

            } else {
                log.error("there is no testStep ${zeroBasedTeststepIndex + 1} for testId ${resultXml.getTestId()}")
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected List<EventResult> persistResultsOfOneTeststep(Integer testStepZeroBasedIndex, WptResultXml resultXml, long jobId) throws OsmResultPersistanceException {

        String testId = resultXml.getTestId()
        Job job = jobDaoService.getJob(jobId)
        JobResult jobResult = JobResult.findByJobAndTestId(job, testId)
        if (jobResult == null) {
            throw new OsmResultPersistanceException(
                    "JobResult couldn't be read from db while persisting associated EventResults for test id '${testId}'!"
            )
        }

        log.debug('getting event name from xml result ...')
        String measuredEventName = resultXml.getEventName(job, testStepZeroBasedIndex)
        log.debug('getting event name from xml result ... DONE')
        log.debug("getting MeasuredEvent from eventname '${measuredEventName}' ...")
        MeasuredEvent event = getMeasuredEvent(measuredEventName);
        log.debug("getting MeasuredEvent from eventname '${measuredEventName}' ... DONE")

        log.debug("persisting result for step=${event}")
        Integer runCount = resultXml.getRunCount()
        log.debug("runCount=${runCount}")

        List<EventResult> resultsOfTeststep = []
        resultXml.getRunCount().times { Integer runNumber ->
            if (resultXml.resultExistForRunAndView(runNumber, CachedView.UNCACHED) &&
                    (job.persistNonMedianResults || resultXml.isMedian(runNumber, CachedView.UNCACHED, testStepZeroBasedIndex))) {
                EventResult firstViewOfTeststep = persistSingleResult(resultXml, runNumber, CachedView.UNCACHED, testStepZeroBasedIndex, jobResult, event)
                if (firstViewOfTeststep != null) resultsOfTeststep.add(firstViewOfTeststep)
            }
            if (resultXml.resultExistForRunAndView(runNumber, CachedView.CACHED) &&
                    (job.persistNonMedianResults || resultXml.isMedian(runNumber, CachedView.CACHED, testStepZeroBasedIndex))) {
                EventResult repeatedViewOfTeststep = persistSingleResult(resultXml, runNumber, CachedView.CACHED, testStepZeroBasedIndex, jobResult, event)
                if (repeatedViewOfTeststep != null) resultsOfTeststep.add(repeatedViewOfTeststep)
            }
        }
        return resultsOfTeststep
    }

    /**
     * Persists a single Run result
     * @param singleViewNode the node of the result
     * @param medianRunIdentificator the id of the median node corresponding to the
     * @param xmlResultVersion
     * @param testStepZeroBasedIndex
     * @param jobRungrails -app/services/de/iteratec/ispc/ResultPersisterService.groovy
     * @param event
     * @return Persisted result. Null if the view node is empty, i.e. the test was a "first view only" and this is the repeated view node
     */
    private EventResult persistSingleResult(
            WptResultXml resultXml, Integer runZeroBasedIndex, CachedView cachedView, Integer testStepZeroBasedIndex, JobResult jobRun, MeasuredEvent event) {

        EventResult result
        GPathResult viewResultsNodeOfThisRun = resultXml.getResultsContainingNode(runZeroBasedIndex, cachedView, testStepZeroBasedIndex)
        GString waterfallAnchor = getWaterfallAnchor(resultXml, event.name, testStepZeroBasedIndex + 1)
        result = persistResult(
                jobRun,
                event,
                cachedView,
                runZeroBasedIndex + 1,
                resultXml.isMedian(runZeroBasedIndex, cachedView, testStepZeroBasedIndex),
                viewResultsNodeOfThisRun,
                testStepZeroBasedIndex + 1,
                waterfallAnchor
        )

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
            JobResult jobRun, MeasuredEvent event, CachedView view, Integer run, Boolean median, GPathResult viewTag, testStepOneBasedIndex, GString waterfallAnchor) {

        EventResult result = jobRun.findEventResult(event, view, run) ?: new EventResult()
        return saveResult(result, jobRun, event, view, run, median, viewTag, testStepOneBasedIndex, waterfallAnchor)

    }

    /**
     * Storing single {@link EventResult}.
     *
     * Should be persisted even if some subdata couldn't get determined (e.g.
     * customer satisfaction or determination fails with an exception. Therefore transaction must not be rollbacked
     * even if an arbitrary exception is thrown.
     *
     * @param result
     * {@link EventResult} to save. A new unpersisted object in most of the cases.
     * @param jobRun
     * {@link JobResult} of the {@link EventResult} to save.
     * @param step
     * {@link MeasuredEvent} of the {@link EventResult} to save.
     * @param view
     * {@link CachedView} of the {@link EventResult} to save.
     * @param run
     *          Run number of the {@link EventResult} to save.
     * @param median
     *          Whether or not the {@link EventResult} is a median result. Always true for tests with just one run.
     * @param viewTag
     *          Xml node with all the result data for the new {@link EventResult}.
     * @param waterfallAnchor
     *          String to build webpagetest server link for this {@link EventResult} from.
     * @return Saved {@link EventResult}.
     */
    protected EventResult saveResult(EventResult result, JobResult jobRun, MeasuredEvent step, CachedView view, Integer run, Boolean median,
                                     GPathResult viewTag, Integer testStepOneBasedIndex, GString waterfallAnchor) {

        log.debug("persisting result: jobRun=${jobRun.testId}, run=${run}, cachedView=${view}, medianValue=${median}")

        result.measuredEvent = step
        result.numberOfWptRun = run
        result.cachedView = view
        result.medianValue = median
        result.wptStatus = viewTag.result.toInteger()
        result.lastStatusUpdate = new Date()
        result.jobResult = jobRun
        result.jobResultDate = jobRun.date
        result.jobResultJobConfigId = jobRun.job.ident()
        JobGroup csiGroup = jobRun.job.jobGroup ?: JobGroup.findByName(JobGroup.UNDEFINED_CSI)
        result.jobGroup = csiGroup
        result.measuredEvent = step
        result.page = step.testedPage
        result.browser = jobRun.job.location.browser
        result.location = jobRun.job.location
        setAllMeasurands(viewTag, result)
        setWaterfallUrl(result, jobRun, waterfallAnchor)
        result.testAgent = jobRun.testAgent
        setConnectivity(result, jobRun)
        result.oneBasedStepIndexInJourney = testStepOneBasedIndex
        setAllUserTimings(viewTag, result)

        result.save(failOnError: true, flush: true)
        return result

    }

    private void setCustomerSatisfaction(EventResult result) {
        try {
            MeasuredEvent step = result.measuredEvent
            log.debug("step=${step}")
            log.debug("step.testedPage=${step.testedPage}")
            CsiConfiguration csiConfigurationOfResult = result.jobGroup.csiConfiguration
            log.debug("result.CsiConfiguration=${csiConfigurationOfResult}")
            if (result.docCompleteTimeInMillisecs) {
                result.csByWptDocCompleteInPercent = timeToCsMappingService.getCustomerSatisfactionInPercent(result.docCompleteTimeInMillisecs, step.testedPage, csiConfigurationOfResult)
            }
            if (result.visuallyCompleteInMillisecs) {
                result.csByWptVisuallyCompleteInPercent = timeToCsMappingService.getCustomerSatisfactionInPercent(result.visuallyCompleteInMillisecs, step.testedPage, csiConfigurationOfResult)
            }
        } catch (Exception e) {
            log.warn("No customer satisfaction can be written for EventResult: ${result}: ${e.message}", e)
        }
    }

    private void setWaterfallUrl(EventResult result, JobResult jobRun, GString waterfallAnchor) {
        try {
            result.testDetailsWaterfallURL = result.buildTestDetailsURL(jobRun, waterfallAnchor);
        } catch (MalformedURLException mue) {
            log.error("Failed to build test's detail url for result: ${result}!")
        } catch (Exception e) {
            log.error("An unexpected error occurred while trying to build test's detail url (result=${result})!", e)
        }
    }

    private GString getWaterfallAnchor(WptResultXml xmlResult, String eventName, Integer testStepOneBasedIndex) {
        switch (xmlResult.version) {
            case WptXmlResultVersion.BEFORE_MULTISTEP:
                return "${STATIC_PART_WATERFALL_ANCHOR}${eventName.replace(PageService.STEPNAME_DELIMITTER, '').replace('.', '')}"
            case WptXmlResultVersion.MULTISTEP_FORK_ITERATEC:
                return "${STATIC_PART_WATERFALL_ANCHOR}${eventName.replace(PageService.STEPNAME_DELIMITTER, '').replace('.', '')}"
            case WptXmlResultVersion.MULTISTEP:
                return "${STATIC_PART_WATERFALL_ANCHOR}_step${testStepOneBasedIndex}"
            default:
                throw new IllegalStateException("Version of result xml isn't specified!")
        }
    }

    private void setAllMeasurands(GPathResult inputValues, EventResult result) {
        Measurand.values().each {
            if (it.getTagInResultXml()) {
                setPropertyWithinEventResult(it, inputValues, result)
            }
        }
        setCustomerSatisfaction(result)
    }

    private void setPropertyWithinEventResult(Measurand measurand, GPathResult inputValues, EventResult result) {
        if (checkIfTagIsThere(inputValues, measurand.getTagInResultXml())) {
            result.setProperty(measurand.getEventResultField(), inputValues.getProperty(measurand.getTagInResultXml()).toInteger())
        }
    }

    private boolean checkIfTagIsThere(GPathResult viewTag, String tag) {
        return !viewTag.getProperty(tag).isEmpty() && viewTag.getProperty(tag).toString().isInteger() && viewTag.getProperty(tag).toInteger() > 0
    }

    private void setAllUserTimings(GPathResult viewTag, EventResult result){
        List<UserTiming> allUserTimings = []
        allUserTimings.addAll(createAllMarks(viewTag, result))
        allUserTimings.addAll(createAllMeasures(viewTag, result))
        result.userTimings = allUserTimings
    }

    private List<UserTiming> createAllMarks(GPathResult viewTag, EventResult result){
        return viewTag.getProperty(UserTimingType.MARK.tagInResultXml).children().findAll{
            it && it[0].name && it.toString().isDouble()
        }.collect{GPathResult valueTag ->
            return new UserTiming(
                    name: valueTag[0].name.toString(),
                    startTime: Double.parseDouble(valueTag.toString()),
                    type: UserTimingType.MARK,
                    eventResult: result
            )
        }
    }

    private List<UserTiming> createAllMeasures(GPathResult viewTag, EventResult result){
        return viewTag.getProperty(UserTimingType.MEASURE.tagInResultXml).value.findAll{
            it.name && it.startTime && it.duration && it.startTime.toString().isDouble() && it.duration.toString().isDouble()
        }.collect{GPathResult valueTag ->
            return new UserTiming(
                name: valueTag.(name).toString(),
                startTime: Double.parseDouble(valueTag.startTime.toString()),
                duration: Double.parseDouble(valueTag.duration.toString()),
                type: UserTimingType.MEASURE,
                eventResult: result
            )
        }
    }

    private void setConnectivity(EventResult result, JobResult jobRun) {
        if (jobRun.job.noTrafficShapingAtAll) {
            result.noTrafficShapingAtAll = true
        } else {
            result.noTrafficShapingAtAll = false
            if (jobRun.job.connectivityProfile) {
                result.connectivityProfile = jobRun.job.connectivityProfile
            } else if (jobRun.job.customConnectivityName) {
                result.customConnectivityName = jobRun.job.customConnectivityName
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void informDependents(WptResultXml resultXml, long jobId) {
        Job job = jobDaoService.getJob(jobId)
        JobResult jobResult = JobResult.findByJobAndTestId(job, resultXml.getTestId())
        if (jobResult == null) {
            throw new OsmResultPersistanceException(
                    "JobResult couldn't be read from db while informing dependents " +
                            "(testId='${resultXml.getTestId()}', jobId='${jobId}')!"
            )
        }
        List<EventResult> results = jobResult.getEventResults()

        log.debug("informing event result dependents about ${results.size()} new results...")
        results.each { EventResult result ->
            informDependent(result)
        }
        log.debug('informing event result dependents ... DONE')

    }

    private void informDependent(EventResult result) {

        if (result.medianValue) {

            if (result.cachedView == CachedView.UNCACHED && !result.measuredEvent.testedPage.isUndefinedPage()) {
                log.debug('informing dependent measured values ...')
                informDependentCsiAggregations(result)
                log.debug('informing dependent measured values ... DONE')
            }
            log.debug('reporting persisted event result ...')
            report(result)
            log.debug('reporting persisted event result ... DONE')

        }
    }

    void informDependentCsiAggregations(EventResult result) {
        try {
            if (csiValueService.isCsiRelevant(result)) {
                long resultId = result.ident()
                csiAggregationUpdateService.createOrUpdateDependentMvs(resultId)
            }
        } catch (Exception e) {
            log.error("An error occurred while creating EventResult-dependent CsiAggregations for result: ${result}", e)
        }
    }

    void report(EventResult result) {
        try {
            metricReportingService.reportEventResultToGraphite(result)
        } catch (GraphiteComunicationFailureException gcfe) {
            log.error("Can't report EventResult to graphite-server: ${gcfe.message}")
        } catch (Exception e) {
            log.error("An error occurred while reporting EventResult to graphite.", e)
        }
    }

    private Location getOrFetchLocation(WebPageTestServer wptserverOfResult, String locationIdentifier) {

        Location location = queryForLocation(wptserverOfResult, locationIdentifier);

        if (location == null) {

            log.warn("Location not found trying to refresh ${wptserverOfResult} and ${locationIdentifier}.")
            wptInstructionService.fetchLocations(wptserverOfResult);

            location = queryForLocation(wptserverOfResult, locationIdentifier);

        }

        if (location == null) {
            throw new IllegalArgumentException("Location not found for LocationIdentifier: ${locationIdentifier}");
        }

        return location;
    }

    private Location queryForLocation(WebPageTestServer wptserverOfResult, String locationIdentifier) {

        def query = Location.where {
            wptServer == wptserverOfResult && uniqueIdentifierForServer == locationIdentifier
        }

        if (query.count() == 0) {
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
    protected MeasuredEvent getMeasuredEvent(String stepName) {
        Page page = pageService.getPageByStepName(stepName)
        String stepNameExcludedPagename = pageService.excludePagenamePart(stepName)
        MeasuredEvent step = MeasuredEvent.findByName(stepNameExcludedPagename) ?:
                persistNewMeasuredEvent(stepNameExcludedPagename, page)
        return step
    }


    protected MeasuredEvent persistNewMeasuredEvent(String stepName, Page page) {
        return new MeasuredEvent(name: stepName, testedPage: page).save(failOnError: true)
    }

    protected Byte[] zip(String s) {
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
    void deleteResultsMarkedAsPendingAndRunning(Job job, String testId) {
        JobResult.findByJobAndTestIdAndHttpStatusCodeLessThan(job, testId, 200)?.delete(failOnError: true, flush: true)
    }
}
