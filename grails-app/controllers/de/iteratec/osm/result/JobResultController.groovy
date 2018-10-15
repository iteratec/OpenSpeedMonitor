package de.iteratec.osm.result

import de.iteratec.osm.measurement.schedule.FailedJobResultDTO
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobStatisticService
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.I18nService

import java.text.SimpleDateFormat

class JobResultController {

    JobStatisticService jobStatisticService
    JobDaoService jobDaoService
    I18nService i18nService

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
                it.jobResultStatus > JobResultStatus.SUCCESS
            }
        }

        List<FailedJobResultDTO> dtos = []
        jobResultsWithStatusGreater200.each {
            FailedJobResultDTO jobResultDTO = new FailedJobResultDTO(it)
            jobResultDTO.date = new SimpleDateFormat(i18nService.msg("default.date.format.medium", "yyyy-MM-dd")).format(it.date)
            dtos << jobResultDTO
        }

        ControllerUtils.sendObjectAsJSON(response, ['jobLabel': job.label, 'jobResults': dtos])
    }
}
