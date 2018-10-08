package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.script.Script
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

@Build([Job, Location, ConnectivityProfile, JobGroup])
class JobDaoServiceSpec extends Specification implements BuildDataTest, ServiceUnitTest<JobDaoService> {
    void setupSpec() {
        mockDomains(Job, Location, ConnectivityProfile, JobGroup, Script)
    }

    void "getAllJobs returns all jobs but deleted ones"() {
        given: "three jobs, one marked as deleted"
        List<Job> jobs = [Job.build(), Job.build()]
        Job.build( deleted: true )

        when: "all jobs requested"
        List<Job> result = service.getAllJobs()

        then: "deleted jobs are not in the result"
        result == jobs
    }

    void "getJobsByIds returns all jobs but deleted ones"() {
        given: "two normal jobs and a deleted"
        Job.build()
        Job normalJob = Job.build()
        Job deletedJob = Job.build(deleted: true)

        when: "jobs requested by ids"
        List<Job> result = service.getJobsByIds([normalJob.id, deletedJob.id])

        then: "deleted jobs are not in the result"
        result == [normalJob]
    }

    void "getJob returns null for deleted jobs"() {
        given: "a normal job and a deleted"
        Job normalJob = Job.build()
        Job deletedJob = Job.build(deleted: true)

        when: "jobs are requested by id"
        Job normalResult = service.getJob(normalJob.id)
        Job resultDeleted = service.getJob(deletedJob.id)

        then: "result is job or null if job is deleted"
        normalResult == normalJob
        resultDeleted == null
    }

    void "test get active jobs"() {
        given: "an active job, an inactive, and a deleted"
        Job activeJob = Job.build(active: true, executionSchedule: '0 */15 * * * ? 2015')
        Job.build()
        Job.build(deleted: true, active: true, executionSchedule: '0 */15 * * * ? 2015')

        when: "active jobs requested"
        List<Job> result = service.getJobs(true)

        then: "result contains only active, not deleted jobs"
        result == [activeJob]
    }

    void "test jobs by location"() {
        given: "two jobs with different location"
        Location location1 = Location.build()
        Job job1 = Job.build(location: location1)
        Job.build(location: location1, deleted: true)
        Job.build(location: Location.build())

        when: "jobs requested by location"
        List<Job> result = service.getJobs(location1)

        then: "result contains jobs for location which are not deleted"
        result == [job1]
    }

    void "test jobs by connectivityProfile"() {
        given: "two jobs with connectivityProfiles, one deleted"
        ConnectivityProfile connectivityProfile = ConnectivityProfile.build()
        Job job1 = Job.build(connectivityProfile: connectivityProfile)
        Job.build(connectivityProfile: connectivityProfile, deleted: true)
        Job.build(connectivityProfile: ConnectivityProfile.build())

        when: "jobs requested by location"
        List<Job> result = service.getJobs(connectivityProfile)

        then: "result contains jobs for connectivityProfile which are not deleted"
        result == [ job1 ]
    }

    void "test jobs by params"() {
        given: "two jobs with labels, one deleted"
        Job job2 = Job.build(label: "xyz")
        Job job1 = Job.build(label: "def")
        Job.build(label: "abc", deleted: true)

        when:
        def params = [:]
        params.sort = "label"
        List<Job> result = service.getJobs(params)

        then:
        result == [job1, job2]
    }

    void "test jobs by jobGroup"() {
        given: "two jobs with jobGroups, one deleted"
        JobGroup jobGroup = JobGroup.build()
        Job job1 = Job.build(jobGroup: jobGroup)
        Job.build(jobGroup: jobGroup, deleted: true)
        Job.build(jobGroup: JobGroup.build())

        when: "jobs requested by jobGroup"
        List<Job> result = service.getJobs(jobGroup)

        then: "result contains jobs for connectivityProfile which are not deleted"
        result == [job1]
    }
}
