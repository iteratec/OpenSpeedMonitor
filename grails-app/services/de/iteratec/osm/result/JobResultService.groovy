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

package de.iteratec.osm.result

import java.sql.SQLException
import java.util.List;

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup;
import de.iteratec.osm.measurement.environment.Browser;
import de.iteratec.osm.measurement.environment.Location

/**
 * FIXME mze-2013-09-13: Rename to JobResultDAOService -> This is a DAO!
 */
class JobResultService {

	/**
	 * <p>
	 * Finds the {@link JobResult} the specified {@link EventResult}
	 * belongs to.
	 * </p>
	 * 
	 * <p>
	 * Implementation note: This implementation is optimistic, which means 
	 * it "trusts" in consistency: One EventResult is assigned to one and 
	 * only one JobResult.
	 * </p>
	 * 
	 * @param eventResult 
	 * 		The EventResult referenced by the JobResult to find, 
	 * 		not <code>null</code>.
	 * @return 
	 * 		The corresponding JobResult, never <code>null</code>.
	 * @throws SQLException
	 * 		if the database is inconsistent, which means the EventResult 
	 * 		has not previously assigned to a JobResult OR the EventResult 
	 * 		is assigned to more than one JobResult 
	 * 		-> this indicated a Bug in the persistence process!
	 * 
	 */
	/* Should not be needed anymore cause since Feb-2015 you can directly navigate from EventResult to JobResult
	public JobResult findJobResultByEventResult(EventResult eventResult) {
		// Note: Grails uses the grails.gorm.CriteriaBuilder in test-mode,
		// but the HibernateCriteriaBuilder in productive mode!?
		// -> different types! Why ever... so we use def-declaration here.
		def criteria = JobResult.createCriteria()

		List results = criteria.list {
			eventResults {
				eq("id", eventResult.id)
			}
		}

		if( results.size != 1 )
		{
			// The database should be consistent so there should always be one result!
			throw new SQLException("Database seems to be inconsistent: "
			+ "Count of found items " + results.size() + " expected was 1.")
		}

		return results.get(0);
	}
	*/
	
	/**
	 * <p>
	 * Finds the {@link JobResult} with the specified database id if existing.
	 * </p>
	 * 
	 * @param databaseId 
	 *         The database id of the job result to find.
	 *         
	 * @return The found job result or <code>null</code> if not exists.
	 */
	public JobResult tryToFindById(long databaseId)
	{
		return JobResult.get(databaseId);
	}

	/**
	 * 
	 * @param mvQueryParams
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public Collection<JobResult> findJobResultsByQueryParams(MvQueryParams mvQueryParams, Date fromDate, Date toDate) {

		def jobResultCriteria = JobResult.createCriteria();
		def jobResults = jobResultCriteria{
			and {
				if(!mvQueryParams.locationIds.isEmpty()) {
					'in'('locationLocation', Location.where { 'in'('id', mvQueryParams.getLocationIds())}.findAll()*.location)
				}

				if(!mvQueryParams.browserIds.isEmpty()) {
					'in'('locationBrowser', Browser.where { 'in'('id', mvQueryParams.getBrowserIds())}.findAll()*.name)
				}

				if(!mvQueryParams.jobGroupIds.isEmpty()) {
					'in'('jobGroupName',  JobGroup.where { 'in'('id', mvQueryParams.getJobGroupIds()) }.findAll()*.name)
				}

				if(!mvQueryParams.measuredEventIds.isEmpty()) {
					eventResults{
						measuredEvent {
							'in'('id', mvQueryParams.getMeasuredEventIds())
						}
					}
				}

				if(!mvQueryParams.pageIds.isEmpty()) {
					eventResults{
						measuredEvent {
							testedPage {
								'in'('id', mvQueryParams.getPageIds())
							}
						}
					}
				}

				ge('date', fromDate)
				le('date', toDate)
			}

			order('date', 'desc')
		};

		return jobResults
	}

	/**
	 * Returns all successful JobResults belonging to the specified Job 
	 */
	public Collection<JobResult> findSuccessfulJobResultsByJob(Job _job, Date fromDate, Date toDate) {
		return JobResult.where { job == _job && httpStatusCode == 200 && date >= fromDate && date <= toDate }.list(sort: 'date', order: 'desc')
	}
}
