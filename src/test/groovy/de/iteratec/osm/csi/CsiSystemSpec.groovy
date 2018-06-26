package de.iteratec.osm.csi

import de.iteratec.osm.measurement.schedule.JobGroup
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import spock.lang.Specification

@Build([JobGroup, CsiConfiguration])
class CsiSystemSpec extends Specification implements BuildDataTest {

    void setupSpec() {
        mockDomains(CsiSystem, JobGroup, CsiConfiguration, CsiDay, JobGroupWeight)
    }

    void "test empty CsiSystem is invalid"() {
        when: "an empty CsiSystem is created"
            CsiSystem system = new CsiSystem(label: "csiSystem")

        then: "it does not validate"
            !system.validate()
    }

    void "test a valid csiSystem"() {
        when: "a valid CsiSystem is created"
            CsiConfiguration csiConfiguration = CsiConfiguration.build()
            CsiSystem system = new CsiSystem(label: "csiSystem")
            system.addToJobGroupWeights(jobGroup: JobGroup.build(csiConfiguration: csiConfiguration), weight: 5.5)
            system.addToJobGroupWeights(jobGroup: JobGroup.build(csiConfiguration: csiConfiguration), weight: 10.7)

        then: "it validates"
            system.validate()
    }

    void "test csiSystem contains only one jobGroupWeight then system is invalid"() {
        when: "a CsiSystem  with only one jobGroupWeight"
            CsiSystem system = new CsiSystem(label: "csiSytesm")
            system.addToJobGroupWeights(jobGroup: JobGroup.build(csiConfiguration: CsiConfiguration.build()), weight: 5.5)

        then: "it does not validate"
            !system.validate()
    }
}
