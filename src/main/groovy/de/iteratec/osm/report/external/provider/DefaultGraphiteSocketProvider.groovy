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

package de.iteratec.osm.report.external.provider

import de.iteratec.osm.report.external.GraphiteServer
import de.iteratec.osm.report.external.GraphiteSocket
import de.iteratec.osm.report.external.GraphiteTCPSocket
import de.iteratec.osm.report.external.GraphiteUDPSocket

class DefaultGraphiteSocketProvider implements GraphiteSocketProvider {
    DefaultGraphiteSocketProvider() {

    }

    @Override
    GraphiteSocket getSocket(GraphiteServer server) {
        return getSocket(server, server.reportProtocol)
    }

    @Override
    GraphiteSocket getSocket(GraphiteServer server, GraphiteSocketProvider.Protocol protocol) {
        if (protocol == GraphiteSocketProvider.Protocol.TCP) {
            return new GraphiteTCPSocket(server.getServerInetAddress(), server.getPort());
        } else if (protocol == GraphiteSocketProvider.Protocol.UDP) {
            return new GraphiteUDPSocket(server.getServerInetAddress(), server.getPort())
        } else {
            throw new IllegalArgumentException("Unknown Protocol" + protocol);
        }
    }
}
