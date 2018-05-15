package geb.de.iteratec.osm

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.security.User
import de.iteratec.osm.util.OsmTestLogin
import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.LoginPage
import geb.pages.de.iteratec.osm.wizards.InfrastructureSetupPage
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Stepwise
/**
 * See http://www.gebish.org/manual/current/ for more instructions
 */
@Integration
@Rollback
@Stepwise
class LoginRedirectBeforeInfrastructureSetupGebSpec extends CustomUrlGebReportingSpec implements OsmTestLogin{

    void "Redirect to infrastructure setup wizard when user login is correct and infrastructure setup didn't run yet"() {
        given: "User starts at login page"
        to LoginPage

        and: "there is an admin and an osm config in db and infrastructure setup didn't run yet"
        User.withNewTransaction {
            OsmConfiguration configuration = OsmConfiguration.list()[0]
            if (!configuration) {
                configuration = OsmConfiguration.build()
            }
            configuration.infrastructureSetupRan=OsmConfiguration.InfrastructureSetupStatus.NOT_STARTED
            configuration.save(flush: true)
            createAdminUser()
        }

        when: "User inserts correct data in form"
        username << getConfiguredUsername()
        password  << getConfiguredPassword()
        submitButton.click()

        then: "User gets to infrastructure setup page"
        at InfrastructureSetupPage
    }

    void cleanupSpec() {
        doLogout()
    }
}
