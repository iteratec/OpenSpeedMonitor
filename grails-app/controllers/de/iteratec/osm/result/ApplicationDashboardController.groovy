package de.iteratec.osm.result

import de.iteratec.osm.api.dto.ApplicationCsiDto
import de.iteratec.osm.api.dto.CsiDto
import de.iteratec.osm.csi.JobGroupCsiAggregationService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.util.ControllerUtils
import grails.validation.Validateable
import org.joda.time.DateTime

class ApplicationDashboardController {

    final static FOUR_WEEKS = 4

    JobGroupCsiAggregationService jobGroupCsiAggregationService

    def getCsiValuesForApplication(ApplicationCommand command) {
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

            applicationCsiListDto.csiDtoList = csiDtoList
            return ControllerUtils.sendObjectAsJSON(response, applicationCsiListDto)

        } else {
            applicationCsiListDto.hasCsiConfiguration = false
            applicationCsiListDto.csiDtoList = []
            return ControllerUtils.sendObjectAsJSON(response, applicationCsiListDto)
        }
    }
}

class ApplicationCommand implements Validateable {
    Long applicationId

    static constraints = {
        applicationId(nullable: false)
    }
}