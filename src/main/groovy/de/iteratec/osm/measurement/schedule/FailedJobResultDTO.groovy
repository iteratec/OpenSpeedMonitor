package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.result.JobResult

class FailedJobResultDTO {
    String testId
    Date date
    Integer httpStatusCode
    String wptStatus
    String description
    URL testUrl

    FailedJobResultDTO(JobResult jobResult) {
        testId = jobResult.testId
        date = jobResult.date
        httpStatusCode = jobResult.httpStatusCode
        wptStatus = jobResult.wptStatus ?: ''
        description = jobResult.description ?: ''
        testUrl = jobResult.tryToGetTestsDetailsURL()
    }
}
