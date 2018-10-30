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

package de.iteratec.osm.measurement.environment

import de.iteratec.osm.d3Data.ScheduleChartData
import de.iteratec.osm.d3Data.ScheduleChartJob
import de.iteratec.osm.measurement.environment.wptserver.HttpRequestService
import de.iteratec.osm.measurement.schedule.CronExpressionFormatter
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.measurement.script.ScriptParser
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultStatus
import de.iteratec.osm.result.PageService
import de.iteratec.osm.util.I18nService
import grails.gorm.transactions.Transactional
import groovy.util.slurpersupport.GPathResult
import org.joda.time.DateTime
import org.quartz.CronExpression

/**
 * QueueAndJobStatusService returns various figures regarding Jobs, Queues and EventResults.
 *
 * @uathor dri
 */
@Transactional
class QueueAndJobStatusService {

    /**
     * If a wpt agents last check time exceeds this value it is assumed not to be an active agent.
     */
    public static final int AGENTS_CHECKTIME_THRESHOLD_IN_MINUTES = 30

    HttpRequestService httpRequestService
    I18nService i18nService
    JobService jobService
    JobDaoService jobDaoService
    PageService pageService
    BrowserService browserService

    /**
     * Retrieves only those locations for the given WebPageTestServer from the database which are also returned
     * when querying getLocations.php and who are active in osm database.
     *
     * @return A list of {@link LocationWithXmlNode}.
     */
    List<LocationWithXmlNode> getActiveLocations(WebPageTestServer wptServer) {
        GPathResult locationsResponse = httpRequestService.getWptServerHttpGetResponse(
            wptServer,
            'getLocations.php',
            [:],
            'text/plain',
            [Accept: 'application/xml']
        )
        List locations = []
        if (locationsResponse != null) {
            locationsResponse.data.location.each { locationTagInXml ->
                List<String> browserNames = locationTagInXml.Browsers.size() != 0 ? locationTagInXml.Browsers.toString().split(",") : [locationTagInXml.Browser.toString()]
                browserNames.each { browserName ->
                    String uniqueIdentfierForServer = locationTagInXml.id.toString().endsWith(":${browserName}") ?: locationTagInXml.id.toString() + ":${browserName}"
                    Location location = Location.findByWptServerAndUniqueIdentifierForServer(wptServer, uniqueIdentfierForServer)
                    if (location && location.active)
                        locations << new LocationWithXmlNode(
                                location: location,
                                locationXmlNode: locationTagInXml
                        )
                }
            }
        }
        return locations
    }

    /**
     * Calls getTesters.php of the specified wptServer and requests result in XML format
     * @param wptServer
     * @return The root node of the returned XML
     */
    GPathResult getAgentsHttpResponse(WebPageTestServer wptServer) {
        return httpRequestService.getWptServerHttpGetResponse(wptServer, 'getTesters.php', [f: 'xml'], 'text/plain', [Accept: 'application/xml'])
    }

    /**
     * Get number of EventResults recorded for the specified location from sinceWhen until now.
     */
    int getEventResultCountBetween(Location location, Date from, Date to) {
        return EventResult.countByLocationAndDateCreatedBetween(location, from, to)
    }

    /**
     * Get number of successfully finished Jobs (i.e. JobResults with jobResultStatus 200)
     * from sinceWhen until now for the specified location
     * @return
     */
    int getFinishedJobResultCountSince(Location location, Date sinceWhen) {
        def query = JobResult.where {
            date >= sinceWhen && jobResultStatus == JobResultStatus.SUCCESS && job.location == location
        }
        return query.list().size()
    }

    /**
     * Get number of unsuccessfully finished Jobs (i.e. JobResults with jobResultStatus 500 or above)
     * from sinceWhen until now for the specified location
     * @return
     */
    int getErroneousJobResultCountSince(Location location, Date sinceWhen) {
        def query = JobResult.where {
            date >= sinceWhen && jobResultStatus >= JobResultStatus.LAUNCH_ERROR && job.location == location
        }
        return query.list().size()
    }

