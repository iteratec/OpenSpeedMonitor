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

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.PageService
import de.iteratec.osm.system.LocationHealthCheck
import de.iteratec.osm.system.LocationHealthCheckDaoService
import de.iteratec.osm.util.PerformanceLoggingService

class QueueStatusController {

    QueueAndJobStatusService queueAndJobStatusService
    PerformanceLoggingService performanceLoggingService
    LocationHealthCheckDaoService locationHealthCheckDaoService

    PageService pageService

    private Map getServersWithQueues() {

        Map queueDataByWptServer = [:].withDefault { [] }

        WebPageTestServer.findAllByActive(true).each { WebPageTestServer wptServer ->
            try {

                List<Location> activeLocationsOfServer = Location.findAllByActiveAndWptServer(true, wptServer)
                List<LocationHealthCheck> healthChecks = locationHealthCheckDaoService.getLatestHealthChecksFor(activeLocationsOfServer)

                activeLocationsOfServer.each{Location location ->

                    List<LocationHealthCheck> healthChecksOfLocation = healthChecks.findAll {it.location == location}

                    if (healthChecksOfLocation.size() == 1){

                        LocationHealthCheck healthCheck = healthChecksOfLocation[0]

                        Map queueDataOfThisLocation = getQueueData(location, healthCheck)
                        queueDataByWptServer[(wptServer.label)] << queueDataOfThisLocation

                    }

                }

            } catch (Exception e) {
                queueDataByWptServer[(wptServer.label)] = e.getMessage()
            }
        }
        return queueDataByWptServer
    }

    private Map getQueueData(Location location, LocationHealthCheck healthCheck) {

        List<JobResult> executingJobResults
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.INFO, "getExecutingJobResults", 2) {
            executingJobResults = queueAndJobStatusService.getExecutingJobResults(location)
        }
        Map<Job, List<JobResult>> executingJobs
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.INFO, "aggregateJobs", 2) {
            executingJobs = queueAndJobStatusService.aggregateJobs(executingJobResults)
        }

        Map queueDataOfThisLocation = [
            dateOfOsmQueueData: healthCheck.date,
            id                  : location.uniqueIdentifierForServer,
            label               : location.location,
            agents              : healthCheck.numberOfAgents,
            jobs                : healthCheck.numberOfPendingJobsInWpt,
            eventResultsLastHour: healthCheck.numberOfEventResultsLastHour,
            jobResultsLastHour  : healthCheck.numberOfJobResultsLastHour,
            errorsLastHour      : healthCheck.numberOfErrorsLastHour,
            jobsNextHour        : healthCheck.getNumberOfJobResultsNextHour(),
            eventsNextHour      : healthCheck.numberOfEventResultsNextHour,
            executingJobs       : executingJobs,
            pendingJobs         : executingJobResults.findAll { it.httpStatusCode == 100 }.size(),
            runningJobs         : executingJobResults.findAll { it.httpStatusCode == 101 }.size()
        ]
        return queueDataOfThisLocation
    }

    def index() {
        redirect(action: 'list')
    }

    def list() {
        [servers  : getServersWithQueues()]
    }

    // for Ajax requests return only the table, not the entire HTML page
    def refresh() {
        render(template: 'allQueues', model: [servers: getServersWithQueues()])
    }
}