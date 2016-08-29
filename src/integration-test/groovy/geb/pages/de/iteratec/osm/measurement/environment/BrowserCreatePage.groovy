package geb.pages.de.iteratec.osm.measurement.environment

import geb.pages.de.iteratec.osm.I18nGebPage


class BrowserCreatePage extends I18nGebPage{
    static url = getUrl("/browser/create")

    static at = {
        title == getI18nMessage("default.create.label", [getI18nMessage("browser.label")])
    }

    static content = {
        browserNameTextField { $("#name") }
        browserWeightTextField { $("#weight") }

        addBrowserAliasButton { $("li.add a") }

        createBrowserButton(to: [BrowserCreatePage, BrowserShowPage]) { $("#create") }

        errorMessageBox { $("div", class: "alert alert-danger") }
        errorMessageBoxText { errorMessageBox.attr("innerHTML") }
    }
}
