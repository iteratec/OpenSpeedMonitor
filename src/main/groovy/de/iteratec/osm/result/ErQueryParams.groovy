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
 * Assembles params which are used to query {@link EventResults}s via tag and further params.
 * </p>
 *
 * @author nkuhn
 * @see MvQueryParams
 */
class ErQueryParams extends MvQueryParams {
	/**
	 * For querying of {@link de.iteratec.osm.result.EventResult}s. Results with load-time-values greater than this get excluded.
	 * @see MeasurandGroup
	 */
	Integer maxLoadTimeInMillisecs
	/**
	 * For querying of {@link de.iteratec.osm.result.EventResult}s. Results with load-time-values smaller than this get excluded.
	 * @see MeasurandGroup
	 */
	Integer minLoadTimeInMillisecs
	/**
	 * For querying of {@link de.iteratec.osm.result.EventResult}s. Results with request-count-values greater than this get excluded.
	 * @see MeasurandGroup
	 */
	Integer maxRequestCount
	/**
	 * For querying of {@link de.iteratec.osm.result.EventResult}s. Results with request-count-values smaller than this get excluded.
	 * @see MeasurandGroup
	 */
	Integer minRequestCount
	/**
	 * For querying of {@link de.iteratec.osm.result.EventResult}s. Results with request-size-values greater than this get excluded.
	 * @see MeasurandGroup
	 */
	Integer maxRequestSizeInBytes
	/**
	 * For querying of {@link de.iteratec.osm.result.EventResult}s. Results with request-size-values smaller than this get excluded.
	 * @see MeasurandGroup
	 */
	Integer minRequestSizeInBytes
    /**
     * Whether or not custom connectivites should be included by regex.
      */
    Boolean includeCustomConnectivity
    /**
     * For querying of {@link de.iteratec.osm.result.EventResult}s.
     * Results which attribute customConnectivityName matches this regex get included.
     */
    String customConnectivityNameRegex
    /**
     * For querying of {@link de.iteratec.osm.result.EventResult}s.
     * If true, Results of which customConnectivityName equals {@link ConnectivityProfileService#CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE}
     * get included. Otherwise respective results get excluded.
     */
    Boolean includeNativeConnectivity = false
}
