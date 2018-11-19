package de.iteratec.osm.result

import de.iteratec.osm.api.dto.ApplicationCsiDto
import de.iteratec.osm.api.dto.PageCsiDto
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupService
import de.iteratec.osm.report.external.GraphiteServer
import de.iteratec.osm.util.ControllerUtils
import grails.validation.Validateable
import org.hibernate.criterion.CriteriaSpecification

class ApplicationDashboardController {

    ApplicationDashboardService applicationDashboardService
    JobGroupService jobGroupService

    def getPagesForApplication(DefaultApplicationCommand command) {

        Long jobGroupId = command.applicationId
        def pages = jobGroupService.getPagesWithResultsOrActiveJobsForJobGroup(jobGroupId)

        return ControllerUtils.sendObjectAsJSON(response, pages)
    }

    def getCsiValuesForApplication(DefaultApplicationCommand command) {
        JobGroup selectedJobGroup = JobGroup.findById(command.applicationId)
        ApplicationCsiDto applicationCsiListDto = applicationDashboardService.getCsiValuesAndErrorsForJobGroup(selectedJobGroup)
        return ControllerUtils.sendObjectAsJSON(response, applicationCsiListDto)
    }

    def getCsiValuesForPages(DefaultApplicationCommand command) {
        JobGroup selectedJobGroup = JobGroup.findById(command.applicationId)
        List<PageCsiDto> pageCsiDtos = []
        if (selectedJobGroup.hasCsiConfiguration()) {
            pageCsiDtos = applicationDashboardService.getMostRecentCsiForPagesOfJobGroup(selectedJobGroup)
        }
        return ControllerUtils.sendObjectAsJSON(response, pageCsiDtos)
    }

    def getMetricsForApplication(DefaultApplicationCommand command) {
        Long jobGroupId = command.applicationId

        List<Map> activePagesAndMetrics = applicationDashboardService.getAllActivePagesAndMetrics(jobGroupId)

        return ControllerUtils.sendObjectAsJSON(response, activePagesAndMetrics)
    }

    def getApplications() {
        def allActiveAndRecent = jobGroupService.getAllActiveAndRecentWithResultInformation()
        return ControllerUtils.sendObjectAsJSON(response, allActiveAndRecent)
    }

    def getCsiValuesForApplications() {
        List<JobGroup> jobGroups = jobGroupService.getAllActiveAndRecent()
        def csiVales = applicationDashboardService.getTodaysCsiValueForJobGroups(jobGroups)
        return ControllerUtils.sendObjectAsJSON(response, csiVales)
    }

    def createCsiConfiguration(DefaultApplicationCommand command) {
        Long jobGroupId = command.applicationId
        Long csiConfigurationId = applicationDashboardService.createOrReturnCsiConfiguration(jobGroupId)

        return ControllerUtils.sendObjectAsJSON(response, [csiConfigurationId: csiConfigurationId])
    }

    def getFailingJobStatistics(DefaultApplicationCommand command) {
        Long jobGroupId = command.applicationId
        def jobsWithErrors = applicationDashboardService.getFailingJobStatistics(jobGroupId)

        return ControllerUtils.sendObjectAsJSON(response, [numberOfFailingJobs: jobsWithErrors[0], minimumFailedJobSuccessRate: jobsWithErrors[1]])
    }

    def getJobHealthGraphiteServers(DefaultApplicationCommand command) {
        Long jobGroupId = command.applicationId
        def jobHealthGraphiteServers = applicationDashboardService.getJobHealthGraphiteServers(jobGroupId)

        return ControllerUtils.sendObjectAsJSON(response, jobHealthGraphiteServers)
    }
}

class DefaultApplicationCommand implements Validateable {
    Long applicationId

    static constraints = {
        applicationId(nullable: false)
    }
}
