package de.iteratec.osm.result

import de.iteratec.osm.api.dto.ApplicationCsiDto
import de.iteratec.osm.api.dto.PageCsiDto
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupService
import de.iteratec.osm.util.ControllerUtils
import grails.validation.Validateable
import grails.converters.JSON

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
        def csiValues = applicationDashboardService.getTodaysCsiValueForJobGroups(jobGroups)
        return ControllerUtils.sendObjectAsJSON(response, csiValues)
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


    /**
     * Rest controller that returns all jobs where at least one of the last five measurements failed.
     * @return JSON in the form of:
     * [
     *     {
     *         "percentageFailLast5": 40,
     *         "location": "otto-prod-netlab",
     *         "application": "develop_Desktop",
     *         "job_id": 773,
     *         "script": "OTTO_PL_Einstiegsseite",
     *         "browser": "Chrome"
     *     }, ...
     * ]
     */
    def getFailingJobs() {
        def jobsWithErrors = applicationDashboardService.getFailingJobs()
        return ControllerUtils.sendObjectAsJSON(response, jobsWithErrors)
    }
}

class DefaultApplicationCommand implements Validateable {
    Long applicationId

    static constraints = {
        applicationId(nullable: false)
    }
}
