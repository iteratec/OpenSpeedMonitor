package de.iteratec.osm.security
import de.iteratec.osm.security.User
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.authentication.dao.NullSaltSource
import grails.plugin.springsecurity.ui.SpringSecurityUiService

class UserController extends grails.plugin.springsecurity.ui.UserController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    SpringSecurityUiService springSecurityUiService

    def index(Integer max) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        respond User.list(params), model:[userCount: User.count()]
    }

    def show(User user) {
        respond user
    }
    def search(){
        params.action = "index"
        redirect(params)
    }
    def update(){
        if (params.password == "*****" ||params.password == "")
            params.password = User.findById(params.id).password
        doUpdate { user ->
            /* grails-spring-security-ui issue 89, PR #95 */
            //uiUserStrategy.updateUser params, user, roleNamesFromParams()
            updateUser(user as User, params, roleNamesFromParams())
        }
    }

    private def updateUser(User user, Map params, List roleNames) {
        changeProperties(user, params)
        updatePassword(user, params)
        user.save(onFailError: true, flush: true)
        changedAuthorities(user, roleNames)
    }

    private void changeProperties(User user, Map params) {
        Map changedProperties = [:]
        ['username', 'email'].each { key ->
            if(params.containsKey(key) && user[key] != params[key]) {
                changedProperties.put(key, params[key])
            }
        }
        ['enabled', 'accountExpired', 'accountLocked', 'passwordExpired'].each {key ->
            if(user[key] == true && !params.containsKey(key)) {
                changedProperties.put(key, false)
            }
            else if(user[key] == false && params[key] == "on") {
                changedProperties.put(key, true)
            }
        }
        changedProperties.each {entry ->
            user[entry.key] = entry.value
        }
    }

    @Transactional
    private def updatePassword(User user, Map properties) {
        String oldPassword = springSecurityUiService.uiPropertiesStrategy.getProperty(user, 'password')
        if (properties.password && properties.password != oldPassword) {
            String salt = springSecurityUiService.saltSource instanceof NullSaltSource ? null : springSecurityUiService.uiPropertiesStrategy.getProperty(user, 'username')
            String password = springSecurityUiService.encodePassword(properties.password, salt)
            springSecurityUiService.uiPropertiesStrategy.setProperties(password: password, user, transactionStatus)
        }
    }

    private def changedAuthorities(User u, List roleNames) {
        List<Role> newRoles = roleNames.collect{ Role.findByAuthority(it) }
        Set<Role> userRoles = u.getAuthorities()
        Set<Role> rolesToDelete = userRoles.findAll{ !(it in newRoles) }
        List<Role> rolesToCreate = newRoles.findAll{ !(it in userRoles)}
        if(!rolesToDelete.empty) {
            UserRole.where {user == u && (role in rolesToDelete)}.deleteAll()
        }
        rolesToCreate.each {
            new UserRole(user: u, role: it).save(onFailError: true, flush: true)
        }
    }
}
