package de.iteratec.osm.result


import de.iteratec.osm.api.dto.ApplicationCsiDto
import de.iteratec.osm.api.dto.PageCsiDto
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.wptserver.Protocol
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupService
import de.iteratec.osm.report.external.GraphiteServer
import de.iteratec.osm.report.external.provider.GraphiteSocketProvider
import de.iteratec.osm.util.ControllerUtils
import grails.validation.Validateable
import org.springframework.http.HttpStatus

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

    def getPerformanceAspectsForApplication(PerformanceAspectManagementRequestCommand command) {
        Long jobGroupId = command.applicationId
        Long pageId = command.pageId
        List<Map> performanceAspects = applicationDashboardService.getPerformanceAspectsForJobGroup(jobGroupId, pageId)

        return ControllerUtils.sendObjectAsJSON(response, performanceAspects)
    }

    def createOrUpdatePerformanceAspect(PerformanceAspectCreationCommand command){
        PerformanceAspectType performanceAspectType = PerformanceAspectType.valueOf(command.performanceAspectType)
        SelectedMeasurand metric = new SelectedMeasurand(command.metricIdentifier, CachedView.UNCACHED)
        Page page = Page.findById(command.pageId)
        JobGroup jobGroup = JobGroup.findById(command.applicationId)

        PerformanceAspect performanceAspect
        if(!command.performanceAspectId){
            performanceAspect = new PerformanceAspect(performanceAspectType: performanceAspectType, page: page,jobGroup: jobGroup)
        } else {
            performanceAspect = PerformanceAspect.findById(command.performanceAspectId)
        }

        try{
            performanceAspect.metric = metric
            performanceAspect = performanceAspect.save(flush: true, failOnError: true)
            Map performanceAspectDto = [:]
            performanceAspectDto.id = performanceAspect.id
            performanceAspectDto.measurand = [name: performanceAspect.metric.name, id: performanceAspect.metric.optionValue]
            performanceAspectDto.performanceAspectType = performanceAspect.performanceAspectType.toString()
            performanceAspectDto.pageId = performanceAspect.page.id
            performanceAspectDto.jobGroupId = performanceAspect.jobGroup.id
            return ControllerUtils.sendObjectAsJSON(response, performanceAspectDto)
        } catch (Exception e) {
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, e.toString())
        }
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

    def getActiveJobHealthGraphiteServers(DefaultApplicationCommand command) {
        Long jobGroupId = command.applicationId
        def jobHealthGraphiteServers = applicationDashboardService.getActiveJobHealthGraphiteServers(jobGroupId)

        return ControllerUtils.sendObjectAsJSON(response, jobHealthGraphiteServers)
    }

    def getAvailableGraphiteServers(DefaultApplicationCommand command) {
        Long jobGroupId = command.applicationId
        Collection<GraphiteServer> availableGraphiteServers = applicationDashboardService.getAvailableGraphiteServers(jobGroupId)

        return ControllerUtils.sendObjectAsJSON(response, availableGraphiteServers)
    }

    def saveJobHealthGraphiteServers(DefaultApplicationCommand command) {
        Long jobGroupId = command.applicationId
        List<Long> graphiteServerIds = command.graphiteServerIds
        def message = applicationDashboardService.saveJobHealthGraphiteServers(jobGroupId, graphiteServerIds)

        return ControllerUtils.sendObjectAsJSON(response, message)
    }

    def removeJobHealthGraphiteServers(DefaultApplicationCommand command) {
        Long jobGroupId = command.applicationId
        List<Long> graphiteServerIds = command.graphiteServerIds
        def message = applicationDashboardService.removeJobHealthGraphiteServers(jobGroupId, graphiteServerIds)

        return ControllerUtils.sendObjectAsJSON(response, message)
    }

    def createGraphiteServer() {
        try {
            GraphiteServer server = new GraphiteServer(
                    serverAdress: params.address,
                    port: Integer.parseInt(params.port),
                    webappPathToRenderingEngine: "render",
                    webappProtocol: Protocol.HTTPS,
                    reportProtocol: GraphiteSocketProvider.Protocol.valueOf(params.protocol),
                    webappUrl: params.webAppAddress,
                    prefix: params.prefix
            )
            server.save(failOnError: true, flush: true)
            return ControllerUtils.sendObjectAsJSON(response, [success: true, id: server.id])
        }
        catch(e) {
            return ControllerUtils.sendObjectAsJSON(response, [success: false, id: null])
        }
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
        return ControllerUtils.sendObjectAsJSON(response, applicationDashboardService.getFailingJobs());
    }
}

class DefaultApplicationCommand implements Validateable {
    Long applicationId
    List<Long> graphiteServerIds

    static constraints = {
        applicationId(nullable: false)
    }
}

class PerformanceAspectCreationCommand implements Validateable {
    Long performanceAspectId
    Long applicationId
    Long pageId
    String metricIdentifier
    String performanceAspectType

    static constraints = {
        performanceAspectId(nullable: true)
        applicationId(nullable: false)
        pageId(nullable: false)
        metricIdentifier(nullable: false)
        performanceAspectType(nullable: false)
    }
}

class PerformanceAspectManagementRequestCommand implements Validateable {
    Long applicationId
    Long pageId

    static constraints = {
        applicationId(nullable: false)
        pageId(nullable: false)
    }
}
