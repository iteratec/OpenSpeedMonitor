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

import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.report.chart.Event
import de.iteratec.osm.report.chart.EventDaoService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(GraphiteEventService)
@Mock([GraphiteServer, BatchActivity, Event])
class GraphiteEventServiceSpec extends Specification{
    GraphiteEventService serviceUnderTest
    void "test parseJSON"() {
        given:
            serviceUnderTest = service
            serviceUnderTest.batchActivityService = new BatchActivityService()
            def amounts = [5,1]
            int amountOfEventSourcePaths = amounts.sum() as Integer
            createGraphiteServer(amounts)
            serviceUnderTest.metaClass.getEventJSON = {String path, GraphiteServer server->amountOfEventSourcePaths--; return []}
        when:
            serviceUnderTest.fetchGraphiteEvents(false)
        then:
        GraphiteServer.list().size() == amounts.size()
            amountOfEventSourcePaths == 0
    }

    void "test createdEvents"(){
        given:
            serviceUnderTest = service
            serviceUnderTest.batchActivityService = new BatchActivityService()
            serviceUnderTest.eventDaoService = new EventDaoService()
            def amounts = [2,3]
            int amountOfEventSourcePaths = amounts.sum() as Integer
            createGraphiteServer(amounts)
            serviceUnderTest.metaClass.getEventJSON = {String path, GraphiteServer server-> createJson()}
        when:
            serviceUnderTest.fetchGraphiteEvents(false)
        then:
            Event.list().size() == amountOfEventSourcePaths * 2
    }
    private void createGraphiteServer(List<Integer> amountsOfEventSourcePaths){
        amountsOfEventSourcePaths.each {
            new GraphiteServer(serverAdress: "",graphiteEventSourcePaths: createEventSourcePath(it),graphitePaths: []).save(flush: true)
        }
    }

    private Object createJson(){
        new JsonSlurper().parseText('[{"target": "Global-Resources", "datapoints": [[null, 1431324300], [null, 1431327300]]}]')
    }

    private List<GraphiteEventSourcePath> createEventSourcePath(int amount){
        def list = []
        (1..amount).each {
                list << new GraphiteEventSourcePath(path: "")
        }
        return list
    }
}
