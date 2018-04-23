package de.iteratec.osm.csi

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback

@Integration
@Rollback
class CsiConfigurationIntegrationSpec extends NonTransactionalIntegrationSpec {

    CsiConfiguration csiConfiguration

    def setup() {
        createTestDataCommonForAllTests()
    }

    void 'delete csiConfig and cascading elements'() {
        when: "delete a csiConfiguration"
        csiConfiguration.delete(failOnError: true)

        then: "csiConfiguration and all weights belonging to it, shall be deleted"
        CsiConfiguration.count == 0
        PageWeight.count == 0
        CsiDay.count == 0
        TimeToCsMapping.count == 0
        BrowserConnectivityWeight.count == 0
    }

    void 'delete csiConfig should not delete associated pages, connectivity profiles and browser'() {
        given:
        int pageCountBefore = Page.count()
        int connectivityCountBefore = ConnectivityProfile.count()
        int browserCountBefore = Browser.count()

        when: "delete a csiConfiguration"
        csiConfiguration.delete(failOnError: true)


        then: "The associated pages, connectivity profiles and browser should remain"
        Page.count() == pageCountBefore
        ConnectivityProfile.count() == connectivityCountBefore
        Browser.count() == browserCountBefore

    }

    private void createTestDataCommonForAllTests() {
        Page page = Page.build()
        Browser browser = Browser.build()
        ConnectivityProfile profile = ConnectivityProfile.build()

        csiConfiguration = CsiConfiguration.build()
        csiConfiguration.timeToCsMappings = [TimeToCsMapping.build(page: page)]
        csiConfiguration.pageWeights = [PageWeight.build()]
        csiConfiguration.browserConnectivityWeights = [BrowserConnectivityWeight.build(browser: browser, connectivity: profile)]
        csiConfiguration.save(failOnError: true)

    }
}