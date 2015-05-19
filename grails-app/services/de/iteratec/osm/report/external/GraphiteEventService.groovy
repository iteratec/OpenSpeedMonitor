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
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.report.chart.Event
import de.iteratec.osm.report.chart.EventDaoService
import groovy.json.JsonSlurper
import org.joda.time.DateTime

/**
 * GraphiteEventService
 * A service to fetch Events from GraphiteServers
 */
class GraphiteEventService {

    EventDaoService eventDaoService
    BatchActivityService batchActivityService
    JsonSlurper slurper = new JsonSlurper()

    static transactional = true

    /**
     * Iterates over all existing GraphiteServer, if they got any GraphiteEventSourcePath,
     * they will be used to fetch new Events
     * @param createBatchActivity defines if a BatchActivity should be created and updated
     */
    public void fetchGraphiteEvents(boolean createBatchActivity) {
        def list = GraphiteServer.list()
        int size = list.size()
        BatchActivity activity = batchActivityService.getActiveBatchActivity(this.class,new Date().getTime(),Activity.CREATE,"Fetch Graphite Events",createBatchActivity)
        list.eachWithIndex {server,index ->
            activity.updateStatus(["progress": batchActivityService.calculateProgress(size,index+1), "stage": "Delete JobResults"])
            server.graphiteEventSourcePaths.each {
                createEvents(it, server)
            }
        }
    }
    /**
     * Fetches the Events from the given GraphiteServer within the path.
     * @param eventSourcePath
     * @param server
     * @return List of fetched Events, they are already saved
     */
    private List<Event> createEvents(GraphiteEventSourcePath eventSourcePath, GraphiteServer server){
        def events = []
        def json = getEventJSON(eventSourcePath.path, server)
        json.each{
            String shortName = it.target
            it.datapoints.each{point ->
                long time = Long.parseLong(point[1] as String)
                events << eventDaoService.createEvent(shortName,new DateTime(time),"",false,eventSourcePath.jobGroups)
            }
        }
        return events
    }
    /**
     * Calls the path from a GraphiteServer and converts it into a JSONObject
     * @param path path within the server
     * @param server GraphiteServer
     * @return
     */
    private Object getEventJSON(String path, GraphiteServer server){
        def url = server.serverAdress + path
        if(!url.startsWith("http")) url = "http://" +url
        url = url.toURL()
        return parseJSON(url.text)
    }

    /**
     * Converts a String into a JSON Object
     * @param text
     * @return
     */
    private Object parseJSON(String text){
        return slurper.parseText(text)
    }
}
