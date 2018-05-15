package de.iteratec.osm.system

import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.measurement.environment.*
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.WptStatus
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.util.slurpersupport.GPathResult
import org.joda.time.DateTime
import spock.lang.Specification

@TestFor(LocationHealthCheckService)
@Mock([Location, WebPageTestServer, LocationHealthCheck, Browser, JobResult, Script, Job, JobGroup,
        OsmConfiguration, BatchActivity])
@Build([Location, JobResult, OsmConfiguration, LocationHealthCheck])
class LocationHealthCheckServiceSpec extends Specification {

    private List<JobResult> jobResults
    private int expectedNumberOfAgents = 2
    private int expectedNumberOfJobResultsNextHour = 3
    private int expectedNumberOfEventResultsNextHour = 9
    private int expectedNumberOfPendingJobsInWpt = 0
    private int expectedNumberOfJobResultsLastHour = 2
    private int expectedNumberOfEventResultsLastHour = 6
    private int expectedNumberOfErrorsLastHour
    private Location location
    private static int INTERNAL_MONITORING_STORAGETIME_IN_DAYS = 30

    def setup() {
        createTestDataCommonForAllTests()
        prepareMocksCommonForAllTests()
    }
    static doWithConfig(c) {
        c.internalMonitoringStorageTimeInDays = INTERNAL_MONITORING_STORAGETIME_IN_DAYS
    }
    static doWithSpring = {
        configService(ConfigService)
        batchActivityService(BatchActivityService)
    }


    void "runHealthCheckForLocation writes a new LocationHealthCheck"() {
        given: "some test data is prepared"
        def xmlNodeMockedInThisTest = null
        LocationWithXmlNode locationWithXmlNode = new LocationWithXmlNode(
                location: location,
                locationXmlNode: xmlNodeMockedInThisTest
        )
        GPathResult getTestersResponseMockedInThisTest = null

        when: "runHealthCheckForLocation is called with a location and no graphite server exists"
        service.runHealthCheckForLocation(locationWithXmlNode, getTestersResponseMockedInThisTest)
        List<LocationHealthCheck> locationHealthChecks = LocationHealthCheck.list()

        then: "a new LocationHealthCheck object got persisted."
        locationHealthChecks.size() == 1
        new DateTime(locationHealthChecks[0].date).isAfter(new DateTime().minusMinutes(5))
        locationHealthChecks[0].location == location
        locationHealthChecks[0].numberOfAgents == expectedNumberOfAgents
        locationHealthChecks[0].numberOfPendingJobsInWpt == expectedNumberOfPendingJobsInWpt
        locationHealthChecks[0].numberOfJobResultsLastHour == expectedNumberOfJobResultsLastHour
        locationHealthChecks[0].numberOfEventResultsLastHour == expectedNumberOfEventResultsLastHour
        locationHealthChecks[0].numberOfErrorsLastHour == expectedNumberOfErrorsLastHour
        locationHealthChecks[0].numberOfJobResultsNextHour == expectedNumberOfJobResultsNextHour
        locationHealthChecks[0].numberOfEventResultsNextHour == expectedNumberOfEventResultsNextHour
        locationHealthChecks[0].numberOfCurrentlyPendingJobs == jobResults.findAll { it.httpStatusCode == WptStatus.Pending.getWptStatusCode() }.size()
        locationHealthChecks[0].numberOfCurrentlyRunningJobs == jobResults.findAll { it.httpStatusCode == WptStatus.Running.getWptStatusCode() }.size()
    }
    void "cleanupHealthChecks deletes just old LocationHealthChecks"(){
        given: "some old and some new LocationHealthChecks exist"
        DateTime now = new DateTime()
        LocationHealthCheck.build(date: now.plusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+1).toDate())
        LocationHealthCheck.build(date: now.plusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+2).toDate())
        LocationHealthCheck.build(date: now.plusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+3).toDate())
        LocationHealthCheck.build(date: now.plusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+4).toDate())
        LocationHealthCheck.build(date: now.minusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+1).toDate())
        LocationHealthCheck.build(date: now.minusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+2).toDate())
        LocationHealthCheck.build(date: now.minusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+3).toDate())
        LocationHealthCheck.build(date: now.minusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+4).toDate())

        when: "cleanupHealthChecks is called"
        service.cleanupHealthChecks()
        List<LocationHealthCheck> locationHealthChecks = LocationHealthCheck.list()

        then: "the LocationHealthChecks older setting internalMonitoringStorageTimeInDays get deleted"
        locationHealthChecks.size() == 4
    }

    private void createTestDataCommonForAllTests() {
        location = Location.build()
        jobResults = []
        2.times {
            jobResults << JobResult.build(httpStatusCode: WptStatus.Pending.getWptStatusCode())
        }
        jobResults << JobResult.build(httpStatusCode: WptStatus.Running.getWptStatusCode())
        2.times {
            jobResults << JobResult.build(httpStatusCode: WptStatus.Completed.getWptStatusCode())
        }
        OsmConfiguration.build()
    }


    private void prepareMocksCommonForAllTests(){
        mockQueueAndJobStatusService()
    }

    private mockQueueAndJobStatusService() {

        service.queueAndJobStatusService = Stub(QueueAndJobStatusService)
        service.queueAndJobStatusService.getExecutingJobResults(_) >> jobResults
        service.queueAndJobStatusService.getNumberOfJobsAndEventsDueToRunFromNowUntil(_, _) >> [jobs: expectedNumberOfJobResultsNextHour, events: expectedNumberOfEventResultsNextHour]
        service.queueAndJobStatusService.getNumberOfAgents(_, _) >> expectedNumberOfAgents
        service.queueAndJobStatusService.getNumberOfPendingJobsFromWptServer(_) >> expectedNumberOfPendingJobsInWpt
        service.queueAndJobStatusService.getFinishedJobResultCountSince(_, _) >> expectedNumberOfJobResultsLastHour
        service.queueAndJobStatusService.getEventResultCountBetween(_, _, _) >> expectedNumberOfEventResultsLastHour
        service.queueAndJobStatusService.getErroneousJobResultCountSince(_, _) >> expectedNumberOfErrorsLastHour

    }

}
