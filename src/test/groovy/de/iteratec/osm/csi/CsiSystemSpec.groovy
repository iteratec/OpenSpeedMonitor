package de.iteratec.osm.csi

import de.iteratec.osm.measurement.schedule.JobGroup
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(CsiSystem)
@Mock([JobGroup, CsiConfiguration, CsiDay, JobGroupWeight])
class CsiSystemSpec extends Specification {

    JobGroupWeight weight1
    JobGroupWeight weight2


    void "setup" () {
        CsiConfiguration csiConfiguration = new CsiConfiguration(label: "a csi configuration", csiDay: new CsiDay())
        JobGroup csiJobGroup1 = new JobGroup(name: "csiGroup 1", csiConfiguration: csiConfiguration)
        JobGroup csiJobGroup2 = new JobGroup(name: "csiGroup 2", csiConfiguration: csiConfiguration)
        weight1 = new JobGroupWeight(jobGroup: csiJobGroup1, weight: 5.5)
        weight2 = new JobGroupWeight(jobGroup: csiJobGroup2, weight: 10.7)
    }

    void "test empty CsiSystem is invalid"() {
        when:
        CsiSystem system = new CsiSystem(label: "csiSystem")

        then:
        !system.validate()
    }

    void "test a valid csiSystem"() {
        when:
        CsiSystem system = new CsiSystem(label: "csiSystem")
        system.addToJobGroupWeights(weight1)
        system.addToJobGroupWeights(weight2)

        then:
        system.validate()
    }

    void "test csiSystem contains only one jobGroupWeight then system is invalid"() {
        when:
        CsiSystem system = new CsiSystem(label: "csiSytesm")
        system.addToJobGroupWeights(weight1)

        then:
        !system.validate()
    }
}
