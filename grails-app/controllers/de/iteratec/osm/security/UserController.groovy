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

    def edit() {
        Map model = super.edit()
        if (!model['roleMap']) {
            List<Role> allRoles = Role.list()
            allRoles.each { role ->
                model['roleMap'].put(role, false)
            }
        }
        return model
    }

    def search(){
        params.action = "index"
        redirect(params)
    }
    def update(){
        if (params.password == "*****" ||params.password == "")
            params.password = User.findById(params.id).password
        doUpdate { user ->
            updateUser(user as User, params, roleNamesFromParams())
        }
    }

    @Transactional
    private def updateUser(User user, Map params, List<String> roleNames) {
        changeProperties(user, params)
        updatePassword(user, params)
        user.save(failOnError: true, flush: true)
        changedAuthorities(user, roleNames)
    }

    private void changeProperties(User user, Map params) {
        Map changedProperties = [:]
        ['username', 'email'].each { key ->
            if(params.containsKey(key) && user[key] != params[key]) {
                changedProperties[key] = params[key]
            }
        }
        ['enabled', 'accountExpired', 'accountLocked', 'passwordExpired'].each {key ->
            if(user[key] == true && !params.containsKey(key)) {
                changedProperties[key] = false
            }
            else if(user[key] == false && params[key] == "on") {
                changedProperties[key] = true
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

    @Transactional
    private def changedAuthorities(User u, List<String> roleNames) {
        List<Role> newRoles = roleNames.collect{ Role.findByAuthority(it) }
        Set<Role> userRoles = u.getAuthorities()
        Set<Role> rolesToDelete = userRoles.findAll{ !(it in newRoles) }
        List<Role> rolesToCreate = newRoles.findAll{ !(it in userRoles)}
        if(!rolesToDelete.empty) {
            List<UserRole> roles = UserRole.findAllByUserAndRoleInList(u, rolesToDelete.toList())
            roles.each {it.delete()}
        }
        rolesToCreate.each {
            new UserRole(user: u, role: it).save(failOnError: true, flush: true)
        }
    }
}
