package de.iteratec.osm.system

import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(LocationHealthCheckDaoService)
@Mock([Location, WebPageTestServer, Browser, LocationHealthCheck])
class LocationHealthCheckDaoServiceSpec extends Specification {

    private static final DateTime NOW = new DateTime(DateTimeZone.UTC)
    private Location activeLocation1
    private Location activeLocation2
    private Location inactiveLocation

    def setup() {
        createSomeTestData()
    }

    def cleanup() {
    }

    void "single location: getLatestHealthChecksFor won't return a check if no check exists for that location"() {
        given: "no health check exists for the location"
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES), activeLocation2)
        when: "getLatestHealthChecksFor is called for the location"
        List<LocationHealthCheck> healthChecks = service.getLatestHealthChecksFor([activeLocation1])
        then: "no health ckeck is returned"
        healthChecks.size() == 0
    }
    void "single location: getLatestHealthChecksFor will return latest check if just one check exists"() {
        given: "just one health check exists for the location"
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES), activeLocation1)
        when: "getLatestHealthChecksFor is called for the location"
        List<LocationHealthCheck> healthChecks = service.getLatestHealthChecksFor([activeLocation1])
        then: "it returns the correct health ckeck"
        healthChecks.size() == 1
        healthChecks.findAll {it.location == activeLocation1}.size() == 1
    }
    void "single location: getLatestHealthChecksFor will return latest check if multiple checks exist"() {
        given: "multiple health checks exists for the location"
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES * 3), activeLocation1)
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES * 2), activeLocation1)
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES), activeLocation1)
        when: "getLatestHealthChecksFor is called for the location"
        List<LocationHealthCheck> healthChecks = service.getLatestHealthChecksFor([activeLocation1])
        then: "it returns the correct health ckeck"
        healthChecks.size() == 1
        healthChecks.findAll {
            it.location == activeLocation1 && it.date == NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES).toDate()
        }.size() == 1
    }

    void "multiple locations: getLatestHealthChecksFor won't return a check if no check exists for the locations"() {
        given: "no health check exists at all"
        //no health checks at all
        when: "getLatestHealthChecksFor is called for two locations"
        List<LocationHealthCheck> healthChecks = service.getLatestHealthChecksFor([activeLocation1, activeLocation2])
        then: "no health ckeck is returned"
        healthChecks.size() == 0
    }
    void "multiple locations: getLatestHealthChecksFor will return latest check if just one health check exist for each location"() {
        given: "health checks exist for both locations"
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES * 12), activeLocation1)
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES), activeLocation2)
        when: "getLatestHealthChecksFor is called for the locations"
        List<LocationHealthCheck> healthChecks = service.getLatestHealthChecksFor([activeLocation1, activeLocation2])
        then: "it returns the correct health ckecks"
        healthChecks.size() == 2
        healthChecks.findAll {
            it.location == activeLocation1 && it.date == NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES * 12).toDate()
        }.size() == 1
        healthChecks.findAll {
            it.location == activeLocation2  && it.date == NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES).toDate()
        }.size() == 1
    }
    void "multiple locations: getLatestHealthChecksFor will return latest check for every location"() {
        given: "multiple health checks exist for both locations"
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES * 2), activeLocation1)
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES * 4), activeLocation1)
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES * 7), activeLocation1)
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES), activeLocation1)
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES * 6), activeLocation2)
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES * 5), activeLocation2)
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES * 4), activeLocation2)
        createAndSaveLocationHealthCheck(NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES * 3), activeLocation2)
        when: "getLatestHealthChecksFor is called for both locations"
        List<LocationHealthCheck> healthChecks = service.getLatestHealthChecksFor([activeLocation1, activeLocation2])
        then: "it returns the correct health ckeck for both locations"
        healthChecks.size() == 2
        healthChecks.findAll {
            it.location == activeLocation1 && it.date == NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES).toDate()
        }.size() == 1
        healthChecks.findAll {
            it.location == activeLocation2 && it.date == NOW.minusMinutes(LocationHealthCheckJob.FREQUENCY_IN_MINUTES * 3).toDate()
        }.size() == 1
    }

    void createSomeTestData() {
        WebPageTestServer wptserver = TestDataUtil.createWebPageTestServer("wptserver", "wptserver", true, "https://wptserver.com")
        Browser browser = TestDataUtil.createBrowser("chrome")
        activeLocation1 = TestDataUtil.createLocation(wptserver, "location1", browser, true)
        activeLocation2 = TestDataUtil.createLocation(wptserver, "location2", browser, true)
        inactiveLocation = TestDataUtil.createLocation(wptserver, "location3", browser, false)
    }
    LocationHealthCheck createAndSaveLocationHealthCheck(DateTime date, Location location){
        return new LocationHealthCheck(
                location: location,
                date: date.toDate(),
                numberOfAgents: 2,
                numberOfPendingJobsInWpt: 12,
                numberOfJobResultsLastHour: 14,
                numberOfEventResultsLastHour: 120,
                numberOfErrorsLastHour: 2,
                numberOfJobResultsNextHour: 16,
                numberOfEventResultsNextHour: 140,
                numberOfCurrentlyPendingJobs: 20,
                numberOfCurrentlyRunningJobs: 4
        ).save(failOnError: true)
    }

}
