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

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.HttpArchive
import de.iteratec.osm.result.JobResult
import grails.gorm.DetachedCriteria
import org.grails.datastore.gorm.AbstractGormApi
import org.grails.datastore.gorm.GormInstanceApi
import org.grails.datastore.mapping.core.Session


class JobService {

	/**
	 * <p>
	 * Returns the group of a job, if it was assigned to a group and the groups 
	 * type is equivalent to the CSI-groups type.
	 * </p>
	 *  
	 * @param job The Job, not <code>null</code>
	 * @return null if the job is not assigned to a group or the assigned 
	 *         group is not a CSI-group, else the group
	 */
	JobGroup getCsiJobGroupOf(Job job){
		JobGroup group = job.getJobGroup();
		/*- 
		 * Use of defensive programming intended here:
		 * 
		 * A Job could not be assigned to a group if the Job is unknown before
		 * and was never manually(!) assigned.
		 */
		if( group != null && group.getGroupType() == JobGroupType.CSI_AGGREGATION ) {
			return group;
		} else {
			return null;
		}
	}

	private List<Job> findByJobGroup(JobGroup group){
		return Job.findAllByJobGroup(group).asList();
	}
	List<Job> getAllCsiJobs(){
		List<Job> csiJobs = []
		JobGroup.findAllByGroupType(JobGroupType.CSI_AGGREGATION).each {
			csiJobs.addAll(findByJobGroup(it).asList())
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

	void updateActivity(Job job, boolean activityToSet){
		job.active = activityToSet
		job.save(failOnError: true)
	}

	void updateExecutionSchedule(Job job, String executionSchedule){
		job.executionSchedule = executionSchedule
		job.save(failOnError: true)
	}

    /**
     * Deletes a Job with all JobResults, HttpArchives and EventResults
     *
     * @param job Job that should be deleted
     * @param c Closure to call if something couldn't be deleted
     */
    void deleteJob(Job job, Closure c){
        def dc = new DetachedCriteria(JobResult).build {
            eq 'job', job
        }
        int count = dc.count()
        int batchSize = 100
        Job.withSession { session ->
            0.step(count,batchSize){offset->
                Job.withTransaction {
                    dc.list(offset: 0,max: batchSize).eachWithIndex {JobResult jobResult,int index->
                        try {
                            log.info("try to delete JobResult with depended objects, ID: ${jobResult.id}")
                            List<HttpArchive> httpArchives = HttpArchive.findAllByJobResult(jobResult)
                            batchDelete(httpArchives,batchSize)
//                            FIXME with IT-456 there will be no cascading delete from JobResult to EventResult and the following lines should be activated
//                            List<EventResult> eventResults = jobResult.getEventResults()
//                            batchDelete(eventResults,batchSize)
                            jobResult.delete()
                        } catch (Exception e){
                            log.error("Couldn't delete JobResult ${e}")
                            c.call()
                        }
                    }
                }
                session.flush()
                session.clear()
            }
            Job.withTransaction {
                job.delete(flush: true)
            }
        }
    }
    /**
     * Deletes a List of objects with a new Transaction and will delete up to batchSize objects with one transaction
     * @param objects Objects to be deleted
     * @param batchSize maximum delete interval
     */
    private void batchDelete(List objects,int batchSize){
        0.step(objects.size(),batchSize){off->
            Job.withTransaction {
                int max = off+batchSize
                max = (max>objects.size())?objects.size():max
                objects.subList(off,max)*.delete()
            }
        }
    }
}
