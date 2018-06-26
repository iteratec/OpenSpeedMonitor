package de.iteratec.osm

import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.script.Script
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

@Build([WebPageTestServer, Job])
class OsmStateServiceSpec extends Specification implements BuildDataTest,
        ServiceUnitTest<OsmStateService> {
    void setupSpec() {
        mockDomains(WebPageTestServer, ConnectivityProfile, Script)
    }

    void "State is untouched with initial state data."() {
        when: "No server and no job exist."
        then:"OSM state is untouched."
        service.untouched() == true
    }

    void "State is not untouched with an existing wpt server."() {
        when: "A wpt server exists."
        WebPageTestServer.build()
        then:"OSM state is NOT untouched."
        service.untouched() == false
    }

    void "State is not untouched with an existing Job."() {
        when: "A measurement Job exists."
        Job.build()
        then:"OSM state is NOT untouched."
        service.untouched() == false
    }

}
