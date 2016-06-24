package geb.pages.de.iteratec.osm.csi

import de.iteratec.osm.csi.CsTargetGraph
import geb.pages.de.iteratec.osm.I18nGebPage

class CsTargetGraphIndexPage extends I18nGebPage {
    static url = getUrl("/csTargetGraph/index")

    static at = {
        title == getI18nMessage("default.list.label", [CsTargetGraph.simpleName])

        $("#Menu .active a").attr("href").contains("/csTargetGraph/index")
    }

    static content = {
        successDiv { $("div", class: "alert alert-info") }
        successDivText { successDiv[0].attr("innerHTML") }

        csTargetGraphTableRows { $("#list-csTargetGraph").$("tbody").$("tr") }

        pageButtons { $(".pagination").$("li") }
    }
}
