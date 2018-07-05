package de.iteratec.osm.result

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.EventResultQueryBuilder
import grails.gorm.transactions.Transactional
import org.joda.time.DateTime

@Transactional
class ApplicationDashboardService {

    OsmConfigCacheService osmConfigCacheService
    ResultSelectionService resultSelectionService

    def getPagesWithResultsOrActiveJobsForJobGroup(DateTime from, DateTime to, Long jobGroupId) {
        def pagesWithResults = getPagesWithExistingEventResults(from, to, jobGroupId)
        def pagesOfActiveJobs = getPagesOfActiveJobs(jobGroupId)


        def pages = (pagesWithResults + pagesOfActiveJobs).collect {
            [
                    'id'  : it.id,
                    'name': it.name
            ]
        } as Set
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

        Date from = new DateTime().minusWeeks(4).toDate()
        Date to = new DateTime().toDate()
        return new EventResultQueryBuilder(osmConfigCacheService.getMinValidLoadtime(), osmConfigCacheService.getMaxValidLoadtime())
                .withJobGroupIdsIn([jobGroupId], false)
                .withProjectedIdForAssociatedDomain('page')
                .withJobResultDateBetween(from, to)
                .withSelectedMeasurands([bytesFullyLoaded, speedIndex, docCompleteTime])
                .getAverageData()
    }
}
