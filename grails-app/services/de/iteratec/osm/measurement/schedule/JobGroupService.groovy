package de.iteratec.osm.measurement.schedule

import grails.gorm.transactions.Transactional

@Transactional
class JobGroupService {

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
}
