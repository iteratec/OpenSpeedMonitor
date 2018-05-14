package de.iteratec.osm.result

enum WptStatus{
    Successful(0),
    Pending(100),
    Running(101),
    Ok(200),
    InvalidTestId(400),
    TestFailedWaitingForDomElement(99996),
    TestTimedOut(99997),
    TestTimedOutContentErrors(99998),
    TestCompletedButIndividualRequestsFailed(99999)

    private Integer statusCode

    private WptStatus(Integer value) {
        this.statusCode = value
    }

    Integer getWptStatusCode(){
        return statusCode
    }
}