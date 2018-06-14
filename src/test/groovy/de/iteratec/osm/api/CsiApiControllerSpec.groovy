package de.iteratec.osm.api

import de.iteratec.osm.api.dto.CsiConfigurationDto
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TimeToCsMapping
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.measurement.schedule.JobGroup
import grails.buildtestdata.mixin.Build
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.grails.web.json.JSONObject
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(CsiApiController)
@Mock([CsiConfiguration, CsiDay, Page, TimeToCsMapping, JobGroup])
@Build([CsiConfiguration, Page, JobGroup])
class CsiApiControllerSpec extends Specification {

    public static final String CSI_CONFIGURATION_LABEL = "csiConfiguration"
    CsiConfiguration csiConfiguration
    Page page
    JobGroup jobGroup

    def setup() {
        createTestDataCommonToAllTests()
        mockTimeToCsMappingsService()
    }

    void "existing csiConfiguration by id as JSON"() {
        given: "1 CsiConfiguration exists"
        int csiConfigurationId = csiConfiguration.id
        CsiConfigurationDto jsonCsiConfiguration = CsiConfigurationDto.create(csiConfiguration)

        when: "REST method to get the CsiConfiguration is called"
        params.id = csiConfigurationId
        controller.getCsiConfiguration()

        then: "it returns the DTO representation of the CsiConfiguration"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.each { key, value ->
            value == jsonCsiConfiguration.getProperty(key)
        }
    }

    void "return 404 when asking for non-existing csiConfiguration"() {
        given: "no CsiConfiguration exists for given ID"
        int csiConfigurationId = Integer.MAX_VALUE

        when: "CsiConfiguration is queried for that ID"
        params.id = csiConfigurationId
        controller.getCsiConfiguration()

        then: "response status is 404 (Not Found)"
        response.status == 404
    }

    void "test translateToCustomerSatisfaction with pageName and system set"() {
        given:
        TranslateCustomerSatisfactionCommand cmd = new TranslateCustomerSatisfactionCommand(
                pageName: page.name,
                loadTimeInMillisecs: 100,
                system: jobGroup.name
        )
        String expectedCustomerSatisfaction = "customerSatisfactionInPercent:50.0"

        when:
        controller.translateToCustomerSatisfaction(cmd)

        then:
        response.status == 200
        response.text.replaceAll("\"", "").contains(expectedCustomerSatisfaction)
    }

    void "test translateToCustomerSatisfaction with pageName and csiConfiguration set" () {
        given:
        TranslateCustomerSatisfactionCommand cmd = new TranslateCustomerSatisfactionCommand(
                csiConfiguration: CSI_CONFIGURATION_LABEL,
                pageName: page.name,
                loadTimeInMillisecs: 100
        )
        String expectedCustomerSatisfaction = "customerSatisfactionInPercent:50.0"

        when:
        controller.translateToCustomerSatisfaction(cmd)

        then:
        response.status == 200
        response.text.replaceAll("\"", "").contains(expectedCustomerSatisfaction)
    }

    void "test translateToCustomerSatisfaction with invalid command object (no csiConfiguration and no system is set)"() {
        given: "command object without any params in it"
        TranslateCustomerSatisfactionCommand cmd = new TranslateCustomerSatisfactionCommand(
                pageName: page.name,
                loadTimeInMillisecs: 100
        )
        String expectedResponse = "Params loadTimeInMillisecs AND pageName AND (csiConfiguration or system) must be set."

        when: "translateToCustomerSatisfaction is called with that invalid command"
        controller.translateToCustomerSatisfaction(cmd)

        then: "responses status is 400 and its text is the correct error message"
        response.status == 400
        response.text.contains(expectedResponse)
    }

    void "test translateToCustomerSatisfaction with invalid command object (no pageName is set)"() {
        given: "command object without any params in it"
        TranslateCustomerSatisfactionCommand cmd = new TranslateCustomerSatisfactionCommand(
                csiConfiguration: CSI_CONFIGURATION_LABEL,
                loadTimeInMillisecs: 100
        )
        String expectedResponse = "Params loadTimeInMillisecs AND pageName AND (csiConfiguration or system) must be set."

        when: "translateToCustomerSatisfaction is called with that invalid command"
        controller.translateToCustomerSatisfaction(cmd)

        then: "responses status is 400 and its text is the correct error message"
        response.status == 400
        response.text.contains(expectedResponse)
    }

    void "test translateToCustomerSatisfaction with invalid command object (no loadTimeInMillisecs is set)"() {
        given: "command object without any params in it"
        TranslateCustomerSatisfactionCommand cmd = new TranslateCustomerSatisfactionCommand(
                csiConfiguration: CSI_CONFIGURATION_LABEL,
                pageName: page.name,
        )
        String expectedResponse = "Params loadTimeInMillisecs AND pageName AND (csiConfiguration or system) must be set."

        when: "translateToCustomerSatisfaction is called with that invalid command"
        controller.translateToCustomerSatisfaction(cmd)

        then: "responses status is 400 and its text is the correct error message"
        response.status == 400
        response.text.contains(expectedResponse)
    }

    private void createTestDataCommonToAllTests() {
        page = Page.build()
        csiConfiguration = CsiConfiguration.build(
                label: CSI_CONFIGURATION_LABEL,
                timeToCsMappings: createTimeToCsMappings()
        )
        jobGroup = JobGroup.build(csiConfiguration: csiConfiguration)
    }

    /**
     * Creates fake time to customer satisfaction mappings so that 100ms get translated to a
     * customer satisfaction of 0.5.
     * @return
     */
    List<TimeToCsMapping> createTimeToCsMappings() {
        List<TimeToCsMapping> mappings = new ArrayList<>()
        int customerSatisfaction = 100
        for (int i = 0; i <= 200; i += 20) {
            mappings.add(new TimeToCsMapping(page: page, mappingVersion: 1, loadTimeInMilliSecs: i, customerSatisfaction: customerSatisfaction))
            customerSatisfaction -= 10
        }
        return mappings
    }

    void mockTimeToCsMappingsService() {
        controller.timeToCsMappingService = Stub(TimeToCsMappingService){
            getCustomerSatisfactionInPercent(_, _, _) >> { loadTimeInMilliSecs, page, csiConfig ->
                TimeToCsMapping mapping = csiConfig.timeToCsMappings.find {
                    (it.loadTimeInMilliSecs == loadTimeInMilliSecs) && (it.page = page)
                }
                return mapping.customerSatisfaction
            }
        }
    }

}
