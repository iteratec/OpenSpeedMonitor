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

import java.io.IOException;
import java.net.InetAddress;

/**
 * <p>
 * This exception indicates a failure on communication with a Graphite server.
 * Details might be found in {@linkplain #getCause() cause}.
 * </p>
 * 
 * @author mze
 * @since 2013-11-06 / JIRA IT-195
 */
public class GraphiteComunicationFailureException extends Exception {

	/**
	 * Required serial version UID.
	 */
	private static final long serialVersionUID = 2364690032165010639L;

	private final InetAddress serverAddress;
	private final int port;

	/**
	 * <p>
	 * Creates a new exception for a communication failure with the specified
	 * server, the specified port and takes the reason. None of the arguments
	 * can be empty.
	 * </p>
	 * 
	 * @param serverAddress
	 * @param port
	 * @param cause
	 * 
	 * @throws NullPointerException
	 *             if {@code serverAddress} is <code>null</code>.
	 */
	public GraphiteComunicationFailureException(InetAddress serverAddress,
			int port, IOException cause) throws NullPointerException {
		super("Failed to communicate with server " + serverAddress.toString()
				+ " on port " + port, cause);
		this.serverAddress = serverAddress;
		this.port = port;
	}

	/**
	 * <p>
	 * The {@link InetAddress} of the server tried to communicate with.
	 * </p>
	 * 
	 * @return not <code>null</code>.
	 */
	public InetAddress getServerAddress() {
		return serverAddress;
	}

	/**
	 * <p>
	 * The port used to try a communication.
	 * </p>
	 * 
	 * @return not <code>null</code>.
	 */
	public int getPort() {
		return port;
	}
}
