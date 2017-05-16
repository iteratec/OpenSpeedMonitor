package geb.pages.de.iteratec.osm

class LoginPage extends I18nGebPage {

    static url = getUrl("/login/auth")

    static at = {
        title == getI18nMessage("springSecurity.login.title")
    }


    static content = {
        loginForm { $("#loginForm") }

        username { loginForm.find("#username") }

        password { loginForm.find("#password") }

        submitButton { loginForm.find("input", type: "submit") }

        errorMessageBox { $("div.alert") }
    }
}