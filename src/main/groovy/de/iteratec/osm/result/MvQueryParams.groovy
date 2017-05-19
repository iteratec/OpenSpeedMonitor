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

/**
 * <p>
 * Assembles params which are used to query {@link CsiAggregation}s via tag.
 * </p>
 *
 * @author nkuhn
 * @author mze
 */
class MvQueryParams {

    /**
     * Database-Ids of {@link JobGroup}s to find,
     * never <code>null</code>.
     */
    final SortedSet<Long> jobGroupIds = new TreeSet<Long>()

    /**
     * Database-Ids of {@link de.iteratec.osm.result.MeasuredEvent}s to find,
     * never <code>null</code>.
     */
    final SortedSet<Long> measuredEventIds = new TreeSet<Long>()

    /**
     * Database-Ids of {@link Page}s to find,
     * never <code>null</code>.
     */
    final SortedSet<Long> pageIds = new TreeSet<Long>()

    /**
     * Database-Ids of {@link Browser}s to find,
     * never <code>null</code>.
     */
    final SortedSet<Long> browserIds = new TreeSet<Long>()

    /**
     * Database-Ids of {@link Location}s to find,
     * never <code>null</code>.
     */
    final SortedSet<Long> locationIds = new TreeSet<Long>()

    /**
     * Database-Ids of {@link ConnectivityProfile}s to find,
     * never <code>null</code>.
     */
    final SortedSet<Long> connectivityProfileIds = new TreeSet<Long>()

    /**
     * if true, results no filtered by connectivity
     */
    protected boolean includeAllConnectivities = false

    @Override
    public String toString() {
        return "jobGroupIds=${jobGroupIds*.toString()}, " +
                "pageIds=${pageIds*.toString()}, " +
                "measuredEventIds=${measuredEventIds*.toString()}, " +
                "browserIds${browserIds*.toString()}, " +
                "locationIds=${locationIds*.toString()}, " +
                "connectivityProfileIds=${connectivityProfileIds*.toString()}"
    }
}
