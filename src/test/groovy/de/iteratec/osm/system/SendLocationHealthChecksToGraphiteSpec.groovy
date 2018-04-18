package de.iteratec.osm.system

import de.iteratec.osm.ConfigService
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.report.external.GraphiteServer
import de.iteratec.osm.report.external.MockedGraphiteSocket
import de.iteratec.osm.report.external.provider.DefaultGraphiteSocketProvider
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Specification

@TestFor(LocationHealthCheckService)
@Mock([LocationHealthCheck, GraphiteServer])
@Build([Location, LocationHealthCheck, WebPageTestServer, GraphiteServer])
class SendLocationHealthChecksToGraphiteSpec extends Specification {

    public static final String GRAPHITESERVERS_HEALTH_PREFIX = "osm.health"
    public static final String WPTSERVERS_LABEL = "mywptserver.com"
    public static final String LOCATIONS_IDENTIFIER = "mylocationidentifier"

    public MockedGraphiteSocket graphiteSocketUsedInTests = new MockedGraphiteSocket()

    def setup() {
        mockGraphiteSocketProvider()
    }
    static doWithSpring = {
        configService(ConfigService)
        batchActivityService(BatchActivityService)
    }

    void "reportToGraphiteServers sends no LocationHealthChecks to graphite if no graphite server exists"(){
        given: "creating a LocationHealthCheck"
        List<LocationHealthCheck> healthChecks = [
                LocationHealthCheck.buildWithoutSave(
                    date: new DateTime().toDate(),
                    numberOfAgents: 2,
                    numberOfPendingJobsInWpt: 12,
                    numberOfJobResultsLastHour: 14,
                    numberOfEventResultsLastHour: 120,
                    numberOfErrorsLastHour: 2,
                    numberOfJobResultsNextHour: 16,
                    numberOfEventResultsNextHour: 140,
                    numberOfCurrentlyPendingJobs: 20,
                    numberOfCurrentlyRunningJobs: 4,
                    location: Location.buildWithoutSave(
                        uniqueIdentifierForServer: LOCATIONS_IDENTIFIER,
                        wptServer: WebPageTestServer.buildWithoutSave(label: WPTSERVERS_LABEL)
                    )
                )
        ]

        when: "reportToGraphiteServers is called for the LocationHealthCheck"
        service.reportToGraphiteServers(healthChecks)

        then: "nothing would have been sent to graphite"
        graphiteSocketUsedInTests.sentDates.size() == 0

    }
    void "reportToGraphiteServers sends no LocationHealthChecks to graphite if no graphite server with reportHealthMetrics = true exists"(){
        given: "creating a LocationHealthCheck and a graphite server with reportHealthMetrics = false"
        GraphiteServer.build(reportHealthMetrics: false)
        List<LocationHealthCheck> healthChecks = [
                LocationHealthCheck.buildWithoutSave(
                    date: new DateTime().toDate(),
                    numberOfAgents: 2,
                    numberOfPendingJobsInWpt: 12,
                    numberOfJobResultsLastHour: 14,
                    numberOfEventResultsLastHour: 120,
                    numberOfErrorsLastHour: 2,
                    numberOfJobResultsNextHour: 16,
                    numberOfEventResultsNextHour: 140,
                    numberOfCurrentlyPendingJobs: 20,
                    numberOfCurrentlyRunningJobs: 4,
                    location: Location.buildWithoutSave(
                            uniqueIdentifierForServer: LOCATIONS_IDENTIFIER,
                            wptServer: WebPageTestServer.buildWithoutSave(label: WPTSERVERS_LABEL)
                    )
                )
        ]

        when: "reportToGraphiteServers is called for the LocationHealthCheck"
        service.reportToGraphiteServers(healthChecks)

        then: "nothing would have been sent to graphite"
        graphiteSocketUsedInTests.sentDates.size() == 0

    }
/*
 * I have commented out the next two tests because after migrating to Grails 3.3.4 they create a bug that prohibits
 * the execution of all unit and integration tests. The bug is like this one:
 * https://github.com/spockframework/spock/issues/783
 * This is because Grails 3.3.4 depends on a new testing framework. The build.gradle file contains the testCompile
 * dependency org.grails:grails-test-mixins:3.3.0, which makes it possible to still use the old testing framework.
 * It might be possible to migrate these commented out tests to use the new testing framework, as described here:
 * https://testing.grails.org/latest/guide/index.html#_example_converted_test
 * and avoid the bug that way.
    void "reportToGraphiteServers sends single LocationHealthCheck to graphite correctly if a graphite server with reportHealthMetrics = true exists"(){
        given: "creating a LocationHealthCheck and a graphite server with reportHealthMetrics = true"
        GraphiteServer.build(healthMetricsReportPrefix: GRAPHITESERVERS_HEALTH_PREFIX, reportHealthMetrics: true)
        List<LocationHealthCheck> healthChecks = [
                LocationHealthCheck.buildWithoutSave(
                    date: new DateTime().toDate(),
                    numberOfAgents: 2,
                    numberOfPendingJobsInWpt: 12,
                    numberOfJobResultsLastHour: 14,
                    numberOfEventResultsLastHour: 120,
                    numberOfErrorsLastHour: 2,
                    numberOfJobResultsNextHour: 16,
                    numberOfEventResultsNextHour: 140,
                    numberOfCurrentlyPendingJobs: 20,
                    numberOfCurrentlyRunningJobs: 4,
                    location: Location.buildWithoutSave(
                            uniqueIdentifierForServer: LOCATIONS_IDENTIFIER,
                            wptServer: WebPageTestServer.buildWithoutSave(label: WPTSERVERS_LABEL)
                    )
                )
        ]
        String expectedGraphitePathPrefix = "${GRAPHITESERVERS_HEALTH_PREFIX}.${WPTSERVERS_LABEL}.${LOCATIONS_IDENTIFIER}"

        when: "reportToGraphiteServers is called for the LocationHealthCheck"
        service.reportToGraphiteServers(healthChecks)
        List<String> pathesThatWasSent = graphiteSocketUsedInTests.sentDates*.path*.toString()

        then: "every graphite relevant healthcheck attribute has been sent to graphite server"
        graphiteSocketUsedInTests.sentDates.size() == service.HEALTHCHECK_ATTRIBUTES_TO_SEND.size()
        service.HEALTHCHECK_ATTRIBUTES_TO_SEND.each { String attributeThatShouldHaveBeenSent ->
            String graphitePathThatShouldHaveBeenSent = "${expectedGraphitePathPrefix}.${attributeThatShouldHaveBeenSent}"
            assert pathesThatWasSent.contains(graphitePathThatShouldHaveBeenSent)
        }

    }

    void "reportToGraphiteServers sends multiple LocationHealthChecks to graphite correctly if a graphite server with reportHealthMetrics = true exists"(){
        given: "creating a LocationHealthCheck and a graphite server with reportHealthMetrics = true"
        GraphiteServer.build(healthMetricsReportPrefix: GRAPHITESERVERS_HEALTH_PREFIX, reportHealthMetrics: true)
        List<LocationHealthCheck> healthChecks = [
                LocationHealthCheck.buildWithoutSave(
                    date: new DateTime().toDate(),
                    numberOfAgents: 2,
                    numberOfPendingJobsInWpt: 12,
                    numberOfJobResultsLastHour: 14,
                    numberOfEventResultsLastHour: 120,
                    numberOfErrorsLastHour: 2,
                    numberOfJobResultsNextHour: 16,
                    numberOfEventResultsNextHour: 140,
                    numberOfCurrentlyPendingJobs: 20,
                    numberOfCurrentlyRunningJobs: 4,
                    location: Location.buildWithoutSave(
                            uniqueIdentifierForServer: LOCATIONS_IDENTIFIER,
                            wptServer: WebPageTestServer.buildWithoutSave(label: WPTSERVERS_LABEL)
                    )
                ),
                LocationHealthCheck.buildWithoutSave(
                    date: new DateTime().toDate(),
                    numberOfAgents: 2,
                    numberOfPendingJobsInWpt: 12,
                    numberOfJobResultsLastHour: 14,
                    numberOfEventResultsLastHour: 120,
                    numberOfErrorsLastHour: 2,
                    numberOfJobResultsNextHour: 16,
                    numberOfEventResultsNextHour: 140,
                    numberOfCurrentlyPendingJobs: 20,
                    numberOfCurrentlyRunningJobs: 4,
                    location: Location.buildWithoutSave(
                            uniqueIdentifierForServer: LOCATIONS_IDENTIFIER,
                            wptServer: WebPageTestServer.buildWithoutSave(label: WPTSERVERS_LABEL)
                    )
                ),
                LocationHealthCheck.buildWithoutSave(
                    date: new DateTime().toDate(),
                    numberOfAgents: 2,
                    numberOfPendingJobsInWpt: 12,
                    numberOfJobResultsLastHour: 14,
                    numberOfEventResultsLastHour: 120,
                    numberOfErrorsLastHour: 2,
                    numberOfJobResultsNextHour: 16,
                    numberOfEventResultsNextHour: 140,
                    numberOfCurrentlyPendingJobs: 20,
                    numberOfCurrentlyRunningJobs: 4,
                    location: Location.buildWithoutSave(
                            uniqueIdentifierForServer: LOCATIONS_IDENTIFIER,
                            wptServer: WebPageTestServer.buildWithoutSave(label: WPTSERVERS_LABEL)
                    )
                )
        ]
        String expectedGraphitePathPrefix = "${GRAPHITESERVERS_HEALTH_PREFIX}.${WPTSERVERS_LABEL}.${LOCATIONS_IDENTIFIER}"

        when: "reportToGraphiteServers is called for the LocationHealthCheck"
        service.reportToGraphiteServers(healthChecks)
        List<String> pathesThatWasSent = graphiteSocketUsedInTests.sentDates*.path*.toString()

        then: "every graphite relevant healthcheck attribute has been sent to graphite server"
        graphiteSocketUsedInTests.sentDates.size() == healthChecks.size() * service.HEALTHCHECK_ATTRIBUTES_TO_SEND.size()
        service.HEALTHCHECK_ATTRIBUTES_TO_SEND.each { String attributeThatShouldHaveBeenSent ->
            String graphitePathThatShouldHaveBeenSent = "${expectedGraphitePathPrefix}.${attributeThatShouldHaveBeenSent}"
            int countThisAttributeHasBeenSent = pathesThatWasSent.findAll { it == graphitePathThatShouldHaveBeenSent }.size()
            assert countThisAttributeHasBeenSent == healthChecks.size()
        }

    }
*/
    /**
     * Mocks {@linkplain de.iteratec.osm.report.external.provider.GraphiteSocketProvider#getSocket}.
     */
    private void mockGraphiteSocketProvider() {
        service.graphiteSocketProvider = Stub(DefaultGraphiteSocketProvider)
        service.graphiteSocketProvider.getSocket(_) >> graphiteSocketUsedInTests
    }
}