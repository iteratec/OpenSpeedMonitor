package pages

import de.iteratec.osm.util.I18nService
import geb.Page
import grails.util.Holders

class LoginPage extends Page {
    I18nService i18nService = Holders.applicationContext.getBean("i18nService")

    static url = "/login/auth"

    static at = { title == i18nService.msg("springSecurity.login.title")}

    static content = {
        loginForm { $("#loginForm") }

        username { loginForm.find("#username") }

        password { loginForm.find("#password") }

        submitButton (to: [LoginPage, EventResultDashboardPage]) { loginForm.find("input", type: "submit") }

        errorMessageBox { $("div.alert") }
    }
}