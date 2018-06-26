package geb.pages.de.iteratec.osm.csi

import de.iteratec.osm.csi.CsTargetGraph
import geb.pages.de.iteratec.osm.I18nGebPage

class CsTargetGraphShowPage extends I18nGebPage{
    static url = getUrl("/csTargetGraph/show")

    static at = {
        title == getI18nMessage("default.show.label", [CsTargetGraph.simpleName])
    }

    static content = {
        labelText { $("#show-csTargetGraph").$("tr")[4].$(".property-value").attr("innerHTML") }
        descriptionText { $("#show-csTargetGraph").$("tr")[5].$(".property-value").attr("innerHTML") }

        successDiv { $("div", class: "alert alert-info") }
        successDivText { successDiv[0].attr("innerHTML") }

        deleteButton (to: CsTargetGraphShowPage) { $("li").$("a", href: "#DeleteModal") }

        deleteConfirmationDialog { $("#DeleteModal") }
        deleteConfirmButton { deleteConfirmationDialog.$("input", type: "submit") }
    }
}
