package de.iteratec.osm.result

import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.api.dto.ApplicationCsiDto
import de.iteratec.osm.api.dto.CsiDto
import de.iteratec.osm.api.dto.PageCsiDto
import de.iteratec.osm.csi.*
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation
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
    JobGroupCsiAggregationService jobGroupCsiAggregationService
    JobDaoService jobDaoService


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

    List<Map> getAllActivePagesAndMetrics(Long jobGroupId) {
        List<Map> recentMetrics = getRecentMetricsForJobGroup(jobGroupId).collect {
            return it.projectedProperties
        }

        getPagesOfActiveJobs(jobGroupId)
                .findAll { Page page -> page.name != Page.UNDEFINED }
                .each { Page page ->
            Map entry = recentMetrics.find {
                it.pageId == page.id
            }
            if (!entry) {
                recentMetrics.add(
                        [
                                'pageId'  : page.id,
                                'pageName': page.name
                        ]
                )
            } else {
                entry.pageName = page.name
            }
        }

        return recentMetrics
    }

    def createOrReturnCsiConfiguration(Long jobGroupId) {
        JobGroup jobGroup = JobGroup.findById(jobGroupId)

        if (jobGroup.hasCsiConfiguration()) {
            return jobGroup.csiConfiguration.id
        }
        CsiConfiguration csiConfiguration
        csiConfiguration = CsiConfiguration.findByLabel(jobGroup.name)
        if (!csiConfiguration) {
            csiConfiguration = new CsiConfiguration(
                    label: jobGroup.name,
                    description: "Initial CSI configuration for JobGroup ${jobGroup.name}",
                    csiDay: CsiDay.first()
            )
        }
        jobGroup.csiConfiguration = csiConfiguration
        jobGroup.save(failOnError: true, flush: true)
        return csiConfiguration.id
    }

    ApplicationCsiDto getCsiValuesAndErrorsForJobGroup(JobGroup jobGroup) {
        Date fourWeeksAgo = new DateTime().withTimeAtStartOfDay().minusWeeks(4).toDate()
        Map<Long, ApplicationCsiDto> csiValuesForJobGroups = getCsiValuesForJobGroupsSince([jobGroup], fourWeeksAgo)
        return csiValuesForJobGroups.get(jobGroup.id)
    }

    Map<Long, ApplicationCsiDto> getTodaysCsiValueForJobGroups(List<JobGroup> jobGroups) {
        Date today = new DateTime().withTimeAtStartOfDay().toDate()
        return getCsiValuesForJobGroupsSince(jobGroups, today)
    }

    private Map<Long, ApplicationCsiDto> getCsiValuesForJobGroupsSince(List<JobGroup> jobGroups, Date startDate) {

        DateTime todayDateTime = new DateTime().withTimeAtStartOfDay()
        Date today = todayDateTime.toDate()

        CsiAggregationInterval dailyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)

        Map<Boolean, JobGroup[]> jobGroupsByExistingCsiConfiguration = jobGroups.groupBy { jobGroup ->
            new Boolean(jobGroup.hasCsiConfiguration())
        } as Map<Boolean, JobGroup[]>

        Map<Long, ApplicationCsiDto> dtosById = [:]
        if (jobGroupsByExistingCsiConfiguration[false]) {
            dtosById.putAll(jobGroupsByExistingCsiConfiguration[false].collectEntries { jobGroup ->
                [(jobGroup.id): ApplicationCsiDto.createWithoutConfiguration()]
            })
        }

        List<JobGroup> csiGroups = jobGroupsByExistingCsiConfiguration[true]
        if (csiGroups) {
            Map<Long, ApplicationCsiDto> dtosByIdWithValues = jobGroupCsiAggregationService
                    .getOrCalculateShopCsiAggregations(startDate, today, dailyInterval, csiGroups)
                    .groupBy { csiAggregation -> csiAggregation.jobGroup.id }
                    .collectEntries { jobGroupId, csiAggregations ->
                [(jobGroupId): csiAggregationsToDto(jobGroupId, csiAggregations, startDate)]
            } as Map<Long, ApplicationCsiDto>
            dtosById.putAll(dtosByIdWithValues)
        }
        return dtosById
    }

    private ApplicationCsiDto csiAggregationsToDto(Long jobGroupId, List<CsiAggregation> csiAggregations, Date startDate) {
        ApplicationCsiDto dto = new ApplicationCsiDto()
        dto.hasCsiConfiguration = true
        dto.csiValues = new ArrayList<CsiDto>()
        csiAggregations.each {
            if (it.csByWptDocCompleteInPercent) {
                CsiDto csiDto = new CsiDto()
                csiDto.date = it.started.format("yyyy-MM-dd")
                csiDto.csiDocComplete = it.csByWptDocCompleteInPercent
                csiDto.csiVisComplete = it.csByWptVisuallyCompleteInPercent
                dto.csiValues << csiDto
            }
        }
        if (!dto.csiValues.length) {
            List<Job> jobs = jobDaoService.getJobs(JobGroup.findById(jobGroupId))
            List<JobResult> jobResults = JobResult.findAllByJobInListAndDateGreaterThan(jobs, startDate)
            if (jobResults) {
                dto.hasJobResults = true
                dto.hasInvalidJobResults = jobResults.every { WptStatus.isFailed(it.httpStatusCode) }
            } else {
                dto.hasJobResults = false
            }
        }
        return dto
    }
}
