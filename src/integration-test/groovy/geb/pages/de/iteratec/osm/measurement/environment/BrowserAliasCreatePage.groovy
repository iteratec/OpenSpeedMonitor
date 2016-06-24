package geb.pages.de.iteratec.osm.measurement.environment

import de.iteratec.osm.measurement.environment.BrowserAlias
import geb.pages.de.iteratec.osm.I18nGebPage

class BrowserAliasCreatePage extends I18nGebPage {
    static url = getUrl("/browserAlias/create")

    static at = {
        title == getI18nMessage("default.create.label", [BrowserAlias.simpleName])
    }

    static content = {
        createBrowserAliasButton(to: [BrowserAliasCreatePage, BrowserAliasShowPage]) { $("#create") }

        errorMessageBox { $("div", class: "alert alert-danger") }
        errorMessageBoxText { errorMessageBox.attr("innerHTML") }

        browserAliasTextField { $("#alias") }

        browserDropdownSelect { $("#browser.id") }
    }

    def selectBrowserByID(String browserID) {
        $(".controls").$("select").click()
        $(".controls").$("select").find("option").find{ it.value() == browserID }.click()
    }
}
