package geb.pages.de.iteratec.osm.csi

import de.iteratec.osm.csi.Page
import geb.pages.de.iteratec.osm.I18nGebPage

class PageCreatePage extends I18nGebPage{
    static url = getUrl("/page/create")

    static at = {
        title == getI18nMessage("default.create.label", [Page.simpleName])
    }

    static content = {
        pageNameTextField { $("#name") }
        pageWeightTextField { $("#weight") }

        createPageButton(to: [PageCreatePage, PageShowPage]) { $("#create") }

        errorMessageBox { $("div", class: "alert alert-danger") }
        errorMessageBoxText { errorMessageBox.attr("innerHTML") }
    }
}
