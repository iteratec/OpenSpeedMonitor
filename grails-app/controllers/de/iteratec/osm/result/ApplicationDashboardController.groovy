package de.iteratec.osm.result

import de.iteratec.osm.api.dto.ApplicationCsiDto
import de.iteratec.osm.api.dto.CsiDto
import de.iteratec.osm.api.dto.PageCsiDto
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.csi.JobGroupCsiAggregationService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupService
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.util.ControllerUtils
import grails.validation.Validateable
import org.joda.time.DateTime

class ApplicationDashboardController {
    final static FOUR_WEEKS = 4

    ApplicationDashboardService applicationDashboardService
    JobGroupCsiAggregationService jobGroupCsiAggregationService
    JobGroupService jobGroupService

    def getPagesForApplication(DefaultApplicationCommand command) {

        DateTime from = new DateTime().minusWeeks(FOUR_WEEKS)
        DateTime to = new DateTime()
        Long jobGroupId = command.applicationId
        def pages = applicationDashboardService.getPagesWithResultsOrActiveJobsForJobGroup(from, to, jobGroupId)

        return ControllerUtils.sendObjectAsJSON(response, pages)
    }

    def getCsiValuesForApplication(DefaultApplicationCommand command) {
        ApplicationCsiDto applicationCsiListDto = new ApplicationCsiDto()
        JobGroup selectedJobGroup = JobGroup.findById(command.applicationId)

        if (selectedJobGroup.hasCsiConfiguration()) {
            applicationCsiListDto.hasCsiConfiguration = true

            DateTime todayDateTime = new DateTime().withTimeAtStartOfDay()
            Date today = todayDateTime.toDate()
            Date fourWeeksAgo = todayDateTime.minusWeeks(FOUR_WEEKS).toDate()

            List<JobGroup> csiGroups = [selectedJobGroup]
            CsiAggregationInterval dailyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)

            List<CsiDto> csiDtoList = []

            jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(fourWeeksAgo, today, dailyInterval, csiGroups).each {
                CsiDto applicationCsiDto = new CsiDto()
                if (it.csByWptDocCompleteInPercent && it.csByWptVisuallyCompleteInPercent) {
                    applicationCsiDto.date = it.started.format("yyyy-MM-dd")
                    applicationCsiDto.csiDocComplete = it.csByWptDocCompleteInPercent
                    applicationCsiDto.csiVisComplete = it.csByWptVisuallyCompleteInPercent
                    csiDtoList << applicationCsiDto
                }
            }

            if (!csiDtoList) {
                List<JobResult> jobResults = JobResult.findAllByJobInListAndDateGreaterThan(Job.findAllByJobGroup(selectedJobGroup), fourWeeksAgo)
                if (jobResults) {
                    applicationCsiListDto.hasJobResults = true
                    applicationCsiListDto.hasInvalidJobResults = jobResults.every {it.wptStatus ? true : false}
                }
                else {
                    applicationCsiListDto.hasJobResults = false
                }
            }

            applicationCsiListDto.csiDtoList = csiDtoList
            return ControllerUtils.sendObjectAsJSON(response, applicationCsiListDto)

        } else {
            applicationCsiListDto.hasCsiConfiguration = false
            applicationCsiListDto.csiDtoList = []
            return ControllerUtils.sendObjectAsJSON(response, applicationCsiListDto)
        }
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

    def getAllActiveAndAllRecent() {
        def allActiveAndRecent = jobGroupService.getAllActiveAndAllRecent()

        return ControllerUtils.sendObjectAsJSON(response, allActiveAndRecent)
    }

    def createCsiConfiguration (DefaultApplicationCommand command) {
        Long jobGroupId = command.applicationId
        JobGroup jobGroup = JobGroup.findById(jobGroupId)

        if (jobGroup.hasCsiConfiguration()){
            return ControllerUtils.sendObjectAsJSON(response, [csiConfigurationId: jobGroup.csiConfiguration.id])
        }
        CsiConfiguration csiConfiguration
        csiConfiguration = CsiConfiguration.findByLabel(jobGroup.name)
        if (!csiConfiguration) {
            csiConfiguration = new CsiConfiguration(
                    label: jobGroup.name,
                    description: "Initial CSI configuration for JobGroup ${jobGroup.name}",
                    csiDay: CsiDay.findAll()[0]
            )
        }
        jobGroup.csiConfiguration = csiConfiguration
        jobGroup.save(failOnError: true, flush: true)
        return ControllerUtils.sendObjectAsJSON(response, [csiConfigurationId : csiConfiguration.id])
    }
}

class DefaultApplicationCommand implements Validateable {
    Long applicationId

    static constraints = {
        applicationId(nullable: false)
    }
}
