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

import java.text.DecimalFormat
import static de.iteratec.osm.util.Constants.*

/**
 * Provides functionality for internet connectivites of performance measurements.
 */
class ConnectivityProfileService {

    static transactional = false

    /**
     * Builds the default name for a custom connectivity defined by params bwDown, bwUp and latency.
     * Fourth param, Packet loss rate, is assumed to be zero.
     * @param bwDown Bandwidth download.
     * @param bwUp Bandwidth upload.
     * @param latency Additional first hop latency.
     * @return Default name for a connectivity defined by params.
     */
    String getCustomConnectivityNameFor(Integer bwDown, Integer bwUp, Integer latency) {

        validateConnectivityAttributes(bwDown, bwUp, latency)

        return getCustomConnectivityNameFor(bwDown, bwUp, latency, "")

    }
    /**
     * Builds the default name for a custom connectivity defined by params bwDown, bwUp, latency and plr.
     * @param bwDown Bandwidth download.
     * @param bwUp Bandwidth upload.
     * @param latency Additional first hop latency.
     * @param plr Packet loss rate.
     * @return Default name for a connectivity defined by params.
     */
    String getCustomConnectivityNameFor(Integer bwDown, Integer bwUp, Integer latency, Integer plr) {

        validateConnectivityAttributes(bwDown, bwUp, latency, plr)

        DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT_PATTERN)
        String plrPart = plr ? ", ${decimalFormat.format(plr)}% PLR" : ""
        return "Custom (${decimalFormat.format(bwDown)}/${decimalFormat.format(bwUp)} Kbps, ${decimalFormat.format(latency)}ms Latency${plrPart})"

    }

    void validateConnectivityAttributes(Integer bwDown, Integer bwUp, Integer latency, Integer plr){
        if (bwDown == null) throw new IllegalArgumentException("bwDown may not be null")
        if (bwDown < ConnectivityProfile.BANDWIDTH_DOWN_MIN) throw new IllegalArgumentException("bwDown may not be less than ${ConnectivityProfile.BANDWIDTH_DOWN_MIN}")
        if (bwUp == null) throw new IllegalArgumentException("bwUp may not be null")
        if (bwUp < ConnectivityProfile.BANDWIDTH_UP_MIN) throw new IllegalArgumentException("bwUp may not be less than ${ConnectivityProfile.BANDWIDTH_UP_MIN}")
        if (latency == null) throw new IllegalArgumentException("latency may not be null")
        if (latency < ConnectivityProfile.LATENCY_MIN) throw new IllegalArgumentException("latency may not be less than ${ConnectivityProfile.LATENCY_MIN}")
        if (plr == null) throw new IllegalArgumentException("plr may not be null")
        if (plr < ConnectivityProfile.PLR_MIN) throw new IllegalArgumentException("plr may not be less than ${ConnectivityProfile.PLR_MIN}")
    }
}
