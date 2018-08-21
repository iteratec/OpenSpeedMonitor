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

import de.iteratec.osm.measurement.environment.wptserver.Protocol
import de.iteratec.osm.report.external.provider.*
import grails.databinding.BindUsing
import grails.gorm.annotation.Entity

/**
 * A graphite server to which measured data is to be sent to.
 * The data to send is configured in {@link #graphitePaths}.
 * {@link de.iteratec.osm.report.chart.Event}s can be fetches fromm server via associated graphiteEventSourcePaths.
 * @see GraphitePathRawData
 * @see GraphitePathCsiData
 * @see GraphiteEventSourcePath
 */
@Entity
class GraphiteServer {

    /**
     * Hostname of this GraphiteServer's carbon component.
     * Graphite carbon component manages storage of metric data
     * (see http://graphite.wikidot.com/high-level-diagram). So this adress is used to send data
     * to graphite server.
     * @see InetAdress#getByName(String s)
     * @see MetricReportingService
     */
    String serverAdress
    /**
     * Port of this GraphiteServer's carbon component.
     * Graphite carbon component manages storage of metric data
     * (see http://graphite.wikidot.com/high-level-diagram). So this port is used to send data
     * to graphite server.
     * @see MetricReportingService
     */
    int port
    /**
     * Hostname of this GraphiteServer's webapp. This is used to read data from graphite server.
     * (see http://graphite.wikidot.com/high-level-diagram)
     * @see GraphiteEventService
     */
    String webappUrl
    /**
     * Port of this GraphiteServer's webapp. This is used to read data from graphite server.
     * (see http://graphite.wikidot.com/high-level-diagram)
     * @see GraphiteEventService
     */
    Protocol webappProtocol
    /**
     * Path to rendering engine of GraphiteServer's webapp.
     */
    String webappPathToRenderingEngine

    GraphiteSocketProvider.Protocol reportProtocol = GraphiteSocketProvider.Protocol.UDP

    /**
     * {@link GraphiteEventSourcePath}s, to create Events.
     */
    Collection<GraphiteEventSourcePath> graphiteEventSourcePaths = []
    static hasMany = [
        graphiteEventSourcePaths: GraphiteEventSourcePath,
        graphitePathsRawData: GraphitePathRawData,
        graphitePathsCsiData: GraphitePathCsiData
    ]
    static transients = ['serverInetAddress']

    Boolean reportCsiAggregationsToGraphiteServer = false
    Boolean reportEventResultsToGraphiteServer = true

    /**
     * Configuration for {@link HealthReportService} with standard values.
     */
    Boolean reportHealthMetrics = true
    String healthMetricsReportPrefix = "osm.healthmetrics"
    String garbageCollectorPrefix = "jvm.gc"
    String memoryReportPrefix = "jvm.mem"
    String threadStatesReportPrefix = "jvm.thread-states"
    String processCpuLoadPrefix = "cpu.processCpuLoad"
    int timeBetweenReportsInSeconds = 300


    public InetAddress getServerInetAddress() {
        return InetAddress.getByName(serverAdress)
    }

    static constraints = {
        serverAdress(unique: 'port', maxSize: 255)
        port(min: 0, max: 65535)
        webappUrl(nullable: false)
        webappProtocol(nullable: false)
        webappPathToRenderingEngine(nullable: false)
        reportHealthMetrics(nullable: false)
        timeBetweenReportsInSeconds(min: 0, max: 65535)
        healthMetricsReportPrefix(nullable: false, maxSize: 255)
        garbageCollectorPrefix(nullable: false, maxSize: 255)
        memoryReportPrefix(nullable: false, maxSize: 255)
        threadStatesReportPrefix(nullable: false, maxSize: 255)
        reportProtocol(nullable: false, inList: GraphiteSocketProvider.Protocol.values() as List)
        graphitePathsCsiData(nullable: true)
        graphitePathsRawData(nullable: true)
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append("instance object: ${super.toString()}\n")
        sb.append((serverAdress && port) ? "carbon: ${serverAdress}:${port}\n" : '')
        sb.append((webappProtocol && webappUrl && webappPathToRenderingEngine) ?
                "webapp's rendering engine: ${webappProtocol.scheme()}${webappUrl}${webappPathToRenderingEngine}\n" :
                '')
        return sb.toString()
    }
}
