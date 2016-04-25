package de.iteratec.osm.csi

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification
@Integration
@Rollback
class CsiConfigurationTests extends NonTransactionalIntegrationSpec {

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

        Page.count > 0  // pages shall not be deleted
        ConnectivityProfile.count > 0  // connectivityProfiles shall not be deleted
        Browser.count > 0   // browsers shall not be deleted

    }

    private void createTestDataCommonForAllTests() {
        Page page = TestDataUtil.createPage("aPage",0)
        Browser browser = TestDataUtil.createBrowser("a",0)
        ConnectivityProfile connectivityProfile = TestDataUtil.createConnectivityProfile("testCon")

        csiConfiguration = TestDataUtil.createCsiConfiguration()
        csiConfiguration.timeToCsMappings = TestDataUtil.createTimeToCsMappingForAllPages([page])
        csiConfiguration.pageWeights = [TestDataUtil.createPageWeight(page, 0)]
        csiConfiguration.browserConnectivityWeights = [TestDataUtil.createBrowserConnectivityWeight(browser,connectivityProfile,0.5)]
        csiConfiguration.save(failOnError: true)

    }
}
