package de.iteratec.osm.csi

import de.iteratec.osm.measurement.schedule.JobGroup
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(JobGroupWeight)
@Mock([JobGroup])
class JobGroupWeightSpec extends Specification {

    JobGroup jobGroup

    void "setup" () {
        jobGroup = new JobGroup(name: "jobGroup")
    }

    void "test jobGroupWeight has to have a jobGroup with csiConfiguration"() {
        when: "creating a jobGroupWeight with a jobGroup without a csiConfiguration"
        JobGroupWeight jobGroupWeight = new JobGroupWeight(jobGroup: jobGroup, weight: 12.0)

        then: "the jobGroupWeight does not validate"
        !jobGroupWeight.validate()
    }
}
