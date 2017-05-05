package de.iteratec.osm

import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import grails.transaction.Transactional

@Transactional
class OsmStateService {

    public boolean measurementInfrastructureWizardAborted = false

    public boolean untouched() {

        List<WebPageTestServer> wptServers = WebPageTestServer.list()
        int countJobs = Job.count()
        return wptServers.size() == 0 && countJobs == 0
    }
}
