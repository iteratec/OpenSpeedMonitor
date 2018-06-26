package geb.de.iteratec.osm

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.security.User
import de.iteratec.osm.util.OsmTestLogin
import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.LandingPage
import geb.pages.de.iteratec.osm.LoginPage
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Stepwise

/**
 * See http://www.gebish.org/manual/current/ for more instructions
 */
@Integration
@Rollback
@Stepwise
class LoginRedirectAfterInfrastructureSetupGebSpec extends CustomUrlGebReportingSpec implements OsmTestLogin{

    def setup(){
    }

    void "Remain on login page after failed login attempt"() {
        given: "User starts at login page"
        to LoginPage

        when: "User inserts wrong data"
        username << "wrongUsername"
        password << "wrongPassword"
        submitButton.click()

        then: "User remains at login page"
        at LoginPage

        and: "an error message is shown"
        errorMessageBox.isDisplayed()
    }

    void "Redirect to homepage when user login is correct and infrastructure setup already ran"() {
        given: "User starts at login page"
        to LoginPage

        and: "there is an admin and an osm config in db and infrastructure setup already ran"
        User.withNewTransaction {
            OsmConfiguration.build(infrastructureSetupRan: OsmConfiguration.InfrastructureSetupStatus.FINISHED)
            createAdminUser()
        }

        when: "User inserts correct data in form"
        username << getConfiguredUsername()
        password  << getConfiguredPassword()
        submitButton.click()

        then: "User gets to landing page"
        at LandingPage
    }

    void cleanupSpec() {
        doLogout()
        User.withNewTransaction {
            OsmConfiguration.first().delete()
        }
    }
}
