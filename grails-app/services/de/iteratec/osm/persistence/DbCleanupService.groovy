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

import de.iteratec.osm.result.JobResult
import grails.gorm.DetachedCriteria
import de.iteratec.osm.result.detail.WebPerformanceWaterfall
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.dao.EventResultDaoService

/**
 * Provides methods for cleanup db. Can be used by quartz-jobs.
 */
class DbCleanupService {

    static transactional = true
	
	EventResultDaoService eventResultDaoService

	/**
	 * Deletes all {@link WebPerformanceWaterfall}s and associated {@link WaterfallEntry}s before date toDeleteBefore.
	 * @param toDeleteBefore	All Waterfalls before this date get deleted. 
	 */
    void deleteWaterfallsBefore(Date toDeleteBefore) {
		
		log.info "Deleting all waterfalls before: ${toDeleteBefore}"
		
//		deleteWaterfallsBeforeViaHql(toDeleteBefore)
//		deleteWaterfallsBeforeViaGormBatching(toDeleteBefore)
		
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

        //deleting code
        try {
            JobResult.list().findAll { it.date.before(toDeleteBefore) }*.delete(flush: true)
        }catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Could not delete JobResults recursive")
        }

        log.info "... DONE"
    }
	
}
