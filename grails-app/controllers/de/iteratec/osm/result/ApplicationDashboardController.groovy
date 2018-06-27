package de.iteratec.osm.result

import de.iteratec.osm.util.ControllerUtils
import grails.validation.Validateable
import org.joda.time.DateTime

class ApplicationDashboardController {

    ApplicationDashboardService applicationDashboardService

    def getPagesForJobGroup(PagesForJobGroupCommand command) {

        DateTime today = new DateTime()
        DateTime fourWeeksAgo = new DateTime().minusWeeks(4)

        def pagesWithResults = applicationDashboardService.getPagesWithExistingEventResults(fourWeeksAgo, today, command.jobGroupId)
        def pagesOfActiveJobs = applicationDashboardService.getPagesOfActiveJobs(command.jobGroupId)


        def pages = (pagesWithResults + pagesOfActiveJobs).collect {
            [
                    'id'  : it.id,
                    'name': it.name
            ]
        } as Set
        pages.unique()

        return ControllerUtils.sendObjectAsJSON(response, pages)
    }
}

class PagesForJobGroupCommand implements Validateable {
    Long jobGroupId

    static constraints = {
        jobGroupId(nullable: false)
    }
}
