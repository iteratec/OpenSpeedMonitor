package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup

class ResultSelectionInformation {

    Date jobResultDate
    JobGroup jobGroup
    Page page
    MeasuredEvent measuredEvent
    Location location
    Browser browser
    ConnectivityProfile connectivityProfile
    String customConnectivityName
    boolean noTrafficShapingAtAll

    static constraints = {
        measuredEvent(nullable: false)
        jobGroup(nullable: false)
        browser(nullable: false)
        page(nullable: false)
        location(nullable: false)
    }
}
