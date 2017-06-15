package de.iteratec.osm

import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import grails.transaction.Transactional

@Transactional
class OsmStateService {

    public boolean untouched() {
        return WebPageTestServer.count() == 0 && Job.count() == 0
    }
}
