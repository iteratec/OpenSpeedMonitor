package geb.de.iteratec.osm.wizard

import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.wizards.InfrastructureSetupPage
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Stepwise

@Integration
@Rollback
@Stepwise
class InfrastructureSetupWithCustomGebSpec extends CustomUrlGebReportingSpec {


    void "User should be able to select a custom WPT Server"() {
        given: "User starts at Infrastructure Page"
        to InfrastructureSetupPage
        InfrastructureSetupPage currentPage = page as InfrastructureSetupPage

        when: "Custom WPT Server is selected"
        currentPage.selectCustomServer()

        then: "All fields should be empty, required fields should be marked with missing value and submit should be disabled"
        currentPage.isCustomServerSelected()
        !currentPage.serverName.value()
        !currentPage.serverUrl.value()
        !currentPage.serverApiKey.value()

        currentPage.hasError(currentPage.serverName)
        currentPage.hasError(currentPage.serverUrl)

        !currentPage.serverName.@disabled
        !currentPage.serverUrl.@disabled
        !currentPage.isSubmitEnabled()
    }

    void "Entering a name and an url should enable the submit button"(){
        given:
        InfrastructureSetupPage currentPage = page as InfrastructureSetupPage

        when:
        currentPage.serverName << "serverName"
        currentPage.serverUrl << "http://localhost"

        then:
        currentPage.isSubmitEnabled()
        !currentPage.hasError(currentPage.serverName)
        !currentPage.hasError(currentPage.serverUrl)
        currentPage.isSubmitEnabled()
    }
}
