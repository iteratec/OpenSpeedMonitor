package de.iteratec.osm.result

import de.iteratec.osm.util.ControllerUtils
import grails.validation.Validateable
import org.joda.time.DateTime

class ApplicationDashboardController {

    final static FOUR_WEEKS = 4

    ApplicationDashboardService applicationDashboardService

    def getPagesForApplication(PagesForApplicationCommand command) {

        DateTime from = new DateTime().minusWeeks(FOUR_WEEKS)
        DateTime to = new DateTime()
        Long jobGroupId = command.applicationId
        def pages = applicationDashboardService.getPagesWithResultsOrActiveJobsForJobGroup(from, to, jobGroupId)

        return ControllerUtils.sendObjectAsJSON(response, pages)
    }

    def getMetricsForApplication(PagesForApplicationCommand command) {
        Long jobGroupId = command.applicationId
        List<Map> recentMetrics = applicationDashboardService.getRecentMetricsForJobGroup(jobGroupId).collect {
            it.projectedProperties
        }

        return ControllerUtils.sendObjectAsJSON(response, recentMetrics)
    }
}

class PagesForApplicationCommand implements Validateable {
    Long applicationId

    static constraints = {
        applicationId(nullable: false)
    }
}
