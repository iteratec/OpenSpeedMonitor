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

import de.iteratec.osm.csi.CsiConfiguration
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
/**
 * Test-suite of {@link JobGroup}.
 */
@TestFor(JobGroup)
@Build([JobGroup, CsiConfiguration])
@Mock([JobGroup, CsiConfiguration])
class JobGroupSpec extends Specification {

    void "if a CSI configuration is set in a JobGroup, it's id is printed in toString"() {
        when: "a JobGroup with CSI configuration exists"
        CsiConfiguration csiConfiguration = CsiConfiguration.build()
        JobGroup group = JobGroup.build(name: 'Test-Group-1', csiConfiguration: csiConfiguration)

        then: "it's toString method contains the CSI configuration's ID"
        group.toString() == 'Test-Group-1 (' + csiConfiguration.ident() + ')'
    }

    void "a JobGroup without CSI configuration simply returns it's name as toString"() {
        when: "a JobGroup without CSI configuration is created"
        JobGroup group = JobGroup.build(name: 'Test-Group-2', csiConfiguration: null)

        then: "it's toString method simply returns its name"
        group.toString() == 'Test-Group-2'
    }
}
