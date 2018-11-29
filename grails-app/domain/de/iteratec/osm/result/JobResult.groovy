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
    Date executionDate
    /** timestamp of completion*/
    Date date
    /** wpt-test-id */
    String testId
    /** tester from result xml */
    String testAgent

    /** Status of the wpt result **/
    WptStatus wptStatus
    /** Status of the osm JobResult **/
    JobResultStatus jobResultStatus
    @BindUsing({ obj, source -> source['description'] })
    String description

    String wptVersion

    //from Job
    String jobConfigLabel
    Integer jobConfigRuns
    boolean firstViewOnly
    Integer expectedSteps

    String wptServerLabel
    String wptServerBaseurl
    String locationLabel
    String locationLocation
    String locationUniqueIdentifierForServer
    String locationBrowser

    String jobGroupName

    static constraints = {
        testId(nullable: false)
        testAgent(nullable: true)
        executionDate(nullable: true)
        date(nullable: false)
        wptStatus(nullable: false)
        jobResultStatus(nullable: false)
        job(nullable: false)
        description(widget: 'textarea')

        //from Job
        jobConfigLabel(maxSize: 255, blank: false)
        jobConfigRuns(blank: false)
        firstViewOnly(nullable: true)
        expectedSteps(nullable: false)

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
        description(type: 'text')
        date(index: 'date_idx')
        testId(index: 'testId_and_jobConfigLabel_idx')
        jobConfigLabel(index: 'testId_and_jobConfigLabel_idx')
        autowire true
    }

    static transients = ['jobStatisticService']

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
     * If {@link jobResultStatus} will change {@link JobStatistic}s of {@link Job} has to be updated.
     */
    def beforeUpdate() {
        try {
            boolean noTest = Environment.getCurrent() != Environment.TEST
            if (noTest && isDirty('jobResultStatus')) {
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
