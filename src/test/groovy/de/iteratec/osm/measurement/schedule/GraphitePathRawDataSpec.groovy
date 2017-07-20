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

package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.report.external.GraphitePathRawData
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(GraphitePathRawData)
@Mock([GraphitePathRawData])
@Build([GraphitePathRawData])
class GraphitePathRawDataSpec extends Specification {

    void "alphanumerical prefixes with trailing dot are valid"(String prefix) {
        given: "a valid built GraphitePath"
        GraphitePathRawData path = GraphitePathRawData.build()

        when: "an invalid prefix is set"
        path.prefix = prefix

        then: "it's not valid anymore"
        path.validate() == true

        where:
        prefix                    | _
        "wpt."                    | _
        "wpt.server."             | _
        "wpt.server.server."      | _
        "wpt.server.server.wpt."  | _
    }

    void "prefixes which are not alphanumerical or without trailing dot are invalid"(String prefix) {
        given: "a valid built GraphitePath"
        GraphitePathRawData path = GraphitePathRawData.build()

        when: "an invalid prefix is set"
        path.prefix = prefix

        then: "it's not valid anymore"
        path.validate() == false

        where:
        prefix                    | _
        ""                        | _
        "wpt.."                   | _
        "wpt.testdt"              | _
        "wpt"                     | _
        "wpt*"                    | _
        ".wpt."                   | _
        ".wpt"                    | _
        "wpt.server.server.wpt.." | _
        "wpt.server.server.wpt"   | _
    }

}
