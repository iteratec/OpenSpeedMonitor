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
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobStatisticService
import grails.databinding.BindUsing
import grails.gorm.annotation.Entity
import grails.util.Environment

/**
 * <p>
 * The result of one execution of a {@linkplain Job jobs} {@linkplain Script
 * script}.
 * </p>
 *
 * <p>
 * An instance represents a single execution of a potently multi-time scheduled
 * job. One result may have multiple {@linkplain EventResult event results}. 
 * </p>
 *
 * @author nkuhn
 * @author mze
 *
 * @see Job
 * @see EventResult
 */
@Entity
class JobResult {

    JobStatisticService jobStatisticService

    Long id

    Job job
    static belongsTo = [job: Job]

    /** timestamp of execution */
    Date date
    /** wpt-test-id */
    String testId
    /** tester from result xml */
    String testAgent

    /** An optional String containing error messages from WPT server **/
    String wptStatus
    /** status code returned by WPT server (such as 200, 101, ...) **/
    Integer httpStatusCode
    @BindUsing({ obj, source -> source['description'] })
    String description

    String wptVersion

    //from Job
    String jobConfigLabel
    Integer jobConfigRuns

    boolean firstViewOnly
    boolean captureVideo
    boolean downloadResultXml
    boolean downloadDetails
    Integer frequencyInMin
    Integer maxDownloadAttempts

    //from Script
    String scriptUrl
    String scriptNavigationscript
    boolean provideAuthenticateInformation
    String authUsername
    String authPassword
    boolean multistep
    boolean applyValidateRule
    String validationRequest
    Integer validationType
    Integer validationMarkas
    Integer validationMarkaselse

    //from WptServer / WptLoction
    String wptServerLabel
    String wptServerBaseurl
    String locationLabel
    String locationLocation
    String locationUniqueIdentifierForServer
    String locationBrowser

    //from Job
    /**
     * This is a copy of {@link JobGroup#name}. It doesn't get updated.
     */
    String jobGroupName

    static constraints = {
        testId(nullable: false)
        testAgent(nullable: true)
        date(nullable: false)
        wptStatus(nullable: true)
        httpStatusCode(nullable: false)
        job(nullable: false)
        description(widget: 'textarea')

        //from Job
        jobConfigLabel(maxSize: 255, blank: false)
        jobConfigRuns(blank: false)
        firstViewOnly(nullable: true)
        captureVideo(nullable: true)
        downloadResultXml(nullable: true)
        downloadDetails(nullable: true)
        frequencyInMin(nullable: true)
        maxDownloadAttempts(nullable: true)

        //from Script
        scriptUrl(nullable: true, url: true)
        scriptNavigationscript(nullable: true, widget: 'textarea')
        provideAuthenticateInformation(nullable: true)
        authUsername(nullable: true)
        authPassword(nullable: true)
        multistep(nullable: true)
        applyValidateRule(nullable: true)
        validationRequest(nullable: true)
        validationType(nullable: true)
        validationMarkas(nullable: true)
        validationMarkaselse(nullable: true)

        //from WptServer / WptLoction
        wptServerLabel(nullable: true)
        wptServerBaseurl(nullable: true)
        locationLabel(nullable: true)
        locationLocation(nullable: true)
        locationUniqueIdentifierForServer(nullable: true)
        locationBrowser(nullable: true)
        jobGroupName(nullable: false)

        wptVersion(nullable: true)
    }

    static mapping = {
        scriptUrl(type: 'text')
        scriptNavigationscript(type: 'text')
        date(index: 'date_idx')
        testId(index: 'testId_and_jobConfigLabel_idx')
        jobConfigLabel(index: 'testId_and_jobConfigLabel_idx')
        wptStatus(type: 'text')
    }

    static transients = ['eventResults', 'jobStatisticService']

    String toString() {
        return (testId ?: id) ?: super.toString()
    }

    /**
     * Returns a Event result identified by it's MeasuredEvent, CachedView and Run.
     * Every EventResult corresponds to it's JobResult, the MeasuredEvent, CachedView and Run.
     *
     * <p>
     * This must be unique by definition. The Agents grants that a MeasuredEvent is unique for a Job.
     * </p>
     * @param event MeasuredEvent
     * @param view CachedView
     * @param run Integer
     * @return EventResult
     *
     */
     EventResult findEventResult(MeasuredEvent event, CachedView view, Integer run) {
        Collection<EventResult> results = this.getEventResults()
        return results.find { it.measuredEvent == event && it.cachedView == view && it.numberOfWptRun == run }
    }
    /**
     * Returns a list of Event results connected to this job result
     *
     * @return list < EventResult >
     *
     */
     List<EventResult> getEventResults() {
        List results = EventResult.createCriteria().list {
            jobResult {
                eq("id", this.id)
            }
        }

        return results
    }
    /**
     * Returns the median {@link EventResult} of the uncached view for one {@link MeasuredEvent}.
     *
     * <p>
     * This must be unique by definition. The Agents grants that a MeasuredEvent is unique per Job.
     * </p>
     * @param event MeasuredEvent
     * @return null only if event isn't measured within this JobResult
     *
     */
     EventResult findMedianUncachedEventResult(MeasuredEvent event) {
        Collection<EventResult> results = this.getEventResults()
        return results.find {
            it.measuredEvent == event && it.cachedView == CachedView.UNCACHED && it.medianValue == true
        }
    }

    /**
     * <p>
     * Returns the tests details {@link URL} if existing. This is an URL to a
     * page on the server, where this result has been created. The referenced
     * page may contains more detail data like a waterfall-view of tests page
     * loads and further more.
     * </p>
     *
     * @return An URL if this test is valid result is complete, the test has
     *         been executed successfully, <code>null</code> else.
     * @since IT-78
     */
     URL tryToGetTestsDetailsURL() {
        URL result = null

        String wptServerBaseurl = this.wptServerBaseurl
        String testId = this.testId

        if (wptServerBaseurl &&
                testId &&
                !testId.isEmpty() &&
                !testId.equals('-1')) {
            result = new URL(wptServerBaseurl + (wptServerBaseurl.endsWith('/') ? '' : '/') + 'result/' + testId)
        }

        return result
    }

    /**
     * Returns a status message matching the integer value stored in statusCode
     * such as 'Pending' for statusCode 100 and so forth
     */
     String getStatusCodeMessage() {
        def state = [0: 'Failure', 100: 'Pending', 101: 'Running', 200: 'Finished', 400: 'Error', 404: 'Not found', 504: 'Timeout']
        def str = state[httpStatusCode]
        return str ?: 'Unknown'
    }

    /**
     * If {@link httpStatusCode} will change {@link JobStatistic}s of {@link Job} has to be updated.
     */
    def beforeUpdate() {
        try {
            boolean noTest = Environment.getCurrent() != Environment.TEST
            if (noTest && isDirty('httpStatusCode')) {
                jobStatisticService.updateStatsFor(job)
            }
        } catch (Exception e) {
            log.info("An exception occurred trying to update statistics of job '${job.label}': ${e.message}")
        }
        return true
    }
    /**
     * {@link JobStatistic}s of {@link Job} has to be updated.
     */
    def afterInsert() {
        boolean noTest = Environment.getCurrent() != Environment.TEST
        if (noTest) {
            jobStatisticService.updateStatsFor(job)
        }
    }

}
