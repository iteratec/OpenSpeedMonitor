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

package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.Page
import de.iteratec.osm.result.MeasuredEvent

/**
 * <p>
 * Default implementation of {@link MeasuredEventDaoService}.
 * </p>
 *
 * @author nkuhn
 * @author mze
 */
class DefaultMeasuredEventDaoService implements MeasuredEventDaoService {

    @Override
    public Set<MeasuredEvent> findAll() {
        Set<MeasuredEvent> result = Collections.checkedSet(new HashSet<MeasuredEvent>(), MeasuredEvent.class);
        result.addAll(MeasuredEvent.list());
        return Collections.unmodifiableSet(result);
    }

    @Override
    public MeasuredEvent tryToFindByName(String name) {
        return MeasuredEvent.findByName(name);
    }
}
