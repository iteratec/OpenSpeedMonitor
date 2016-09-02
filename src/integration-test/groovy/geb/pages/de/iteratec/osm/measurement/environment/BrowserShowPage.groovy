package geb.pages.de.iteratec.osm.measurement.environment

import geb.pages.de.iteratec.osm.I18nGebPage

class BrowserShowPage extends I18nGebPage{
    static url = getUrl("/browser/show")

    static at = {
        title == getI18nMessage("default.show.label", [getI18nMessage("browser.label")])
    }

    static content = {
        successDiv { $("div", class: "alert alert-info") }
        successDivText { successDiv[0].attr("innerHTML") }

        name { $("tr")[0].$("td")[2].$("div").attr("innerHTML") }
        weight { $("tr")[1].$("td")[2].$("div").attr("innerHTML") }

        deleteButton (to: BrowserShowPage) { $("li").$("a", href: "#DeleteModal") }

        deleteConfirmationDialog { $("#DeleteModal") }
        deleteConfirmButton { deleteConfirmationDialog.$("input", type: "submit") }

        alertDivText { $(".alert").attr("innerHTML") }
    }
}
