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

package de.iteratec.osm.persistence

import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdater
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.DetachedCriteria

/**
 * Provides methods for cleanup db. Can be used by quartz-jobs.
 */
class DbCleanupService {

    BatchActivityService batchActivityService
    PerformanceLoggingService performanceLoggingService

    /**
     * Deletes all {@link EventResult}s {@link JobResult}s before date toDeleteBefore.
     * @param toDeleteBefore	All results-data before this date get deleted.
     */
    void deleteResultsDataBefore(Date toDeleteBefore, boolean createBatchActivity = true){
        log.info "begin with deleteResultsDataBefore"

        // use gorm-batching
        def dc = new DetachedCriteria<JobResult>(JobResult).build {
            lt 'date', toDeleteBefore
        }

        int count = dc.count().toInteger()
        String jobName = "Nightly cleanup of JobResults with dependents objects"
        //TODO: check if the QuartzJob is available... after app restart, the QuartzJob is shutdown, but the activity is in database
        if (count > 0 && !batchActivityService.runningBatch(this.class, jobName, Activity.DELETE)) {
            BatchActivityUpdater batchActivity = batchActivityService.getActiveBatchActivity(this.class, Activity.DELETE, jobName, 1, createBatchActivity)
            batchActivity.beginNewStage("Delete JobResults", count)
            //batch size -> hibernate doc recommends 10..50
            int batchSize = 50
            0.step(count, batchSize) { int offset ->
                JobResult.withNewTransaction {
                    def list = dc.list(max: batchSize)
                    list.each { JobResult jobResult ->
                        try {
                            jobResult.getEventResults().each { EventResult eventResult ->
                                eventResult.delete()
                            }
                            jobResult.delete()
                        } catch (Exception ignored) {
                        }
                    }
                    batchActivity.addProgressToStage(list.size())
                }
                //clear hibernate session first-level cache
                JobResult.withSession { session -> session.clear() }
            }
            batchActivity.done()
        }

        log.info "end with deleteResultsDataBefore"
    }

    /**
     * Deletes all {@link CsiAggregation}s {@link CsiAggregationUpdateEvent}s before date toDeleteBefore.
     * @param toDeleteBefore	All results-data before this date get deleted.
     */
    void deleteCsiAggregationsAndCsiAggregationUpdateEventsBefore(Date toDeleteBefore, boolean createBatchActivity = true){
        log.info "begin with deleteCsiAggregationsAndCsiAggregationUpdateEventsBefore"

        def csiAggregationDetachedCriteria = new DetachedCriteria(CsiAggregation).build {
            lt 'started', toDeleteBefore
        }
        int csiAggregationCount = csiAggregationDetachedCriteria.count()
        log.info "CsiAggregation - Count : ${csiAggregationCount}"

        def csiAggregationUpdateEventDetachedCriteria = new DetachedCriteria(CsiAggregationUpdateEvent).build {
            'in'('csiAggregationId', csiAggregationDetachedCriteria.list()*.id )
        }
        int csiAggregationUpdateEventsCount = csiAggregationUpdateEventDetachedCriteria.count()
        log.info "CsiAggregationUpdateEvent - Count : ${csiAggregationUpdateEventsCount}"

        int globalCount = csiAggregationCount + csiAggregationUpdateEventsCount
        String jobName = "Nightly cleanup of CsiAggregations and CsiAggregationUpdateEvents"
        //TODO: check if the QuartzJob is available... after app restart, the QuartzJob is shutdown, but the activity is in database
        if(csiAggregationCount > 0 && !batchActivityService.runningBatch(this.class, jobName, Activity.DELETE)) {
            BatchActivityUpdater batchActivity = batchActivityService.getActiveBatchActivity(this.class, Activity.DELETE, jobName,2, createBatchActivity)
            //batch size -> hibernate doc recommends 10..50
            int batchSize = 50
            log.debug('Starting deletion of CsiAggregationUpdateEvents and CsiAggregations')

            //First clean CsiAggregationUpdateEvents
            batchActivity.beginNewStage('delete CsiAggregationUpdateEvents', globalCount)
            0.step(csiAggregationUpdateEventsCount, batchSize){ int offset ->
                CsiAggregationUpdateEvent.withNewTransaction {
                    csiAggregationUpdateEventDetachedCriteria.list(max: batchSize).each{ CsiAggregationUpdateEvent csiAggregationUpdateEvent
                        ->
                        try {
                            csiAggregationUpdateEvent.delete()
                            batchActivity.addProgressToStage()
                        }
                        catch(Exception e){
                        }
                    }
                    batchActivity
                }
                //clear hibernate session first-level cache
                CsiAggregationUpdateEvent.withSession { session -> session.clear() }
            }

            log.debug('Deletion of CsiAggregationUpdateEvents finished')

            batchActivity.beginNewStage('delete CsiAggregations', csiAggregationCount)
            //After then clean CsiAggregations
            0.step(csiAggregationCount, batchSize) { int offset ->
                CsiAggregation.withNewTransaction {
                    csiAggregationDetachedCriteria.list(max: batchSize).each { CsiAggregation csiAggregation ->
                        try {
                            csiAggregation.delete()
                            batchActivity.addProgressToStage()
                        } catch (Exception e) {
                        }
                    }
                }
                //clear hibernate session first-level cache
                CsiAggregation.withSession { session -> session.clear() }
                batchActivity
            }
            log.debug('Deletion of CsiAggregations finished')
            batchActivity.done()
        }

        log.info "end with deleteCsiAggregationsAndCsiAggregationUpdateEventsBefore"
    }

    /**
     * Deletes all {@link BatchActivity}s before date toDeleteBefore.
     * @param toDeleteBefore	All BatchActivities before this date get deleted.
     */
    void deletBatchActivityDataBefore(Date toDeleteBefore, boolean createBatchActivity = true){
        String jobName = "Nightly cleanup of BatchActivities"
        log.info "begin with $jobName"

        // use gorm-batching
        def dc = new DetachedCriteria(BatchActivity).build {
            lt 'startDate', toDeleteBefore
        }
        int count = dc.count()


        if(count > 0 && !batchActivityService.runningBatch(BatchActivity.class, jobName, Activity.DELETE)) {
            BatchActivityUpdater batchActivityUpdater = batchActivityService.getActiveBatchActivity(BatchActivity.class, Activity.DELETE, jobName, 1, createBatchActivity)
            batchActivityUpdater.beginNewStage("Delete BatchActivites", count)
            //batch size -> hibernate doc recommends 10..50
            int batchSize = 50
            0.step(count, batchSize) { int offset ->
                BatchActivity.withNewTransaction {
                    def list = dc.list(max: batchSize)
                    list.each { BatchActivity batchActivity ->
                        try {
                            batchActivity.delete()
                            batchActivityUpdater.addProgressToStage()
                        } catch (Exception e) {
                            batchActivityUpdater.addFailures("Couldn't delete BatchActivity ${batchActivity.id}")
                        }
                    }
                }
                //clear hibernate session first-level cache
                JobResult.withSession { session -> session.clear() }
            }
            batchActivityUpdater.done()
        }

        log.info "end with $jobName"
    }
}

