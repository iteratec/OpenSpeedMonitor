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
import de.iteratec.osm.csi.CsiDay
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
/**
 * Test-suite for {@link de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService}.
 */
@TestFor(DefaultJobGroupDaoService)
@Mock([JobGroup, CsiConfiguration, CsiDay])
@Build([JobGroup, CsiConfiguration])
class DefaultJobGroupDaoServiceTests extends Specification{

    void "testFindCSIGroups"() {
        given: "3 existing JobGroups, 2 csi, 1 non csi"
        JobGroup csiGroup1 = JobGroup.build(csiConfiguration: CsiConfiguration.build())
        JobGroup csiGroup2 = JobGroup.build(csiConfiguration: CsiConfiguration.build())
        JobGroup nonCsiGroup = JobGroup.build()

        when: "findCSIGroups() method is called"
        Set<JobGroup> foundCsiGroups = service.findCSIGroups()

        then: "it returns the 2 csi JobGroups"
        foundCsiGroups.size() == 2
        foundCsiGroups.contains(csiGroup1)
        foundCsiGroups.contains(csiGroup2)
        ! foundCsiGroups.contains(nonCsiGroup)
    }

    void "testFindAll"() {
        given: "2 arbitrary JobGroups"
        JobGroup jobGroup1 = JobGroup.build()
        JobGroup jobGroup2 = JobGroup.build()

        when: "findAll() method is called"
        Set<JobGroup> jobGroups = service.findAll()

        then: "it provides the 2 groups"
        jobGroups.size() == 2
        jobGroups.contains(jobGroup1)
        jobGroups.contains(jobGroup2)

        when: "another JobGroup gets persisted"
        JobGroup jobGroup3 = JobGroup.build()
        Set<JobGroup> jobGroupsAfterAdding = service.findAll();

        then: "findAll() method provides all 3 JobGroups afterwards"
        jobGroupsAfterAdding.size() == 3
        jobGroupsAfterAdding.contains(jobGroup1)
        jobGroupsAfterAdding.contains(jobGroup2)
        jobGroupsAfterAdding.contains(jobGroup3)
    }
}
