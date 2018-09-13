package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.result.JobResult
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

@Build([JobGroup, CsiConfiguration, JobResult, Job])
class JobGroupServiceSpec extends Specification implements BuildDataTest, ServiceUnitTest<JobGroupService> {
    void setupSpec() {
        mockDomains(JobGroup, CsiConfiguration, CsiDay)
    }

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
        !foundCsiGroups.contains(nonCsiGroup)
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
        Set<JobGroup> jobGroupsAfterAdding = service.findAll()

        then: "findAll() method provides all 3 JobGroups afterwards"
        jobGroupsAfterAdding.size() == 3
        jobGroupsAfterAdding.contains(jobGroup1)
        jobGroupsAfterAdding.contains(jobGroup2)
        jobGroupsAfterAdding.contains(jobGroup3)
    }
}

