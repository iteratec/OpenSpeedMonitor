package pages

import geb.Page

class LoginPage extends Page {
    static url = "/login/auth"

    static at = { title == "Login" }

    static content = {
        loginForm { $("#loginForm") }

        username { loginForm.find("#username") }

        password { loginForm.find("#password") }

        submitButton (to: [LoginPage, EventResultDashboardPage]) { loginForm.find("input", type: "submit") }

        errorMessageBox { $("div.alert") }
    }
}