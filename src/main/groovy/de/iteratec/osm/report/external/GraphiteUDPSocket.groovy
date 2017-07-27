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

package de.iteratec.osm.report.external

import com.codahale.metrics.graphite.GraphiteUDP
import de.iteratec.osm.result.Contract

/**
 * <p>
 * A graphite socket using UDP to send data to Graphite.
 * </p>
 *
 */
class GraphiteUDPSocket implements GraphiteSocket {

    private final InetAddress serverAddress
    private final int port

    /**
     * <p>
     * Creates a TCP-Socket based Graphite socket.
     * </p>
     *
     * @param serverAddress The server adress to connect to, not <code>null</code>.
     * @param port The port to use for communication; must satisfy
     * {@code 0 <= port <= 65535}.
     * @throws NullPointerException if {@code serverAddress} is <code>null</code>.
     * @throws {@link IllegalArgumentException} if {@code port} is less than 0
     *                              or greater than 65353.
     */
    GraphiteUDPSocket(InetAddress serverAddress, int port)
            throws NullPointerException, IllegalArgumentException {
        Contract.requiresArgumentNotNull("serverAddress", serverAddress)
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("The port number must be between 0 and 65535" + " (both inclusive).")
        }

        this.serverAddress = serverAddress
        this.port = port
    }

    @Override
    void sendDate(GraphitePathName path, double value, Date timestamp)
            throws NullPointerException, GraphiteComunicationFailureException {

        GraphiteUDP graphiteSocket
        try {
            graphiteSocket = new GraphiteUDP(serverAddress.getHostAddress(), this.port)
            graphiteSocket.connect()
            // use seconds
            long metricTimestamp = timestamp.getTime() / 1000
            graphiteSocket.send(path.toString(), String.valueOf(value), metricTimestamp)
        } catch (IOException cause) {
            throw new GraphiteComunicationFailureException(serverAddress, port, cause)
        } finally {
            if (graphiteSocket) {
                graphiteSocket.close()
            }
        }
    }
}
