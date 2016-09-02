package geb.pages.de.iteratec.osm.csi

import de.iteratec.osm.csi.Page
import geb.pages.de.iteratec.osm.I18nGebPage

class PageEditPage extends I18nGebPage{
    static url = getUrl("/page/edit")

    static at = {
        title == getI18nMessage("default.edit.label", [Page.simpleName])
    }

    static content = {
        nameTextField { $("#name") }
        weightTextField { $("#weight") }

        saveButton (to: [PageEditPage, PageShowPage]) { $("input").find{it.attr("name") == "_action_update"} }
        resetButton { $("button", type: "reset") }

        addPageAliasButton { $("li.add").$("a") }

        errorMessageBox { $("div", class: "alert alert-error") }
        errorMessageBoxText { errorMessageBox.attr("innerHTML") }
    }
}
