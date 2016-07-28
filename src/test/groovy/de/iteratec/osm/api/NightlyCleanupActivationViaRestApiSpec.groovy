package de.iteratec.osm.api

import de.iteratec.osm.InMemoryConfigService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(RestApiController)
@Mock([ApiKey])
class NightlyCleanupActivationViaRestApiSpec extends Specification {

    private RestApiController controllerUnderTest
    InMemoryConfigService inMemoryConfigService

    static String apiKeyAllowed = 'allowed'
    static String apiKeyNotAllowed = 'not-allowed'
    def doWithSpring = {
        inMemoryConfigService(InMemoryConfigService)
    }
    void setup(){
        controllerUnderTest = controller
        //test data common to all tests
        ApiKey.withTransaction {
            new ApiKey(secretKey: apiKeyAllowed, valid: true, allowedForNightlyDatabaseCleanupActivation: true).save(failOnError: true)
            new ApiKey(secretKey: apiKeyNotAllowed, valid: true, allowedForNightlyDatabaseCleanupActivation: false).save(failOnError: true)
        }
        //mocks common to all tests
        controllerUnderTest.inMemoryConfigService = grailsApplication.mainContext.getBean('inMemoryConfigService')
    }

    // successful calls /////////////////////////////////////////////////////////////////////

    void "successful activation of nightly-database-cleanup"(){
        when:
        params.apiKey = apiKeyAllowed
        NightlyDatabaseCleanupActivationCommand cmd = new NightlyDatabaseCleanupActivationCommand(
                apiKey: apiKeyAllowed,
                activationToSet: true
        )
        cmd.validate()
        controllerUnderTest.securedViaApiKeySetNightlyDatabaseCleanupActivation(cmd)

        then:
        response.status == 200
        response.text == "Set nightly-database-cleanup activation to: true"
        controllerUnderTest.inMemoryConfigService.isDatabaseCleanupEnabled() == true
    }

    void "successful deactivation of nightly-database-cleanup"(){
        when:
        params.apiKey = apiKeyAllowed
        NightlyDatabaseCleanupActivationCommand cmd = new NightlyDatabaseCleanupActivationCommand(
                apiKey: apiKeyAllowed,
                activationToSet: false
        )
        cmd.validate()
        controllerUnderTest.securedViaApiKeySetNightlyDatabaseCleanupActivation(cmd)

        then:
        response.status == 200
        response.text == "Set nightly-database-cleanup activation to: false"
        controllerUnderTest.inMemoryConfigService.isDatabaseCleanupEnabled() == false
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
        controllerUnderTest.securedViaApiKeySetNightlyDatabaseCleanupActivation(cmd)

        then:
        controllerUnderTest.inMemoryConfigService.areMeasurementsGenerallyEnabled() == defaultPermission
        response.status == 400
        response.contentAsString == "Error field apiKey: "+RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE+"\n"
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
        controllerUnderTest.securedViaApiKeySetNightlyDatabaseCleanupActivation(cmd)

        then:
        controllerUnderTest.inMemoryConfigService.areMeasurementsGenerallyEnabled() == defaultPermission
        response.status == 400
        response.text == "Error field activationToSet: nullable\n"
    }
}
