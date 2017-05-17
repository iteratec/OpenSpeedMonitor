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

package de.iteratec.osm.measurement.schedule.dao

import de.iteratec.osm.measurement.schedule.JobGroup
/**
 * <p>
 * An data-access object (DAO) for {@link de.iteratec.osm.measurement.schedule.JobGroup}.
 * </p>
 * 
 * <p>
 * Contains only methods that query {@link de.iteratec.osm.measurement.schedule.JobGroup}s from database. Doesn't
 * contain any dependencies to other domains or service-logic.
 * </p>
 * 
 * @author nkuhn
 * @author mze
 */
public interface JobGroupDaoService {

	/**
	 * <p>
	 * Finds all {@linkplain de.iteratec.osm.measurement.schedule.JobGroup job groups} currently known in database.
	 * The returned {@link Set} is unmodifiable.
	 * </p>
	 * 
	 * @return Never <code>null</code> but possibly
	 *         {@linkplain Collection#isEmpty() empty}.
	 */
	Set<JobGroup> findAll();

	/**
	 * <p>
	 * Gets a {@link Set} of CSI related {@linkplain JobGroup job groups}.
	 * The returned set is unmodifiable.
	 * </p>
	 * 
	 * @return Never <code>null</code> but possibly
	 *         {@linkplain Collection#isEmpty() empty}.
	 */
	public Set<JobGroup> findCSIGroups();

    /**
     * Returns all tags added to {@link JobGroup}s.
     * Identical tags of multiple {@link JobGroup}s get returned just once.
     * @return
     */
    public List<String> getAllUniqueTags();
    /**
     * Returns at most maxNumberOfTags tags added to {@link JobGroup}s.
     * Identical tags of multiple {@link JobGroup}s get returned just once.
     * @return
     */
    public List<String> getMaxUniqueTags(int maxNumberOfTags);

    /**
     * Returns a Map with all existing {@link JobGroup} tags as keys and
     * a list of all {@link JobGroup} names which contain that tag.
     *
     * @return A Map with all existing {@link JobGroup} tags as keys and
     * a list of all {@link JobGroup} names which contain that tag.
     */
    public Map<String, List<String>> getTagToJobGroupNameMap();
}
