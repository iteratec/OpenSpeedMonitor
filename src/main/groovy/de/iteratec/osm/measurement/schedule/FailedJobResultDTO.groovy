package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultStatus
import de.iteratec.osm.result.WptStatus

import java.text.SimpleDateFormat

class FailedJobResultDTO {
    String testId
    String date
    JobResultStatus jobResultStatus
    WptStatus wptStatus
    String description
    URL testUrl

    FailedJobResultDTO(JobResult jobResult) {
        testId = jobResult.testId
        date = new SimpleDateFormat().format(jobResult.date)
        jobResultStatus = jobResult.jobResultStatus
        wptStatus = jobResult.wptStatus ?: ''
        description = jobResult.description ?: ''
        testUrl = jobResult.tryToGetTestsDetailsURL()
    }
}
