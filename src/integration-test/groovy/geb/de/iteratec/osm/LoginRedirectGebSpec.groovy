package geb.de.iteratec.osm

import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.security.User
import de.iteratec.osm.util.OsmTestLogin
import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.LoginPage
import geb.pages.de.iteratec.osm.result.EventResultDashboardPage
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Stepwise

/**
 * See http://www.gebish.org/manual/current/ for more instructions
 */
@Integration
@Rollback
@Stepwise
class LoginRedirectGebSpec extends CustomUrlGebReportingSpec implements OsmTestLogin{

    void "Remain on login page after failed login attempt"() {
        given: "User starts at login page"
        to LoginPage

        when: "User inserts wrong data"
        username << "admin"
        password << "wrongPassword"
        submitButton.click()

        then: "User remains at login page"
        at LoginPage

        and: "an error message is shown"
        errorMessageBox.isDisplayed()
    }

    void "Redirect to homepage when user login is correct"() {
        given: "User starts at login page"
        to LoginPage

        and: "there is an admin and an osm config in db"
        User.withNewTransaction {
            TestDataUtil.createOsmConfig()
            TestDataUtil.createAdminUser()
        }

        when: "User inserts correct data in form"
        username << configuredUsername
        password  << configuredPassword
        submitButton.click()

        then: "User gets to dashboard page"
        at EventResultDashboardPage
    }

    void cleanupSpec() {
        doLogout()
    }
}
