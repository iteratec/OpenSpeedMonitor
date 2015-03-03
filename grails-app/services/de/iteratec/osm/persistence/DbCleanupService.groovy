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

import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent
import de.iteratec.osm.result.HttpArchive
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.DetachedCriteria
import de.iteratec.osm.result.detail.WebPerformanceWaterfall
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.dao.EventResultDaoService
//import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition

/**
 * Provides methods for cleanup db. Can be used by quartz-jobs.
 */
class DbCleanupService {

    static transactional = false

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
     * Deletes all {@link EventResult}s {@link JobResult}s {@link HttpArchive}s {@link MeasuredValue}s {@link MeasuredValueUpdateEvent}s before date toDeleteBefore.
     * @param toDeleteBefore	All results-data before this date get deleted.
     */
    void deleteResultsDataBefore(Date toDeleteBefore){
        log.info "Deleting all results-data before: ${toDeleteBefore}"

        //TODO: batching does not persist in database
        // use gorm-batching
        def dc = new DetachedCriteria(JobResult).build {
            lt 'date', toDeleteBefore
        }
        int count = dc.count()

        //batch size -> hibernate doc recommends 10..50
        int batchSize = 50
        0.step(count, batchSize) {
            JobResult.withNewTransaction {
                dc.list(max: batchSize).each { JobResult jobResult ->
                    try {
                        log.info("try to delete JobResult with dependened objects... delete JobResult: {$jobResult.id}")

                        HttpArchive.findAllByJobResult(jobResult)*.delete()

                        jobResult.getEventResults().each {EventResult eventResult ->
                            eventResult.delete()
                        }

                        jobResult.delete()
                    } catch (Exception e) {
                        log.error("JobResult could not deleted ${e}" )
                    }
                }
            }
            //clear hibernate session first-level cache
            JobResult.withSession { session -> session.clear() }
        }

        dc = new DetachedCriteria(MeasuredValue).build {
            lt 'started', toDeleteBefore
        }
        count = dc.count()

        batchSize = 50
        0.step(count, batchSize) {
            MeasuredValue.withNewTransaction {
                dc.list(max: batchSize).each { MeasuredValue measuredValue ->
                    try {
                        def innerDc = new DetachedCriteria(MeasuredValueUpdateEvent).build {
                            eq 'measuredValueId', measuredValue.id
                        }
                        int innerCount = innerDc.count()

                        0.step(innerCount, batchSize){ innerOffset ->
                            MeasuredValueUpdateEvent.withNewTransaction {
                                innerDc.list(offset:  innerOffset, max: batchSize).each { MeasuredValueUpdateEvent measuredValueUpdateEvent ->
                                    try{
                                        log.info("try to delete MeasuredValueUpdateEvent {$measuredValueUpdateEvent.id}")
                                        measuredValueUpdateEvent.delete()
                                    }catch (Exception e){
                                        log.error("MeasuredValueUpdateEvent could not deleted ${e}")
                                    }
                                }
                            }
                            MeasuredValueUpdateEvent.withSession { session -> session.clear() }
                        }

                        log.info("try to delete MeasuredValue {$measuredValue.id}")
                        measuredValue.delete()
                    } catch (Exception e) {
                        log.error("MeasuredValue could not deleted ${e}")
                    }
                }
            }
            MeasuredValue.withSession { session -> session.clear() }
        }

        log.info "... DONE"
    }
}

