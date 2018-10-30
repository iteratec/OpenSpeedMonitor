package de.iteratec.osm.result

enum JobResultStatus {
    WAITING(100),
    RUNNING(101),
    SUCCESS(200),
    LAUNCH_ERROR(500),
    FETCH_ERROR(501),
    PERSISTANCE_ERROR(503),
    TIMEOUT(504),
    FAILED(505),
    ORPHANED(506),
    INCOMPLETE(507)

    private int jobResultStatusCode

    private JobResultStatus(Integer value) {
        this.jobResultStatusCode = value
    }

    boolean isFailed() {
        return jobResultStatusCode > SUCCESS.jobResultStatusCode
    }

    boolean isSuccess() {
        return jobResultStatusCode == SUCCESS.jobResultStatusCode
    }

    boolean isTerminated() {
        return isSuccess() || isFailed()
    }
}
