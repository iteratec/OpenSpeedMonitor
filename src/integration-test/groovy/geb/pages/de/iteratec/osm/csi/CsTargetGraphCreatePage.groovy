package geb.pages.de.iteratec.osm.csi

import de.iteratec.osm.csi.CsTargetGraph
import geb.pages.de.iteratec.osm.I18nGebPage

class CsTargetGraphCreatePage extends I18nGebPage{
    static url = getUrl("/csTargetGraph/create")

    static at = {
        title == getI18nMessage("default.create.label", [CsTargetGraph.simpleName])
    }

    static content = {
        createCsTargetGraphButton (to: [CsTargetGraphCreatePage, CsTargetGraphShowPage]) { $("#create") }

        labelTextField { $("#label") }
        descriptionTextField { $("#description") }

        errorMessageBox { $("div", class: "alert alert-danger") }
        errorMessageBoxText { errorMessageBox.attr("innerHTML") }
    }

    def selectPointTwo () {
        $("#pointTwo").find("option")[1].click()
    }
}
