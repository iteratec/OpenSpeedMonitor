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

package de.iteratec.osm.csi

import de.iteratec.osm.util.I18nService
import grails.buildtestdata.BuildDataTest
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.buildtestdata.mixin.Build
import spock.lang.Specification
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.util.PerformanceLoggingService

@TestFor(CsiConfigurationController)
@Mock([CsiConfiguration, CsiDay, JobGroup])
@Build([CsiConfiguration, CsiDay, JobGroup])
class CsiConfigurationControllerSpec extends Specification implements BuildDataTest {

    def doWithSpring = {
        performanceLoggingService(PerformanceLoggingService)
    }

    void "saving a CSI configuration as copy results in one configuration more"() {
        given: "a CSI configuration"
        CsiConfiguration.build(label: "CSI Configuration")
        int numberOfConfigurationsBeforeCopy = CsiConfiguration.count

        when: "this CSI configuration gets saved as copy"
        params.sourceCsiConfigLabel = "CSI Configuration"
        params.label = "CSI Configuration Copy"
        controller.saveCopy()

        then: "there is exactly one CSI configuration more"
        CsiConfiguration.count == numberOfConfigurationsBeforeCopy + 1
    }

    void "copied CSI configuration has the same attributes as the original one"() {
        given: "a CSI configuration"
        CsiDay csiDay = CsiDay.build()
        CsiConfiguration.build(
                label: "CSI Configuration",
                description: "Description of this CSI configuration",
                csiDay: csiDay
        )

        when: "this CSI configuration gets saved as copy"
        params.label = "CSI Configuration Copy"
        params.sourceCsiConfigLabel = "CSI Configuration"
        controller.saveCopy()

        then: "both configurations have the same attributes"
        CsiConfiguration copyOfCsiConfiguration = CsiConfiguration.findByLabel("CSI Configuration Copy")
        copyOfCsiConfiguration.label == "CSI Configuration Copy"
        copyOfCsiConfiguration.description == "Description of this CSI configuration"
        copyOfCsiConfiguration.csiDay.hour0Weight == csiDay.hour0Weight
        copyOfCsiConfiguration.csiDay.hour2Weight == csiDay.hour2Weight
        copyOfCsiConfiguration.csiDay.hour7Weight == csiDay.hour7Weight
        copyOfCsiConfiguration.csiDay.hour13Weight == csiDay.hour13Weight
        copyOfCsiConfiguration.csiDay.hour19Weight == csiDay.hour19Weight
    }

    void "after deleting a CSI configuration there is one less"() {
        given: "two CSI configurations"
        CsiConfiguration.build()
        CsiConfiguration.build(label: "CSI Configuration to delete")

        when: "one gets deleted"
        params.label = "CSI Configuration to delete"
        controller.deleteCsiConfiguration()

        then: "there is exactly one left"
        CsiConfiguration.count == 1
    }

    void "test deleteCsiConfiguration when jobGroup using this configuration"() {
        given: "two CSI configurations and one Job Group connected to one CSI configuration"
        CsiConfiguration.build()
        CsiConfiguration csiConfiguration = CsiConfiguration.build(label: "CSI Configuration to delete")
        JobGroup jobGroup = JobGroup.build(csiConfiguration: csiConfiguration)

        when: "the CSI configuration connected to the Job Group gets deleted"
        params.label = "CSI Configuration to delete"
        controller.deleteCsiConfiguration()

        then: "the CSI configuration attribute of the Job Group is null"
        CsiConfiguration.count == 1
        jobGroup.csiConfiguration == null
    }

    void "an exception is thrown when configuration does not exist"() {
        given: "two CSI configurations"
        CsiConfiguration.build(label: "CSI Configuration One")
        CsiConfiguration.build(label: "CSI Configuration Two")

        when: "one tries to delete one which doesn't exist"
        params.label = "CSI Configuration doesn't exist"
        controller.deleteCsiConfiguration()

        then: "an IllegalArgumentException gets thrown"
        thrown(IllegalArgumentException)
    }

    void "an exception is thrown if one tries to delete the last csiConfiguration"() {
        given: "exactly one CSI configuration"
        CsiConfiguration.build(label: "CSI Configuration to delete")

        when: "one tries to delete this configuration"
        params.label = "CSI Configuration to delete"
        controller.deleteCsiConfiguration()

        then: "an IllegalStateException gets thrown"
        thrown(IllegalStateException)
    }

    void "JSON response is empty when deletion of CSI configuration is allowed"() {
        given: "two CSI configurations"
        CsiConfiguration.build()
        CsiConfiguration.build()

        when: "validation to delete a configuration is called"
        controller.validateDeletion()

        then: "is the JSON response empty"
        response.json.errorMessages.isEmpty()
    }

    void "JSON response contains an error message when deletion of CSI configuration is not allowed"() {
        given: "only one CSI configuration"
        controller.i18nService = Mock(I18nService)
        CsiConfiguration.build()

        when: "validation to delete a configuration is called"
        controller.validateDeletion()
        List errorMessages = response.json.errorMessages as List

        then: "contains the JSON response one error message"
        errorMessages.size() == 1
    }
}
