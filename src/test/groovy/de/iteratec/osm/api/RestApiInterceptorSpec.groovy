package de.iteratec.osm.api

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(RestApiInterceptor)
@Mock([ApiKey])
class RestApiInterceptorSpec extends Specification {

    RestApiInterceptor interceptorUnderTest

    def setup() {
        interceptorUnderTest = interceptor
    }

    def cleanup() {

    }

    void "Test restApi interceptor - wrong controller, correct action won't match"() {
        when:"A request does not match the interceptor"
        withRequest(controller:"script", action: "securedViaApiKeySetNightlyDatabaseCleanupActivation")

        then:"The interceptor does match"
        !interceptorUnderTest.doesMatch()
    }
    void "Test restApi interceptor - correct controller, wrong action won't match"() {
        when:"A request does not match the interceptor"
        withRequest(controller:"restApi", action: "allBrowsers")

        then:"The interceptor does match"
        !interceptorUnderTest.doesMatch()
    }
    void "Test restApi interceptor - wrong controller, wrong action won't match"() {
        when:"A request does not match the interceptor"
        withRequest(controller:"script", action: "list")

        then:"The interceptor does match"
        !interceptorUnderTest.doesMatch()
    }
    void "Test restApi interceptor - correct controller, correct action will match"() {
        when:"A request matches the interceptor"
        withRequest(controller:"restApi", action: "securedViaApiKeySetNightlyDatabaseCleanupActivation")

        then:"The interceptor does match"
        interceptorUnderTest.doesMatch()
    }
    void "Test restApi interceptor - Missing apiKey param leads to status of 400"(){
        when:
        interceptorUnderTest.before()
        then:
        interceptorUnderTest.response.status == 400
        interceptorUnderTest.response.text.equals("This api function requires an apiKey with respected permission. You " +
                "have to submit this key as param 'apiKey'.")
    }
    void "Test restApi interceptor - ApiKey param without matching ApiKey in db leads to status of 404"(){
        when:
        params.apiKey = 'missingApiKey'
        interceptorUnderTest.before()
        then:
        interceptorUnderTest.response.status == 404
        interceptorUnderTest.response.text.equals("The submitted apiKey doesn't exist.")
    }
    void "Test restApi interceptor - Invalid ApiKey leads to status of 403"(){
        setup:
        String existingButInvalidKey = 'keyvalue'
        new ApiKey(secretKey: existingButInvalidKey, valid: false).save(failOnError: true)
        when:
        params.apiKey = existingButInvalidKey
        interceptorUnderTest.before()
        then:
        interceptorUnderTest.response.status == 403
        interceptorUnderTest.response.text.equals("The submitted apiKey is invalid.")
    }

}
