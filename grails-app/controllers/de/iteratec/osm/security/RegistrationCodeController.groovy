package de.iteratec.osm.security

import grails.plugin.springsecurity.ui.RegistrationCode

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND

class RegistrationCodeController extends grails.plugin.springsecurity.ui.RegistrationCodeController {
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        respond RegistrationCode.list(params), model:[registrationCodeCount: RegistrationCode.count()]
    }

    def search(){
        params.action = "index"
        redirect(params)
    }


}
