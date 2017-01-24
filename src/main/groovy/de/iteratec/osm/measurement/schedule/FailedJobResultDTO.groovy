package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.result.JobResult

import java.text.SimpleDateFormat

class FailedJobResultDTO {
    String testId
    String date
    Integer httpStatusCode
    String wptStatus
    String description
    URL testUrl

    FailedJobResultDTO(JobResult jobResult) {
        testId = jobResult.testId
        date = new SimpleDateFormat().format(jobResult.date)
        httpStatusCode = jobResult.httpStatusCode
        wptStatus = jobResult.wptStatus ?: ''
        description = jobResult.description ?: ''
        testUrl = jobResult.tryToGetTestsDetailsURL()
    }
}
