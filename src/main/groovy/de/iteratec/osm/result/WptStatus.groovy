package de.iteratec.osm.result

enum WptStatus{
    SUCCESSFUL(0),
    PENDING(100),
    RUNNING(101),
    COMPLETED(200),
    INVALID_TEST_ID(400),
    TIME_OUT(504),
    OUTDATED_JOB(900),
    TEST_FAILED_WAITING_FOR_DOM_ELEMENT(99996),
    TEST_TIMED_OUT(99997),
    TEST_TIMED_OUT_CONTENT_ERRORS(99998),
    TEST_COMPLETED_BUT_INDIVIDUAL_REQUEST_FAILED(99999)

    private int statusCode

    private WptStatus(Integer value) {
        this.statusCode = value
    }

    int getWptStatusCode(){
        return statusCode
    }

    static boolean isFailed(Integer httpStatusCode) {
        return httpStatusCode > 200 && httpStatusCode < 99999
    }
}