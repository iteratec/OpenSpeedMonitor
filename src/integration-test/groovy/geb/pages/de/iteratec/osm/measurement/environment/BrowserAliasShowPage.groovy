package geb.pages.de.iteratec.osm.measurement.environment

import de.iteratec.osm.measurement.environment.BrowserAlias
import geb.pages.de.iteratec.osm.I18nGebPage

class BrowserAliasShowPage extends I18nGebPage {
    static url = getUrl("/browserAlias/show")

    static at = {
        title == getI18nMessage("default.show.label", [BrowserAlias.simpleName])
    }

    static content = {
        successDiv { $("div", class: "alert alert-info") }
        successDivText { successDiv[0].attr("innerHTML") }

        alias { $("#show-browserAlias").$("tr")[0].$("td")[2].attr("innerHTML") }
        browserName { $("#show-browserAlias").$("tr")[2].$("td")[2].attr("innerHTML") }

        deleteButton (to: BrowserAliasShowPage) { $("li").$("a", href: "#DeleteModal") }

        deleteConfirmationDialog { $("#DeleteModal") }
        deleteConfirmButton { deleteConfirmationDialog.$("input", type: "submit") }
    }
}
