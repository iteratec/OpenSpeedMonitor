package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.result.JobResult

import java.text.SimpleDateFormat

class FailedJobResultDTO {
    String testId
    String date
    String jobResultStatus
    String wptStatus
    String description
    URL testUrl

    FailedJobResultDTO(JobResult jobResult) {
        testId = jobResult.testId
        date = new SimpleDateFormat().format(jobResult.date)
        jobResultStatus = jobResult.jobResultStatus.getMessage()
        wptStatus = jobResult.wptStatus.getMessage()
        description = jobResult.description
        testUrl = jobResult.tryToGetTestsDetailsURL()
    }
}
