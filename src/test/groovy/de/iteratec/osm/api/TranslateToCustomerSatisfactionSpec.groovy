package de.iteratec.osm.api

import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TimeToCsMapping
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.measurement.schedule.JobGroup
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(RestApiController)
@Mock([CsiConfiguration, CsiDay, Page, TimeToCsMapping, JobGroup])
@Build([Page, CsiConfiguration, JobGroup])
class TranslateToCustomerSatisfactionSpec extends Specification {

    public static final String CSI_CONFIGURATION_LABEL = "csiConfiguration"
    RestApiController controllerUnderTest

    CsiConfiguration csiConfiguration
    Page page
    JobGroup jobGroup

    void "setup"() {
        controllerUnderTest = controller
        createTestDataCommonToAllTests()
        mockTimeToCsMappingsService()
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
        controllerUnderTest.translateToCustomerSatisfaction(cmd)

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
        controllerUnderTest.translateToCustomerSatisfaction(cmd)

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
        controllerUnderTest.translateToCustomerSatisfaction(cmd)

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
        controllerUnderTest.translateToCustomerSatisfaction(cmd)

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
        controllerUnderTest.translateToCustomerSatisfaction(cmd)

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
        controllerUnderTest.timeToCsMappingService = Stub(TimeToCsMappingService){
            getCustomerSatisfactionInPercent(_, _, _) >> { loadTimeInMilliSecs, page, csiConfig ->
                TimeToCsMapping mapping = csiConfig.timeToCsMappings.find {
                    (it.loadTimeInMilliSecs == loadTimeInMilliSecs) && (it.page = page)
                }
                return mapping.customerSatisfaction
            }
        }
    }
}
