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
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent
import de.iteratec.osm.result.HttpArchive
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.DetachedCriteria
import de.iteratec.osm.result.detail.WebPerformanceWaterfall
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.dao.EventResultDaoService
import org.quartz.core.QuartzScheduler

//import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition

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
	 * Deletes all {@link WebPerformanceWaterfall}s and associated {@link WaterfallEntry}s before date toDeleteBefore.
	 * @param toDeleteBefore	All Waterfalls before this date get deleted. 
	 */
    void deleteWaterfallsBefore(Date toDeleteBefore) {
		
		log.info "Deleting all waterfalls before: ${toDeleteBefore}"
		
//		deleteWaterfallsBeforeViaHql(toDeleteBefore)
//		deleteWaterfallsBeforeViaGormBatching(JobtoDeleteBefore)
		
		log.info "... DONE"
    }
	
	/**
	 * Uses HQL.
	 * @param toDeleteBefore
	 */
	void deleteWaterfallsBeforeViaHql(Date toDeleteBefore){
		//remove Waterfalls from EventResults
		EventResult.executeUpdate("update EventResult res set res.webPerformanceWaterfall=:null " +
			"where res.jobResultDate<:toDeleteBefore", [toDeleteBefore: toDeleteBefore])
		//delete waterfalls
		EventResult.executeUpdate("delete WebPerformanceWaterfall wf where wf.startDate<:toDeleteBefore",
			[toDeleteBefore: toDeleteBefore])
	}
	/**
	 * Uses gorm-batching.
	 * @param toDeleteBefore
	 * @see http://stackoverflow.com/questions/18848067/what-is-the-best-way-to-process-a-large-list-of-domain-objects
	 */
	void deleteWaterfallsBeforeViaGormBatching(Date toDeleteBefore){
		def dc = new DetachedCriteria(WebPerformanceWaterfall).build{
			lt 'startDate', toDeleteBefore
		}
		def count = dc.count()

		// Optional:
		// dc = dc.build{
		//     projections { property('username') }
		// }
		
		def batchSize = 50 // Hibernate Doc recommends 10..50
		0.step(count, batchSize){ offset->
			dc.list(offset:offset, max:batchSize).each{
			   // doSmthWithTransaction(it)
			}
			//clear the first-level cache
			//def hiberSession = sessionFactory.getCurrentSession()
			//hiberSession.clear()
			// or
			WebPerformanceWaterfall.withSession { session -> session.clear() }
		}
	}

    /**
     * Deletes all {@link EventResult}s {@link JobResult}s {@link HttpArchive}s before date toDeleteBefore.
     * @param toDeleteBefore	All results-data before this date get deleted.
     */
    void deleteResultsDataBefore(Date toDeleteBefore){
        log.info "begin with deleteResultsDataBefore"

        // use gorm-batching
        def dc = new DetachedCriteria(JobResult).build {
            lt 'date', toDeleteBefore
        }
        int count = dc.count()

        //TODO: check if the QuartzJob is availible... after app restart, the QuartzJob is shutdown, but the activity is in database
        if(count > 0 && !batchActivityService.runningBatch(this.class, 1)) {
            BatchActivity batchActivity = batchActivityService.getActiveBatchActivity(this.class, 1, Activity.DELETE, "Nightly cleanup of JobResults with dependents objects" )
            //batch size -> hibernate doc recommends 10..50
            int batchSize = 50
            0.step(count, batchSize) { int offset ->
                batchActivityService.updateStatus(batchActivity, ['progress': batchActivityService.calculateProgress(count, offset)])
                JobResult.withNewTransaction {
                    dc.list(max: batchSize).each { JobResult jobResult ->
                        try {
                            HttpArchive.findAllByJobResult(jobResult)*.delete()

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
            batchActivityService.updateStatus(batchActivity, [ "progress": "100 %", "endDate": new Date(), "status": Status.DONE])
        }

        log.info "end with deleteResultsDataBefore"
    }


    /**
     * Deletes all {@link MeasuredValue}s {@link MeasuredValueUpdateEvent}s before date toDeleteBefore.
     * @param toDeleteBefore	All results-data before this date get deleted.
     */
    void deleteMeasuredValuesAndMeasuredValueUpdateEventsBefore(Date toDeleteBefore){
        log.info "begin with deleteMeasuredValuesAndMeasuredValueUpdateEventsBefore"

        def measuredValueDetachedCriteria = new DetachedCriteria(MeasuredValue).build {
            lt 'started', toDeleteBefore
        }
        int measuredValueCount = measuredValueDetachedCriteria.count()
        log.info "MeasuredValue - Count : ${measuredValueCount}"

        def measuredValueUpdateEventDetachedCriteria = new DetachedCriteria(MeasuredValueUpdateEvent).build {
            'in'('measuredValueId', measuredValueDetachedCriteria.list()*.id )
        }
        int measuredValueUpdateEventsCount = measuredValueUpdateEventDetachedCriteria.count()
        log.info "MeasuredValueUpdateEvent - Count : ${measuredValueUpdateEventsCount}"

        int globalCount = measuredValueCount + measuredValueUpdateEventsCount

        //TODO: check if the QuartzJob is availible... after app restart, the QuartzJob is shutdown, but the activity is in database
        if(measuredValueCount > 0 && !batchActivityService.runningBatch(this.class, 2)) {
            BatchActivity batchActivity = batchActivityService.getActiveBatchActivity(this.class, 2, Activity.DELETE, "Nightly cleanup of MeasuredValues and MeasuredValueUpdateEvents" )
            //batch size -> hibernate doc recommends 10..50
            int batchSize = 50
            log.debug('Starting deletion of MeasuredValueUpdateEvents and MeasuredValues')

            //First clean MeasuredValueUpdateEvents
            0.step(measuredValueUpdateEventsCount, batchSize){ int offset ->
                batchActivityService.updateStatus(batchActivity, ['progress': batchActivityService.calculateProgress(globalCount, offset), 'stage': 'delete MeasuredValueUpdateEvents'])
                MeasuredValueUpdateEvent.withNewTransaction {
                    measuredValueUpdateEventDetachedCriteria.list(max: batchSize).each{ MeasuredValueUpdateEvent measuredValueUpdateEvent ->
                        try {
                            measuredValueUpdateEvent.delete()
                        }
                        catch(Exception e){
                        }
                    }
                }
                //clear hibernate session first-level cache
                MeasuredValueUpdateEvent.withSession { session -> session.clear() }
            }

            log.debug('Deletion of MeasuredValueUpdateEvents finished')

            //After then clean MeasuredValues
            0.step(measuredValueCount, batchSize) { int offset ->
                batchActivityService.updateStatus(batchActivity, ['progress': batchActivityService.calculateProgress(measuredValueCount, offset+measuredValueUpdateEventsCount), 'stage': 'delete MeasuredValues'])
                MeasuredValue.withNewTransaction {
                    measuredValueDetachedCriteria.list(max: batchSize).each { MeasuredValue measuredValue ->
                        try {
                            measuredValue.delete()
                        } catch (Exception e) {
                        }
                    }
                }
                //clear hibernate session first-level cache
                MeasuredValue.withSession { session -> session.clear() }
            }
            batchActivityService.updateStatus(batchActivity, [ "progress": "100 %", "endDate": new Date(), "status": Status.DONE])
            log.debug('Deletion of MeasuredValues finished')
        }

        log.info "end with deleteMeasuredValuesAndMeasuredValueUpdateEventsBefore"
    }
}

