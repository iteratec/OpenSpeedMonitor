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

package de.iteratec.osm.report.chart

import groovy.transform.EqualsAndHashCode
import org.joda.time.DateTime


/**
 * <p>
 * A point of a measured value to be shown in a Highchart graph.
 * </p>
 * 
 * <p>
 * This point has an x- and y-coordinate. The x-coordinate is the
 * {@linkplain #time time} when the value was measured. The y-coordinate is the
 * {@linkplain #measuredValue measured value}. Additionally this value may have
 * an {@linkplain #sourceURL source URL} where details of this value might be
 * found (reachable through a click in the graph).
 * </p>
 * 
 * <p>
 * Instances of this class are unmodifiable.
 * </p>
 * 
 * @author mze
 * @since IT-78
 */
@EqualsAndHashCode
class OsmChartPoint{

	/**
	 * <p>
	 * The count of aggregated values to receive {@link #measuredValue}. 
	 * </p>
	 * 
	 * <p>
	 * This is an informative value that could be used in the Highchart-points
	 * hover info dialog to inform the user about values background
	 * </p>
	 */
	int countOfAggregatedResults

	/**
	 * <p>
	 * The measured value.
	 * </p>
	 * 
	 * <p>
	 * This value is intended to be the y-coordinate of a Highchart graph. OsmChartPoints without this value are invalid.
	 * </p>
	 * 
	 * @see #countOfAggregatedResults
	 */
	double measuredValue

	/**
	 * <p>
	 * An {@link URL} which represents the source of the {@link #measuredValue}.
	 * This URL is optional. Details of the value may be available from this
	 * URL.
	 * </p>
	 * 
	 * <p>
	 * This value might be <code>null</code> to indicate that no further details
	 * are available.
	 * </p>
	 * 
	 * @see #hasAnSourceURL()
	 */
	URL sourceURL

	/**
	 * <p>
	 * The time this value was measured or an aggregated value takes place (gets
	 * relevant) represented as milliseconds since Epoch. Only OsmChartPoints with time greater or equal 0 are valid.
	 * </p>
	 * 
	 * <p>
	 * This value is intended to be the x-coordinate of a Highchart graph.
	 * </p>
	 * 
	 * @see #measuredValue
	 * @see Date#getTime()
	 */
	long time
	
	/**
	 * Measuring machine name (can contain ip adress) 
	 */
	String testingAgent

	/**
	 * <p>
	 * Has this point a source {@link URL} with more details about its value?
	 * </p>
	 *
	 * <p>
	 * This is the recommend way to check for an URL. Your schould use this
	 * method instead of comaparing {@link #sourceURL} with <code>null</code>.
	 * </p>
	 *
	 * @return <code>true</code> if it has a source URL, <code>false</code>
	 *         else.
	 */
	public boolean hasAnSourceURL() {
		return this.sourceURL != null
	}

	/**
	 * Only points with a {@link #time} greater 0 and a {@link #measuredValue} not null are valid. 
	 * @return <code>true</code> if {@link #time} is greater than 0 and {@link #measuredValue} is not null.
	 */
    public boolean isValid(){
        return (this.time > 0 && this.measuredValue != null)
    }

	public String toString(){
		return "${new DateTime(time)} | ${measuredValue}"
	}
}
