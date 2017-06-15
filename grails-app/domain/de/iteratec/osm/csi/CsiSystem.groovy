package de.iteratec.osm.csi

import de.iteratec.osm.measurement.schedule.JobGroup


class CsiSystem {

    String label

    Collection<JobGroupWeight> jobGroupWeights = []

    static hasMany = [jobGroupWeights: JobGroupWeight]

    static constraints = {
        label unique: true, blank: false
        jobGroupWeights minSize: 2, validator: {jobGroupWeights, object ->
            // All JobGroups have to be different
            jobGroupWeights*.jobGroup.unique(false).size() == jobGroupWeights*.jobGroup.size()
        }
    }

    List<JobGroup> getAffectedJobGroups() {
        return jobGroupWeights*.jobGroup
    }
}
