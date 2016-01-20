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

package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.report.external.GraphiteServer

@Deprecated
public enum JobGroupType {
    /**
     * Just results with this type get factored in calculation for customer satisfaction index.
     */
    CSI_AGGREGATION,
    /**
     * To group results of several (not csi relevant) jobs that belong together. For more convenient selection in osm-dashboards.
     */
            RAW_DATA_SELECTION,
}

/**
 * Groups {@link Job}s for different reasons.
 * @author nkuhn
 *
 */
class JobGroup {

    /**
     * The name for an undefined JobGroup, respectively CSI. Please use {@link #isUndefinedCsiJobGroup()}
     * to test if a page is undefined, respectively CSI.
     */
    public static final String UNDEFINED_CSI = 'undefined'

    String name

    JobGroupType groupType

    CsiConfiguration csiConfiguration

    /**
     * Graphite-Servers to which results of this JobGroup should be sent.
     */
    Collection<GraphiteServer> graphiteServers = []
    static hasMany = [graphiteServers: GraphiteServer]

    static constraints = {
        name(unique: true, maxSize: 255)
        groupType(maxSize: 255)
        graphiteServers()
        csiConfiguration(nullable: true)
    }

    @Override
    public String toString() {
        return name + ' (' + groupType + ')';
    }
    static transients = ['undefinedCsiJobGroup']
    /**
     * <p>
     * Tests weather this JobGroup is an undefined, not specified JobGroup, respectively CSI. This happens, if a result
     * comes in, which Job isn't dedicated to a JobGroup.
     * </p>
     *
     * @return <code>true</code> if and only if this browser is undefined as
     *         described above, <code>false</code> else.
     *
     */
    public boolean isUndefinedCsiJobGroup() {
        return this.name.equals(JobGroup.UNDEFINED_CSI)
    }
}
