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


/**
 * Connection information for job runs.
 *
 * @author dri
 */
class ConnectivityProfile {

    ConnectivityProfileService connectivityProfileService

    public static final int BANDWIDTH_DOWN_MIN = 0
    public static final int BANDWIDTH_UP_MIN = 0
    public static final int LATENCY_MIN = 0
    public static final int PLR_MIN = 0

    String name
    Integer bandwidthDown
    Integer bandwidthUp
    Integer latency
    Integer packetLoss

    static constraints = {
        name(blank: false, maxSize: 255)
        bandwidthDown(min: BANDWIDTH_DOWN_MIN)
        bandwidthUp(min: BANDWIDTH_UP_MIN)
        latency(min: LATENCY_MIN)
        packetLoss(min: PLR_MIN)
    }

    static transients = ['connectivityProfileService']

    public String toString() {
        return "${name}: ${connectivityProfileService.getConnectivitySpecificationFor(bandwidthDown, bandwidthUp, latency, packetLoss)}"
    }
}
