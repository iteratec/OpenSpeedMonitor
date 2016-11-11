package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.script.Script
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Shared
import spock.lang.Specification

@TestFor(JobDaoService)
@Mock([Job, JobGroup, WebPageTestServer, Browser, Location, Script, ConnectivityProfile])
class JobDaoServiceSpec extends Specification {

    @Shared
    Job job1
    @Shared
    Job job2
    @Shared
    Job deletedJob
    private WebPageTestServer wptServer
    private Browser browser
    private Location location
    private JobGroup jobGroup


    void "setup"() {
        Script script = TestDataUtil.createScript()
        wptServer = TestDataUtil.createUnusedWptServer()
        browser = TestDataUtil.createBrowser("ff")
        location = TestDataUtil.createLocation(wptServer, "uniqueLocation", this.browser, false)
        jobGroup = TestDataUtil.createJobGroup("jobGroup")
        job1 = TestDataUtil.createJob("job1", script, this.location, this.jobGroup, "a job", 1, false, 100)
        job2 = TestDataUtil.createJob("job2", script, this.location, this.jobGroup, "a job", 1, false, 100)
        deletedJob = TestDataUtil.createJob("deletedJob", script, this.location, this.jobGroup, "a deleted job", 1, false, 100)
        deletedJob.deleted = true
        deletedJob.save(failOnError: true, flush: true)
    }

    void "test getAllJobs"() {
        when: "all jobs requested"
        List<Job> result = service.getAllJobs()

        then: "deleted jobs are not in the result"
        result.size() == 2
        result.contains(job1)
        result.contains(job2)
        !result.contains(deletedJob)
    }

    void "test getJobsByIds"() {
        when: "jobs requested by ids"
        List<Job> result = service.getJobsByIds([job1.id, deletedJob.id])

        then: "deleted jobs are not in the result"
        result.size() == 1
        result.contains(job1)
        !result.contains(deletedJob)
    }

    void "test getJobById"() {
        when: "job requested by id"
        Job result = service.getJobById(job1.id)
        Job resultDeleted = service.getJobById(deletedJob.id)

        then: "result is job or null if job is deleted"
        result == job1
        resultDeleted == null
    }

    void "test getJobByLabel"() {
        when: "job requested by label"
        Job result = service.getJob(job1.label)
        Job resultDeleted = service.getJob(deletedJob.label)

        then: "result is job or null if job is deleted"
        result == job1
        resultDeleted == null
    }

    void "test get active jobs"() {
        given: "an active job"
        job1.active = true
        job1.save(flush: true)

        when: "active jobs requested"
        List<Job> result = service.getJobs(true)

        then: "result contains only active, not deleted jobs"
        result.size() == 1
        result.contains(job1)
        !result.contains(job2)
        !result.contains(deletedJob)
    }

    void "test jobs by location"() {
        given: "a job with different location"
        job1.location = TestDataUtil.createLocation(wptServer, "differentLocation", browser, false)
        job1.save(flush: true)

        when: "jobs requested by location"
        List<Job> result = service.getJobs(location)

        then: "result contains jobs for location which are not deleted"
        result.size() == 1
        !result.contains(job1)
        result.contains(job2)
        !result.contains(deletedJob)
    }

    void "test jobs by connectivityProfile"() {
        given: "a job with connectivityProfile"
        ConnectivityProfile connectivityProfile = TestDataUtil.createConnectivityProfile("connectivityProfile")
        job1.connectivityProfile = connectivityProfile
        job1.save(flush: true)

        when: "jobs requested by location"
        List<Job> result = service.getJobs(connectivityProfile)

        then: "result contains jobs for connectivityProfile which are not deleted"
        result.size() == 1
        result.contains(job1)
        !result.contains(job2)
        !result.contains(deletedJob)
    }

    void "test jobs by params"() {
        when:
        def params = [:]
        params.sort = "label"
        List<Job> result = service.getJobs(params)

        then:
        result.size() == 2
        result.contains(job1)
        result.contains(job2)
        !result.contains(deletedJob)
    }

    void "test jobs by jobGroup"() {
        given: "a job with different jobGroup"
        job1.jobGroup = TestDataUtil.createJobGroup("different group")
        job1.save(flush: true)

        when: "jobs requested by location"
        List<Job> result = service.getJobs(jobGroup)

        then: "result contains jobs for connectivityProfile which are not deleted"
        result.size() == 1
        !result.contains(job1)
        result.contains(job2)
        !result.contains(deletedJob)
    }
}
