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

import de.iteratec.osm.ConfigService
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
 * Persists locations and results. Observer of JobResultPersisterService.
 */
class EventResultPersisterService implements iResultListener {

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
    ConfigService configService

    /**
     * Persisting fetched {@link EventResult}s. If associated JobResults and/or Jobs and/or Locations don't exist they will be persisted, too.
     * Dependent {@link de.iteratec.osm.report.chart.CsiAggregation}s will be created/marked/calculated.
     * Persisted {@link EventResult} will be reported to graphite if configured respectively.
     * <br><b>Note:</b> Persistance of the {@link EventResult}s of one test step (i.e. for one {@link MeasuredEvent}) is wrapped into a transaction. So ANY other downstream operations may not rollback the persistance
     * of the {@link EventResult}s
     */
    @Override
    String getListenerName() {
        return "EventResultPersisterService"
    }

    @Override
    public void listenToResult(
            WptResultXml resultXml,
            WebPageTestServer wptserverOfResult,
            long jobId) {

        try {
            checkJobAndLocation(resultXml, wptserverOfResult, jobId)
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
    void checkJobAndLocation(WptResultXml resultXml, WebPageTestServer wptserverOfResult, long jobId) {
        Job job
        performanceLoggingService.logExecutionTime(DEBUG, "get or persist Job ${resultXml.getLabel()} while processing test ${resultXml.getTestId()}...", 4) {
            job = jobDaoService.getJob(jobId)
        }
        performanceLoggingService.logExecutionTime(DEBUG, "updateLocationIfNeededAndPossible while processing test ${resultXml.getTestId()}...", 4) {
            updateLocationIfNeededAndPossible(job, resultXml, wptserverOfResult);
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

    void persistResultsForAllTeststeps(WptResultXml resultXml, long jobId) {
        Integer testStepCount = resultXml.getTestStepCount()
        log.debug("starting persistance of ${testStepCount} event results for test steps")

        for (int zeroBasedTeststepIndex = 0; zeroBasedTeststepIndex < testStepCount; zeroBasedTeststepIndex++) {
            try {
                if (!persistResultsOfOneTeststep(zeroBasedTeststepIndex, resultXml, jobId)) {
                    break
                }
            } catch (Exception e) {
                log.error("an error occurred while persisting EventResults of testId ${resultXml.getTestId()} of teststep ${zeroBasedTeststepIndex}", e)
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected boolean persistResultsOfOneTeststep(Integer testStepZeroBasedIndex, WptResultXml resultXml, long jobId) throws OsmResultPersistanceException {
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
        if (!measuredEventName) {
            log.error("there is no testStep ${testStepZeroBasedIndex + 1} for testId ${resultXml.getTestId()}")
            return false
        }
        log.debug("getting MeasuredEvent from eventname '${measuredEventName}' ...")
        MeasuredEvent event = getMeasuredEvent(measuredEventName);

        log.debug("persisting result for step=${event}")
        int runCount = resultXml.getRunCount()
        log.debug("runCount=${runCount}")

        boolean allResultsValid = true
        for (int runNumber = 0; runNumber < resultXml.getRunCount(); runNumber++) {
            for (CachedView cached : [CachedView.UNCACHED, CachedView.CACHED]) {
                if (resultXml.resultExistForRunAndView(runNumber, cached) &&
                        (job.persistNonMedianResults || resultXml.isMedian(runNumber, cached, testStepZeroBasedIndex))) {
                    if (!persistSingleResult(resultXml, runNumber, cached, testStepZeroBasedIndex, jobResult, event)) {
                        allResultsValid = false
                    }
                }
            }
        }

        return allResultsValid
    }

    /**
     * Persists a single Run result
     * @return True if the step is valid, false otherwise
     */
    private boolean persistSingleResult(
            WptResultXml resultXml, Integer runZeroBasedIndex, CachedView cachedView, Integer testStepZeroBasedIndex, JobResult jobRun, MeasuredEvent event) {

        GPathResult viewResultsNodeOfThisRun = resultXml.getResultsContainingNode(runZeroBasedIndex, cachedView, testStepZeroBasedIndex)
        if (!resultXml.isValidTestStep(viewResultsNodeOfThisRun)) {
            return false
        }
        persistResult(
                jobRun,
                event,
                cachedView,
                runZeroBasedIndex + 1,
                resultXml.isMedian(runZeroBasedIndex, cachedView, testStepZeroBasedIndex),
                viewResultsNodeOfThisRun,
                testStepZeroBasedIndex + 1
        )
        return true
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
            JobResult jobRun, MeasuredEvent event, CachedView view, Integer run, Boolean median, GPathResult viewTag, testStepOneBasedIndex) {

        EventResult result = jobRun.findEventResult(event, view, run) ?: new EventResult()
        return saveResult(result, jobRun, event, view, run, median, viewTag, testStepOneBasedIndex)

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
                                     GPathResult viewTag, Integer testStepOneBasedIndex) {

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
        result.testAgent = jobRun.testAgent
        setConnectivity(result, jobRun)
        result.oneBasedStepIndexInJourney = testStepOneBasedIndex
        setAllUserTimings(viewTag, result)
        setBreakdownMeasurands(viewTag, result)

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

    private void setAllMeasurands(GPathResult inputValues, EventResult result) {
        Measurand.values().each {
            if (it.getTagInResultXml()) {
                setPropertyWithinEventResult(it, inputValues, result)
            }
        }
        setCustomerSatisfaction(result)
    }

    private void setPropertyWithinEventResult(Measurand measurand, GPathResult inputValues, EventResult result) {
        if (tagExists(inputValues, measurand.getTagInResultXml())) {
            result.setProperty(measurand.getEventResultField(), inputValues.getProperty(measurand.getTagInResultXml()).toInteger())
        }
    }

    private boolean tagExists(GPathResult viewTag, String tag) {
        return !viewTag.getProperty(tag).isEmpty() && viewTag.getProperty(tag).toString().isBigInteger() && viewTag.getProperty(tag).toBigInteger() > 0
    }

    private void setBreakdownMeasurands(GPathResult viewtag, EventResult result) {
        if(tagExists(viewtag.parent(), "breakdown")) {
            viewtag.parent().breakdown.children().forEach { measurand ->
                Measurand thisMeasurand = Measurand.byResultXmlTag(measurand.name())
                if(thisMeasurand) {
                    result.setProperty(thisMeasurand.getEventResultField(), measurand.toInteger())
                }
            }
        }
    }

    private void setAllUserTimings(GPathResult viewTag, EventResult result){
        List<UserTiming> allUserTimings = []
        allUserTimings.addAll(createAllMarks(viewTag, result))
        allUserTimings.addAll(createAllMeasures(viewTag, result))
        allUserTimings.addAll(createAllHeroTimings(viewTag, result))
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

    private List<UserTiming> createAllHeroTimings(GPathResult viewTag, EventResult result){
        return viewTag.heroElementTimes.children().collect {
            if(it && it.name() && it.toString().isDouble()) {
                return new UserTiming(
                        name: it.name(),
                        startTime: it.toDouble(),
                        type: UserTimingType.HERO_MARK,
                        eventResult: result
                )
            }
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

}
