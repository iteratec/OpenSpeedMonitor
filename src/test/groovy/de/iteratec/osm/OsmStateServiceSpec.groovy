package de.iteratec.osm

import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(OsmStateService)
@Mock([WebPageTestServer])
@Build([WebPageTestServer, Job])
class OsmStateServiceSpec extends Specification {

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
