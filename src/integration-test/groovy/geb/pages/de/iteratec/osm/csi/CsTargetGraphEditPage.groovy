package geb.pages.de.iteratec.osm.csi

import de.iteratec.osm.csi.CsTargetGraph
import geb.pages.de.iteratec.osm.I18nGebPage

class CsTargetGraphEditPage extends I18nGebPage{
    static url = getUrl("/csTargetGraph/edit")

    static at = {
        title == getI18nMessage("default.edit.label", [CsTargetGraph.simpleName])
    }

    static content = {
        labelTextField { $("#label") }
        descriptionTextField { $("#description") }

        saveButton (to: [CsTargetGraphEditPage, CsTargetGraphShowPage]) { $("input").find{it.attr("name") == "_action_update"} }
        resetButton { $("button", type: "reset") }

        errorMessageBox { $("div", class: "alert alert-danger") }
        errorMessageBoxText { errorMessageBox.attr("innerHTML") }
    }
}
