package de.iteratec.osm.api

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.filters.SecureApiFunctionsFilters
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification


@TestFor(RestApiController)
@Mock([SecureApiFunctionsFilters, ApiKey])
class NightlyCleanupActivationViaRestApiSpec extends Specification {

    private RestApiController controllerUnderTest
    InMemoryConfigService inMemoryConfigService

    static String apiKeyAllowed = 'allowed'
    static String apiKeyNotAllowed = 'not-allowed'

    void setup(){
        controllerUnderTest = controller
        //test data common to all tests
        ApiKey.withTransaction {
            new ApiKey(secretKey: apiKeyAllowed, valid: true, allowedForNightlyCleanupActivation: true).save(failOnError: true)
            new ApiKey(secretKey: apiKeyNotAllowed, valid: true, allowedForNightlyCleanupActivation: false).save(failOnError: true)
        }
        //mocks common to all tests
//        mockFilters(SecureApiFunctionsFilters)
        inMemoryConfigService = new InMemoryConfigService()
        controllerUnderTest.inMemoryConfigService = inMemoryConfigService
    }

    // successful calls /////////////////////////////////////////////////////////////////////

    void "successful activation of nightly-cleanup"(){
        when:
        params.apiKey = apiKeyAllowed
        NightlyDatabaseCleanupActivationCommand cmd = new NightlyDatabaseCleanupActivationCommand(
                apiKey: apiKeyAllowed,
                activationToSet: true
        )
        cmd.validate()
        withFilters(action:"securedViaApiKeySetNightlyCleanupActivation") {
            controllerUnderTest.securedViaApiKeySetNightlyCleanupActivation(cmd)
        }

        then:
        response.status == 200
        response.text == "Set nightly-cleanup activation to: true"
        inMemoryConfigService.areMeasurementsGenerallyEnabled() == true
    }

    void "successful deactivation of nightly-cleanup"(){
        when:
        params.apiKey = apiKeyAllowed
        NightlyDatabaseCleanupActivationCommand cmd = new NightlyDatabaseCleanupActivationCommand(
                apiKey: apiKeyAllowed,
                activationToSet: false
        )
        cmd.validate()
        withFilters(action:"securedViaApiKeySetNightlyCleanupActivation") {
            controllerUnderTest.securedViaApiKeySetNightlyCleanupActivation(cmd)
        }

        then:
        response.status == 200
        response.text == "Set nightly-cleanup activation to: false"
        inMemoryConfigService.areMeasurementsGenerallyEnabled() == false
    }

    // failing calls /////////////////////////////////////////////////////////////////////

    void "should fail cause of api key without permission"(){
        setup:
        boolean defaultPermission = false

        when:
        params.apiKey = apiKeyNotAllowed
        NightlyDatabaseCleanupActivationCommand cmd = new NightlyDatabaseCleanupActivationCommand(
                apiKey: apiKeyNotAllowed,
                activationToSet: true
        )
        cmd.validate()
        withFilters(action:"securedViaApiKeySetNightlyCleanupActivation") {
            controllerUnderTest.securedViaApiKeySetNightlyCleanupActivation(cmd)
        }

        then:
        inMemoryConfigService.areMeasurementsGenerallyEnabled() == defaultPermission
        response.status == 400
        response.text == "Error field apiKey: The submitted ApiKey doesn't have the permission to (de)activate the nightly-cleanup.\n"
    }

    void "should fail cause of missing boolean activationToSet"(){
        setup:
        boolean defaultPermission = false

        when:
        params.apiKey = apiKeyAllowed
        NightlyDatabaseCleanupActivationCommand cmd = new NightlyDatabaseCleanupActivationCommand(
                apiKey: apiKeyAllowed
        )
        cmd.validate()
        withFilters(action:"securedViaApiKeySetNightlyCleanupActivation") {
            controllerUnderTest.securedViaApiKeySetNightlyCleanupActivation(cmd)
        }

        then:
        inMemoryConfigService.areMeasurementsGenerallyEnabled() == defaultPermission
        response.status == 400
        response.text == "Error field activationToSet: nullable\n"
    }
}
