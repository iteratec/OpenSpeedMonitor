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

import grails.transaction.Transactional
import org.joda.time.DateTime
import org.quartz.CronExpression

import grails.web.mapping.LinkGenerator

class JobService {

    JobDaoService jobDaoService
    LinkGenerator grailsLinkGenerator

    /**
     * <p>
     * Returns the group of a job, if it was assigned to a group and the group
     * has a CsiConfiguration.
     * </p>
     *
     * @param job The Job, not <code>null</code>
     * @return null if the job is not assigned to a group or the assigned
     *         group is not a CSI-group, else the group
     */
    JobGroup getCsiJobGroupOf(Job job) {
        JobGroup group = job.getJobGroup()
        /*-
         * Use of defensive programming intended here:
         *
         * A Job could not be assigned to a group if the Job is unknown before
         * and was never manually(!) assigned.
         */
        if (group != null && group.hasCsiConfiguration()) {
            return group
        } else {
            return null
        }
    }


    List<Job> getAllCsiJobs() {
        List<Job> csiJobs = []
        JobGroup.findAllByCsiConfigurationIsNotNull().each {
            csiJobs.addAll(jobDaoService.getJobs(it))
        }
        return csiJobs
    }

    /**
     * Returns a list of all Jobs which are tagged along with their tags.
     * This was implemented using HQL since iterating over all jobs returned by Job.list()
     * and querying job.tags on each of time usually takes at least 50 ms per job
     * resulting in unacceptably long load times.
     *
     * @return A list of Maps. Each Map contains two items: "jobId" (long) and
     *   "tag" (String). So if one Job has multiple tags, multiple Maps
     *   with the same jobId but different tags will be returned.
     */
    List<Map> listJobsWithTags() {
        return Job.executeQuery("""SELECT new map(tagLink.tagRef as jobId, tagLink.tag.name as tag)
								   FROM TagLink tagLink
								   WHERE tagLink.type = 'job'""")
    }

    @Transactional
    void updateActivity(Job job, boolean activityToSet) {
        job.active = activityToSet
        job.save(failOnError: true)
    }

    @Transactional
    void updateExecutionSchedule(Job job, String executionSchedule) {
        job.executionSchedule = executionSchedule
        job.save(failOnError: true)
    }

    /**
     * If there are csiAggregations for a job it can't be delted.
     * So jobs are getting marked as delted and renamed so the user doesn't see a differents.
     *
     * @param job Job that should be deleted
     */
    void deleteJob(Job job) {
        Job.withSession {
            removeJobFromJobSet(job)
            markAsDeleted(job)
        }
    }

    private void markAsDeleted(Job job) {
        job.label = job.label + "_deleted_id_" + job.id
        job.active = false
        job.deleted = true
        job.save(failOnError: true, flush: true)
    }

    /**
     * Removes deleted Job from JobSets.
     * If it was the last job in JobSet, the jobSet gets deleted
     */
    private void removeJobFromJobSet(Job job) {
        List<JobSet> jobSets = JobSet.createCriteria().list {
            'jobs' {
                idEq(job.id)
            }
        }

        List<JobSet> jobSetsToRemove = []
        jobSets.each {
            if (it.jobs.size() == 1) {
                jobSetsToRemove << it
            } else {
                it.jobs.remove(job)
                it.save(flush: true)
            }
        }

        jobSetsToRemove*.delete(flush: true)
    }

    /**
     * Creates a list of execution dates for the given Job in the given interval
     * @param job The job, not <code>null</code>
     * @param startDate the begin of the interval
     * @param endDate the end of the interval
     * @return a List of DateTimes when the job will be executed
     */
    List<DateTime> getExecutionDatesInInterval(Job job, DateTime startDate, DateTime endDate) {
        Date startDateJava = startDate.toDate()
        Date endDatejava = endDate.toDate()

        List<DateTime> executionDates = new ArrayList<>()

        if (job.getExecutionSchedule() != null) {

            Date nextExecution = job.getNextExecutionTime()
            CronExpression expr = new CronExpression(job.getExecutionSchedule())
            while (nextExecution && nextExecution.before(endDatejava)) {

                if (nextExecution.after(startDateJava)) {
                    executionDates << new DateTime(nextExecution)
                }

                nextExecution = CronExpressionFormatter.getNextValidTimeAfter(expr, nextExecution)
            }
        }
        return executionDates
    }

    String createResultLinkForJob(Job it){
        Map<String,Object> params = [:]
        params["_overwriteWarningAboutLongProcessingTime"] = ""
        params["&overwriteWarningAboutLongProcessingTime"] = "on"
        params["_action_showAll"] = "Anzeigen"
        params["selectedInterval"] = "-1"
        params["selectedTimeFrameInterval"] = "604800"// One Week
        params["selectedFolder"] = "$it.jobGroupId"
        Set pages = []
        it.script.testedPages.each {page ->
            pages << "$page.id"
        }
        params["selectedPages"] = pages
        params["_selectedAllMeasuredEvents"] = ""
        params["selectedAllMeasuredEvents"] = "on"
        params["selectedBrowsers"] = "$it.location.browserId"
        params["_selectedAllBrowsers"] = ""
        params["selectedLocations"] = "$it.location.id"
        params["_selectedAllLocations"] = ""
        params["selectedConnectivityProfiles"] = "$it.connectivityProfileId"
        params["_selectedAllConnectivityProfiles"] = ""
        params["_includeNativeConnectivity"] = ""
        params["selectedAggrGroupValuesUnCached"] = "docCompleteTimeInMillisecsUncached"
        params["trimBelowLoadTimes"] = ""
        params["trimAboveLoadTimes"] = ""
        params["trimBelowRequestCounts"] = ""
        params["trimAboveRequestCounts"] = ""
        params["trimBelowRequestSizes"] = ""
        params["trimAboveRequestSizes"] = ""
        return grailsLinkGenerator.link(controller: 'EventResultDashboard', action: 'showAll',absolute: true, params: params)
    }



}
