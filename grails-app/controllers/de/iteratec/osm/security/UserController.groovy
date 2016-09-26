package de.iteratec.osm.security

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

}
