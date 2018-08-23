package de.iteratec.osm.result

import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.api.dto.PageCsiDto
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.PageCsiAggregationService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.EventResultQueryBuilder
import grails.gorm.transactions.Transactional
import org.joda.time.DateTime

@Transactional
class ApplicationDashboardService {


    ConfigService configService
    OsmConfigCacheService osmConfigCacheService
    ResultSelectionService resultSelectionService
    PageCsiAggregationService pageCsiAggregationService

    def getPagesWithResultsOrActiveJobsForJobGroup(DateTime from, DateTime to, Long jobGroupId) {
        def pagesWithResults = getPagesWithExistingEventResults(from, to, jobGroupId)
        def pagesOfActiveJobs = getPagesOfActiveJobs(jobGroupId)

        List<Page> pages = (pagesWithResults + pagesOfActiveJobs).collect()
        pages.unique()
        return pages

    }

    def getPagesWithExistingEventResults(DateTime from, DateTime to, Long jobGroupId) {

        ResultSelectionCommand pagesForGivenJobGroup = new ResultSelectionCommand(
                jobGroupIds: [jobGroupId],
                from: from,
                to: to
        )

        def pages = resultSelectionService.query(pagesForGivenJobGroup, ResultSelectionController.ResultSelectionType.Pages, { existing ->
            if (existing) {
                not { 'in'('page', existing) }
            }
            projections {
                distinct('page')
            }
        })

        return pages
    }

    def getPagesOfActiveJobs(Long jobGroupId) {
        def scriptsForJobGroup = Job.createCriteria().list {
            eq('jobGroup.id', jobGroupId)
            eq('active', true)
            isNotNull('script')
            projections {
                property('script')
            }
        }

        def pages = scriptsForJobGroup.collect {
            it.testedPages
        }.flatten()

        return pages
    }

    List<EventResultProjection> getRecentMetricsForJobGroup(Long jobGroupId) {
        SelectedMeasurand bytesFullyLoaded = new SelectedMeasurand(Measurand.FULLY_LOADED_INCOMING_BYTES.toString(), CachedView.UNCACHED)
        SelectedMeasurand speedIndex = new SelectedMeasurand(Measurand.SPEED_INDEX.toString(), CachedView.UNCACHED)
        SelectedMeasurand docCompleteTime = new SelectedMeasurand(Measurand.DOC_COMPLETE_TIME.toString(), CachedView.UNCACHED)

        Date from = new DateTime().minusHours(configService.getMaxAgeForMetricsInHours()).toDate()
        Date to = new DateTime().toDate()

        return new EventResultQueryBuilder(osmConfigCacheService.getMinValidLoadtime(), osmConfigCacheService.getMaxValidLoadtime())
                .withJobGroupIdsIn([jobGroupId], false)
                .withProjectedIdForAssociatedDomain('page')
                .withJobResultDateBetween(from, to)
                .withoutPagesIn([Page.findByName(Page.UNDEFINED)])
                .withSelectedMeasurands([bytesFullyLoaded, speedIndex, docCompleteTime])
                .getAverageData()
    }

    List<Page> getRecentPagesForJobGroup(Long jobGroupId) {
        DateTime from = new DateTime().minusHours(configService.getMaxAgeForMetricsInHours())
        DateTime to = new DateTime()

        List<Page> pages = getPagesWithResultsOrActiveJobsForJobGroup(from, to, jobGroupId)
        return pages
    }

    private List<PageCsiDto> getAllCsiForPagesOfJobGroup(JobGroup jobGroup) {

        List<PageCsiDto> pageCsiDtos = []
        List<JobGroup> csiGroup = [jobGroup]
        DateTime to = new DateTime().withTimeAtStartOfDay()
        DateTime from = to.minusWeeks(4)
        CsiAggregationInterval dailyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)

        List<Page> pages = getPagesWithResultsOrActiveJobsForJobGroup(from, to, jobGroup.id)

        pageCsiAggregationService.getOrCalculatePageCsiAggregations(from.toDate(), to.toDate(), dailyInterval,
                csiGroup, pages).each {
            PageCsiDto pageCsiDto = new PageCsiDto()
            if (it.csByWptDocCompleteInPercent && it.csByWptVisuallyCompleteInPercent) {
                pageCsiDto.pageId = it.page.id
                pageCsiDto.date = it.started.format("yyy-MM-dd")
                pageCsiDto.csiDocComplete = it.csByWptDocCompleteInPercent
                pageCsiDto.csiVisComplete = it.csByWptVisuallyCompleteInPercent
                pageCsiDtos << pageCsiDto
            }
        }
        return pageCsiDtos
    }

    List<PageCsiDto> getMostRecentCsiForPagesOfJobGroup(JobGroup jobGroup) {
        List<PageCsiDto> recentCsi = getAllCsiForPagesOfJobGroup(jobGroup)
        recentCsi.sort {
            a, b -> b.date <=> a.date
        }
        recentCsi.unique()
        return recentCsi

    }

}
