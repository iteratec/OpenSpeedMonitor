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

import spock.lang.Specification


/**
 * <p>
 * Test-suite of {@link GraphitePathName}.
 * </p>
 *
 * @author mze
 * @since 2013-11-06 (currently no JIRA-Ticket, draft)
 */
class GraphitePathNameSpec extends Specification {

    void "a GraphitePathName constructed of multiple path elements should be correct"() {
        given:
        String path = "wpt.lhotse.daily.hp.csi"

        when: "a GraphitePath is created from multiple strings"
        GraphitePathName out = GraphitePathName.valueOf(path.split("\\."))

        then: "the toString() method should return the full path"
        out.toString() == path
    }

    void "test escaping of GraphitePathName.valueOf"() {
        when: "a GraphiteName is constructed of strings with characters that have to be escaped "
        GraphitePathName out = GraphitePathName.valueOf("wpt", "lhotse", "daily/update", "hp AB 123", "csi")

        then: "toString() should return the escaped result"
        out.toString() == "wpt.lhotse.daily-update.hp_AB_123.csi"
    }

    void "Equals and HashCode should work"() {
        when: "We create two equal GraphitePathNames and one different"
        GraphitePathName out = GraphitePathName.valueOf("wpt.lhotse.daily.hp.csi")
        GraphitePathName equalToOut = GraphitePathName.valueOf("wpt.lhotse.daily.hp.csi")
        GraphitePathName notEqualToOut = GraphitePathName.valueOf("wpt.lhotse.weekly.hp.csi")

        then: "the first two should be equal and share the same hashcode, but not with the third"
        out == equalToOut
        out.hashCode() == equalToOut.hashCode()

        out != notEqualToOut
    }

    void "GraphitePathName construction with a path"() {
        given: "two paths"
        String path1 = "wpt.lhotse.daily.hp.csi"
        String path2 = "wpt.lhotse.weekly.hp.csi"

        when: "two GraphiteNamePaths where created from two different strings"
        GraphitePathName out1 = GraphitePathName.valueOf(path1)
        GraphitePathName out2 = GraphitePathName.valueOf(path2)

        then: "each toString method from the paths should return the original path"
        out1.toString() == path1
        out2.toString() == path2
    }

    void "invalidPath: doubleDots"() {
        when: "one tries to create a GraphitePathName with a not supported path"
        GraphitePathName.valueOf("wpt.lhotse.daily.hp..csi")

        then: "one should face an IllegalArgumentException"
        thrown IllegalArgumentException
    }

    void "invalidPath: empty String"() {
        when: "one tries to create a GraphitePathName with an empty path"
        GraphitePathName.valueOf("")

        then: "one should face an IllegalArgumentException"
        thrown IllegalArgumentException
    }

    void "invalidPath: null path"() {
        when: "one tries to create a GraphitePathName with null as path"
        GraphitePathName.valueOf((String) null)

        then: "one should face an IllegalArgumentException"
        thrown NullPointerException
    }

    void "invalidPath: null array"() {
        when: "one tries to create a GraphitePathName with a null list"
        GraphitePathName.valueOf((String[]) null)

        then: "one should face an IllegalArgumentException"
        thrown NullPointerException
    }
}
