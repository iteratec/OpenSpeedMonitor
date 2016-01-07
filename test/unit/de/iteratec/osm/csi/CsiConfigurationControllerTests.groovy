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

import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(CsiConfigurationController)
@Mock([CsiConfiguration, Day ,HourOfDay, JobGroup])
class CsiConfigurationControllerTests extends Specification{

    CsiConfiguration config1
    CsiConfiguration config2

    void setup() {
        Day testDay = new Day(name:"testDay")
        (1..24).each{
            testDay.addToHoursOfDay(new HourOfDay(fullHour: it, weight: it))
        }
        Day testDay2 = new Day(name:"testDay2")
        (1..24).each{
            testDay2.addToHoursOfDay(new HourOfDay(fullHour: (24-it), weight: it))
        }
        config1 = new CsiConfiguration(label: "config1", day: testDay)
        config2 = new CsiConfiguration(label: "config2", day: testDay2)
        config1.save(failOnError: true)
        config2.save(failOnError: true)
    }

    void "test saveCopy" () {
        given:
        String labelOfCopy = "ConfigCopy"
        int configCountBeforeCopy = CsiConfiguration.count

        when:
        params.label = labelOfCopy
        params.sourceCsiConfigLabel = "config1"

        controller.saveCopy()
        CsiConfiguration copy = CsiConfiguration.findByLabel(labelOfCopy)

        then:
        CsiConfiguration.count == configCountBeforeCopy + 1
        CsiConfiguration.findAllByLabel(labelOfCopy).size() == 1
        copy.label == labelOfCopy
        copy.label != config1.label
    }

    void "test deleteCsiConfiguration" () {
        given:
        int csiConfigurationCountBeforeDeleting = CsiConfiguration.count

        when:
        params.label = config1.label
        controller.deleteCsiConfiguration()

        then:
        CsiConfiguration.count == csiConfigurationCountBeforeDeleting - 1
    }

    void "test deleteCsiConfiguration when jobGroup using this configuration" () {
        given:
        int csiConfigurationCountBeforeDeleting = CsiConfiguration.count
        JobGroup jobGroup = new JobGroup(name: "jobGroup", groupType: JobGroupType.CSI_AGGREGATION, csiConfiguration: config1)
        jobGroup.save()

        when:
        params.label = config1.label
        controller.deleteCsiConfiguration()

        then:
        CsiConfiguration.count == csiConfigurationCountBeforeDeleting - 1
        jobGroup.csiConfiguration == null
    }

    void "test exception is thrown when configuration not exists" () {
        when:
        params.label = "doesNotExist"
        controller.deleteCsiConfiguration()

        then:
        thrown(IllegalArgumentException)
    }

    void "test exception is thrown if an attempt is made to delete the last csiConfiguration" () {
        given:
        config2.delete()

        when:
        params.label = config1.label
        controller.deleteCsiConfiguration()

        then:
        thrown(IllegalStateException)
    }


}
