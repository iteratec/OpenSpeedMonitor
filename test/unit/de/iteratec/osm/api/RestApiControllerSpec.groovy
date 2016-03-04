package de.iteratec.osm.api

import de.iteratec.osm.api.dto.JsonCsiConfiguration
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.csi.TimeToCsMapping
import de.iteratec.osm.measurement.schedule.JobGroup
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.json.JSONObject
import spock.lang.Specification

@TestFor(RestApiController)
@Mock([CsiConfiguration, CsiDay, Page, TimeToCsMapping, JobGroup])
class RestApiControllerSpec extends Specification {

    RestApiController controllerUnderTest
    CsiConfiguration csiConfiguration
    String csiConfigurationLabel
    Page page
    JobGroup jobGroup

    void "setup"() {
        controllerUnderTest = controller

        page = new Page(name: "testPage").save(failOnError: true)

        csiConfigurationLabel = "csiConfiguration"
        csiConfiguration = TestDataUtil.createCsiConfiguration()
        csiConfiguration.timeToCsMappings = TestDataUtil.createTimeToCsMappingForAllPages([page])
        csiConfiguration.save()

        jobGroup = new JobGroup(csiConfiguration: csiConfiguration, name: "jobGroup").save(failOnError: true)
    }

    void "existing csiConfiguration by id as JSON"() {
        given:
        int csiConfigurationId = csiConfiguration.id
        JsonCsiConfiguration jsonCsiConfiguration = JsonCsiConfiguration.create(csiConfiguration)

        when:
        params.id = csiConfigurationId
        controllerUnderTest.getCsiConfiguration()

        then:
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.each { key, value ->
            value == jsonCsiConfiguration.getProperty(key)
        }
    }

    void "return 400 when asking for non-existing id"() {
        given:
        int csiConfigurationId = Integer.MAX_VALUE

        when:
        params.id = csiConfigurationId
        controllerUnderTest.getCsiConfiguration()

        then:
        response.status == 400
    }
}
