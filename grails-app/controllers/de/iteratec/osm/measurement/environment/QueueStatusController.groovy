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

import groovy.time.TimeCategory
import groovy.util.slurpersupport.GPathResult
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel

class QueueStatusController {
	QueueAndJobStatusService queueAndJobStatusService
	PerformanceLoggingService performanceLoggingService

	private Map getServersWithQueues() {
		Map model = [:].withDefault { [] }
		// for all active WptServers...
		WebPageTestServer.findAllByActive(true).each { WebPageTestServer wptServer ->
			// Request getTesters.php and getLocations.php and iterate over returned locations:
			try {
				GPathResult agentsResponse
				performanceLoggingService.logExecutionTime(LogLevel.INFO, "getAgentsHttpResponse", IndentationDepth.ONE) {
					agentsResponse = queueAndJobStatusService.getAgentsHttpResponse(wptServer)
				}
				List<Map<Location, Object>> filteredLocations
				performanceLoggingService.logExecutionTime(LogLevel.INFO, "getFilteredLocations", IndentationDepth.ONE) {
					filteredLocations = queueAndJobStatusService.getFilteredLocations(wptServer)
				}
				filteredLocations.each{
					Location location = it.location
					Object locationTagInXml = it.tag
					
					List<JobResult> executingJobResults
					performanceLoggingService.logExecutionTime(LogLevel.INFO, "getExecutingJobResults", IndentationDepth.TWO) {
						executingJobResults = queueAndJobStatusService.getExecutingJobResults(location)
					}
					Map<Job, List<JobResult>> executingJobs
					performanceLoggingService.logExecutionTime(LogLevel.INFO, "aggregateJobs", IndentationDepth.TWO) {
						executingJobs = queueAndJobStatusService.aggregateJobs(executingJobResults)
					}
					
					use (TimeCategory) {
						Date now = new Date()
						Map nextHour
						performanceLoggingService.logExecutionTime(LogLevel.INFO, "getNumberOfJobsAndEventsDueToRunFromNowUntil", IndentationDepth.TWO) {
							nextHour = queueAndJobStatusService.getNumberOfJobsAndEventsDueToRunFromNowUntil(location, now + 1.hour)
						}
						performanceLoggingService.logExecutionTime(LogLevel.INFO, "build map", IndentationDepth.TWO) {
							Integer numberOfAgents
							performanceLoggingService.logExecutionTime(LogLevel.INFO, "getNumberOfAgents", IndentationDepth.THREE) {
								numberOfAgents = queueAndJobStatusService.getNumberOfAgents(locationTagInXml, agentsResponse)
							}
							Integer numberOfPendingJobs
							performanceLoggingService.logExecutionTime(LogLevel.INFO, "getNumberOfPendingJobsFromWptServer", IndentationDepth.THREE) {
								numberOfPendingJobs = queueAndJobStatusService.getNumberOfPendingJobsFromWptServer(locationTagInXml)
							}
							int eventResultCount
							performanceLoggingService.logExecutionTime(LogLevel.INFO, "getEventResultCountSince", IndentationDepth.THREE) {
								eventResultCount = queueAndJobStatusService.getEventResultCountBetween(location, now - 1.hour, now)
							}
							int finishedJobResultCountSince
							performanceLoggingService.logExecutionTime(LogLevel.INFO, "getFinishedJobResultCountSince", IndentationDepth.THREE) {
								finishedJobResultCountSince = queueAndJobStatusService.getFinishedJobResultCountSince(location, now - 1.hour)
							}
							int erroneousJobResultCountSince
							performanceLoggingService.logExecutionTime(LogLevel.INFO, "getErroneousJobResultCountSince", IndentationDepth.THREE) {
								erroneousJobResultCountSince = queueAndJobStatusService.getErroneousJobResultCountSince(location, now - 1.hour)
							}
							Map output = [
								id: location.uniqueIdentifierForServer,
								label: location.location,
								agents: numberOfAgents,
								jobs: numberOfPendingJobs,
								eventResultsLastHour: eventResultCount,
								jobResultsLastHour:  finishedJobResultCountSince,
								errorsLastHour:  erroneousJobResultCountSince,
								jobsNextHour: nextHour.jobs,
								eventsNextHour: nextHour.events,
								executingJobs: executingJobs,
								runningJobs: executingJobResults.findAll{ it.httpStatusCode == 101 }.size(),
								pendingJobs: executingJobResults.findAll{ it.httpStatusCode == 100 }.size()
							]
							model[(wptServer.label)] << output
						}
					}
				}
			} catch (Exception e) {
				model[(wptServer.label)] = e.getMessage()
			}
		}
		return model
	}
	
	def index() {
		redirect(action: 'list')
	}
	
	def list() {
		[servers: getServersWithQueues()]
	}
	
	// for Ajax requests return only the table, not the entire HTML page
	def refresh() {
		render(template: 'allQueues', model: [servers: getServersWithQueues()])
	}
}