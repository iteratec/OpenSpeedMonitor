package geb.de.iteratec.osm

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.security.Role
import de.iteratec.osm.security.User
import de.iteratec.osm.security.UserRole
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
            OsmConfiguration.build(infrastructureSetupRan: OsmConfiguration.InfrastructureSetupStatus.NOT_STARTED)
            createAdminUser()
        }

        when: "User inserts correct data in form"
        username << getConfiguredUsername()
        password  << getConfiguredPassword()
        submitButton.click()

        then: "User gets to infrastructure setup page"
        at InfrastructureSetupPage
    }

    private void createAdminUser(){
        User user = User.build(
                username: getConfiguredUsername(),
                password: getConfiguredPassword(),
                enabled: true,
                accountExpired: false,
                accountLocked: false,
                passwordExpired: false
        )
        Role adminRole = Role.build(authority: 'ROLE_ADMIN')
        // UserRole doesn't work with build-test-data plugin :(
        new UserRole(user: user, role: adminRole).save(failOnError: true)
    }

    void cleanupSpec() {
        doLogout()
    }
}
