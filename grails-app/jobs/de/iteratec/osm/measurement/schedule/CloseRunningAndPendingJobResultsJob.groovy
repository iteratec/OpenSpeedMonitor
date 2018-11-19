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

package de.iteratec.osm.measurement.schedule
/**
 * This class doesn't represent one static quartz job like the other job classes under grails-app/jobs.
 * It provides the entrypoint for all the dynamically scheduled and unscheduled quartz triggers (see {@link JobSchedulingService}).
 */
class CloseRunningAndPendingJobResultsJob {

    JobRunService jobRunService

    static triggers = {
        /**
         * Each Day at 1:30 am.
         */
        cron(name: 'CloseRunningAndPendingJobResults', cronExpression: '0 30 1 ? * *')
    }

    /**
     * Entrypoint for all the dynamically scheduled and unscheduled quartz triggers
     */
    def execute() {
        jobRunService.closeRunningAndWaitingJobRuns()
    }
}
