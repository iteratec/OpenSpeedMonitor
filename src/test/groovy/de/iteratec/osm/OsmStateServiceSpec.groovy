package de.iteratec.osm

import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(OsmStateService)
@Mock([WebPageTestServer, Job, Script, Browser, Location, JobGroup])
class OsmStateServiceSpec extends Specification {

    private static final String BASE_URL_PUBLIC_WEBPAGETEST_SERVER = "https://www.webpagetest.org/"

    def setup() {
    }

    def cleanup() {
        WebPageTestServer.list().each {WebPageTestServer server ->
            if (!server.baseUrl.equals(BASE_URL_PUBLIC_WEBPAGETEST_SERVER)) server.delete()
        }
        Job.list().each {Job job ->
            job.delete()
        }
    }

    void "isFirstRun method returns true with initial state data"() {
        when: "no server and no job exist"
        then:"osm state is firstrun"
            service.untouched() == true
    }

    void "isFirstRun method returns false with more than one wpt servers"() {
        when: "two wpt servers exist"
        createWptServer();
        TestDataUtil.createWebPageTestServer(
            "url.of.wptserver.org",
            "url.of.wptserver.org",
            true,
            "https://url.of.wptserver.org/"
        )
        then:"osm state is NOT firstrun"
        service.untouched() == false
    }

    void "isFirstRun method returns false with existing measurement jobs"() {
        when: "two wpt servers exist"
        createWptServer();
        createMeasurementJob()
        then:"osm state is NOT firstrun"
        service.untouched() == false
    }


    private void createWptServer(){
        TestDataUtil.createWebPageTestServer(
            "www.webpagetest.org",
            "www.webpagetest.org",
            true,
            "https://www.webpagetest.org/"
        )
    }
    private createMeasurementJob(){
        TestDataUtil.createJob(
            "job", TestDataUtil.createScript(), TestDataUtil.createLocation(), TestDataUtil.createJobGroup("jobgroup"), "job description", 1, true, 60
        )
    }
}
