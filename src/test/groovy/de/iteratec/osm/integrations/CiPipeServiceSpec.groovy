package de.iteratec.osm.integrations

import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

@Build([Job, WebPageTestServer, Location])
@Unroll
class CiPipeServiceSpec extends Specification implements BuildDataTest, ServiceUnitTest<CiPipeService> {

    void "OsmCiPipeCheck script gets parametrized correctly for job #jobLabel (id=#jobId) and baseUrl #baseUrlWptServer"() {
        given: "Job with id #jobId associated to WebPageTestServer with baseUrl #baseUrlWptServer"
        Job job = Job.build(id: jobId, label: jobLabel,
            location: Location.build(wptServer: WebPageTestServer.build(baseUrl: baseUrlWptServer)))

        when:"CiIntegrationScript gets parametrized with that Jobs data"
        String ciScript = service.getCiIntegrationScriptFor(job)

        then: "Resulting script will run correct Job (id=#jobId) on correct OpenSpeedMonitor instance (baseUrl=#baseUrlWptServer)"
        //(?m) makes the regex multiline - allows you to use beginning (^) and end ($) of string operators
        ciScript ==~ /(?ms).*new\sPerformanceCheck\(${jobId}\).*/
        //(?s) - dotall flag - makes the regex match newlines with . (dot) operators
        ciScript ==~ /(?ms).*request\.uri\s\=\s\'${baseUrlWptServer}\'.*/

        where:
        jobId    | jobLabel  | baseUrlWptServer
        23       | 'job23'   | 'https://my.osm.instance1.org/'
        42       | 'job42'   | 'https://my.osm.instance2.org/'
    }
}
