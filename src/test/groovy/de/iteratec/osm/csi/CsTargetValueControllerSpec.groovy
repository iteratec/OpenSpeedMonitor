package de.iteratec.osm.csi

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(CsTargetValueController)
@Mock([CsTargetValue])
class CsTargetValueControllerSpec extends Specification {

    void "test creating a csTargetValue without any params"() {
        when: "nothing is in params"
            request.method = 'POST'
            controller.save()

        then: "response with errors"
            !model.csTargetValue.validate()
            CsTargetValue.count() == 0
    }

    void "test creating a csTargetValue with valid params"() {
        when: "there are valid params"
            request.method = 'POST'
            params.date = date
            params.csInPercent = csInPercent
            controller.save()

        then: "csTargetValue is created"
            model.csTargetValue.validate()
            CsTargetValue.count() == 1

        where:
            date         | csInPercent
            "05-05-2015" | 42
            new Date()   | 42
    }
}
