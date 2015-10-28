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

import de.iteratec.osm.d3Data.ScheduleChartJob
import de.iteratec.osm.d3Data.ScheduleChartData
import de.iteratec.osm.measurement.environment.wptserverproxy.HttpRequestService
import de.iteratec.osm.measurement.schedule.CronExpressionFormatter
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.measurement.script.ScriptParser
import de.iteratec.osm.result.*
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.I18nService
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.ContentType
import org.joda.time.DateTime
import org.quartz.CronExpression

/**
 * QueueAndJobStatusService returns various figures regarding Jobs, Queues and EventResults.
 *
 * @uathor dri
 */
class QueueAndJobStatusService {
    static transactional = true

    HttpRequestService httpRequestService
    MeasuredValueTagService measuredValueTagService
    EventResultDaoService eventResultDaoService
    I18nService i18nService
    JobService jobService
    PageService pageService

    /**
     * Retrieves only those locations for the given WebPageTestServer from the database which are also returned
     * when querying getLocations.php
     *
     * @return A list of maps. Each map's key is the retrieved Location and its value is the <location> tag in the XML
     *  	response returned by getLocations.php
     */
    List<Map<Location, Object>> getFilteredLocations(WebPageTestServer wptServer) {
        GPathResult locationsResponse = httpRequestService.getWptServerHttpGetResponseAsGPathResult(wptServer, 'getLocations.php', [:], ContentType.TEXT, [Accept: 'application/xml'])
        List locations = []
        if (locationsResponse != null) {
            locationsResponse.data.location.each { locationTagInXml ->
                Location location = Location.findByWptServerAndUniqueIdentifierForServer(wptServer, locationTagInXml.id.text())
                if (location)
                    locations << [location: location, tag: locationTagInXml]
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
        return httpRequestService.getWptServerHttpGetResponseAsGPathResult(wptServer, 'getTesters.php', [f: 'xml'], ContentType.TEXT, [Accept: 'application/xml'])
    }

    /**
     * Get number of EventResults recorded for the specified location from sinceWhen until now.
     */
    int getEventResultCountBetween(Location location, Date from, Date to) {
        MvQueryParams queryParams = new MvQueryParams()
        queryParams.locationIds.add(location.id)
        return eventResultDaoService.getMedianEventResultsBy(from, to, [CachedView.UNCACHED, CachedView.CACHED] as Set, queryParams).size()
    }

    /**
     * Get number of successfully finished Jobs (i.e. JobResults with httpStatusCode 200)
     * from sinceWhen until now for the specified location
     * @return
     */
    int getFinishedJobResultCountSince(Location location, Date sinceWhen) {
        def query = JobResult.where {
            date >= sinceWhen && httpStatusCode == 200 && job.location == location
        }
        return query.list().size()
    }

    /**
     * Get number of successfully finished Jobs (i.e. JobResults with httpStatusCode 200)
     * from sinceWhen until now for the specified location
     * @return
     */
    int getErroneousJobResultCountSince(Location location, Date sinceWhen) {
        def query = JobResult.where {
            date >= sinceWhen && httpStatusCode >= 400 && job.location == location
        }
        return query.list().size()
    }

    /**
     * Get all JobResults representing currently running tests (i.e. pending (100) or running (101))
     * for the specified location.
     */
    List<JobResult> getExecutingJobResults(Location location) {
        def query = JobResult.where {
            (httpStatusCode == 100 || httpStatusCode == 101) && job.location == location
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
        Job.findAllByActiveAndLocation(true, location).each {
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
     * Retrieve number of agents from getTester.php which match a location returned by getLocations.php
     */
    Integer getNumberOfAgents(Object locationTag, Object agentsResponse) {
        Object agentLocation = agentsResponse.data.location.find { agentLocationTag ->
            agentLocationTag.id.text() == locationTag.location.text()
        }
        return agentLocation?.testers.size()
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
    Map<Long, Object> getRunningAndRecentlyFinishedJobs(Date successfulSinceWhen, Date errorSinceWhen, Date runningSinceWhen) {
        Map<Long, Object> jobResults = [:].withDefault { [] }
        Date oldestDate = [successfulSinceWhen, errorSinceWhen, runningSinceWhen].sort().first()
        JobResult.findAllByDateGreaterThanEquals(oldestDate, [sort: 'date']).each { JobResult result ->
            jobResults[result.job.id] << [
                    testId    : result.testId,
                    status    : result.httpStatusCode,
                    date      : result.date,
                    terminated: result.httpStatusCode >= 200,
                    message   : result.getSatusCodeMessage() + (result.httpStatusCode >= 400 && result.wptStatus ? ': ' + result.wptStatus : ''),
                    wptStatus : result.wptStatus,
                    testUrl   : (result.wptServerBaseurl.endsWith('/') ? result.wptServerBaseurl : "${result.wptServerBaseurl}/") + "result/${result.testId}"]
        }

        // keep only the newest erroneous (httpStatusCode >= 400) result and delete all erroneous results
        // succeeded by successful/currently running results
        Map filteredJobResults = jobResults.each {
            it.value = it.value
                    .findAll { result -> result['status'] < 400 || result == it.value.last() }
                    .findAll { result -> (result['date'] >= runningSinceWhen && (result['status'] == 100 || result['status'] == 101)) || (result['date'] >= successfulSinceWhen && result['status'] == 200) || (result['date'] >= errorSinceWhen && result['status'] >= 400) }
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
            List<ScheduleChartData> locationChartData = new ArrayList<>()

            List locations = getFilteredLocations(server)

            Map<String, ScheduleChartData> scheduleChartLocations = new HashMap<>()

            // iterate over locations
            locations.each { loc ->
                Location currentLocation = loc.location

                // filter only active locations
                if (currentLocation.active == true) {
                    if (!scheduleChartLocations.containsKey(currentLocation.label)) {
                        scheduleChartLocations.put(currentLocation.label, new ScheduleChartData(name: currentLocation.label, discountedJobsLabel: discountedJobsLabel, agentCount: getAgentCount(currentLocation)))
                    }

                    ScheduleChartData scheduleChartLocation = scheduleChartLocations.get(currentLocation.label)
                    def jobs = Job.findAllByLocation(currentLocation)

                    // iterate over jobs
                    for (Job j : jobs) {
                        ScriptParser parser = new ScriptParser(pageService, j.script.navigationScript);
                        def minutes = parser.calculateDurationInMinutes()
                        // Add jobs which are going to run in given interval to the list
                        // otherwise the job is added to the list of discounted jobs
                        ScheduleChartJob scheduleChartJob = new ScheduleChartJob(executionDates: jobService.getExecutionDatesInInterval(j, start, end), name: j.label, description: "(" + currentLocation.browser.name + ")", durationInMinutes: minutes)
                        if (scheduleChartJob.executionDates && !scheduleChartJob.executionDates.isEmpty()) {
                            scheduleChartLocation.addJob(scheduleChartJob)
                        } else {
                            scheduleChartLocation.addDiscountedJob(j.label)
                        }
                    }

                }
            }

            // add locations to schedule chart
            scheduleChartLocations.each { k, v ->
                locationChartData.add(v)
            }

           result.put(server, locationChartData)
        }

        return result
    }

    /**
     * gets the count of agents for a location
     * @param location
     * @return
     */
    private int getAgentCount(Location location) {
        4
    }
}