package geb.pages.de.iteratec.osm.measurement.environment

import geb.pages.de.iteratec.osm.I18nGebPage

class BrowserEditPage extends I18nGebPage{
    static url = getUrl("/browser/edit")

    static at = {
        title == getI18nMessage("default.edit.label", [getI18nMessage("browser.label")])
    }

    static content = {
        nameTextField { $("#name") }
        weightTextField { $("#weight") }

        saveButton (to: [BrowserEditPage, BrowserShowPage]) { $("input").find{it.attr("name") == "_action_update"} }
        resetButton { $("button", type: "reset") }

        addBrowserAliasButton { $("li.add").$("a") }

        errorMessageBox { $("div", class: "alert alert-danger") }
        errorMessageBoxText { errorMessageBox.attr("innerHTML") }
    }
}
