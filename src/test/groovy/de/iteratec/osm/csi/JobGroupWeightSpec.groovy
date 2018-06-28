package de.iteratec.osm.csi

import de.iteratec.osm.measurement.schedule.JobGroup
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import spock.lang.Specification

@Build([JobGroup])
class JobGroupWeightSpec extends Specification implements BuildDataTest {
    void setupSpec() {
        mockDomains(JobGroupWeight, JobGroup)
    }

    void "test jobGroupWeight has to have a jobGroup with csiConfiguration"() {
        when: "creating a jobGroupWeight with a jobGroup without a csiConfiguration"
        JobGroupWeight jobGroupWeight = new JobGroupWeight(jobGroup: JobGroup.build(), weight: 12.0)

        then: "the jobGroupWeight does not validate"
        !jobGroupWeight.validate()
    }
}
