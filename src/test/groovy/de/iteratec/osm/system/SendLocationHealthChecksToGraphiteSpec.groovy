package de.iteratec.osm.system

import de.iteratec.osm.ConfigService
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.report.external.GraphiteServer
import de.iteratec.osm.report.external.MockedGraphiteSocket
import de.iteratec.osm.report.external.provider.DefaultGraphiteSocketProvider
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.mock.interceptor.MockFor
import org.joda.time.DateTime
import spock.lang.Specification

@TestFor(LocationHealthCheckService)
@Mock([LocationHealthCheck, GraphiteServer])
class SendLocationHealthChecksToGraphiteSpec extends Specification {

    public static final String GRAPHITESERVERS_HEALTH_PREFIX = "osm.health"
    public static final String WPTSERVERS_LABEL = "mywptserver.com"
    public static final String LOCATIONS_IDENTIFIER = "mylocationidentifier"

    public MockedGraphiteSocket graphiteSocketUsedInTests = new MockedGraphiteSocket()

    def setup() {
        prepareMocksCommonForAllTests()
    }
    static doWithSpring = {
        configService(ConfigService)
        batchActivityService(BatchActivityService)
    }

    void "reportToGraphiteServers sends no LocationHealthChecks to graphite if no graphite server exists"(){
        given: "creating a LocationHealthCheck"
        mockGraphiteSocketProvider()
        List<LocationHealthCheck> healthChecks = [
                new LocationHealthCheck(
                        date: new DateTime().toDate(),
                        numberOfAgents: 2,
                        numberOfPendingJobsInWpt: 12,
                        numberOfJobResultsLastHour: 14,
                        numberOfEventResultsLastHour: 120,
                        numberOfErrorsLastHour: 2,
                        numberOfJobResultsNextHour: 16,
                        numberOfEventResultsNextHour: 140,
                        numberOfCurrentlyPendingJobs: 20,
                        numberOfCurrentlyRunningJobs: 4
                )
        ]

        when: "reportToGraphiteServers is called for the LocationHealthCheck"
        service.reportToGraphiteServers(healthChecks)

        then: "nothing would have been sent to graphite"
        graphiteSocketUsedInTests.sentDates.size() == 0

    }
    void "reportToGraphiteServers sends no LocationHealthChecks to graphite if no graphite server with reportHealthMetrics = true exists"(){
        given: "creating a LocationHealthCheck and a graphite server with reportHealthMetrics = false"
        mockGraphiteSocketProvider()
        TestDataUtil.createGraphiteServer("https://my-graphite.com", 2003, [], false, GRAPHITESERVERS_HEALTH_PREFIX)
        List<LocationHealthCheck> healthChecks = [
                new LocationHealthCheck(
                        date: new DateTime().toDate(),
                        numberOfAgents: 2,
                        numberOfPendingJobsInWpt: 12,
                        numberOfJobResultsLastHour: 14,
                        numberOfEventResultsLastHour: 120,
                        numberOfErrorsLastHour: 2,
                        numberOfJobResultsNextHour: 16,
                        numberOfEventResultsNextHour: 140,
                        numberOfCurrentlyPendingJobs: 20,
                        numberOfCurrentlyRunningJobs: 4
                )
        ]

        when: "reportToGraphiteServers is called for the LocationHealthCheck"
        service.reportToGraphiteServers(healthChecks)

        then: "nothing would have been sent to graphite"
        graphiteSocketUsedInTests.sentDates.size() == 0

    }

    void "reportToGraphiteServers sends single LocationHealthCheck to graphite correctly if a graphite server with reportHealthMetrics = true exists"(){
        given: "creating a LocationHealthCheck and a graphite server with reportHealthMetrics = true"
        mockGraphiteSocketProvider()
        TestDataUtil.createGraphiteServer("https://my-graphite.com", 2003, [], true, GRAPHITESERVERS_HEALTH_PREFIX)
        List<LocationHealthCheck> healthChecks = [
                new LocationHealthCheck(
                        date: new DateTime().toDate(),
                        numberOfAgents: 2,
                        numberOfPendingJobsInWpt: 12,
                        numberOfJobResultsLastHour: 14,
                        numberOfEventResultsLastHour: 120,
                        numberOfErrorsLastHour: 2,
                        numberOfJobResultsNextHour: 16,
                        numberOfEventResultsNextHour: 140,
                        numberOfCurrentlyPendingJobs: 20,
                        numberOfCurrentlyRunningJobs: 4
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
        mockGraphiteSocketProvider()
        TestDataUtil.createGraphiteServer("https://my-graphite.com", 2003, [], true, GRAPHITESERVERS_HEALTH_PREFIX)
        List<LocationHealthCheck> healthChecks = [
                new LocationHealthCheck(
                        date: new DateTime().toDate(),
                        numberOfAgents: 2,
                        numberOfPendingJobsInWpt: 12,
                        numberOfJobResultsLastHour: 14,
                        numberOfEventResultsLastHour: 120,
                        numberOfErrorsLastHour: 2,
                        numberOfJobResultsNextHour: 16,
                        numberOfEventResultsNextHour: 140,
                        numberOfCurrentlyPendingJobs: 20,
                        numberOfCurrentlyRunningJobs: 4
                ),
                new LocationHealthCheck(
                        date: new DateTime().toDate(),
                        numberOfAgents: 2,
                        numberOfPendingJobsInWpt: 12,
                        numberOfJobResultsLastHour: 14,
                        numberOfEventResultsLastHour: 120,
                        numberOfErrorsLastHour: 2,
                        numberOfJobResultsNextHour: 16,
                        numberOfEventResultsNextHour: 140,
                        numberOfCurrentlyPendingJobs: 20,
                        numberOfCurrentlyRunningJobs: 4
                ),
                new LocationHealthCheck(
                        date: new DateTime().toDate(),
                        numberOfAgents: 2,
                        numberOfPendingJobsInWpt: 12,
                        numberOfJobResultsLastHour: 14,
                        numberOfEventResultsLastHour: 120,
                        numberOfErrorsLastHour: 2,
                        numberOfJobResultsNextHour: 16,
                        numberOfEventResultsNextHour: 140,
                        numberOfCurrentlyPendingJobs: 20,
                        numberOfCurrentlyRunningJobs: 4
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

    private void prepareMocksCommonForAllTests(){
        mockLocationInHealthCheckObject()
    }

    private mockLocationInHealthCheckObject() {

        LocationHealthCheck.metaClass.location = [
                uniqueIdentifierForServer: LOCATIONS_IDENTIFIER,
                wptServer: [
                        label: WPTSERVERS_LABEL
                ]
        ]

    }

    /**
     * Mocks {@linkplain de.iteratec.osm.report.external.provider.GraphiteSocketProvider#getSocket}.
     */
    private void mockGraphiteSocketProvider() {
        def graphiteSocketProvider = new MockFor(DefaultGraphiteSocketProvider, true)
        graphiteSocketProvider.demand.getSocket() { GraphiteServer server ->
            return graphiteSocketUsedInTests
        }
        service.graphiteSocketProvider = graphiteSocketProvider.proxyInstance()
    }
}