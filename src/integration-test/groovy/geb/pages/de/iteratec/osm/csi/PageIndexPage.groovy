package geb.pages.de.iteratec.osm.csi

import de.iteratec.osm.csi.Page
import geb.pages.de.iteratec.osm.I18nGebPage

class PageIndexPage extends I18nGebPage {
    static url = getUrl("/page/index")

    static at = {
        title == getI18nMessage("default.list.label", [Page.simpleName])

        $("#Menu .active a").attr("href").contains("/page/index")
    }

    static content = {
        successDiv { $("div", class: "alert alert-info") }
        successDivText { successDiv[0].attr("innerHTML") }

        pageTableRows { $("#list-page").$("tbody").$("tr") }

        pageButtons { $(".pagination").$("li") }
    }
}
