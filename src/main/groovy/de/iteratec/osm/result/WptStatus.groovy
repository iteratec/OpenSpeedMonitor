package de.iteratec.osm.result

enum WptStatus{
    SUCCESSFUL(0),
    IN_PROGRESS(100),
    WAITING(101),
    COMPLETED(200),
    TESTED_APPLICATION_CLIENTERROR(400),
    TESTED_APPLICATION_INTERNALSERVERERROR(500),
    UNKNOWN(700),
    TEST_DID_NOT_START(701),
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

    static boolean isFailed(Integer wptStatusCode) {
        return wptStatusCode > 200 && wptStatusCode < 99999
    }
}