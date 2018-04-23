package de.iteratec.osm.csi

import de.iteratec.osm.measurement.schedule.JobGroup
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(JobGroupWeight)
@Build([JobGroup])
@Mock([JobGroup])
class JobGroupWeightSpec extends Specification implements BuildDataTest {

    void "test jobGroupWeight has to have a jobGroup with csiConfiguration"() {
        when: "creating a jobGroupWeight with a jobGroup without a csiConfiguration"
        JobGroupWeight jobGroupWeight = new JobGroupWeight(jobGroup: JobGroup.build(), weight: 12.0)

        then: "the jobGroupWeight does not validate"
        !jobGroupWeight.validate()
    }
}
