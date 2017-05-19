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

package de.iteratec.osm.measurement.environment

import de.iteratec.osm.measurement.environment.dao.LocationDaoService
import org.junit.Test;

import grails.test.mixin.*

import org.junit.*
import spock.lang.Specification

/**
 * Test-suite for {@link de.iteratec.osm.measurement.environment.dao.LocationDaoService}.
 */
@TestFor(DefaultLocationDaoService)
@Mock([Location])
class DefaultLocationDaoServiceTests extends Specification {

    public static final String nameLocation1 = 'location1'
    public static final String nameLocation2 = 'location2'
    public static final String nameLocation3 = 'location3'
    public static final String nameLocation4 = 'location4'

    LocationDaoService serviceUnderTest

    void "setup"() {
        serviceUnderTest = service
    }

    void "test findAll"() {
        given:
        new Location(location: 'agent1.agentserver.example.com', label: 'Agent1').save(failOnError: true, validate: false);
        new Location(location: 'agent2.agentserver.example.com', label: 'Agent2').save(failOnError: true, validate: false);

        when:
        Set<Location> result = serviceUnderTest.findAll()

        then:
        result != null
        result.size() == 2
        result.count({ it.label == 'Agent1' }) == 1
        result.count({ it.label == 'Agent2' }) == 1

        new Location(location: 'agent3.agentserver.example.com', label: 'Agent3').save(failOnError: true, validate: false);

        Set<Location> resultAfterAdding = serviceUnderTest.findAll()

        resultAfterAdding != null
        resultAfterAdding.size() == 3
        resultAfterAdding.count({ it.label == 'Agent1' }) == 1
        resultAfterAdding.count({ it.label == 'Agent2' }) == 1
        resultAfterAdding.count({ it.label == 'Agent3' }) == 1
    }
}
