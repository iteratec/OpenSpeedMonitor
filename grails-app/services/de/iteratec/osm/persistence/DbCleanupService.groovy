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
import de.iteratec.osm.batch.Status
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.result.detail.AssetGroup
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.DetachedCriteria

//import org.springframework.transaction.TransactionDefinition
/**
 * Provides methods for cleanup db. Can be used by quartz-jobs.
 */
class DbCleanupService {

    static transactional = false

    BatchActivityService batchActivityService
	EventResultDaoService eventResultDaoService
    PerformanceLoggingService performanceLoggingService

    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

    /**
     * Deletes all {@link EventResult}s {@link JobResult}s before date toDeleteBefore.
     * @param toDeleteBefore	All results-data before this date get deleted.
     */
    void deleteResultsDataBefore(Date toDeleteBefore, boolean createBatchActivity = true){
        log.info "begin with deleteResultsDataBefore"

        // use gorm-batching
        def dc = new DetachedCriteria(JobResult).build {
            lt 'date', toDeleteBefore
        }
        int count = dc.count()

        //TODO: check if the QuartzJob is availible... after app restart, the QuartzJob is shutdown, but the activity is in database
        if(count > 0 && !batchActivityService.runningBatch(this.class, 1)) {
            BatchActivity batchActivity = batchActivityService.getActiveBatchActivity(this.class, 1, Activity.DELETE, "Nightly cleanup of JobResults with dependents objects",createBatchActivity)
            //batch size -> hibernate doc recommends 10..50
            int batchSize = 50
            0.step(count, batchSize) { int offset ->
                batchActivity.updateStatus(['progress': batchActivityService.calculateProgress(count, offset)])
                JobResult.withNewTransaction {
                    dc.list(max: batchSize).each { JobResult jobResult ->
                        try {

                            jobResult.getEventResults().each { EventResult eventResult ->
                                eventResult.delete()
                            }

                            jobResult.delete()
                        } catch (Exception e) {
                        }
                    }
                }
                //clear hibernate session first-level cache
                JobResult.withSession { session -> session.clear() }
            }
            batchActivity.updateStatus([ "progress": "100 %", "endDate": new Date(), "status": Status.DONE])
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

        //TODO: check if the QuartzJob is availible... after app restart, the QuartzJob is shutdown, but the activity is in database
        if(csiAggregationCount > 0 && !batchActivityService.runningBatch(this.class, 2)) {
            BatchActivity batchActivity = batchActivityService.getActiveBatchActivity(this.class, 2, Activity.DELETE, "Nightly cleanup of CsiAggregations and CsiAggregationUpdateEvents",createBatchActivity )
            //batch size -> hibernate doc recommends 10..50
            int batchSize = 50
            log.debug('Starting deletion of CsiAggregationUpdateEvents and CsiAggregations')

            //First clean CsiAggregationUpdateEvents
            0.step(csiAggregationUpdateEventsCount, batchSize){ int offset ->
                batchActivity.updateStatus(['progress': batchActivityService.calculateProgress(globalCount, offset), 'stage': 'delete CsiAggregationUpdateEvents'])
                CsiAggregationUpdateEvent.withNewTransaction {
                    csiAggregationUpdateEventDetachedCriteria.list(max: batchSize).each{ CsiAggregationUpdateEvent csiAggregationUpdateEvent
                        ->
                        try {
                            csiAggregationUpdateEvent.delete()
                        }
                        catch(Exception e){
                        }
                    }
                }
                //clear hibernate session first-level cache
                CsiAggregationUpdateEvent.withSession { session -> session.clear() }
            }

            log.debug('Deletion of CsiAggregationUpdateEvents finished')

            //After then clean CsiAggregations
            0.step(csiAggregationCount, batchSize) { int offset ->
                batchActivity.updateStatus(['progress': batchActivityService.calculateProgress(csiAggregationCount, offset+csiAggregationUpdateEventsCount), 'stage': 'delete CsiAggregations'])
                CsiAggregation.withNewTransaction {
                    csiAggregationDetachedCriteria.list(max: batchSize).each { CsiAggregation csiAggregation ->
                        try {
                            csiAggregation.delete()
                        } catch (Exception e) {
                        }
                    }
                }
                //clear hibernate session first-level cache
                CsiAggregation.withSession { session -> session.clear() }
            }
            batchActivity.updateStatus([ "progress": "100 %", "endDate": new Date(), "status": Status.DONE])
            log.debug('Deletion of CsiAggregations finished')
        }

        log.info "end with deleteCsiAggregationsAndCsiAggregationUpdateEventsBefore"
    }
    public void deleteHarDataBefore(Date toDeleteBefore, boolean createBatchActivity = true){
        log.info "begin with deleteResultsDataBefore"

        // use gorm-batching
        def dc = new DetachedCriteria(AssetGroup).build {
            lt 'date', toDeleteBefore.getTime()
        }
        int count = dc.count()

        if(count > 0 && !batchActivityService.runningBatch(AssetGroup.class, 1)) {
            BatchActivity batchActivity = batchActivityService.getActiveBatchActivity(AssetGroup.class, 1, Activity.DELETE, "Nightly cleanup of Har data",createBatchActivity)
            //batch size -> hibernate doc recommends 10..50
            int batchSize = 50
            //TODO all three delete methods have many things in common,
            //we should definitely do some closure magic to avoid code clones
            0.step(count, batchSize) { int offset ->
                batchActivity.updateStatus(['progress': batchActivityService.calculateProgress(count, offset)])
                AssetGroup.withNewTransaction {
                    dc.list(max: batchSize).each { AssetGroup assetGroup ->
                        try {
                            assetGroup.delete()
                        } catch (Exception e) {
                            println e
                        }
                    }
                }
                //clear hibernate session first-level cache
                AssetGroup.withSession { session -> session.clear() }
            }
            batchActivity.updateStatus([ "progress": "100 %", "endDate": new Date(), "status": Status.DONE])
        }

        log.info "end with deleteHarDataBefore"
    }
}

