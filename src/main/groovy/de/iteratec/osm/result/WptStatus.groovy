package de.iteratec.osm.result

enum WptStatus {
    UNKNOWN(-1),
    SUCCESSFUL(0),
    PENDING(100),
    IN_PROGRESS(101),
    COMPLETED(200),
    TESTED_APPLICATION_CLIENTERROR(400, 499),
    TESTED_APPLICATION_INTERNALSERVERERROR(500, 599),
    TEST_DID_NOT_START(701),
    TEST_FAILED_WAITING_FOR_DOM_ELEMENT(99996),
    TEST_TIMED_OUT(99997),
    TEST_TIMED_OUT_CONTENT_ERRORS(99998),
    TEST_COMPLETED_BUT_INDIVIDUAL_REQUEST_FAILED(99999)

    private int statusCode
    private int statusCodeRangeEnd

    private WptStatus(int value) {
        this.statusCode = value
        this.statusCodeRangeEnd = value
    }

    private WptStatus(int statusCodeRangeStart, int statusCodeRangeEnd) {
        this.statusCode = statusCodeRangeStart
        this.statusCodeRangeEnd = statusCodeRangeEnd
    }

    boolean matchesStatusCode(int wptStatusCode) {
        return wptStatusCode >= this.statusCode && wptStatusCode <= this.statusCodeRangeEnd
    }

    int getWptStatusCode(){
        return statusCode
    }

    boolean isFailed() {
        return wptStatusCode > COMPLETED.wptStatusCode && wptStatusCode < TEST_COMPLETED_BUT_INDIVIDUAL_REQUEST_FAILED.wptStatusCode
    }

    boolean isFinished() {
        return wptStatusCode >= COMPLETED.wptStatusCode || wptStatusCode == SUCCESSFUL.wptStatusCode
    }
}
