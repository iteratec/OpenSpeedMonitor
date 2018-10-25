package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.ConfigService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.ResultSelectionCommand
import de.iteratec.osm.result.ResultSelectionController
import de.iteratec.osm.result.ResultSelectionService
import grails.gorm.transactions.Transactional
import org.joda.time.DateTime

@Transactional
class JobGroupService {
    ResultSelectionService resultSelectionService
    ConfigService configService

    Set<JobGroup> findAll() {
        Set<JobGroup> result = Collections.checkedSet(new HashSet<JobGroup>(), JobGroup.class);
        result.addAll(JobGroup.list());
        return Collections.unmodifiableSet(result);
    }

    Set<JobGroup> findCSIGroups() {
        Set<JobGroup> result = Collections.checkedSet(new HashSet<JobGroup>(), JobGroup.class);
        result.addAll(JobGroup.findAllByCsiConfigurationIsNotNull());
        return Collections.unmodifiableSet(result);
    }

    List<String> getAllUniqueTags() {
        return JobGroup.allTags
    }

    List<String> getMaxUniqueTags(int maxNumberOfTags) {
        return JobGroup.findAllTagsWithCriteria([max: maxNumberOfTags]) {}
    }

    Map<String, List<String>> getTagToJobGroupNameMap() {
        return getAllUniqueTags().inject([:]) { map, tag ->
            map[tag] = JobGroup.findAllByTag(tag)*.name
            return map
        }
    }

    List<JobGroup> getAllActiveJobGroups() {
        return Job.createCriteria().list {
            eq('active', true)
            projections {
                distinct('jobGroup')
            }
        }
    }

    def getAllActiveAndRecentWithResultInformation() {
        return getAllActiveAndRecent().collect {
            def name = it.name
            JobResult lastDateOfResult = (JobResult) JobResult.createCriteria().list(max: 1) {
                eq("jobGroupName", name)
                order("date", "desc")
            }[0]
            def formattedLastDateOfResult = lastDateOfResult?.date?.format("yyyy-MM-dd")
            return [
                    id                : it.id,
                    name              : it.name,
                    dateOfLastResults : formattedLastDateOfResult,
                    csiConfigurationId: it.csiConfigurationId,
                    pageCount         : getPagesWithResultsOrActiveJobsForJobGroup(it.id)?.size()
            ]
        }
    }

    List<JobGroup> getAllActiveAndRecent() {
        List<JobGroup> allActiveAndRecent = getAllActiveJobGroups()
        DateTime today = new DateTime()
        DateTime fourWeeksAgo = configService.getStartDateForRecentMeasurements()

        ResultSelectionCommand queryLastFourWeeks = new ResultSelectionCommand(from: fourWeeksAgo, to: today)
        List<JobGroup> recentJobGroups = (List<JobGroup>) resultSelectionService.query(queryLastFourWeeks, ResultSelectionController.ResultSelectionType.JobGroups, { existing ->
            if (existing) {
                not { 'in'('jobGroup', existing) }
            }
            projections {
                distinct('jobGroup')
            }
        })
        allActiveAndRecent.addAll(recentJobGroups)
        return allActiveAndRecent.unique { a, b -> a.id <=> b.id }
    }

    def getPagesWithResultsOrActiveJobsForJobGroup(Long jobGroupId) {
        DateTime today = new DateTime()
        DateTime fourWeeksAgo = configService.getStartDateForRecentMeasurements()
        def pagesWithResults = getPagesWithExistingEventResults(fourWeeksAgo, today, jobGroupId)
        def pagesOfActiveJobs = getPagesOfActiveJobs(jobGroupId)

        List<Page> pages = (pagesWithResults + pagesOfActiveJobs).collect()
        pages.unique()
        return pages

    }

    private List<Page> getPagesWithExistingEventResults(DateTime from, DateTime to, Long jobGroupId) {

        ResultSelectionCommand pagesForGivenJobGroup = new ResultSelectionCommand(
                jobGroupIds: [jobGroupId],
                from: from,
                to: to
        )

        return resultSelectionService.query(pagesForGivenJobGroup, ResultSelectionController.ResultSelectionType.Pages, { existing ->
            if (existing) {
                not { 'in'('page', existing) }
            }
            projections {
                distinct('page')
            }
        }) as List<Page>
    }

    private List<Page> getPagesOfActiveJobs(Long jobGroupId) {
        List<Script> scripts = Job.createCriteria().list {
            eq('jobGroup.id', jobGroupId)
            eq('active', true)
            isNotNull('script')

            projections {
                property('script')
            }
        } as List<Script>

        return scripts.collect { it.testedPages }.flatten() as List<Page>
    }
}
