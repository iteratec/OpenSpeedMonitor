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
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Date;

import de.iteratec.osm.result.Contract;

/**
 * <p>
 * A graphite socket using TCP to send data to Graphite.
 * </p>
 * 
 * <p>
 * TODO Add a class GraphiteUDPSocket to add a UPD connection and select the
 * implementation to use on startup. See note in {@link GraphiteSocket}!
 * </p>
 * 
 * @author mze
 * @since 2013-11-06 / JIRA IT-195
 */
public class GraphiteTCPSocket implements GraphiteSocket {

	private static final byte CAN = 0x24;
	private static final byte LF = 0x0a;

	private final InetAddress serverAddress;
	private final int port;

	/**
	 * <p>
	 * Creates a TCP-Socket based Graphite socket.
	 * </p>
	 * 
	 * @param serverAddress
	 *            The server adress to connect to, not <code>null</code>.
	 * @param port
	 *            The port to use for communication; must satisfy
	 *            {@code 0 <= port <= 65535}.
	 * 
	 * @throws NullPointerException
	 *             if {@code serverAddress} is <code>null</code>.
	 * @throws {@link IllegalArgumentException} if {@code port} is less than 0
	 *         or greater than 65353.
	 */
	public GraphiteTCPSocket(InetAddress serverAddress, int port)
			throws NullPointerException, IllegalArgumentException {
		Contract.requiresArgumentNotNull("serverAddress", serverAddress);
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException(
					"The port number must be between 0 and 65535"
							+ " (both inclusive).");
		}

		this.serverAddress = serverAddress;
		this.port = port;
	}

	@Override
	public void sendDate(GraphitePathName path, double value, Date timestamp)
			throws NullPointerException, GraphiteComunicationFailureException {

		Socket graphiteSocket = null;
		try {
			graphiteSocket = new Socket(serverAddress, this.port);
			//if the server isn't reachable the try to report should stop after 10 seconds
			graphiteSocket.setSoTimeout(10000);

			OutputStream graphiteFeedStream = graphiteSocket.getOutputStream();

			// use seconds, UNiX system V
			long metricTimestamp = timestamp.getTime() / 1000;

			String messageToSendToGraphite = path.toString() + " "
					+ String.valueOf(value) + " "
					+ String.valueOf(metricTimestamp);

			byte[] messageToSendToGraphiteInUSASCII = messageToSendToGraphite
					.getBytes(Charset.forName("US-ASCII"));

			graphiteFeedStream.write(messageToSendToGraphiteInUSASCII);
			graphiteFeedStream.write(LF);
			graphiteFeedStream.write(CAN);

			graphiteFeedStream.flush();
			graphiteSocket.close();
		} catch (IOException cause) {
			throw new GraphiteComunicationFailureException(serverAddress, port,
					cause);
		} finally {
			if (graphiteSocket != null) {
				try {
					graphiteSocket.close();
				} catch (IOException ignored) {
					// Ignored, we've just tried it.
				}
			}
		}
	}

}
