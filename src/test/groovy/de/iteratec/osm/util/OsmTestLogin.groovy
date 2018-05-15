package de.iteratec.osm.util

import de.iteratec.osm.security.Role
import de.iteratec.osm.security.User
import de.iteratec.osm.security.UserRole
import grails.util.Holders

trait OsmTestLogin {

    static String getConfiguredUsername(){
        def login = Holders.applicationContext.getBean("grailsApplication")?.config?.grails?.de?.iteratec?.osm?.test?.geb?.login
        return login?:"admin"
    }
    static String getConfiguredPassword(){
        def password = Holders.applicationContext.getBean("grailsApplication")?.config?.grails?.de?.iteratec?.osm?.test?.geb?.password
        return password?:"password"
    }

    static User createAdminUser() {
        String adminUserName = getConfiguredUsername()
        User user = User.findByUsername(adminUserName)
        if (!user) {
            user = User.build(
                    username: adminUserName,
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
        return user
    }
}
