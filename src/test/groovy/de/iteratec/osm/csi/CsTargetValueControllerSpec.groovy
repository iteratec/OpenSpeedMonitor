package de.iteratec.osm.csi

import grails.testing.gorm.DomainUnitTest
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import spock.lang.Specification

class CsTargetValueControllerSpec extends Specification implements ControllerUnitTest<CsTargetValueController>,
        DomainUnitTest<CsTargetValue> {

    void "test creating a csTargetValue without any params"() {
        when: "nothing is in params"
        request.method = 'POST'
        controller.save()

        then: "response with errors"
        !model.csTargetValue.validate()
        CsTargetValue.count() == 0
    }

    void "test creating a csTargetValue with valid params"(Object dateParam, DateTime expectedDate, double csInPercent) {
        when: "there are valid params"
        request.method = 'POST'
        params.date = dateParam
        params.csInPercent = csInPercent
        controller.save()

        then: "csTargetValue is created"
        model.csTargetValue.validate()
        List<CsTargetValue> targetValues = CsTargetValue.list()
        targetValues*.csInPercent == [csInPercent]
        targetValues*.date == [expectedDate.toDate()]

        where:
        dateParam                                 | expectedDate                   | csInPercent
        "2015-05-05"                              | new DateTime(2015, 5, 5, 0, 0) | 42
        new DateTime(2015, 5, 5, 0, 0).toDate() | new DateTime(2015, 5, 5, 0, 0) | 42
    }
}