    /**
     * Get all JobResults representing currently running tests (i.e. pending (100) or running (101))
     * for the specified location.
     */
    List<JobResult> getExecutingJobResults(Location location) {
        def query = JobResult.where {
            (jobResultStatus == JobResultStatus.WAITING || jobResultStatus == JobResultStatus.RUNNING) && job.location == location
        }
        return query.list(sort: 'date', order: 'desc')
    }

    /**
     * Aggregate the given list of JobResults by Job
     *
     * @return A map mapping each Job to a list of its JobResults
     */
    Map<Job, List<JobResult>> aggregateJobs(List<JobResult> results) {
        Map<Job, List<JobResult>> jobs = [:].withDefault { [] }
        results.each {
            jobs[it.job] << it
        }
        return jobs
    }

    /**
     * Get number of jobs and events which will be launched between now and untilWhen
     * by being active and their executionSchedule becoming valid between now and untilWhen.
     * @return A map of two integers 'jobs' and 'events'
     */
    Map getNumberOfJobsAndEventsDueToRunFromNowUntil(Location location, Date untilWhen) {
        int totalJobRunsDue = 0
        int totalEventsCountDue = 0
        Date now = new Date()
        jobDaoService.getJobs(true, location).each {
            CronExpression expr = new CronExpression(it.executionSchedule)
            Date date = now
            int jobRunsDue = 0
            while ((date = CronExpressionFormatter.getNextValidTimeAfter(expr, date)) != null && date <= untilWhen) {
                jobRunsDue++
            }
            totalEventsCountDue += jobRunsDue * it.script.measuredEventsCount * it.runs * (it.firstViewOnly ? 1 : 2)
            totalJobRunsDue += jobRunsDue
        }
        return [jobs: totalJobRunsDue, events: totalEventsCountDue]
    }

    /**
     * Retrieve number of active agents from getTester.php which match a location returned by getLocations.php
     */
    Integer getNumberOfAgents(GPathResult locationTagFromGetLocations, GPathResult getTestersResponse) {
        GPathResult locationTagFromGetTesters = getTestersResponse.data.location.find { locationTagFromGetTesters ->
            locationTagFromGetTesters.id.text() == locationTagFromGetLocations.location.text()
        }
        if (locationTagFromGetTesters == null){
            String message = "Location '${locationTagFromGetLocations.location.text()}' couldn't be found in getTesters response."
            log.error(message)
            throw new IllegalArgumentException(message)
        }
        return getNumberOfActiveLocations(locationTagFromGetTesters)
    }

    Integer getNumberOfPendingJobsFromWptServer(Object locationTag) {
        return locationTag.PendingTests.Total.toInteger()
    }

    /**
     * Get currently executing Jobs and recently finished Jobs.
     * @param successfulSinceWhen Jobs with result 200 since this date.
     * @param errorSinceWhen Jobs with result >= 400 since this date.
     * @param runningSinceWhen Jobs with result 100 or 101 since this date.
     * @return A map mapping the ID of each Job to a list of maps which contain
     * 	 testId, status, date, terminated, message and wptStatus.
     */
    Map<Long, List> getRunningAndRecentlyFinishedJobs(Date successfulSinceWhen, Date errorSinceWhen, Date runningSinceWhen) {
        Map<Long, List> jobResults = [:].withDefault { [] }
        Date oldestDate = [successfulSinceWhen, errorSinceWhen, runningSinceWhen].sort().first()
        JobResult.findAllByDateGreaterThanEquals(oldestDate, [sort: 'date']).each { JobResult result ->
            jobResults[result.job.id] << [
                    testId    : result.testId,
                    status    : result.jobResultStatus,
                    date      : result.date,
                    terminated: result.jobResultStatus.isTerminated(),
                    message   : 'JobStatus: ' + result.jobResultStatus + ' wptStatus:' + result.wptStatus,
                    wptStatus : result.wptStatus,
                    testUrl   : (result.wptServerBaseurl.endsWith('/') ? result.wptServerBaseurl : "${result.wptServerBaseurl}/") + "result/${result.testId}"]
        }

        // keep only the newest erroneous (JobResultStatus >= 400) result and delete all erroneous results
        // succeeded by successful/currently running results
        Map filteredJobResults = jobResults.each {
            it.value = it.value
                    .findAll { result -> !result['status'].isFailed() || result == it.value.last() }
                    .findAll { result ->
                (result['date'] >= runningSinceWhen && (!result['status'].isTerminated()) ||
                        (result['date'] >= successfulSinceWhen && result['status'].isSuccess())) ||
                        (result['date'] >= errorSinceWhen && result['status'].isFailed())
            }
        }
        return filteredJobResults
    }

