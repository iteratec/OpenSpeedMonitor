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
import spock.lang.Specification
import grails.test.mixin.*
import grails.test.mixin.support.*

/**
 * These class contains tests for custom validators or any other logic of class {@link Job}.
 * Default gorm persistance isn't tested.
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([Job, Location, Script])
class JobSpec extends Specification {

    void "connectivityProfile: not custom or all set manually"() {
        when:
        Job predefinedNoCustom = getMostSimpleValidJob()
        Job notPredefinedNoCustom = getMostSimpleValidJob()
        notPredefinedNoCustom.connectivityProfile = null
        Job predefinedCustom = getMostSimpleValidJob()
        predefinedCustom.with {
            customConnectivityProfile = true
            customConnectivityName = 'custom...'
            bandwidthDown = 6000
            bandwidthUp = 512
            latency = 50
            packetLoss = 0
        }
        Job notPredefinedCustom = getMostSimpleValidJob()
        notPredefinedCustom.with {
            connectivityProfile = null
            customConnectivityProfile = true
            customConnectivityName = 'custom...'
            bandwidthDown = 6000
            bandwidthUp = 512
            latency = 50
            packetLoss = 0
        }

        then:
        predefinedNoCustom.validate() == true
        notPredefinedNoCustom.validate() == false
        predefinedCustom.validate() == true
        notPredefinedCustom.validate() == true
    }

    void "customConnectivityName: predefined or name is set"() {
        when:
        Job predefinedNoCustomName = getMostSimpleValidJob()
        Job notPredefinedNoCustomName = getMostSimpleValidJob()
        notPredefinedNoCustomName.connectivityProfile = null
        Job predefinedCustomName = getMostSimpleValidJob()
        predefinedCustomName.with {
            customConnectivityProfile = true
            customConnectivityName = 'custom...'
            bandwidthDown = 6000
            bandwidthUp = 512
            latency = 50
            packetLoss = 0
        }
        Job notPredefinedCustomName = getMostSimpleValidJob()
        notPredefinedCustomName.with {
            connectivityProfile = null
            customConnectivityProfile = true
            customConnectivityName = 'custom...'
            bandwidthDown = 6000
            bandwidthUp = 512
            latency = 50
            packetLoss = 0
        }

        then:
        predefinedNoCustomName.validate() == true
        notPredefinedNoCustomName.validate() == false
        predefinedCustomName.validate() == true
        notPredefinedCustomName.validate() == true
    }

    void "execution schedule: must be valid if set"() {
        when:
        Job withoutSchedule = getMostSimpleValidJob()
        Job validSchedule = getMostSimpleValidJob()
        validSchedule.executionSchedule = '0 */15 * * * ? 2015'
        Job invalidSchedule = getMostSimpleValidJob()
        invalidSchedule.executionSchedule = '0 */15 * * * * ? 2015'

        then:
        withoutSchedule.validate() == true
        validSchedule.validate() == true
        invalidSchedule.validate() == false
    }

    void "active: may only be true if execution schedule is set"() {
        when:
        Job activeWithoutSchedule = getMostSimpleValidJob()
        activeWithoutSchedule.active = true
        Job activeWithSchedule = getMostSimpleValidJob()
        activeWithSchedule.executionSchedule = '0 */15 * * * ? 2015'

        then:
        activeWithoutSchedule.validate() == false
        activeWithSchedule.validate() == true
    }

    private Job getMostSimpleValidJob() {
        return new Job(
                label: 'label',
                script: new Script(),
                location: new Location(),
                jobGroup: new JobGroup(),
                description: '',
                runs: 1,
                persistNonMedianResults: false,
                connectivityProfile: new ConnectivityProfile(),
                customConnectivityProfile: false,
                maxDownloadTimeInMinutes: 60,
                active: false,
                provideAuthenticateInformation: false,
        )
    }
}
