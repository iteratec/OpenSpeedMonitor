package de.iteratec.osm.result

enum JobResultStatus {
    WAITING(100, "Waiting"),
    RUNNING(101, "Running"),
    SUCCESS(200, "Finished"),
    INCOMPLETE(600, "Incomplete"),
    LAUNCH_ERROR(500, "Failed to start"),
    FETCH_ERROR(501, "Failed to fetch result"),
    PERSISTANCE_ERROR(503, "Failed to save result"),
    TIMEOUT(504, "Timed out"),
    FAILED(505, "Failed"),
    CANCELED(506, "Canceled"),
    ORPHANED(507, "Orphaned")

    private int jobResultStatusCode
    private String message

    private JobResultStatus(Integer value, String message) {
        this.jobResultStatusCode = value
        this.message = message
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

    boolean hasResults() {
        return jobResultStatusCode == SUCCESS.jobResultStatusCode || jobResultStatusCode == INCOMPLETE.jobResultStatusCode
    }

    String getMessage() {
        return message
    }
}
