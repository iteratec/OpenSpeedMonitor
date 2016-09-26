package de.iteratec.osm.security

class RoleController extends grails.plugin.springsecurity.ui.RoleController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        respond Role.list(params), model:[roleCount: Role.count()]
    }

    def show(Role role) {
        respond role
    }
    def search(){
        params.action = "index"
        redirect(params)
    }
}
