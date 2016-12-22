package de.iteratec.osm.result

import de.iteratec.osm.measurement.schedule.FailedJobResultDTO
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobStatisticService
import de.iteratec.osm.util.ControllerUtils

class JobResultController {

    JobStatisticService jobStatisticService
    JobDaoService jobDaoService

    def listFailed(Long jobId) {
        Map<Long, String> allJobs = jobDaoService.getJobLabels()

        String selectedJobId = jobId ?: ''

        return ['allJobs': allJobs, 'selectedJobId': selectedJobId]
    }

    def getJobResults(Long jobId) {
        List<JobResult> jobResultsWithStatusGreater200
        Job job = Job.get(jobId)
        if (job) {
            List<JobResult> jobResultsForJob = jobStatisticService.getLast150CompletedJobResultsFor(job)

            jobResultsWithStatusGreater200 = jobResultsForJob.findAll {
                it.httpStatusCode > 200
            }
        }

        List<FailedJobResultDTO> dtos = []
        jobResultsWithStatusGreater200.each { dtos << new FailedJobResultDTO(it) }

        ControllerUtils.sendObjectAsJSON(response, ['jobLabel': job.label, 'jobResults': dtos])
    }
}
