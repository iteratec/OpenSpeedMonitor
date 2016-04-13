package de.iteratec.osm.api

import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TimeToCsMapping
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.measurement.schedule.JobGroup
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.mock.interceptor.MockFor
import spock.lang.Specification

@TestFor(RestApiController)
@Mock([CsiConfiguration, CsiDay, Page, TimeToCsMapping, JobGroup])
class TranslateToCustomerSatisfactionSpec extends Specification {

    RestApiController controllerUnderTest
    CsiConfiguration csiConfiguration
    String csiConfigurationLabel
    Page page
    JobGroup jobGroup

    void "setup"() {
        controllerUnderTest = controller

        page = new Page(name: "testPage").save(failOnError: true)

        ArrayList<TimeToCsMapping> timeToCsMappings = createTimeToCsMappings()

        csiConfigurationLabel = "csiConfiguration"
        csiConfiguration = new CsiConfiguration(label: csiConfigurationLabel,
                csiDay: new CsiDay(label: "unused"),
                timeToCsMappings: timeToCsMappings)
        csiConfiguration.save()

        jobGroup = new JobGroup(csiConfiguration: csiConfiguration, name: "jobGroup").save(failOnError: true)

        mockTimeToCsMappingsService()
    }

    void "test translateToCustomerSatisfaction with csiConfiguration set"() {
        given:
        TranslateCustomerSatisfactionCommand cmd = new TranslateCustomerSatisfactionCommand(
                pageName: page.name,
                loadTimeInMillisecs: 100,
                system: jobGroup.name)
        String expectedCustomerSatisfaction = "customerSatisfactionInPercent:50.0"

        when:
        controllerUnderTest.translateToCustomerSatisfaction(cmd)

        then:
        response.text.replaceAll("\"", "").contains(expectedCustomerSatisfaction)
    }

    void "test translateToCustomerSatisfaction with jobGroup set" () {
        given:
        TranslateCustomerSatisfactionCommand cmd = new TranslateCustomerSatisfactionCommand(csiConfiguration: csiConfigurationLabel,
                pageName: page.name,
                loadTimeInMillisecs: 100)
        String expectedCustomerSatisfaction = "customerSatisfactionInPercent:50.0"

        when:
        controllerUnderTest.translateToCustomerSatisfaction(cmd)

        then:
        response.text.replaceAll("\"", "").contains(expectedCustomerSatisfaction)
    }

    void "test translateToCustomerSatisfaction with not valid command object"() {
        given:
        TranslateCustomerSatisfactionCommand cmd = new TranslateCustomerSatisfactionCommand()
        String expectedResponse = "Params loadTimeInMillisecs AND pageName AND csiConfiguration must be set."

        when:
        controllerUnderTest.translateToCustomerSatisfaction(cmd)

        then:
        response.text.contains(expectedResponse)
    }


    List<TimeToCsMapping> createTimeToCsMappings() {
        List<TimeToCsMapping> mappings = new ArrayList<>()
        int customerSatisfaction = 100

        for (int i = 0; i <= 500; i += 20) {
            mappings.add(new TimeToCsMapping(page: page, mappingVersion: 1, loadTimeInMilliSecs: i, customerSatisfaction: customerSatisfaction))
            customerSatisfaction -= 10
        }

        return mappings
    }

    void mockTimeToCsMappingsService() {
        def timeToCsMappingService = new MockFor(TimeToCsMappingService)
        timeToCsMappingService.demand.getCustomerSatisfactionInPercent(0..10000) { loadTimeInMilliSecs, page, csiConfig ->
            if (loadTimeInMilliSecs && page && csiConfig) {
                TimeToCsMapping mapping = csiConfig.timeToCsMappings.find {
                    (it.loadTimeInMilliSecs == loadTimeInMilliSecs) && (it.page = page)
                }
                return mapping.customerSatisfaction
            }
        }
        controllerUnderTest.timeToCsMappingService = timeToCsMappingService.proxyInstance()
    }
}
