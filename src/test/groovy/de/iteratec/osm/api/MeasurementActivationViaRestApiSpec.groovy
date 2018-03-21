package de.iteratec.osm.api

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.de.iteratec.osm.api.MeasurementActivationCommand
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import static de.iteratec.osm.util.Constants.*

/**
 * Created by nkuhn on 12.05.15.
 */
@TestFor(GeneralMeasurementApiController)
@Mock([ApiKey])
class MeasurementActivationViaRestApiSpec extends Specification {

    InMemoryConfigService inMemoryConfigService

    static String apiKeyAllowed = 'allowed'
    static String apiKeyNotAllowed = 'not-allowed'

    def doWithSpring = {
        inMemoryConfigService(InMemoryConfigService)
    }

    void setup(){
        //test data common to all tests
        ApiKey.withTransaction {
            new ApiKey(secretKey: apiKeyAllowed, valid: true, allowedForMeasurementActivation: true).save(failOnError: true)
            new ApiKey(secretKey: apiKeyNotAllowed, valid: true, allowedForMeasurementActivation: false).save(failOnError: true)
        }
        //mocks common to all tests
        inMemoryConfigService = grailsApplication.mainContext.getBean('inMemoryConfigService')
        controller.inMemoryConfigService = inMemoryConfigService
    }

    // successful calls /////////////////////////////////////////////////////////////////////

    void "successful activation of measurements"(){
        when:
        params.apiKey = apiKeyAllowed
        MeasurementActivationCommand cmd = new MeasurementActivationCommand(
                apiKey: apiKeyAllowed
        )
        cmd.validate()
        controller.securedViaApiKeyActivateMeasurement(cmd)

        then:
        response.status == 200
        response.text == "Set measurements activation to: true"
        inMemoryConfigService.areMeasurementsGenerallyEnabled() == true
    }

    void "successful deactivation of measurements"(){
        when:
        params.apiKey = apiKeyAllowed
        MeasurementActivationCommand cmd = new MeasurementActivationCommand(
                apiKey: apiKeyAllowed
        )
        cmd.validate()
        controller.securedViaApiKeyDeactivateMeasurement(cmd)

        then:
        response.status == 200
        response.text == "Set measurements activation to: false"
        inMemoryConfigService.areMeasurementsGenerallyEnabled() == false
    }

    // failing calls /////////////////////////////////////////////////////////////////////

    void "should fail cause of api key without permission"(){
        setup:
        boolean defaultPermission = false

        when:
        params.apiKey = apiKeyNotAllowed
        MeasurementActivationCommand cmd = new MeasurementActivationCommand(
                apiKey: apiKeyNotAllowed
        )
        cmd.validate()
        controller.securedViaApiKeyActivateMeasurement(cmd)

        then:
        inMemoryConfigService.areMeasurementsGenerallyEnabled() == defaultPermission
        response.status == 400
        response.contentAsString == "Error field apiKey: " + DEFAULT_ACCESS_DENIED_MESSAGE + "\n"
    }

}
