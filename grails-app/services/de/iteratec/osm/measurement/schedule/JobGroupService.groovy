package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.ResultSelectionCommand
import de.iteratec.osm.result.ResultSelectionController
import de.iteratec.osm.result.ResultSelectionService
import grails.gorm.transactions.Transactional
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime

@Transactional
class JobGroupService {
    ResultSelectionService resultSelectionService

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
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                jobGroup {
                    distinct('id')
                    property('id', 'id')
                    property('name', 'name')
                }
            }
        }
    }

    def getAllActiveAndAllRecent() {
        Set<JobGroup> allActiveAndRecent = getAllActiveJobGroups() as Set

        DateTime today = new DateTime()
        DateTime fourWeeksAgo = new DateTime().minusWeeks(4)

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

        List allActiveAndRecentFormattedJobGroups = new ArrayList()
        allActiveAndRecent.each {
            def name = it.name
            JobResult lastDateOfResult = (JobResult) JobResult.createCriteria().list(max: 1) {
                eq("jobGroupName", name)
                order("date", "desc")
            }[0]

            def formattedLastDateOfResult
            if (lastDateOfResult) {
                formattedLastDateOfResult = lastDateOfResult.date?.format("yyyy-MM-dd")
            }

            allActiveAndRecentFormattedJobGroups.add(
                    [
                            id                : it.id,
                            name              : it.name,
                            dateOfLastResults : formattedLastDateOfResult,
                            csiConfigurationId: it.csiConfigurationId
                    ]
            )
        }

        return allActiveAndRecentFormattedJobGroups
    }
}
