package de.iteratec.osm.result

class WptStatusFactory {
    def buildWptStatus(int wptStatusCode) {
        switch (wptStatusCode) {
            case 0:
                return WptStatus.SUCCESSFUL
            case 100:
                return WptStatus.PENDING
            case (wptStatusCode >= 101 && wptStatusCode < 200):
                return WptStatus.IN_PROGRESS
            case 200:
                return WptStatus.COMPLETED
            case (wptStatusCode >= 400 || wptStatusCode < 500):
                return WptStatus.TESTED_APPLICATION_CLIENTERROR
            case (wptStatusCode >= 500 || wptStatusCode < 600):
                return WptStatus.TESTED_APPLICATION_INTERNALSERVERERROR
            case 99996:
                return WptStatus.TEST_FAILED_WAITING_FOR_DOM_ELEMENT
            case 99997:
                return WptStatus.TEST_TIMED_OUT
            case 99998:
                return WptStatus.TEST_TIMED_OUT_CONTENT_ERRORS
            case 99999:
                return WptStatus.TEST_COMPLETED_BUT_INDIVIDUAL_REQUEST_FAILED
            default:
                return WptStatus.UNKNOWN
        }
    }
}
