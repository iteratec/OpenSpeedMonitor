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


/**
 * A graphite server to which measured data is to be sent to.
 * The data to send is configured in {@link #graphitePaths}.
 */
class GraphiteServer {

    /**
     * Hostname of this GraphiteServer.
     * @see InetAdress#getByName(String s)
     */
    String serverAdress;
    int port;

    /**
     * {@link GraphitePath}s, for which results should be sent for this server.
     */
    Collection<GraphitePath> graphitePaths = []
    /**
     * {@link GraphiteEventSourcePath}s, to create Events.
     */
    Collection<GraphiteEventSourcePath> graphiteEventSourcePaths = []
    static hasMany = [graphitePaths:GraphitePath, graphiteEventSourcePaths:GraphiteEventSourcePath]
    static transients = ['serverInetAddress']

    public InetAddress getServerInetAddress() {
        return InetAddress.getByName(serverAdress)
    }

    static constraints = {
        serverAdress(unique: 'port', maxSize: 255)
        port(min: 0, max: 65535)
        graphitePaths()
    }

    @Override
    public String toString(){
        return "${getServerAdress()}:${port}"
    }
}
