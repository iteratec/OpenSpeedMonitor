package de.iteratec.osm.api

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.de.iteratec.osm.api.NightlyDatabaseCleanupActivationCommand
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import static de.iteratec.osm.util.Constants.*

@TestFor(GeneralMeasurementApiController)
@Mock([ApiKey])
class NightlyCleanupActivationViaRestApiSpec extends Specification {

    InMemoryConfigService inMemoryConfigService

    static String apiKeyAllowed = 'allowed'
    static String apiKeyNotAllowed = 'not-allowed'
    def doWithSpring = {
        inMemoryConfigService(InMemoryConfigService)
    }
    void setup(){
        //test data common to all tests
        ApiKey.withTransaction {
            new ApiKey(secretKey: apiKeyAllowed, valid: true, allowedForNightlyDatabaseCleanupActivation: true).save(failOnError: true)
            new ApiKey(secretKey: apiKeyNotAllowed, valid: true, allowedForNightlyDatabaseCleanupActivation: false).save(failOnError: true)
        }
        //mocks common to all tests
        controller.inMemoryConfigService = grailsApplication.mainContext.getBean('inMemoryConfigService')
    }

    // successful calls /////////////////////////////////////////////////////////////////////

    void "successful activation of nightly-database-cleanup"(){
        when:
        params.apiKey = apiKeyAllowed
        NightlyDatabaseCleanupActivationCommand cmd = new NightlyDatabaseCleanupActivationCommand(
                apiKey: apiKeyAllowed
        )
        cmd.validate()
        controller.securedViaApiKeyActivateNightlyCleanup(cmd)

        then:
        response.status == 200
        response.text == "Set nightly-database-cleanup activation to: true"
        controller.inMemoryConfigService.isDatabaseCleanupEnabled() == true
    }

    void "successful deactivation of nightly-database-cleanup"(){
        when:
        params.apiKey = apiKeyAllowed
        NightlyDatabaseCleanupActivationCommand cmd = new NightlyDatabaseCleanupActivationCommand(
                apiKey: apiKeyAllowed
        )
        cmd.validate()
        controller.securedViaApiKeyDeactivateNightlyCleanup(cmd)

        then:
        response.status == 200
        response.text == "Set nightly-database-cleanup activation to: false"
        controller.inMemoryConfigService.isDatabaseCleanupEnabled() == false
    }

    // failing calls /////////////////////////////////////////////////////////////////////

    void "should fail cause of api key without permission"(){
        setup:
        boolean defaultPermission = false

        when:
        params.apiKey = apiKeyNotAllowed
        NightlyDatabaseCleanupActivationCommand cmd = new NightlyDatabaseCleanupActivationCommand(
                apiKey: apiKeyNotAllowed
        )
        cmd.validate()
        controller.securedViaApiKeyActivateNightlyCleanup(cmd)

        then:
        controller.inMemoryConfigService.areMeasurementsGenerallyEnabled() == defaultPermission
        response.status == 400
        response.contentAsString == "Error field apiKey: " + DEFAULT_ACCESS_DENIED_MESSAGE + "\n"
    }

}
