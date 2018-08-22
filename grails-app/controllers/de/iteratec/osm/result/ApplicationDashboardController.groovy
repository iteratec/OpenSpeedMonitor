package de.iteratec.osm.result

import de.iteratec.osm.api.dto.ApplicationCsiDto
import de.iteratec.osm.api.dto.CsiDto
import de.iteratec.osm.api.dto.PageCsiDto
import de.iteratec.osm.csi.JobGroupCsiAggregationService
import de.iteratec.osm.csi.Page
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

    def getPagesForApplication(PagesForApplicationCommand command) {

        DateTime from = new DateTime().minusWeeks(FOUR_WEEKS)
        DateTime to = new DateTime()
        Long jobGroupId = command.applicationId
        def pages = applicationDashboardService.getPagesWithResultsOrActiveJobsForJobGroup(from, to, jobGroupId)

        return ControllerUtils.sendObjectAsJSON(response, pages)
    }

    def getCsiValuesForApplication(PagesForApplicationCommand command) {
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

    def getCsiValuesForPages(PagesForApplicationCommand command) {
        JobGroup selectedJobGroup = JobGroup.findById(command.applicationId)
        List<PageCsiDto> pageCsiDtos = []
        if (selectedJobGroup.hasCsiConfiguration()) {
            pageCsiDtos = applicationDashboardService.getMostRecentCsiForPagesOfJobGroup(selectedJobGroup)
        }
        return ControllerUtils.sendObjectAsJSON(response, pageCsiDtos)
    }

    def getMetricsForApplication(PagesForApplicationCommand command) {
        Long jobGroupId = command.applicationId
        List<Map> recentMetrics = applicationDashboardService.getRecentMetricsForJobGroup(jobGroupId).collect {
            it.projectedProperties.pageName = Page.findById(it.pageId).name
            return it.projectedProperties
        }
        return ControllerUtils.sendObjectAsJSON(response, recentMetrics)
    }

    def getAllActiveAndAllRecent() {
        def allActiveAndRecent = jobGroupService.getAllActiveAndAllRecent()

        return ControllerUtils.sendObjectAsJSON(response, allActiveAndRecent)
    }
}

class PagesForApplicationCommand implements Validateable {
    Long applicationId

    static constraints = {
        applicationId(nullable: false)
    }
}
