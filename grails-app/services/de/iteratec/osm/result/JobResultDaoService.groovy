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

import de.iteratec.osm.measurement.schedule.Job

/**
 * Provides DAO functionality for {@link JobResult} domain.
 */
class JobResultDaoService {

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
	 * Returns all successful JobResults belonging to the specified Job
	 */
	public Collection<JobResult> findSuccessfulJobResultsByJob(Job _job, Date fromDate, Date toDate) {
		return JobResult.where { job == _job && httpStatusCode == 200 && date >= fromDate && date <= toDate }.list(sort: 'date', order: 'desc')
	}
}
