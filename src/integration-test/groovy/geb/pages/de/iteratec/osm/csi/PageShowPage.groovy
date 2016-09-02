package geb.pages.de.iteratec.osm.csi

import de.iteratec.osm.csi.Page
import geb.pages.de.iteratec.osm.I18nGebPage

class PageShowPage extends I18nGebPage{
    static url = getUrl("/page/show")

    static at = {
        title == getI18nMessage("default.show.label", [Page.simpleName])
    }

    static content = {
        successDiv { $("div", class: "alert alert-info") }
        successDivText { successDiv[0].attr("innerHTML") }

        name { $("tr")[0].$("td")[1].attr("innerHTML") }
        weight { $("tr")[1].$("td")[1].attr("innerHTML") }

        deleteButton (to: PageShowPage) { $("li").$("a", href: "#DeleteModal") }

        deleteConfirmationDialog { $("#DeleteModal") }
        deleteConfirmButton { deleteConfirmationDialog.$("input", type: "submit") }

        alertDivText { $(".alert").attr("innerHTML") }
    }
}
