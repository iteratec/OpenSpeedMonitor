package geb.pages.de.iteratec.osm.measurement.environment

import geb.pages.de.iteratec.osm.I18nGebPage

class BrowserIndexPage extends I18nGebPage {
    static url = getUrl("/browser/index")

    static at = {
        title == getI18nMessage("default.list.label", [getI18nMessage("browser.label")])

        $("#Menu .active a").attr("href").contains("/browser/index")
    }

    static content = {

        successDiv { $("div", class: "alert alert-info") }
        successDivText { successDiv[0].attr("innerHTML") }

        browserTableRows { $("#list-browser").$("tbody").$("tr") }

        pageButtons { $(".pagination").$("li") }
    }
}
