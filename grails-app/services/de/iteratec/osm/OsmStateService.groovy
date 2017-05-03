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

        /*boolean justWebpagetestOrgNoJobsAndNeverRunWizard = wptServers.size() == 1 &&
                wptServers[0].baseUrl.equals("https://www.webpagetest.org/") &&
                countJobs == 0 &&
                measurementInfrastructureWizardAborted == false

        return justWebpagetestOrgNoJobsAndNeverRunWizard*/
        return wptServers.size() == 0 && countJobs == 0;
    }
}
