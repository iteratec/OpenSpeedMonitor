package geb.pages.de.iteratec.osm.measurement.environment

import de.iteratec.osm.measurement.environment.BrowserAlias
import geb.pages.de.iteratec.osm.I18nGebPage

class BrowserAliasEditPage extends I18nGebPage{
    static url = getUrl("/browserAlias/edit")

    static at = {
        title == getI18nMessage("default.edit.label", [BrowserAlias.simpleName])
    }

    static content = {
        aliasTextField { $("#alias") }

        saveButton (to: [BrowserAliasEditPage, BrowserAliasShowPage]) { $("input").find{it.attr("name") == "_action_update"} }
        resetButton { $("button", type: "reset") }

        errorMessageBox { $("div", class: "alert alert-danger") }
        errorMessageBoxText { errorMessageBox.attr("innerHTML") }

        browserDropdown { $("form").$("select") }
    }
}
