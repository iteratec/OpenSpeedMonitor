package de.iteratec.osm.security
import de.iteratec.osm.security.User

class UserController extends grails.plugin.springsecurity.ui.UserController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

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
            params.password = de.iteratec.osm.security.User.findById(params.id).password
        doUpdate { user ->
            uiUserStrategy.updateUser params, user, roleNamesFromParams()
        }
    }

}
