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

package de.iteratec.osm.csi

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.EventResult;

/**
 * A value for calculation of customer satisfaction indices. 
 * @author nkuhn
 *
 */
public interface CsiValue {
    /**
     * Delivers csi-relevant value calculated by docComplete-time
     */
    public Double retrieveCsByWptDocCompleteInPercent()

    /**
     * Delivers csi-relevant value calculated by visuallyComplete-time
     */
    public Double retrieveCsByWptVisuallyCompleteInPercent()

    /**
     * Delivers csi-relevant date of this {@link CsiValue}.
     * @return
     */
    public Date retrieveDate()

    /**
     * Delivers connectivity Profile
     */
    public ConnectivityProfile retrieveConnectivityProfile()

    /**
     * Delivers the id's of all {@link EventResult}s which underly this csByWptDocCompleteInPercent-value.
     * @return
     */
    public List<Long> retrieveUnderlyingEventResultsByDocComplete()

    /**
     * Delivers all EventResults which underly this csByWptVisuallyCompleteInPercent-value.
     * @return
     */
    public List<EventResult> retrieveUnderlyingEventResultsByVisuallyComplete()

    /**
     * Delivers the {@link JobGroup} for this csiValue. Can be null.
     * @return
     */
    public JobGroup retrieveJobGroup()

    /**
     * Delivers the {@link Page} for this csiValue. Can be null.
     * @return
     */
    public Page retrievePage()

    /**
     * Delivers the {@link Browser} for this csiValue. Can be null.
     * @return
     */
    public Browser retrieveBrowser()
}
