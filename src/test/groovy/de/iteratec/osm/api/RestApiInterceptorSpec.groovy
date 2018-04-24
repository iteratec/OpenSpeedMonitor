package de.iteratec.osm.api

import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

import static de.iteratec.osm.util.Constants.*

@Build(ApiKey)
class RestApiInterceptorSpec extends Specification implements BuildDataTest, InterceptorUnitTest<RestApiInterceptor> {

    RestApiInterceptor interceptorUnderTest

    def setup() {
        interceptorUnderTest = interceptor
    }

    void setupSpec() {
        mockDomain(ApiKey)
    }

    def cleanup() {
    }

    void "Test restApi interceptor - wrong action won't match"() {
        when:"A request does not match the interceptor"
        withRequest(action: "allBrowsers")

        then:"The interceptor does not match"
        !interceptorUnderTest.doesMatch()
    }
    void "Test restApi interceptor - correct action will match"() {
        when:"A request matches the interceptor"
        withRequest(action: "securedViaApiKeySetNightlyDatabaseCleanupActivation")

        then:"The interceptor does match"
        interceptorUnderTest.doesMatch()
    }
    void "Test restApi interceptor - Missing apiKey param leads to status of 400"(){
        when:
        interceptorUnderTest.before()
        then:
        interceptorUnderTest.response.status == 403
        interceptorUnderTest.response.text.equals(DEFAULT_ACCESS_DENIED_MESSAGE)
    }
    void "Test restApi interceptor - ApiKey param without matching ApiKey in db leads to status of 404"(){
        when:
        params.apiKey = 'missingApiKey'
        interceptorUnderTest.before()
        then:
        interceptorUnderTest.response.status == 403
        interceptorUnderTest.response.text.equals(DEFAULT_ACCESS_DENIED_MESSAGE)
    }
    void "Test restApi interceptor - Invalid ApiKey leads to status of 403"(){
        setup:
        String existingButInvalidKey = 'key'
        ApiKey.build(secretKey: existingButInvalidKey, valid: false)
        when:
        params.apiKey = existingButInvalidKey
        interceptorUnderTest.before()
        then:
        interceptorUnderTest.response.status == 403
        interceptorUnderTest.response.text.equals(DEFAULT_ACCESS_DENIED_MESSAGE)
    }

}
