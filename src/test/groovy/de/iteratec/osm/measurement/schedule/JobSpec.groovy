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

import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.script.Script
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import spock.lang.Specification

/**
 * These class contains tests for custom validators or any other logic of class {@link Job}.
 * Default gorm persistance isn't tested.
 */
@Build([Job, ConnectivityProfile])
class JobSpec extends Specification implements BuildDataTest {
    void setupSpec() {
        mockDomains(Job, Location, Script)
    }

    void "connectivityProfile: not custom or all set manually"() {
        when:
        Job predefinedNoCustom = Job.build(connectivityProfile: ConnectivityProfile.build(), customConnectivityProfile: false)
        Job notPredefinedNoCustom = Job.build(customConnectivityProfile: false)
        notPredefinedNoCustom.connectivityProfile = null
        Job predefinedCustom = Job.build(
                connectivityProfile: ConnectivityProfile.build(),
                customConnectivityProfile: true,
                customConnectivityName: 'custom...',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
        )
        Job notPredefinedCustom = Job.build(
                connectivityProfile: null,
                customConnectivityProfile: true,
                customConnectivityName: 'custom...',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
        )
        notPredefinedCustom.connectivityProfile = null

        then:
        predefinedNoCustom.validate() == true
        notPredefinedNoCustom.validate() == false
        predefinedCustom.validate() == true
        notPredefinedCustom.validate() == true
    }

    void "valid without predefined and custom, but noTrafficShapingAtAll"() {
        when:
        Job withNoTrafficShaping = Job.build(customConnectivityProfile: false, noTrafficShapingAtAll: true)
        withNoTrafficShaping.connectivityProfile = null
        Job withoutNoTrafficShaping = Job.build(customConnectivityProfile: false, noTrafficShapingAtAll: false)
        withoutNoTrafficShaping.connectivityProfile = null

        then:
        withoutNoTrafficShaping.validate() == false
        withNoTrafficShaping.validate() == true
    }

    void "customConnectivityName: custom profile needs a name"() {
        when:
        Job withoutName = Job.build(
                customConnectivityName: "test",
                customConnectivityProfile: true,
                connectivityProfile: null,
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
        )
        withoutName.customConnectivityName = null
        Job withName = Job.build(
                customConnectivityName: "test",
                customConnectivityProfile: true,
                connectivityProfile: null,
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
        )

        then:
        withName.validate() == true
        withoutName.validate() == false
    }

    void "execution schedule: must be valid if set"() {
        when:
        Job withoutSchedule = Job.build()
        withoutSchedule.executionSchedule = null
        Job validSchedule = Job.build()
        validSchedule.executionSchedule =   '0 */15 * * * ? 2015'
        Job invalidSchedule = Job.build()
        invalidSchedule.executionSchedule = '0 */15 * * * * ? 2015'

        then:
        withoutSchedule.validate() == true
        validSchedule.validate() == true
        invalidSchedule.validate() == false
    }

    void "active: may only be true if execution schedule is set"() {
        when:
        Job activeWithoutSchedule = Job.build(active: true, executionSchedule: '0 */15 * * * ? 2015')
        activeWithoutSchedule.executionSchedule = null
        Job activeWithSchedule = Job.build(active: true, executionSchedule: '0 */15 * * * ? 2015')

        then:
        activeWithoutSchedule.validate() == false
        activeWithSchedule.validate() == true
    }

}
