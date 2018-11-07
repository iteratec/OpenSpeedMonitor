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
import de.iteratec.osm.measurement.schedule.JobGroupService
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
    PageCsiAggregationService pageCsiAggregationService
    JobGroupCsiAggregationService jobGroupCsiAggregationService
    JobDaoService jobDaoService
    JobGroupService jobGroupService

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

    private List<PageCsiDto> getAllCsiForPagesOfJobGroup(JobGroup jobGroup) {

        List<PageCsiDto> pageCsiDtos = []
        List<JobGroup> csiGroup = [jobGroup]
        DateTime to = new DateTime()
        DateTime from = configService.getStartDateForRecentMeasurements()
        CsiAggregationInterval dailyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)

        List<Page> pages = jobGroupService.getPagesWithResultsOrActiveJobsForJobGroup(jobGroup.id)

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

        jobGroupService.getPagesWithResultsOrActiveJobsForJobGroup(jobGroupId)
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
        Date fourWeeksAgo = configService.getStartDateForRecentMeasurements().toDate()
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
        dto.hasJobResults = true
        ArrayList<CsiDto> csiDtos = new ArrayList<CsiDto>()
        csiAggregations.each {
            if (it.csByWptDocCompleteInPercent) {
                CsiDto csiDto = new CsiDto()
                csiDto.date = it.started.format("yyyy-MM-dd")
                csiDto.csiDocComplete = it.csByWptDocCompleteInPercent
                csiDto.csiVisComplete = it.csByWptVisuallyCompleteInPercent
                csiDtos << csiDto
            }
        }
        dto.csiValues = csiDtos
        if (!dto.csiValues.length) {
            List<Job> jobs = jobDaoService.getJobs(JobGroup.findById(jobGroupId))
            List<JobResult> jobResults = JobResult.findAllByJobInListAndDateGreaterThan(jobs, startDate)
            if (jobResults) {
                dto.hasJobResults = true
                dto.hasInvalidJobResults = jobResults.every { it.jobResultStatus.isFailed() }
            } else {
                dto.hasJobResults = false
            }
        }
        return dto
    }

    def getFailingJobStatistics(Long jobGroupId) {
        def jobsWithErrors = Job.createCriteria().get {
            projections {
                countDistinct 'id'
                jobStatistic {
                    min 'percentageSuccessfulTestsOfLast5'
                }
            }
            and {
                eq 'jobGroup.id', jobGroupId
                eq 'deleted', false
                eq 'active', true
                jobStatistic {
                    lt 'percentageSuccessfulTestsOfLast5', 90d
                }
            }
        }
        return jobsWithErrors
    }
}
