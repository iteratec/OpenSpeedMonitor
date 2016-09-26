package de.iteratec.osm.security

import grails.plugin.springsecurity.ui.RegistrationCode
import grails.plugin.springsecurity.ui.ResetPasswordCommand


class RegisterController extends grails.plugin.springsecurity.ui.RegisterController {
    def listUser(){
        params.action = "register"
        redirect(params)
    }
    def verifyRegistration() {

        String token = params.t

        RegistrationCode registrationCode = token ? RegistrationCode.findByToken(token) : null
        if (!registrationCode) {
            flash.error = message(code: 'spring.security.ui.register.badCode')
            redirect uri: successHandlerDefaultTargetUrl
            return
        }

        def user = uiRegistrationCodeStrategy.finishRegistration(registrationCode)

        if (!user) {
            flash.error = message(code: 'spring.security.ui.register.badCode')
            redirect uri: successHandlerDefaultTargetUrl
            return
        }

        if (user.hasErrors()) {
            // expected to be handled already by ErrorsStrategy.handleValidationErrors
            return
        }
        user.save()

        flash.message = message(code: 'spring.security.ui.register.complete')
        redirect uri: registerPostRegisterUrl ?: successHandlerDefaultTargetUrl
    }

}
