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

import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdater
import de.iteratec.osm.measurement.environment.wptserverproxy.HttpRequestService
import de.iteratec.osm.report.chart.Event
import de.iteratec.osm.report.chart.EventDaoService
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

/**
 * GraphiteEventService
 * A service to fetch Events from GraphiteServers.
 */
class GraphiteEventService {

    EventDaoService eventDaoService
    BatchActivityService batchActivityService
	HttpRequestService httpRequestService
    CsiAggregationUtilService csiAggregationUtilService

    static final DateTimeFormatter GRAPHITE_RENDERING_ENGINES_DATETIME_FORMAT =  DateTimeFormat.forPattern("HH:mm_yyyyMMdd")

    static transactional = true

    /**
     * Iterates over all existing GraphiteServers. If they got any GraphiteEventSourcePath,
     * these will be used to fetch new Events from.
     * @param createBatchActivity defines if a BatchActivity should be created and updated
     * @param lastNMinutes Defines oldest Events that get fetched from EventSources.
     * @see de.iteratec.osm.report.external.GraphiteEventCollectorJob
     */
    public void fetchGraphiteEvents(boolean createBatchActivity, int lastNMinutes) {

        DateTime until = csiAggregationUtilService.getNowInUtc()
        DateTime from = until.minusMinutes(lastNMinutes)
        String untilFormatted = GRAPHITE_RENDERING_ENGINES_DATETIME_FORMAT.print(until)
        String fromFormatted = GRAPHITE_RENDERING_ENGINES_DATETIME_FORMAT.print(from)

        log.debug("Fetching of graphite events: Start -> from $fromFormatted to $untilFormatted.")

        def graphiteServers = GraphiteServer.list()
        BatchActivityUpdater activity = batchActivityService.getActiveBatchActivity(this.class,Activity.CREATE,"Fetch Graphite Events "+new Date().getTime(),1, createBatchActivity)
        activity.beginNewStage("Create Events", graphiteServers.size())
        graphiteServers.eachWithIndex {server,index ->
            server.graphiteEventSourcePaths.each {eventSourcePath->
                createEvents(eventSourcePath, server, fromFormatted, untilFormatted)
            }
            activity.addProgressToStage()
        }
        activity.done()
    }
    /**
     * Fetches the Events from the given GraphiteServer within the path.
     * @param eventSourcePath
     * @param server
     * @param from
     * @param until
     * @return List of fetched Events, they are already saved
     */
    private List<Event> createEvents(GraphiteEventSourcePath eventSourcePath, GraphiteServer server, String from, String until){
        log.debug("Fetching of graphite events: Create events for graphite server: $server, eventSourcePath: $eventSourcePath, from: $from, until: $until.")
        def events = []
        def json = getEventJSON(eventSourcePath.targetMetricName, server, from, until)
        log.debug("Fetching of graphite events: Fetched json from server: $json")
        json.each{
            String shortName = it.target
            it.datapoints.findAll{it[0]}.each{point ->
                long unixTimeStamp = Long.parseLong(point[1] as String) * 1000L
                events << eventDaoService.createEvent(
                        "${eventSourcePath.staticPrefix}${shortName}",
                        new DateTime(unixTimeStamp),
                        "Read from Graphite: ${shortName} [${eventSourcePath.targetMetricName}]",
                        false,
                        eventSourcePath.jobGroups)
            }
        }
        log.debug("${events.size()} events written: $events")
        return events
    }
    /**
     * Calls the path from a GraphiteServer and converts it into a JSONObject
     * @param eventSourceMetricName Name of the metric to fetch.
     * @param server GraphiteServer
     * @param from
     * @param until
     * @return
     */
    private Object getEventJSON(String eventSourceMetricName, GraphiteServer server, String from, String until){

        String webappUrl = createWebappUrlOf(server)
        LinkedHashMap<String, String> queryParams = ['format': 'json', 'from': from, 'until': until, 'target': eventSourceMetricName]

        return httpRequestService.getJsonResponse(webappUrl, server.webappPathToRenderingEngine, queryParams)
    }

    String createWebappUrlOf(GraphiteServer server){

        try {
            validateWebappDataOf(server)
        } catch (IllegalArgumentException iae) {
            log.error("No events could get retrieved from grahite server cause webapp isn't set correctly: $server")
        }

        return getWebappUrlOf(server)

    }

    String getWebappUrlOf(GraphiteServer server) {
        return server.webappProtocol.scheme() + getUrlOfWebappInclTrailingSlash(server)
    }

    String getUrlOfWebappInclTrailingSlash(server){
        return httpRequestService.addTrailingSlashIfMissing(
                httpRequestService.removeLeadingSlashIfExisting(server.webappUrl)
            )
    }

    void validateWebappDataOf(GraphiteServer server){
        if (server.webappProtocol == null) throw new IllegalArgumentException("webappProtocol not set on graphite server: ${server}")
        if (server.webappUrl == null) throw new IllegalArgumentException("webappUrl not set on graphite server: ${server}")
        if (server.webappPathToRenderingEngine == null) throw new IllegalArgumentException("webappPathToRenderingEngine not set on graphite server: ${server}")
    }

}
