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

package de.iteratec.osm.report.external.provider;

import de.iteratec.osm.report.external.GraphiteServer;
import de.iteratec.osm.report.external.GraphiteSocket;
import de.iteratec.osm.report.external.GraphiteTCPSocket;


public class DefaultGraphiteSocketProvider implements GraphiteSocketProvider {
	public DefaultGraphiteSocketProvider() {
	
	}

	@Override
	public GraphiteSocket getSocket(GraphiteServer server) {
		return new GraphiteTCPSocket(server.getServerInetAddress(), server.getPort());
	}

	@Override
	public GraphiteSocket getSocket(GraphiteServer server, GraphiteSocketProvider.Protocol protocol) {
		if(protocol.equals(GraphiteSocketProvider.Protocol.TCP)) {
			return getSocket(server);
		} else {
			throw new IllegalArgumentException("The Protocol must be TCP");
		}
	}
}
