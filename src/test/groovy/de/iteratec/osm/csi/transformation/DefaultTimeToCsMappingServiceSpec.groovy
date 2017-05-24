package de.iteratec.osm.csi.transformation

import de.iteratec.osm.csi.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(DefaultTimeToCsMappingService)
@Mock([DefaultTimeToCsMapping, Page, CsiConfiguration, CsiDay, TimeToCsMapping])
class DefaultTimeToCsMappingServiceSpec extends Specification{

    final String DEFAULT_TTCS_MAPPING_1_NAME = "mapping1"
    final String DEFAULT_TTCS_MAPPING_2_NAME = "mapping2"

    Page page1
    Page page2
    CsiConfiguration csiConfiguration

    void "setup" () {
        page1 = new Page(name: "pageAggregator").save(failOnError: true)
        page2 = new Page(name: "other pageAggregator").save(failOnError: true)
        csiConfiguration = new CsiConfiguration(label: "csiConfig", csiDay: new CsiDay()).save(failOnError: true)
        createDefaultTimeToCsMappings()
    }


    void "test copyDefaultMappingToPage to empty csiConfiguration" () {
        when: "copying DefaultMappingToPage"
        service.copyDefaultMappingToPage(page1, DEFAULT_TTCS_MAPPING_1_NAME, csiConfiguration)

        then: "csiConfiguration contains default mappings and the page"
        TimeToCsMapping.count == 5
        csiConfiguration.timeToCsMappings.size() == 5
        csiConfiguration.timeToCsMappings.every {it.page == page1}
    }

    void "test copyDefaultMappingToPage overwrite existing mapping" () {
        given: "DefaultMappings copied to two pages"
        service.copyDefaultMappingToPage(page1, DEFAULT_TTCS_MAPPING_1_NAME, csiConfiguration)
        service.copyDefaultMappingToPage(page2, DEFAULT_TTCS_MAPPING_2_NAME, csiConfiguration)

        when: "copying DefaultMapping to first page again"
        service.copyDefaultMappingToPage(page1, DEFAULT_TTCS_MAPPING_1_NAME, csiConfiguration)

        then: "csiConfiguration still has two sets of pages and default mappings"
        TimeToCsMapping.count == 10
        csiConfiguration.timeToCsMappings.size() == 10
        csiConfiguration.timeToCsMappings.findAll{it.page == page1}.size() == 5
    }

    void "test copyDefaultMappingToPage add TimeToCsMappings to other page"() {
        given: "a csiConfiguration with a page and default mappings"
        service.copyDefaultMappingToPage(page1, DEFAULT_TTCS_MAPPING_1_NAME, csiConfiguration)

        when: "copying DefaultMappingToPage to another page"
        service.copyDefaultMappingToPage(page2, DEFAULT_TTCS_MAPPING_1_NAME, csiConfiguration)

        then: "csiConfiguration has two sets of pages and default mappings"
        TimeToCsMapping.count == 10
        csiConfiguration.timeToCsMappings.size() == 10
        csiConfiguration.timeToCsMappings.findAll{it.page == page1}.size() == 5
        csiConfiguration.timeToCsMappings.findAll{it.page == page2}.size() == 5
    }


    void createDefaultTimeToCsMappings() {
        new DefaultTimeToCsMapping(name: DEFAULT_TTCS_MAPPING_1_NAME, loadTimeInMilliSecs: 100, customerSatisfactionInPercent: 100).save(failOnError: true)
        new DefaultTimeToCsMapping(name: DEFAULT_TTCS_MAPPING_1_NAME, loadTimeInMilliSecs: 200, customerSatisfactionInPercent: 80).save(failOnError: true)
        new DefaultTimeToCsMapping(name: DEFAULT_TTCS_MAPPING_1_NAME, loadTimeInMilliSecs: 300, customerSatisfactionInPercent: 60).save(failOnError: true)
        new DefaultTimeToCsMapping(name: DEFAULT_TTCS_MAPPING_1_NAME, loadTimeInMilliSecs: 400, customerSatisfactionInPercent: 40).save(failOnError: true)
        new DefaultTimeToCsMapping(name: DEFAULT_TTCS_MAPPING_1_NAME, loadTimeInMilliSecs: 500, customerSatisfactionInPercent: 20).save(failOnError: true)

        new DefaultTimeToCsMapping(name: DEFAULT_TTCS_MAPPING_2_NAME, loadTimeInMilliSecs: 100, customerSatisfactionInPercent: 90).save(failOnError: true)
        new DefaultTimeToCsMapping(name: DEFAULT_TTCS_MAPPING_2_NAME, loadTimeInMilliSecs: 200, customerSatisfactionInPercent: 80).save(failOnError: true)
        new DefaultTimeToCsMapping(name: DEFAULT_TTCS_MAPPING_2_NAME, loadTimeInMilliSecs: 300, customerSatisfactionInPercent: 50).save(failOnError: true)
        new DefaultTimeToCsMapping(name: DEFAULT_TTCS_MAPPING_2_NAME, loadTimeInMilliSecs: 400, customerSatisfactionInPercent: 30).save(failOnError: true)
        new DefaultTimeToCsMapping(name: DEFAULT_TTCS_MAPPING_2_NAME, loadTimeInMilliSecs: 500, customerSatisfactionInPercent: 10).save(failOnError: true)
    }
}
