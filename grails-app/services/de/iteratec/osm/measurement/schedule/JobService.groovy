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

import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdater
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import grails.gorm.DetachedCriteria
import grails.transaction.Transactional
import org.joda.time.DateTime
import org.quartz.CronExpression

class JobService {
    BatchActivityService batchActivityService

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

    private List<Job> findByJobGroup(JobGroup group) {
        return Job.findAllByJobGroup(group)
    }

    List<Job> getAllCsiJobs() {
        List<Job> csiJobs = []
        JobGroup.findAllByCsiConfigurationIsNotNull().each {
            csiJobs.addAll(findByJobGroup(it))
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
     * Deletes a Job with all JobResults and EventResults
     *
     * @param job Job that should be deleted
     */
    void deleteJob(Job job) {
        String activityName = "Job ${job.label} delete"
        if (batchActivityService.runningBatch(Job.class, activityName, Activity.DELETE)) {
            return
        }
        BatchActivityUpdater activity = batchActivityService.getActiveBatchActivity(Job.class, Activity.DELETE, "Job ${job.label} delete", 2, true)
        def dc = new DetachedCriteria(JobResult).build {
            eq 'job', job
        }
        int count = 0
        Job.withTransaction {
            count = dc.count()
        }
        activity.beginNewStage("Delete JobResults", count).update()
        int batchSize = 100
        Job.withSession { session ->
            0.step(count, batchSize) { offset ->
                Job.withTransaction {
                    dc.list(offset: 0, max: batchSize).eachWithIndex { JobResult jobResult, int index ->
                        try {
                            log.info("try to delete JobResult with depended objects, ID: ${jobResult.id}")
                            List<EventResult> eventResults = jobResult.getEventResults()
                            batchDelete(eventResults, batchSize)
                            jobResult.delete()
                            activity.addProgressToStage().update()
                        } catch (Exception e) {
                            log.error("Couldn't delete JobResult ${e}")
                            activity.addFailures().setLastFailureMessage("Couldn't delete JobResult: ${jobResult.id}").update()
                        }
                    }
                    session.flush()
                    session.clear()
                }
            }
            activity.beginNewStage("Delete Job",1).update()
            Job.withTransaction {
                try {
                    job.delete(flush: true)
                } catch (Exception e) {
                    activity.addFailures().setLastFailureMessage("Job could't be deleted").update()
                    e.printStackTrace()
                }
            }
            activity.addProgressToStage().done()
        }
    }

    /**
     * Deletes a List of objects with a new Transaction and will delete up to batchSize objects with one transaction
     * @param objects Objects to be deleted
     * @param batchSize maximum delete interval
     */
    private void batchDelete(List objects, int batchSize) {
        0.step(objects.size(), batchSize) { off ->
            Job.withTransaction {
                int max = off + batchSize
                max = (max > objects.size()) ? objects.size() : max
                objects.subList(off, max)*.delete()
            }
        }
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
}
