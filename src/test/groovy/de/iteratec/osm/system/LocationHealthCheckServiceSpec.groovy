package de.iteratec.osm.system

import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.*
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.JobResult
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.util.slurpersupport.GPathResult
import org.joda.time.DateTime
import spock.lang.Specification

@TestFor(LocationHealthCheckService)
@Mock([Location, WebPageTestServer, LocationHealthCheck, Browser, JobResult, Script, Job, JobGroup,
        OsmConfiguration, BatchActivity])
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
        prepareMocksCommonForAllTests()
        createTestDataCommonForAllTests()
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
        locationHealthChecks[0].numberOfCurrentlyPendingJobs == jobResults.findAll { it.httpStatusCode == 100 }.size()
        locationHealthChecks[0].numberOfCurrentlyRunningJobs == jobResults.findAll { it.httpStatusCode == 101 }.size()
    }
    void "cleanupHealthChecks deletes just old LocationHealthChecks"(){
        given: "some old and some new LocationHealthChecks exist"
        DateTime now = new DateTime()
        new LocationHealthCheck(date: now.plusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+1)).save(validate: false)
        new LocationHealthCheck(date: now.plusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+2)).save(validate: false)
        new LocationHealthCheck(date: now.plusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+3)).save(validate: false)
        new LocationHealthCheck(date: now.plusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+4)).save(validate: false)
        new LocationHealthCheck(date: now.minusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+1)).save(validate: false)
        new LocationHealthCheck(date: now.minusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+2)).save(validate: false)
        new LocationHealthCheck(date: now.minusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+3)).save(validate: false)
        new LocationHealthCheck(date: now.minusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS+4)).save(validate: false)

        when: "cleanupHealthChecks is called"
        service.cleanupHealthChecks()
        List<LocationHealthCheck> locationHealthChecks = LocationHealthCheck.list()

        then: "the LocationHealthChecks older setting internalMonitoringStorageTimeInDays get deleted"
        locationHealthChecks.size() == 4
    }

    private void createTestDataCommonForAllTests() {
        location = TestDataUtil.createLocation()
        Script script = TestDataUtil.createScript()
        JobGroup jobGroup = TestDataUtil.createJobGroup("jobGroup")
        Job job = TestDataUtil.createJob("job", script, this.location, jobGroup)
        jobResults = []
        jobResults << TestDataUtil.createJobResult("testid", new Date(), job, this.location, 100)
        jobResults << TestDataUtil.createJobResult("testid", new Date(), job, this.location, 100)
        jobResults << TestDataUtil.createJobResult("testid", new Date(), job, this.location, 101)
        jobResults << TestDataUtil.createJobResult("testid", new Date(), job, this.location, 200)
        jobResults << TestDataUtil.createJobResult("testid", new Date(), job, this.location, 200)
        TestDataUtil.createOsmConfig()
    }


    private void prepareMocksCommonForAllTests(){
        mockQueueAndJobStatusService()
    }

    private mockQueueAndJobStatusService() {
        QueueAndJobStatusService queueAndJobStatusService = new QueueAndJobStatusService()
        queueAndJobStatusService.metaClass.getExecutingJobResults = { Location location ->
            return jobResults
        }
        queueAndJobStatusService.metaClass.getNumberOfJobsAndEventsDueToRunFromNowUntil = { Location location, Date untilWhen ->
            return [jobs: expectedNumberOfJobResultsNextHour, events: expectedNumberOfEventResultsNextHour]
        }
        queueAndJobStatusService.metaClass.getNumberOfAgents = { Object locationTag, Object agentsResponse ->
            return expectedNumberOfAgents
        }
        queueAndJobStatusService.metaClass.getNumberOfPendingJobsFromWptServer = { Object locationTag ->
            return expectedNumberOfPendingJobsInWpt
        }
        queueAndJobStatusService.metaClass.getFinishedJobResultCountSince = { Location location, Date sinceWhen ->
            return expectedNumberOfJobResultsLastHour
        }
        queueAndJobStatusService.metaClass.getEventResultCountBetween = { Location location, Date from, Date to ->
            return expectedNumberOfEventResultsLastHour
        }
        queueAndJobStatusService.metaClass.getErroneousJobResultCountSince = { Location location, Date sinceWhen ->
            return expectedNumberOfErrorsLastHour
        }
        service.queueAndJobStatusService = queueAndJobStatusService
    }

}
