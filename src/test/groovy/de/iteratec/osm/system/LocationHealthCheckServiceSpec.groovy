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
/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(LocationHealthCheckService)
@Mock([Location, WebPageTestServer, LocationHealthCheck, Browser, JobResult, Script, Job, JobGroup, OsmConfiguration, BatchActivity])
class LocationHealthCheckServiceSpec extends Specification {

    List<JobResult> jobResults
    int expectedNumberOfAgents = 2
    int expectedNumberOfJobResultsNextHour = 3
    int expectedNumberOfEventResultsNextHour = 9
    int expectedNumberOfPendingJobsInWpt = 0
    int expectedNumberOfJobResultsLastHour = 2
    int expectedNumberOfEventResultsLastHour = 6
    int expectedNumberOfErrorsLastHour
    private Location location
    public static final INTERNAL_MONITORING_STORAGETIME_IN_DAYS = 30

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

        when: "runHealthCheckForLocation is called with a location"
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
        new LocationHealthCheck(date: now.minusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS-1)).save(validate: false)
        new LocationHealthCheck(date: now.minusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS-2)).save(validate: false)
        new LocationHealthCheck(date: now.minusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS-3)).save(validate: false)
        new LocationHealthCheck(date: now.minusDays(INTERNAL_MONITORING_STORAGETIME_IN_DAYS-4)).save(validate: false)
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

    public void createTestDataCommonForAllTests() {
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


    void prepareMocksCommonForAllTests(){
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
