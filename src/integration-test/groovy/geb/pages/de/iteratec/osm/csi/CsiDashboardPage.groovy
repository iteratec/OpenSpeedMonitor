package geb.pages.de.iteratec.osm.csi

import geb.Page

/**
 * Created by marko on 07.07.16.
 */
class CsiDashboardPage extends Page{
    static url = "/csiDashboard/showAll"

    static at = { title == "Dashboard" }

    static content = {
        timeFrameSelect{$("#timeframeSelect").find("option").contextElements[0]}
        showButton (to: CsiDashboardPage) {$("#chart-submit")}
        fromDatepicker{$("#fromDatepicker")}
        toDatepicker{$("#toDatepicker")}


    }
}
