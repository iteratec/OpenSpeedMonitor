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

package de.iteratec.osm.report.external;

import java.util.Date;

/**
 * <p>
 * A graphite socket realizes communication with a graphite server. It
 * represents the connection layer and is not domain specific.
 * </p>
 * 
 * <p>
 * TODO mze NOTE: To support multiple graphite servers one way would be to
 * provide a GraphiteSocketProvider instead of the socket itself for injection
 * that takes a server address or alias. Doing this provides two enhancements:
 * 1. You can support as many servers you like and 2. you can select the
 * implementation (UDP or TCP) based on server or configuration easily.
 * See: {@link de.iteratec.osm.report.external.provider.GraphiteSocketProvider}.
 * </p>
 * 
 * @author mze
 * @since 2013-11-06 / JIRA IT-195
 */
public interface GraphiteSocket {

	/**
	 * <p>
	 * Sends the specified data to Graphite.
	 * </p>
	 * 
	 * @param path
	 *            The path where the data-value is to be stored in, not
	 *            <code>null</code>.
	 * @param value
	 *            The data-value to send.
	 * @param timestamp
	 *            The time-stamp the data-value belongs to. Note: Only the
	 *            seconds (UNiX System V time-stamp) of {@link Date#getTime()}
	 *            are recognized.
	 * 
	 * @throws NullPointerException
	 *             if at least one arguement is <code>null</code>.
	 * @throws GraphiteComunicationFailureException
	 *             if a communication failure with the graphite server occured.
	 * 
	 */
	void sendDate(GraphitePathName path, double value, Date timestamp)
			throws NullPointerException, GraphiteComunicationFailureException;
}