    /**
     * Creates a schedule chart data object for each active server.
     *
     * @param start starting point of specified interval
     * @param end ending point of specified interval
     * @return List of schedule chart data objects for schedule chart
     */
    Map<WebPageTestServer, List<ScheduleChartData>> createChartData(DateTime start, DateTime end) {

        Map<WebPageTestServer, List<ScheduleChartData>> result = new HashMap<>()

        def wptServer = WebPageTestServer.findAllByActive(true)

        String discountedJobsLabel = i18nService.msg("de.iteratec.osm.d3Data.ScheduleChart.discardedJobsLabel", "Discarded Jobs")

        // Iterate over active servers
        for (WebPageTestServer server : wptServer) {
            List<ScheduleChartData> serverChartData = new ArrayList<>()

            Map<String, Integer> locationsAndTesterCount = getActiveLocationsAndTesterCount(server)

            locationsAndTesterCount.each { locString, agentCount ->
                ScheduleChartData locationChartData = new ScheduleChartData(
                    name: locString,
                    discountedJobsLabel: discountedJobsLabel,
                    agentCount: agentCount
                )

                // collect all Jobs
                List<Job> jobs = []
                Location.findAllByWptServerAndLocation(server, locString).each { l ->
                    jobs.addAll(jobDaoService.getJobs(l))
                }

                // iterate over jobs
                jobs.each { job ->
                    if (job.active) {
                        ScriptParser parser = new ScriptParser(pageService, job.script.navigationScript, job.script.label);
                        int seconds = parser.calculateDurationInSeconds()

                        // Add jobs which are going to run in given interval to the list
                        // otherwise the job is added to the list of discounted jobs
                        ScheduleChartJob scheduleChartJob = new ScheduleChartJob(
                            executionDates: jobService.getExecutionDatesInInterval(job, start, end),
                            name: job.label,
                            description: "(" + job.location.browser.name + ")",
                            durationInSeconds: seconds,
                            linkId: job.id
                        )
                        if (scheduleChartJob.executionDates && !scheduleChartJob.executionDates.isEmpty()) {
                            locationChartData.addJob(scheduleChartJob)
                        } else {
                            locationChartData.addDiscountedJob(job.label)
                        }
                    }
                }

                serverChartData.add(locationChartData)
            }

            result.put(server, serverChartData)
        }

        return result
    }

    /**
     * Gets all locations and the count of testers for it from 'getTesters.php'
     * @param wptServer the server
     * @return map , mapping a location to its tester count
     */
    private Map<String, Integer> getActiveLocationsAndTesterCount(WebPageTestServer wptServer) {
        Map<String, Integer> result = new HashMap<>();

        GPathResult getTestersResponse = httpRequestService.getWptServerHttpGetResponse(wptServer, 'getTesters.php', [:], 'text/plain', [Accept: 'application/xml'])

        getTestersResponse.data.location.each { GPathResult locationTagInXml ->

            String currentLocation = locationTagInXml.id
            if (currentLocation) {
                result.put(currentLocation, getNumberOfActiveLocations(locationTagInXml))
            }

        }

        return result;
    }

    /**
     * Gets number of active locations in given locationTagInGetTestersXml.
     * A location is defined as active if its elapsed time in minutes is smaller
     * {@link #AGENTS_CHECKTIME_THRESHOLD_IN_MINUTES}.
     * @param locationTagInGetTestersXml
     * @return
     */
    static Integer getNumberOfActiveLocations(GPathResult locationTagInGetTestersXml){
        Integer numberOfActiveLocations = 0
        if(locationTagInGetTestersXml?.testers?.tester?.size() > 0){
            numberOfActiveLocations = locationTagInGetTestersXml.testers.tester.find{ tester ->
                Integer.parseInt(tester.elapsed.toString()) < AGENTS_CHECKTIME_THRESHOLD_IN_MINUTES
            }.size()
        }
        return numberOfActiveLocations
    }

}
